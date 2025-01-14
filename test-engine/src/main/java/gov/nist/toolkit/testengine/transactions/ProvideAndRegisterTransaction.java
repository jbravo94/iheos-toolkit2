package gov.nist.toolkit.testengine.transactions;

import gov.nist.toolkit.commondatatypes.MetadataSupport;
import gov.nist.toolkit.commondatatypes.client.MetadataTypes;
import gov.nist.toolkit.registrymetadata.Metadata;
import gov.nist.toolkit.registrymetadata.MetadataParser;
import gov.nist.toolkit.soap.axis2.Swa;
import gov.nist.toolkit.testengine.engine.StepContext;
import gov.nist.toolkit.utilities.io.Io;
import gov.nist.toolkit.utilities.xml.Util;
import gov.nist.toolkit.utilities.xml.XmlUtil;
import gov.nist.toolkit.valregmsg.service.SoapActionFactory;
import gov.nist.toolkit.xdsexception.ExceptionUtil;
import gov.nist.toolkit.xdsexception.HttpCodeException;
import gov.nist.toolkit.xdsexception.client.XdsException;
import gov.nist.toolkit.xdsexception.client.XdsInternalException;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.wsdl.WSDLConstants;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class ProvideAndRegisterTransaction extends RegisterTransaction {
	boolean use_mtom = true;
	boolean is_xca = false;
	boolean useIG = false;
	boolean removeHomeFromRequest = false;

	HashMap<String, String> document_id_filenames = new HashMap<String, String>();

	public ProvideAndRegisterTransaction(StepContext s_ctx, OMElement instruction, OMElement instruction_output) {
		super(s_ctx, instruction, instruction_output);
	}

	public void setIsXca(boolean isXca) {
		is_xca = isXca;
		xds_version = BasicTransaction.xds_b;
	}

	@Override
   protected  String getBasicTransactionName() {
		return "pr";
	}

	@Override
   public void run(OMElement metadata_element)
	throws XdsException {
		boolean my_swa = false;

		validate_xds_version();

		if (metadata_filename == null)
			throw new XdsInternalException("No MetadataFile element found for RegisterTransaction instruction within step " + this.s_ctx.get("step_id"));


        // This should be rewritten to use MtomBuilder
		OMElement body = null;
		if (xds_version == BasicTransaction.xds_b) {
			OMElement pnr = MetadataSupport.om_factory.createOMElement("ProvideAndRegisterDocumentSetRequest", MetadataSupport.xdsB);
			if (no_convert)
				pnr.addChild(metadata_element);
			else {
				Metadata metadata = MetadataParser.parse(metadata_element);
				pnr.addChild(metadata.getV3SubmitObjectsRequest());
			}
			for (String id : document_id_filenames.keySet() ) {
				String filename = document_id_filenames.get(id);

				if (nameUuidMap != null) {
					String newId = nameUuidMap.get(id);
					if (newId != null && !id.equals(""))
						id = newId;
				}

				javax.activation.DataHandler dataHandler = new javax.activation.DataHandler(new FileDataSource(filename));
				OMText t = MetadataSupport.om_factory.createOMText(dataHandler, true);
				t.setOptimize(use_mtom);
				OMElement document = MetadataSupport.om_factory.createOMElement("Document", MetadataSupport.xdsB);
				document.addAttribute("id", id, null);
				document.addChild(t);
				pnr.addChild(document);
			}

//			log_metadata(pnr);

			if (testConfig.prepare_only)
				return;

			body = pnr;
			try {
				OMElement result = null;

//				setMetadata(body);
				useMtom = use_mtom;
				useAddressing = true;

				soapCall(body);
				result = getSoapResult();


				if (result == null) {
					testLog.add_name_value(instruction_output, "Result", "None");
					s_ctx.set_error("Result was null");
				} else {
//					testLog.add_name_value(instruction_output, "Result", result);

					validate_registry_response(
							result,
							"RegistryResponse",
							(xds_version == xds_a) ? MetadataTypes.METADATA_TYPE_R : MetadataTypes.METADATA_TYPE_SQ);
				}

			}
			catch (Exception e) {
				throw new XdsInternalException(ExceptionUtil.exception_details(e));
			}
		}
		else if (my_swa) {  // non-Axis2 implementation
			Metadata metadata = MetadataParser.parse(metadata_element);

			body = metadata.getV2SubmitObjectsRequest();
			log_metadata(body);

			try {
				Swa swa = new Swa();
				swa.connect(this.endpoint);

				for (String id : this.document_id_filenames.keySet()) {
					String filename = document_id_filenames.get(id);

					String mime_type;
					String ext = file_extension(filename);
					if (ext.equals("txt")) mime_type = "text/plain";
					else if (ext.equals("xml")) mime_type = "text/xml";
					else mime_type = "application/octet-stream";

					FileDataSource fileDataSource = new FileDataSource(new File(filename));
					InputStream is = fileDataSource.getInputStream();
					byte[] document = Io.getBytesFromInputStream(is);
					swa.addAttachment(document, mime_type, id);
				}

				OMElement response = swa.send(null, body);

				OMElement soap_header_in = swa.getHeader();
				testLog.add_name_value(instruction_output, "InHeader", Util.deep_copy( soap_header_in));

				OMElement soap_header_out = null;
				testLog.add_name_value(instruction_output, "OutHeader", Util.deep_copy( soap_header_out));

				OMElement result = swa.getBody();
				if (result == null) {
					testLog.add_name_value(instruction_output, "Result", "None");
					testLog.add_name_value(instruction_output, "StepStatus", "Failure");
					System.out.println("The result was NULL");
				} else {
					OMElement fault = XmlUtil.firstChildWithLocalName(result, "Fault");
					if (fault != null) {
						//s_ctx.add_name_value(instruction_output, "StepStatus", "Failure");
						OMElement faultstring_ele = XmlUtil.firstChildWithLocalName(fault, "faultstring");
						String faultstring;
						if (faultstring_ele != null) {
							faultstring = faultstring_ele.getText();
						} else {
							faultstring = "SOAP Fault: cannot parse message";
						}
					} else {
						OMElement rr = XmlUtil.firstChildWithLocalName(result, "RegistryResponse");
						if (rr == null) {
//							testLog.add_name_value(instruction_output, "Result", Util.parse_xml(result.toString()));
							testLog.add_name_value(instruction_output, "StepStatus", "Failure");
							s_ctx.set_error("Result has no RegistryResponse");
						} else {
//							testLog.add_name_value(instruction_output, "Result", Util.parse_xml(rr.toString()));
							validate_registry_response(rr, "RegistryResponse", MetadataTypes.METADATA_TYPE_PR);
						}
					}
				}
			}
			catch (HttpCodeException e) {
				throw new XdsInternalException(ExceptionUtil.exception_details(e));
			}
			catch (Exception e) {
				throw new XdsInternalException(ExceptionUtil.exception_details(e));
			}
		}
		else {   // swa
			Metadata metadata = MetadataParser.parse(metadata_element);
			body = metadata.getV2SubmitObjectsRequest();
			log_metadata(body);

			try {

				Options options = new Options();
				options.setTo(new EndpointReference(endpoint));
				options.setProperty(Constants.Configuration.ENABLE_SWA,
						Constants.VALUE_TRUE);
				options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
				// Increase the time out when sending large attachments
				options.setTimeOutInMilliSeconds(20000);
				options.setAction("SubmitObjectsRequest");


				ServiceClient sender = new ServiceClient(/*configContext, null*/);
				sender.setOptions(options);
				OperationClient mepClient = sender
				.createClient(ServiceClient.ANON_OUT_IN_OP);

				MessageContext mc = new MessageContext();

				if (System.getenv("XDSHTTP10") != null) {
					System.out.println("Generating HTTP 1.0");

					sender.getOptions().setProperty
					(org.apache.axis2.transport.http.HTTPConstants.HTTP_PROTOCOL_VERSION,
							org.apache.axis2.transport.http.HTTPConstants.HEADER_PROTOCOL_10);

					sender.getOptions().setProperty
					(org.apache.axis2.transport.http.HTTPConstants.CHUNKED,
							Boolean.FALSE);

				}



				for (String id : this.document_id_filenames.keySet()) {
					String filename = document_id_filenames.get(id);
					if (nameUuidMap != null) {
						String newId = nameUuidMap.get(id);
						if (newId != null && !id.equals(""))
							id = newId;
					}
					FileDataSource fileDataSource = new FileDataSource(new File(filename));
					DataHandler dataHandler = new DataHandler(fileDataSource);
					mc.addAttachment(id, dataHandler);
				}

				SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
				SOAPEnvelope env = fac.getDefaultEnvelope();

				env.getBody().addChild(body);
				mc.setEnvelope(env);
				mepClient.addMessageContext(mc);

				mepClient.execute(true);

				MessageContext response = mepClient
				.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);

				OMElement soap_header_in = response.getEnvelope().getHeader();
				testLog.add_name_value(instruction_output, "InHeader", Util.deep_copy( soap_header_in));

				@SuppressWarnings("unused")
            MessageContext request = mepClient
				.getMessageContext(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
				OMElement soap_header_out = response.getEnvelope().getHeader();
				testLog.add_name_value(instruction_output, "OutHeader", Util.deep_copy( soap_header_out));

				SOAPBody result = response.getEnvelope().getBody();
				if (result == null) {
					testLog.add_name_value(instruction_output, "Result", "None");
					testLog.add_name_value(instruction_output, "StepStatus", "Failure");
					System.out.println("The result was NULL");
				} else {
					OMElement rr = XmlUtil.firstChildWithLocalName(result, "RegistryResponse");
					if (rr == null) {
//						testLog.add_name_value(instruction_output, "Result", Util.parse_xml(result.toString()));
						testLog.add_name_value(instruction_output, "StepStatus", "Failure");
						s_ctx.set_error("Result has no RegistryResponse");
					} else {
//						testLog.add_name_value(instruction_output, "Result", Util.parse_xml(rr.toString()));
						validate_registry_response(rr, "RegistryResponse", MetadataTypes.METADATA_TYPE_PR);
					}
				}
			}
			catch (AxisFault e) {
				throw new XdsInternalException(ExceptionUtil.exception_details(e, "Error connecting to endpoint\nEndpoint is '" + endpoint));
			}
			catch (Exception e) {
				throw new XdsInternalException(ExceptionUtil.exception_details(e));
			}

		}
	}

	ArrayList<String> singleton(String value) {
		ArrayList<String> al = new ArrayList<String>();
		al.add(value);
		return al;
	}

	String htmlize(String xml) {
		return xml.replaceAll("<", "&lt;");
	}

	String file_extension(String path) {
		String[] parts = path.split("/");
		String filename;
		if (parts.length < 2)
			filename = path;
		else
			filename = parts[parts.length-1];
		int dot = filename.indexOf(".");
		if (dot == -1)
			return "";
		return filename.substring(dot+1);
	}

	OMElement getHeader(OMElement envelope) {
		return XmlUtil.firstChildWithLocalName(envelope, "Header");
	}

	OMElement getBody(OMElement envelope) {
		return XmlUtil.firstChildWithLocalName(envelope, "Body");
	}

	@Override
   protected void parseInstruction(OMElement part) throws XdsInternalException {
		String part_name = part.getLocalName();
		if (part_name.equals("Document")) {
			String id = part.getAttributeValue(MetadataSupport.id_qname);
			if (id == null || id.equals("")) throw new XdsInternalException("ProvideAndRegisterTransaction: empty id attribute on Document element");
			String filename = part.getText();
			if (filename == null || filename.equals("")) throw new XdsInternalException("ProvideAndRegisterTransaction: Document with id " + id + " has no filename specified");
			document_id_filenames.put(id, testConfig.testplanDir + File.separator + filename);
		}
		else if (part_name.equals("XDSb")) {
			xds_version = BasicTransaction.xds_b;
		}
		else if (part_name.equals("XDSa")) {
			xds_version = BasicTransaction.xds_a;
		}
		else if (part_name.equals("NoMTOM")) {
			this.use_mtom = false;
		}
		else {
			parseBasicInstruction(part);
		}
	}

	@Override
   protected String getRequestAction() {
		if (xds_version == BasicTransaction.xds_b) {
			if (async) {
				return SoapActionFactory.pnr_b_async_action;
			}
         return SoapActionFactory.pnr_b_action;
		}
      return SoapActionFactory.anon_action;
	}


}
