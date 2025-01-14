package gov.nist.toolkit.itTests.xdr

import gov.nist.toolkit.configDatatypes.server.SimulatorActorType
import gov.nist.toolkit.configDatatypes.server.SimulatorProperties
import gov.nist.toolkit.itTests.support.ToolkitSpecification
import gov.nist.toolkit.registrymetadata.Metadata
import gov.nist.toolkit.registrymetadata.MetadataParser
import gov.nist.toolkit.registrymsg.registry.RegistryError
import gov.nist.toolkit.registrymsg.registry.RegistryErrorListParser
import gov.nist.toolkit.testengine.engine.UniqueIdFactory
import gov.nist.toolkit.toolkitApi.BasicSimParameters
import gov.nist.toolkit.toolkitApi.DocumentSource
import gov.nist.toolkit.toolkitApi.SimulatorBuilder
import gov.nist.toolkit.toolkitServicesCommon.RawSendRequest
import gov.nist.toolkit.toolkitServicesCommon.RawSendResponse
import gov.nist.toolkit.toolkitServicesCommon.SimConfig
import gov.nist.toolkit.toolkitServicesCommon.resource.DocumentResource
import gov.nist.toolkit.utilities.xml.OMFormatter
import gov.nist.toolkit.utilities.xml.Util
import org.apache.axiom.om.OMElement
import spock.lang.Shared

import java.nio.file.Paths

/**
 * This is not named *Spec on purpose.  The recipient must be created manually in
 * a separate copy of toolkit so this must be run by hand.
 */
class XdrSrcTls extends ToolkitSpecification {
    @Shared SimulatorBuilder spi
    @Shared String recipientEndpoint = 'http://vm:8080/xdstools2/sim/bill__rec/rep/xdrpr'
    @Shared String recipientTLSEndpoint = 'https://vm:8443/xdstools2/sim/bill__rec/rep/xdrpr'

    @Shared BasicSimParameters srcParams = new BasicSimParameters()

    def setupSpec() {   // one time setup done when class launched
        startGrizzly('8889')

        // Initialize remote api for talking to toolkit on Grizzly
        // Needed to build simulators
        spi = getSimulatorApi(remoteToolkitPort)
    }

    def cleanupSpec() {  // one time shutdown when everything is done
        spi.delete(srcParams.id, srcParams.user)
//        server.stop()
//        ListenerFactory.terminateAll()
    }


    def setup() {  // run before each test method
        srcParams.id = 'source'
        srcParams.user = 'bill'
        srcParams.actorType = SimulatorActorType.DOCUMENT_SOURCE
        srcParams.environmentName = 'NA2016'
    }

    def 'environment gets added to configuration in sim create' () {
        when:
        println 'STEP - DELETE DOCSRC SIM'
        spi.delete(srcParams.id, srcParams.user)

        and:
        println 'STEP - CREATE DOCSRC SIM'
        DocumentSource documentSource = spi.createDocumentSource(
                srcParams.id,
                srcParams.user,
                srcParams.environmentName
        )

        then: 'verify sim built'
        documentSource.getId() == srcParams.id

        when: 'extract environment name'
        String env = documentSource.asString(SimulatorProperties.environment)

        then: 'matches config'
        env == srcParams.environmentName
        documentSource.getEnvironmentName() == srcParams.environmentName

    }

    def 'Send XDR with TLS'() {
        when:
        println 'STEP - DELETE DOCSRC SIM'
        spi.delete(srcParams.id, srcParams.user)

        and:
        println 'STEP - CREATE DOCSRC SIM'
        DocumentSource documentSource = spi.createDocumentSource(
                srcParams.id,
                srcParams.user,
                srcParams.environmentName
        )

        then: 'verify sim built'
        documentSource.getId() == srcParams.id

        when:
        println 'STEP - UPDATE - SET DOC REC ENDPOINTS INTO DOC SRC'
        documentSource.setProperty(SimulatorProperties.pnrEndpoint, recipientEndpoint)
        documentSource.setProperty(SimulatorProperties.pnrTlsEndpoint, recipientTLSEndpoint)
//        documentSource.setProperty(SimulatorProperties.environment, srcParams.environmentName)
        SimConfig updatedVersion = documentSource.update(documentSource.getConfig())
        println "Updated Src Sim config is ${updatedVersion.describe()}"

        then:
        updatedVersion

        when:
        println 'STEP - SEND XDR'
        RawSendRequest req = documentSource.newRawSendRequest()

        String submission = Paths.get(this.getClass().getResource('/').toURI()).resolve('testdata/PnR1Doc.xml').text
        Metadata metadata = MetadataParser.parse(submission)
        UniqueIdFactory.assign(metadata)
        req.metadata = new OMFormatter(metadata.asProvideAndRegister()).toString()
        req.addDocument('Document01', new DocumentResource('text/plain', 'Hello World!'.bytes))
        req.tls = true

        RawSendResponse response = documentSource.sendProvideAndRegister(req)

        String responseSoapBody = response.responseSoapBody;
        OMElement responseEle = Util.parse_xml(responseSoapBody)
        RegistryErrorListParser rel = new RegistryErrorListParser(responseEle)
        List<RegistryError> errors = rel.registryErrorList
        errors.each { RegistryError err ->
            print 'ERROR - '
            println err.codeContext
        }

        then:
        errors.size() == 0
    }
}
