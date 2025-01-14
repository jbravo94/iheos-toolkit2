package gov.nist.toolkit.services.server.orchestration

import gov.nist.toolkit.actortransaction.shared.ActorType
import gov.nist.toolkit.configDatatypes.server.SimulatorProperties
import gov.nist.toolkit.configDatatypes.client.Pid
import gov.nist.toolkit.configDatatypes.client.PidBuilder
import gov.nist.toolkit.configDatatypes.client.TransactionType
import gov.nist.toolkit.installation.server.Installation
import gov.nist.toolkit.results.client.TestInstance
import gov.nist.toolkit.results.client.SiteBuilder
import gov.nist.toolkit.services.client.*
import gov.nist.toolkit.services.server.RawResponseBuilder
import gov.nist.toolkit.services.server.ToolkitApi
import gov.nist.toolkit.session.server.Session
import gov.nist.toolkit.simcommon.client.SimId
import gov.nist.toolkit.simcommon.client.SimIdFactory
import gov.nist.toolkit.simcommon.client.SimulatorConfig
import gov.nist.toolkit.simcommon.client.config.SimulatorConfigElement
import gov.nist.toolkit.simcommon.server.SimDb
import gov.nist.toolkit.sitemanagement.client.Site
import gov.nist.toolkit.sitemanagement.client.SiteSpec
import groovy.transform.TypeChecked
/**
 * Build environment for testing Responding Gateway SUT.
 */
@TypeChecked
class RgOrchestrationBuilder extends AbstractOrchestrationBuilder {
    Session session
    RgOrchestrationRequest request
    ToolkitApi api
    Util util

    public RgOrchestrationBuilder(ToolkitApi api, Session session, RgOrchestrationRequest request) {
        super(session, request)
        this.api = api
        this.session = session
        this.request = request
        this.util = new Util(api)
    }

    RawResponse buildTestEnvironment() {
        switch (request.getPifType()) {
            case PifType.NONE:
                buildTestEnvironment_NoPif()
                break
            case PifType.V2:
                buildTestEnvironment_V2Pif()
                break
        }
    }

    RawResponse buildTestEnvironment_NoPif() {
        RgOrchestrationResponse response = new RgOrchestrationResponse()

        TestInstance testInstanceReadme = TestInstanceManager.initializeTestInstance(request.testSession, new TestInstance("RgManualPif-Readme", request.testSession))
        MessageItem itemReadme = response.addMessage(testInstanceReadme, true, "")

        boolean sutSaml = false
        SimId sutSimId = null
        SimulatorConfig sutSimConfig = null
        SimulatorConfigElement stsSce = null
        boolean forceNewPatientIds = !request.isUseExistingState()
        try {
            String home
            Map<String, TestInstanceManager> pidNameMap

            OrchestrationProperties orchProps
            boolean isPifTypeSameAsPersisted = true
            Pid simplePid

            if (!request.isOnDemand()) {
                pidNameMap = [
                        simplePid:  new TestInstanceManager(request, response, '15823'),
                ]
                orchProps = new OrchestrationProperties(session, request.testSession, ActorType.RESPONDING_GATEWAY, pidNameMap.keySet(), forceNewPatientIds)
                isPifTypeSameAsPersisted = orchProps.getProperty("pifType") != null && request.pifType == PifType.valueOf(orchProps.getProperty("pifType"))

                if (!isPifTypeSameAsPersisted) {
                    // When PIF mode is switched, use a fresh set of PIDs
                    forceNewPatientIds = true
                    orchProps = new OrchestrationProperties(session, request.testSession, ActorType.RESPONDING_GATEWAY, pidNameMap.keySet(), forceNewPatientIds)
                    // Save the PIF setting
                    orchProps.setProperty("pifType", request.pifType.name())
                }

                simplePid = PidBuilder.createPid(orchProps.getProperty("simplePid"))

                response.setSimplePid(simplePid)

                // Setup PIF patientid param
                pidNameMap.each { String key, TestInstanceManager value ->
                    String pidId = key
                    TestInstanceManager testInstanceManager = value
                    testInstanceManager.messageItem.params.put('$patientid$', orchProps.getProperty(pidId))
                }
            } else {
                pidNameMap = [:]
            }

            String supportIdName = 'rg_support'
            SimId supportSimId
            SimulatorConfig supportSimConfig
            SiteSpec rrSite

            Site site = gov.nist.toolkit.results.server.SiteBuilder.siteFromSiteSpec(request.siteUnderTest, session.id)
            if (site == null) return RawResponseBuilder.build(String.format("Responding Gateway under Test (%s) does not exist in site configurations.", request.siteUnderTest.toString()))
            rrSite = request.siteUnderTest
            response.siteUnderTest = rrSite
            home = site.home

            if (request.isOnDemand()) {

            } else {

                if (request.useExposedRR) {
                    // RG and RR in same site - verify site contents

                    // Momentarily turn off SAML if the SUT is a simulator. Need manual intervening for real systems.
                    if (SimIdFactory.isSimId(request.siteUnderTest.name)) {
                        sutSimId = SimDb.simIdBuilder(request.siteUnderTest.name)
                        if (Installation.instance().propertyServiceManager().getPropertyManager().isEnableSaml()) {

                            sutSimConfig = api.getConfig(sutSimId)
                            if (sutSimConfig != null) {
                                stsSce = sutSimConfig.get(SimulatorProperties.requiresStsSaml)

                                if (stsSce != null && stsSce.hasBoolean() && stsSce.asBoolean()) {
                                    sutSaml = true
                                    stsSce.setBooleanValue(false) // Turn off SAML for orchestration
                                    api.saveSimulator(sutSimConfig)
                                }
                            }
                        }
                    }

                    if (!site.hasTransaction(TransactionType.PROVIDE_AND_REGISTER)) return RawResponseBuilder.build("Responding Gateway under test is not configured to accept a Provide and Register transaction.")
                    request.registrySut = request.siteUnderTest  // PifSender expects this
                    response.regrepSite = rrSite
                    response.sameSite = true
                } else {    // NOT REALLY USED YET - DISABLED ON UI
                    // use external RR
                    // build RR sim - pass back details for configuration of SUT
                    // SUT and supporting RR are defined by different sites
                }

                TestInstance testInstance12318 = new TestInstance('12318', request.testSession)
                MessageItem item12318 = response.addMessage(testInstance12318, true, "")
                item12318.params.put('$patientid$', orchProps.getProperty('simplePid'))
                item12318.params.put('$testdata_home$', home)
                response.testParams.put(testInstance12318, item12318.params)

                if (orchProps.updated() || !isPifTypeSameAsPersisted) {
                    List<TestInstance> tILogToBeDeleted = new ArrayList<>()
                    pidNameMap.each { String key, TestInstanceManager value ->
                        String pidId = key
                        TestInstanceManager testInstanceManager = value
                        tILogToBeDeleted.add(testInstanceManager.messageItem.testInstance)
                    }
                    tILogToBeDeleted.add(testInstanceReadme)
                    tILogToBeDeleted.add(testInstance12318)
                    session.getXdsTestServiceManager().delTestResults(tILogToBeDeleted, request.getEnvironmentName(), request.getTestSession())

                    // Instruct the user to manually Run PIF feed on their system and then run load the test data.

                } else {
                    pidNameMap.each { String key, TestInstanceManager value ->
                        String pidId = key
                        TestInstanceManager testInstanceManager = value
                        testInstanceManager.messageItem.setSuccess(api.getTestLogs(testInstanceManager.messageItem.testInstance).isSuccess())
                    }
                    item12318.setSuccess(api.getTestLogs(testInstance12318).isSuccess())
                }
            }

            if (orchProps)
                orchProps.save()

            return response
        }
        catch (Exception e) {
            return RawResponseBuilder.build(e)
        } finally {
            if (sutSaml) {
                stsSce.setBooleanValue(true)
                api.saveSimulator(sutSimConfig)
            }
        }

        }

    RawResponse buildTestEnvironment_V2Pif() {
        boolean sutSaml = false
        SimId sutSimId = null
        SimulatorConfig sutSimConfig = null
        SimulatorConfigElement stsSce = null
        try {
            String home
            RgOrchestrationResponse response = new RgOrchestrationResponse()
            Map<String, TestInstanceManager> pidNameMap

            OrchestrationProperties orchProps
            boolean isPifTypeSameAsPersisted = true
            Pid simplePid
            boolean forceNewPatientIds = !request.isUseExistingState()

            if (!request.isOnDemand()) {
                pidNameMap = [
                        simplePid:  new TestInstanceManager(request, response, '15823'),
                ]
                orchProps = new OrchestrationProperties(session, request.testSession, ActorType.RESPONDING_GATEWAY, pidNameMap.keySet(), forceNewPatientIds)
                isPifTypeSameAsPersisted = orchProps.getProperty("pifType") != null && request.pifType == PifType.valueOf(orchProps.getProperty("pifType"))

                if (!isPifTypeSameAsPersisted) {
                    // When PIF mode is switched, use a fresh set of PIDs
                    forceNewPatientIds = true
                    orchProps = new OrchestrationProperties(session, request.testSession, ActorType.RESPONDING_GATEWAY, pidNameMap.keySet(), forceNewPatientIds)
                    // Save the PIF setting
                    orchProps.setProperty("pifType", request.pifType.name())
                }

                simplePid = PidBuilder.createPid(orchProps.getProperty("simplePid"))

                response.setSimplePid(simplePid)
            } else {
                pidNameMap = [:]
            }

            String supportIdName = 'rg_support'
            SimId supportSimId
            SimulatorConfig supportSimConfig
            SiteSpec rrSite
            boolean reuse = false  // updated as we progress


            Site site = gov.nist.toolkit.results.server.SiteBuilder.siteFromSiteSpec(request.siteUnderTest, session.id)
            if (site == null) return RawResponseBuilder.build(String.format("Responding Gateway under Test (%s) does not exist in site configurations.", request.siteUnderTest.toString()))
            rrSite = request.siteUnderTest
            response.siteUnderTest = rrSite
            home = site.home

            if (request.isOnDemand()) {

            } else {

                if (request.useExposedRR) {
                    // RG and RR in same site - verify site contents

                    // Momentarily turn off SAML if the SUT is a simulator. Need manual intervening for real systems.
                    if (SimIdFactory.isSimId(request.siteUnderTest.name)) {
                        sutSimId = SimDb.simIdBuilder(request.siteUnderTest.name)
                        if (Installation.instance().propertyServiceManager().getPropertyManager().isEnableSaml()) {

                            sutSimConfig = api.getConfig(sutSimId)
                            if (sutSimConfig != null) {
                                stsSce = sutSimConfig.get(SimulatorProperties.requiresStsSaml)

                                if (stsSce != null && stsSce.hasBoolean() && stsSce.asBoolean()) {
                                    sutSaml = true
                                    stsSce.setBooleanValue(false) // Turn off SAML for orchestration
                                    api.saveSimulator(sutSimConfig)
                                }
                            }
                        }
                    }

                    // TODO - document that SUT with exposed RR must support PIF v2 or have PID validation disabled
                    if (!site.hasTransaction(TransactionType.PROVIDE_AND_REGISTER)) return RawResponseBuilder.build("Responding Gateway under test is not configured to accept a Provide and Register transaction.")
                    request.registrySut = request.siteUnderTest  // PifSender expects this
                    response.regrepSite = rrSite
                    response.sameSite = true
                } else {    // NOT REALLY USED YET - DISABLED ON UI
                    // use external RR
                    // build RR sim - pass back details for configuration of SUT
                    // SUT and supporting RR are defined by different sites

                    supportSimId = new SimId(request.testSession, supportIdName, ActorType.REPOSITORY_REGISTRY.name, request.environmentName)
                    if (!request.isUseExistingState()) {
                        api.deleteSimulatorIfItExists(supportSimId)
                        orchProps.clear()
                    }

                    if (api.simulatorExists(supportSimId)) {
                        supportSimConfig = api.getConfig(supportSimId)
                        reuse = true
                    } else {
                        supportSimConfig = api.createSimulator(supportSimId).getConfig(0)
                    }

                    // disable checking of Patient Identity Feed
                    if (!reuse) {
                        SimulatorConfigElement idsEle = supportSimConfig.getConfigEle(SimulatorProperties.VALIDATE_AGAINST_PATIENT_IDENTITY_FEED)
                        idsEle.setBooleanValue(false)

                        api.saveSimulator(supportSimConfig)
                    }

                    rrSite = new SiteBuilder().siteSpecFromSimId(supportSimId)
                    response.siteUnderTest = request.siteUnderTest
                    response.regrepSite = rrSite
                    response.sameSite = false
                    response.regrepConfig = supportSimConfig;
                    home = rrSite.homeId
                }

                TestInstance testInstance12318 = new TestInstance('12318', request.testSession)
                MessageItem item12318 = response.addMessage(testInstance12318, true, "")

                if (orchProps.updated() || !isPifTypeSameAsPersisted) {
                    // send necessary Patient ID Feed messages
                    new PifSender(api, request.testSession, rrSite, orchProps).send(PifType.V2, pidNameMap)

                    // Submit test data
                    try {
                        rrSite.isTls = request.isUseTls()
                        util.submit(request.testSession.value, rrSite, testInstance12318, simplePid, home)
                    } catch (Exception e) {
                        item12318.setMessage("Initialization of " + request.siteUnderTest.name + " failed:\n" + e.getMessage())
                        item12318.setSuccess(false)
                    }

                } else {
                    item12318.setSuccess(api.getTestLogs(testInstance12318).isSuccess())
                }

            }

            if (orchProps)
                orchProps.save()

            return response
        }
        catch (Exception e) {
            return RawResponseBuilder.build(e)
        } finally {
            if (sutSaml) {
                stsSce.setBooleanValue(true)
                api.saveSimulator(sutSimConfig)
            }
        }
    }



}

