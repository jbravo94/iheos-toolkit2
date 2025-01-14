package gov.nist.toolkit.fhir.simulators.sim.reg.mu;

import gov.nist.toolkit.commondatatypes.MetadataSupport;
import gov.nist.toolkit.errorrecording.ErrorRecorder;
import gov.nist.toolkit.errorrecording.client.XdsErrorCode;
import gov.nist.toolkit.errorrecording.client.XdsErrorCode.Code;
import gov.nist.toolkit.fhir.simulators.sim.reg.store.DocEntry;
import gov.nist.toolkit.fhir.simulators.sim.reg.store.ProcessMetadataInterface;
import gov.nist.toolkit.fhir.simulators.sim.reg.store.StatusValue;
import gov.nist.toolkit.registrymetadata.Metadata;
import gov.nist.toolkit.valsupport.client.ValidationContext.MetadataPattern;
import gov.nist.toolkit.xdsexception.client.MetadataException;
import org.apache.axiom.om.OMElement;

public class DocumentEntryUpdate  {
	ErrorRecorder er;

	public void run(MuSim muSim, Metadata m, OMElement docEle, OMElement ssAssoc, String prevVer) {
		er = muSim.er;
		String lid = m.getLid(docEle);
		String id = m.getId(docEle);
		String prefix = "Update (trigger=" + muSim.UUIDToSymbolic.get(id) +") - ";

		muSim.getCommon().vc.addMetadataPattern(MetadataPattern.UpdateDocumentEntry);

		DocEntry latest = muSim.delta.docEntryCollection.getLatestVersion(lid);

		if (latest == null) {
			Code code = Code.XDSMetadataVersionError;
//			if (muSim.getCommon().vc.isRMU)
//				code = Code.UnresolvedReferenceException;
			er.err(code, prefix + "existing DocumentEntry not present in Registry", this, "ITI TF-2b:3.57.4.1.3.3.1.3");
			return;
		}

		if (latest.getAvailabilityStatus() != StatusValue.APPROVED && !muSim.getCommon().vc.isRMU)
			er.err(Code.XDSMetadataUpdateError,
					prefix + "previous version does not have availabilityStatus of Approved, " + latest.getAvailabilityStatus() + " found instead",
					this, "ITI TF-2b:3.57.4.1.3.3.1.3");

		// from CP 1190-2
		//m.setStatus(docEle, latest.getAvailabilityStatus().name());

		if (muSim.getCommon().vc.isRMU) {
			// availabilityStatus carries over from previous version
			StatusValue statusValue = latest.getAvailabilityStatus();
			if (statusValue == StatusValue.DEPRECATED) {
				m.setStatus(docEle, MetadataSupport.statusType_deprecated);
			}
		}

		String submittedUid = "";
		try {
			submittedUid = m.getUniqueIdValue(docEle);
		} catch (MetadataException e) {
			er.err(Code.XDSMetadataUpdateError,
					prefix + "cannot extract uniqueId from submitted metadata",
					this, "ITI TF-2b:3.57.4.1.3.3.1.3");
		}

		if (!latest.getUid().equals(submittedUid)) {
			Code code = Code.XDSMetadataUpdateError;
			if (muSim.getCommon().vc.isRMU)
				code = Code.XDSMetadataIdentifierError;
			er.err(code,
						prefix + "previous version does not have same value for uniqueId: " +
								" previous version has " + latest.getUid() +
								" update has " + submittedUid,
						this, "ITI TF-2b:3.57.4.1.3.3.1.3");
		}

		String objectType = "";
		try {
			objectType = m.getObjectTypeById(id);
		} catch (MetadataException e) {
			Code code = Code.XDSMetadataUpdateError;
			if (muSim.getCommon().vc.isRMU)
				code = Code.XDSObjectTypeError;
			er.err(code,
					prefix + "cannot extract objectType from submitted metadata",
					this, "ITI TF-2b:3.57.4.1.3.3.1.3");
		}

		if (!latest.objecttype.equals(objectType)) {
			Code code = Code.XDSMetadataUpdateError;
			if (muSim.getCommon().vc.isRMU)
				code = Code.XDSObjectTypeError;
			er.err(code,
					prefix + "previous version does not have same value for objectType: " +
							" previous version has " + latest.objecttype +
							" update has " + objectType,
					this, "ITI TF-2b:3.57.4.1.3.3.1.3");
		}

		String latestVerStr = String.valueOf(latest.version);
		if (!latestVerStr.equals(prevVer))
			er.err(Code.XDSMetadataVersionError,
					prefix + "PreviousVersion from submission and latest Registry version do not match - " +
					" PreviousVersion is " + prevVer +
					" and latest version in Registry is " + latestVerStr,
					this, "ITI TF-2b:3.57.4.1.3.3.1.3");

		m.setVersion(docEle, String.valueOf(latest.version + 1));

		// install default version in Association, Classification, ExternalIdentifier
		try {
			m.setDefaultVersionOfUnversionedElements();
		} catch (MetadataException e) {
			er.err(XdsErrorCode.Code.XDSRegistryMetadataError, e);
		}

		Metadata operation = new Metadata();

		operation.addExtrinsicObject(docEle);
		operation.add_association(ssAssoc);
		if (m.getSubmissionSet() == null) {
			er.err(Code.XDSMetadataUpdateError, "Update contains no SubmissionSet object", null,null);
			return;
		}
		operation.addSubmissionSet(m.getSubmissionSet());

		// run normal processing for a Register
		// Note that this method farms out all the work to other
		// worker methods that can be overridden by this class to
		// control the processing. (That's why this class inherits
		// from RegRSim).
		ProcessMetadataInterface pmi = new ProcessMetadataForDocumentEntryUpdate(er, muSim.mc, muSim.delta);
		try {
			muSim.processMetadata(operation, pmi);
		} catch (Exception e) {
			er.err(XdsErrorCode.Code.XDSRegistryMetadataError, e);
		}

		if (!muSim.hasErrors()) {
			muSim.save(operation, false);
			// deprecate last version
			muSim.delta.changeAvailabilityStatus(latest.getId(), StatusValue.APPROVED, StatusValue.DEPRECATED);
		}

	}


}
