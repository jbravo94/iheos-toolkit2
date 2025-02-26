package gov.nist.toolkit.valregmsg.registry.storedquery.generic;

import gov.nist.toolkit.docref.SqDocRef;
import gov.nist.toolkit.registrymetadata.Metadata;
import gov.nist.toolkit.registrysupport.logging.LoggerException;
import gov.nist.toolkit.valregmsg.registry.storedquery.support.StoredQuerySupport;
import gov.nist.toolkit.xdsexception.client.MetadataException;
import gov.nist.toolkit.xdsexception.client.MetadataValidationException;
import gov.nist.toolkit.xdsexception.client.XdsException;
import gov.nist.toolkit.xdsexception.client.XdsInternalException;


/**
Generic implementation of GetAssociations Stored Query. This class knows how to parse a 
 * GetAssociations Stored Query request producing a collection of instance variables describing
 * the request.  A sub-class must provide the runImplementation() method that uses the pre-parsed
 * information about the stored query and queries a metadata database.
 * @author bill
 *
 */
abstract public class GetFoldersForDocument extends StoredQuery {
	
	/**
	 * Method required in subclasses (implementation specific class) to define specific
	 * linkage to local database
	 * @return matching metadata
	 * @throws MetadataException
	 * @throws XdsException
	 * @throws LoggerException
	 */
	abstract protected Metadata runImplementation() throws MetadataException, XdsException, LoggerException;

	/**
	 * Basic constructor
	 * @param sqs
	 */
	public GetFoldersForDocument(StoredQuerySupport sqs) {
		super(sqs);
	}
	
	public void validateParameters() throws MetadataValidationException {
		//                         param name,                             required?, multiple?, is string?,   same size as,    alternative
		sqs.validate_parm("$XDSDocumentEntryUniqueId",                 true,      false,     true,         null,            "$XDSDocumentEntryEntryUUID");
		sqs.validate_parm("$XDSDocumentEntryEntryUUID",                true,      false,     true,         null,            "$XDSDocumentEntryUniqueId");

		if (sqs.has_validation_errors) 
			throw new MetadataValidationException(QueryParmsErrorPresentErrMsg, SqDocRef.Individual_query_parms);
	}

	protected String uid;
	protected String uuid;

	protected void parseParameters() throws XdsInternalException, XdsException, LoggerException {
		super.parseParameters();
		uid 			= sqs.params.getStringParm("$XDSDocumentEntryUniqueId");
		uuid 		    = sqs.params.getStringParm("$XDSDocumentEntryEntryUUID");
	}
	
	/**
	 * Implementation of Stored Query specific logic including parsing and validating parameters.
	 * @throws XdsException
	 * @throws LoggerException
	 */
	public Metadata runSpecific() throws XdsException, LoggerException {

		validateParameters();
		parseParameters();

		if (uuid == null && uid == null) 
			throw new XdsInternalException("GetFoldersForDocument Stored Query");
		return runImplementation();
	}
}
