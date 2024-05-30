package gov.nist.toolkit.actortransaction.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import gov.nist.toolkit.configDatatypes.client.TransactionType;
import org.apache.http.annotation.Obsolete;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// This file must be kept up to date with SimulatorActorTypes.java

/**
 * Actor types defined by test engine.  A subset of these are available as simulators.
 *
 * The profile/actor/option are now coded in the following way.
 * Test collections.txt uses the actor(profile)_option format.
 * actorCode should match the actor code used in the actor part of the test collections.txt file.
 * profile should match the profile part of the test collections.txt file. If the profile is not specified, XDS is assumed, but the ActorType actorCode will need to declare XDS in its profile.
 * option should match the option part of the test collections.txt file. If the option is not specified, Required is assumed, but the options list needs to contain the Required option.
 * This aligns with the Conformance Tool configuration file
 * ConfTestsTabs.xml which lives in toolkitx.
 *
 * So the big picture is that the actorCode/profile/option is now, in some cases, actually the profile/actor/option type.
 */
public enum ActorType implements IsSerializable, Serializable {
    XDR_DOC_SRC(
            "XDR Document Source",
            Arrays.asList("XDR_Source"),
            "xdrsrc",
            "gov.nist.toolkit.simcommon.server.factories.XdrDocSrcActorFactory",
            "gov.nist.toolkit.fhir.simulators.sim.src.XdrDocSrcActorSimulator",
            Arrays.asList(TransactionType.XDR_PROVIDE_AND_REGISTER),
            false,
            null,
            false,
            Constants.USE_SHORTNAME
    ),
    FIXED_REPLY(
            "Fixed Reply",
            Arrays.asList(""),
            "fixed",
            "gov.nist.toolkit.actorfactory.FixedReplyFactory",
            "gov.nist.toolkit.simulators.sim.fixed.FixedReplySimulator",
            Arrays.asList(TransactionType.PROVIDE_AND_REGISTER,
                    TransactionType.REGISTER,
                    TransactionType.RETRIEVE,
                    TransactionType.XC_RETRIEVE,
                    TransactionType.XC_QUERY),
            true,
            null,
            false,
            Constants.USE_SHORTNAME
    ),
    REGISTRY(
            "Document Registry",
            Arrays.asList("DOC_REGISTRY", "registryb", "initialize_for_stored_query"),
            "reg",
            "gov.nist.toolkit.simcommon.server.factories.RegistryActorFactory",
            "gov.nist.toolkit.fhir.simulators.sim.reg.RegistryActorSimulator",
            Arrays.asList(TransactionType.REGISTER, TransactionType.REGISTER_ODDE, TransactionType.STORED_QUERY, TransactionType.UPDATE, TransactionType.RMU, TransactionType.MPQ, TransactionType.REMOVE_METADATA, TransactionType.XAD_PID),
            true,
            null,
            false,
            Constants.USE_SHORTNAME
    ),
    REPOSITORY(
            "Document Repository",
            Arrays.asList("DOC_REPOSITORY", "repositoryb"),
            "rep",
            "gov.nist.toolkit.simcommon.server.factories.RepositoryActorFactory",
            "gov.nist.toolkit.fhir.simulators.sim.rep.RepositoryActorSimulator",
            Arrays.asList(TransactionType.PROVIDE_AND_REGISTER, TransactionType.RETRIEVE, TransactionType.REMOVE_DOCUMENTS),
            true,
            "repository",
            false,
            Constants.USE_SHORTNAME
    ),
    ONDEMAND_DOCUMENT_SOURCE(
            "On-Demand Document Source",
            Arrays.asList("ODDS", "ON_DEMAND_DOC_SOURCE"),
            "odds",
            "gov.nist.toolkit.simcommon.server.factories.OnDemandDocumentSourceActorFactory",
            "gov.nist.toolkit.fhir.simulators.sim.rep.od.OddsActorSimulator",
            Arrays.asList(TransactionType.ODDS_RETRIEVE),
            true,
            "odds",
            false,
            Constants.USE_SHORTNAME
    ),
    ISR(
            "Integrated Source/Repository",
            Arrays.asList("EMBED_REPOS"),
            "isr",
            null,
            null,
            Arrays.asList(TransactionType.ISR_RETRIEVE),
            true,
            "isr",
            false,
            Constants.USE_SHORTNAME
    ),
    REPOSITORY_REGISTRY(
            "Document Repository/Registry",
            new ArrayList<String>(),
            "rr",
            "gov.nist.toolkit.simcommon.server.factories.RepositoryRegistryActorFactory",
            "gov.nist.toolkit.fhir.simulators.sim.RegRepActorSimulator",
            Arrays.asList(TransactionType.REGISTER, TransactionType.STORED_QUERY, TransactionType.UPDATE, TransactionType.MPQ, TransactionType.PROVIDE_AND_REGISTER, TransactionType.RETRIEVE),
            true,
            null,
            false,
            Constants.USE_SHORTNAME
    ),
    DOCUMENT_RECIPIENT(
            "Document Recipient",
            Arrays.asList("DOC_RECIPIENT"),
            "rec",
            "gov.nist.toolkit.simcommon.server.factories.RecipientActorFactory",
            "gov.nist.toolkit.fhir.simulators.sim.RegRepActorSimulator",
            Arrays.asList(TransactionType.XDR_PROVIDE_AND_REGISTER),
            true,
            null,
            false,
            Constants.USE_SHORTNAME
    ),
    /*
    MHD_DOC_RECIPIENT(    // needs to be split into responder and recipient
            "MHD Document Recipient",
            Arrays.asList(""),
            "mhddocrec",
            "gov.nist.toolkit.simcommon.server.factories.MhdRecipientFactory",
            "gov.nist.toolkit.fhir.simulators.proxy.sim.MhdRecipientSimulator",
            Arrays.asList(TransactionType.FHIR, TransactionType.PROV_DOC_BUNDLE, TransactionType.FIND_DOC_REFS, TransactionType.READ_DOC_REF, TransactionType.READ_BINARY),
            true,  // show in config - only partially configured - only used in IT tests
            null,  // actorsFileLabel
            null,   // httpSimulatorClassName
            null,    // http transaction types
            false,    // is fhir
            // proxy transaform configs
            Arrays.asList(
                    // Provide Document Bundle Transaction
                    new ProxyTransformConfig(TransactionType.PROV_DOC_BUNDLE,
                            TransactionDirection.REQUEST,
                            FhirVerb.TRANSACTION,
                            "gov.nist.toolkit.fhir.simulators.proxy.transforms.MhdToXdsEndpointTransform").toString(),
                    new ProxyTransformConfig(TransactionType.PROV_DOC_BUNDLE,
                            TransactionDirection.REQUEST,
                            FhirVerb.TRANSACTION,
                            "gov.nist.toolkit.fhir.simulators.proxy.transforms.MhdToPnrContentTransform").toString(),
                    new ProxyTransformConfig(TransactionType.PROV_DOC_BUNDLE,
                            TransactionDirection.RESPONSE,
                            FhirVerb.TRANSACTION,
                            "gov.nist.toolkit.fhir.simulators.proxy.transforms.RegistryResponseToOperationOutcomeTransform").toString(),

                    // broken
                    new ProxyTransformConfig(TransactionType.FIND_DOC_REFS,
                            TransactionDirection.REQUEST,
                            FhirVerb.QUERY,
                            "gov.nist.toolkit.fhir.simulators.proxy.transforms.MhdToXdsEndpointTransform").toString(),
                    new ProxyTransformConfig(TransactionType.FIND_DOC_REFS,
                            TransactionDirection.REQUEST,
                            FhirVerb.QUERY,
                            "gov.nist.toolkit.fhir.simulators.proxy.transforms.MhdToSQRequestTransform").toString(),
                    new ProxyTransformConfig(TransactionType.FIND_DOC_REFS,
                            TransactionDirection.RESPONSE,
                            FhirVerb.QUERY,
                            "gov.nist.toolkit.fhir.simulators.proxy.transforms.SQResponseToFhirSearchResponseTransform").toString(),

                    // DocumentReference READ
                    new ProxyTransformConfig(TransactionType.READ_DOC_REF,
                            TransactionDirection.REQUEST,
                            FhirVerb.READ,
                            "gov.nist.toolkit.fhir.simulators.proxy.transforms.MhdToXdsEndpointTransform").toString(),
                    new ProxyTransformConfig(TransactionType.READ_DOC_REF,
                            TransactionDirection.REQUEST,
                            FhirVerb.READ,
                            "gov.nist.toolkit.fhir.simulators.proxy.transforms.FhirReadDocRefTransform").toString(),
                    new ProxyTransformConfig(TransactionType.READ_DOC_REF,
                            TransactionDirection.RESPONSE,
                            FhirVerb.READ,
                            "gov.nist.toolkit.fhir.simulators.proxy.transforms.SQResponseToFhirReadResponseTransform").toString(),

                    // Binary READ
                    new ProxyTransformConfig(TransactionType.READ_BINARY,
                                             TransactionDirection.REQUEST,
                                             FhirVerb.READ,
                            "gov.nist.toolkit.fhir.simulators.proxy.transforms.MhdToXdsEndpointTransform").toString(),
                    new ProxyTransformConfig(TransactionType.READ_BINARY,
                                             TransactionDirection.REQUEST,
                                             FhirVerb.READ,
                            "gov.nist.toolkit.fhir.simulators.proxy.transforms.MhdToRetrieveRequestTransform").toString(),
                    new ProxyTransformConfig(TransactionType.READ_BINARY,
                                             TransactionDirection.RESPONSE,
                                             FhirVerb.READ,
                            "gov.nist.toolkit.fhir.simulators.proxy.transforms.RetrieveResponseToFhirTransform").toString()

            ),
            "rec"   // must match rec from rec(mhd) in collections.txt
    ),

     */
    INITIATING_GATEWAY(
            "Initiating Gateway",
            Arrays.asList("INIT_GATEWAY"),
            "ig",
            "gov.nist.toolkit.simcommon.server.factories.IGActorFactory",
            "gov.nist.toolkit.fhir.simulators.sim.ig.IgActorSimulator",
            Arrays.asList(TransactionType.IG_QUERY, TransactionType.IG_RETRIEVE),
            true,
            null,
            false,
            Constants.USE_SHORTNAME
    ),

    INITIATING_GATEWAY_X(
            "Initiating Gateway X",
            Arrays.asList("INIT_GATEWAY"),
            "igx",
            "gov.nist.toolkit.simcommon.server.factories.IGXActorFactory",
            "gov.nist.toolkit.fhir.simulators.sim.ig.IgxActorSimulator",
            Arrays.asList(TransactionType.IG_QUERY, TransactionType.IG_RETRIEVE),
            true,
            null,
            false,
            Constants.USE_SHORTNAME
    ),

    INITIATING_IMAGING_GATEWAY(
       "Initiating Imaging Gateway",
       Arrays.asList("INIT_IMG_GATEWAY"),
       "iig",
            "gov.nist.toolkit.simcommon.server.factories.IigActorFactory",
       "gov.nist.toolkit.fhir.simulators.sim.iig.IigActorSimulator",
       Arrays.asList(TransactionType.RET_IMG_DOC_SET_GW),
       true,
       null,
       false,
       Constants.USE_SHORTNAME
    ),

    COMBINED_INITIATING_GATEWAY(
       "Combined Initiating Gateway",
       Arrays.asList("COMB_INIT_GATEWAY"),
       "cig",
            "gov.nist.toolkit.simcommon.server.factories.CigActorFactory",
       "gov.nist.toolkit.fhir.simulators.sim.CigActorSimulator",
       Arrays.asList(TransactionType.IG_QUERY, TransactionType.IG_RETRIEVE,TransactionType.RET_IMG_DOC_SET_GW),
       true,
       null,
       false,
       Constants.USE_SHORTNAME
       ),
    RESPONDING_GATEWAY(
            "Responding Gateway",
            Arrays.asList("RESP_GATEWAY"),
            "rg",
            "gov.nist.toolkit.simcommon.server.factories.RGActorFactory",
            "gov.nist.toolkit.fhir.simulators.sim.rg.RGADActorSimulator",
            Arrays.asList(
                    TransactionType.XC_QUERY,
                    TransactionType.XC_RETRIEVE,
                    TransactionType.XCPD,
                    TransactionType.XC_PROVIDE,
                    TransactionType.RMU,
                    TransactionType.XCRMU),
            true,
            null,
            false,
            Constants.USE_SHORTNAME
    ),

    RESPONDING_GATEWAY_X(
            "Responding Gateway X",
            Arrays.asList("RESP_GATEWAY_X"),
            "rgx",
            "gov.nist.toolkit.simcommon.server.factories.RGXActorFactory",
            "gov.nist.toolkit.fhir.simulators.sim.rg.RGXADActorSimulator",
            Arrays.asList(
                    TransactionType.XC_QUERY,
                    TransactionType.XC_RETRIEVE,
                    TransactionType.XCPD,
                    TransactionType.XC_PROVIDE,
                    TransactionType.RMU,
                    TransactionType.XCRMU),
            true,
            null,
            false,
            Constants.USE_SHORTNAME
    ),

    OD_RESPONDING_GATEWAY(
            "Responding Gateway - On Demand",
            Arrays.asList("On_DEMAND_RESP_GATEWAY"),
            "odrg",
            "gov.nist.toolkit.simcommon.server.factories.ODRGActorFactory",
            "gov.nist.toolkit.fhir.simulators.sim.rg.ODRGActorSimulator",
            Arrays.asList(TransactionType.XC_QUERY, TransactionType.XC_RETRIEVE),
            true,
            null,
            false,
            Constants.USE_SHORTNAME
    ),
    RESPONDING_IMAGING_GATEWAY(
            "Responding Imaging Gateway",
            Arrays.asList("RESP_IMG_GATEWAY"),
            "rig",
            "gov.nist.toolkit.simcommon.server.factories.RigActorFactory",
            "gov.nist.toolkit.fhir.simulators.sim.rig.RigActorSimulator",
            Arrays.asList(TransactionType.XC_RET_IMG_DOC_SET),
            true,
            null,
            false,
            Constants.USE_SHORTNAME
    ),
    COMBINED_RESPONDING_GATEWAY(
            "Combined Responding Gateway",
            Arrays.asList("COMB_RESP_GATEWAY"),
            "crg",
            "gov.nist.toolkit.simcommon.server.factories.CrgActorFactory",
            "gov.nist.toolkit.fhir.simulators.sim.CrgActorSimulator",
            Arrays.asList(TransactionType.XC_QUERY, TransactionType.XC_RETRIEVE, TransactionType.XC_RET_IMG_DOC_SET),
            true,
            null,
            false,
            Constants.USE_SHORTNAME
    ),
    RSNA_EDGE_DEVICE(
            "RSNA Image Sharing Source",
            Arrays.asList("RSNA_EDGE"),
            "ris",
            null,
            "gov.nist.toolkit.fhir.simulators.sim.ris.RisActorSimulator", //TODO: Change to correct domain
            Arrays.asList(TransactionType.RET_IMG_DOC_SET_GW), //TODO: Change to correct Transaction Type
            true,
            null,
            false,
            Constants.USE_SHORTNAME
    ),
    INITIALIZE_FOR_STORED_QUERY (  // this is an artificial type used by test indexer
            "Initialize for Stored Query",
            new ArrayList<String>(),
            "initialize_for_stored_query",
            null,
            null,
            new ArrayList<TransactionType>(),
            false,
            null,
            false,
            Constants.USE_SHORTNAME
    ),

    // Is this comment obsolete after the implementing the boolean logic in Site#determineActorTypeByTransactionsInSite ?
    // Issue 78 (ODDS Issue 98)
    // TODO - actorType lookup problem
    // This at the end of the list on purpose.  From the UI actor types are selected by the transactions they support.
    // A problem came up in TransactionSelectionManager#generateSiteSpec() where this gets chosen instead of
    // REGISTRY when searching on STORED_QUERY.  getSiteSpec() (and the stuff around it) needs to make decisions
    // on more than just what transactions are offered.  Probably needs to maintain specific actor type so
    // the lookup by transaction is not necessary
    DOC_CONSUMER(
            "XDS Document Consumer",
            Arrays.asList("XDS_Consumer", "doccons"),
            "cons",
            "gov.nist.toolkit.simcommon.server.factories.ConsumerActorFactory",
            "gov.nist.toolkit.fhir.simulators.sim.cons.DocConsActorSimulator",
            Arrays.asList(TransactionType.STORED_QUERY, TransactionType.RETRIEVE),
            false,
            null,
            false,
            Constants.USE_SHORTNAME
    ),
    IMAGING_DOC_SOURCE(
            "Imaging Document Source",
            Arrays.asList("IMAGING_DOC_SOURCE"),
            "ids",
            "gov.nist.toolkit.simcommon.server.factories.ImagingDocSourceActorFactory",
            "gov.nist.toolkit.fhir.simulators.sim.ids.IdsActorSimulator",
            Arrays.asList(TransactionType.RET_IMG_DOC_SET),
            true,
            null,
            "gov.nist.toolkit.fhir.simulators.sim.ids.IdsHttpActorSimulator",
            Arrays.asList(TransactionType.WADO_RETRIEVE),
            false,
            null,
            Constants.USE_SHORTNAME
        ),
    IMAGING_DOC_CONSUMER(
            "Imaging Document Consumer",
            Arrays.asList("IMAGING_DOC_CONSUMER", "XDSI_Consumer"),
            "idc",
            "gov.nist.toolkit.simcommon.server.factories.ImgConsumerActorFactory",
            "gov.nist.toolkit.fhir.simulators.sim.idc.ImgDocConsActorSimulator",
            Arrays.asList(TransactionType.RET_IMG_DOC_SET),
            false,
            null,
            false,
            Constants.USE_SHORTNAME
    ),
    DOC_SOURCE(
            "Document Source",
            Arrays.asList(""),
            "mhdsrc",
            "",
            "",
            Arrays.asList(TransactionType.PIF),  // place holder - transaction types
            false,  // show in config - only partially configured - only used in IT tests
            null,  // actorsFileLabel
            null,   // httpSimulatorClassName
            null,    // http transaction types
            true,    // is fhir
            // request transform classes
            null,
            "src"   // must match src from src(mhd) in collections.txt
    ),
    DOC_ADMIN(
            "Document Administrator",
            Arrays.asList("docadmin"),
            "docadmin",
            "", // no factory
            "", // no simulator
            Arrays.asList(TransactionType.STORED_QUERY, TransactionType.UPDATE),
            false,  // show in config - only partially configured
            null,  // actorsFileLabel
            null,   // httpSimulatorClassName
            null,    // http transaction types
            false,    // is fhir
            // proxy transform classes
            null,
            Constants.USE_SHORTNAME   // must match docadmin in collections.txt
    ),
    /*
        FHIR_SERVER(
            "FHIR Server",
            Arrays.asList(""),
            "fhir",
            "gov.nist.toolkit.simcommon.server.factories.FhirActorFactory",
            "gov.nist.toolkit.fhir.simulators.fhir.FhirSimulator",
            Arrays.asList(TransactionType.FHIR),
            true,
            null,
            true,
            Constants.USE_SHORTNAME
    ),

    FHIR_SUPPORT(
            "FHIR Support",
            Arrays.asList(""),
            "fhirsupport",
            "",
            "",
            Arrays.asList(TransactionType.NONE),  // place holder - transaction types
            false,  // show in config - only partially configured - only used in IT tests
            null,  // actorsFileLabel
            null,   // httpSimulatorClassName
            null,    // http transaction types
            true,    // is fhir
            // proxy transform classes
            null,
            Constants.USE_SHORTNAME   // must match src from src(mhd) in collections.txt
    ),
     */
    FILTER_PROXY(
            "Filter Proxy",
            Arrays.asList("filter_PROXY", "filterproxy", "Filter Proxy"),
            "fproxy",
            "gov.nist.toolkit.simcommon.server.factories.FilterProxyActorFactory",
            "gov.nist.toolkit.fhir.simulators.filterProxy.FilterProxySimulator",
            Arrays.asList(TransactionType.ANY),
            false,
            null,
            false,
            Constants.USE_SHORTNAME
    ),
    SIM_PROXY(   // this is really an abstract type - offers no transactions in configuration, PIF is only placeholder
            "Sim Proxy",
            Arrays.asList(""),
            "simproxy",
            "gov.nist.toolkit.simcommon.server.factories.SimProxyFactory",
            "gov.nist.toolkit.fhir.simulators.proxy.sim.SimProxySimulator",  // only constructor should be used
            Arrays.asList(TransactionType.NONE),  // place holder - transaction types
            false,  // show in config - only partially configured - only used in IT tests
            null,  // actorsFileLabel
            null,   // httpSimulatorClassName
            null,    // http transaction types
            false,    // is fhir
            null,
            Constants.USE_SHORTNAME
    ),
    ANY(
            "Any",
            Arrays.asList(""),
            "any",
            null,
            null,
            Arrays.asList(TransactionType.NONE),  // place holder
            false,
            null,
            false,
            Constants.USE_SHORTNAME
    );

    private static final long serialVersionUID = 1L;
    String name;
    List<String> altNames;
    String shortName;
    List<TransactionType> transactionTypes; // TransactionTypes this actor can receive
    boolean showInConfig;
    String actorsFileLabel;
    String simulatorFactoryName;
    String simulatorClassName;
    List<TransactionType> httpTransactionTypes;
    String httpSimulatorClassName;
    boolean isFhir;
    List<String> proxyTransforms;
    String actorCode;
    IheItiProfile profile;
    /**
     * Conformance Test Options
     */
    List<OptionType> options;

    ActorType() {
    } // for GWT

    // Basic constructor for "older" simulator types
    ActorType(String name, List<String> altNames, String shortName, String simulatorFactoryName, String simulatorClassName, List<TransactionType> tt, boolean showInConfig, String actorsFileLabel, boolean isFhir, String actorCode) {
        this.name = name;
        this.altNames = altNames;
        this.shortName = shortName;
        this.simulatorFactoryName = simulatorFactoryName;
        this.simulatorClassName = simulatorClassName;
        this.transactionTypes = tt;   // This actor receives
        this.showInConfig = showInConfig;
        this.actorsFileLabel = actorsFileLabel;
        this.httpTransactionTypes = new ArrayList<>();
        this.httpSimulatorClassName = null;
//        this.isFhir = isFhir;
        this.proxyTransforms = null;
        this.actorCode = actorCode;
    }

    // All growth happens here
    ActorType(String name, List<String> altNames, String shortName, String simulatorFactoryName,
       String simulatorClassName, List<TransactionType> tt, boolean showInConfig,
       String actorsFileLabel, String httpSimulatorClassName, List<TransactionType> httpTt,
              boolean isFhir,
              List<String> proxyTransforms,
              String actorCode) {
       this(name, altNames, shortName, simulatorFactoryName, simulatorClassName, tt, showInConfig, actorsFileLabel, false, actorCode);
       if (httpTt == null)
           httpTt = new ArrayList<>();
       this.httpTransactionTypes = httpTt;
       this.httpSimulatorClassName = httpSimulatorClassName;
//       this.isFhir = isFhir;
       if (proxyTransforms == null)
           proxyTransforms = new ArrayList<>();
       this.proxyTransforms = proxyTransforms;
   }

    public String getActor()  {
        return shortName;
   }

   /*
   Not needed after Asbestos was introduced.
   public boolean isProxy() {
        if (proxyTransforms == null) return false;
        if (proxyTransforms.size() == 0) return false;
        return true;
   }
    */

   public boolean isFhir() { return isFhir; }

   public String getSimulatorFactoryName() { return simulatorFactoryName; }

    public boolean showInConfig() {
        return showInConfig;
    }

    public boolean isRepositoryActor() {
        return this.equals(REPOSITORY);
    }

    public boolean isRegistryActor() {
        return this.equals(REGISTRY);
    }

    public boolean isRGActor() {
        return this.equals(RESPONDING_GATEWAY);
    }

    public boolean isRGXActor() {
        return this.equals(RESPONDING_GATEWAY_X);
    }

    public boolean isRigActor() {
        return this.equals(RESPONDING_IMAGING_GATEWAY);
    }

    public boolean isIGActor() {
        return this.equals(INITIATING_GATEWAY);
    }

    public boolean isIGXActor() {
        return this.equals(INITIATING_GATEWAY_X);
    }

    public boolean isIigActor() {
        return this.equals(INITIATING_IMAGING_GATEWAY);
    }

    public boolean isGW() { return isRGActor() || isIGActor() || isIigActor() || isRigActor() || isIGXActor() || isRGXActor(); }

    public boolean isImagingDocumentSourceActor() {
        return this.equals(IMAGING_DOC_SOURCE);
    }

    public String getSimulatorClassName() { return simulatorClassName; }

    public String getHttpSimulatorClassName() { return httpSimulatorClassName; }

    public String getActorsFileLabel() {
        return actorsFileLabel;
    }

    static public List<String> getActorNames() {
        List<String> names = new ArrayList<>();

        for (ActorType a : values())
            names.add(a.name);

        return names;
    }

    static public List<String> getActorNamesForConfigurationDisplays() {
        List<String> names = new ArrayList<String>();

        for (ActorType a : values())
            if (a.showInConfig())
                names.add(a.name);

        return names;
    }

    /**
     * Within toolkit, each TransactionType maps to a unique ActorType
     * (as receiver of the transaction). To make this work, transaction
     * names are customized to make this mapping unique.  This goes
     * beyond the definition in the TF.
     *
     * NOT SAFE - THERE CAN BE MULTIPLE ACTORS DECLARING SINGLE
     * TRANSACTIONTYPE
     *
     * @param tt
     * @return
     */
    @Obsolete
    static public ActorType getActorType(TransactionType tt) {
        if (tt == null)
            return null;
        for (ActorType at : values()) {
            if (at.hasTransaction(tt))
                return at;
        }
        return null;
    }

    static public Set<ActorType> getActorTypes(TransactionType tt) {
        Set<ActorType> types = new HashSet<>();
        if (tt == null)
            return types;
        for (ActorType at : values()) {
            if (at.hasTransaction(tt))
                types.add(at);
        }
        return types;
    }

    static public Set<ActorType> getAllActorTypes() {
        Set<ActorType> types = new HashSet<>();
        for (ActorType at : values()) {
                types.add(at);
        }
        return types;
    }

    static public ActorType findActor(String name) {
        if (name == null)
            return null;

        for (ActorType actor : values()) {
            if (actor.name.equals(name))
                return actor;
            if (actor.shortName.equals(name))
                return actor;
            if (actor.altNames.contains(name))
                return actor;
            if (actor.actorCode != null && actor.actorCode.equals(name))
                return actor;
        }
        return null;
    }

    /**
     * Finds actor type by its test collection code.
     * @param tcCode
     * @return
     */
    static public ActorType findActorByTcCode(String tcCode) {
        if (tcCode == null)
            return null;

        for (ActorType actor : values()) {
            if (actor.getActorCode().equalsIgnoreCase(tcCode)) return actor;
        }
        return null;
    }

    static public TransactionType find(String receivingActorStr, String transString) {
        if (receivingActorStr == null || transString == null) return null;

        ActorType a = findActor(receivingActorStr);
        return a.getTransaction(transString);
    }

    /**
     * Return TransactionType for passed transaction name.
    * @param name of transaction, matched to TransactionType short name, name,
    * or id. Both SOAP and Http transactions are searched
    * @return TransactionType for this name, or null if no match found.
    */
   public TransactionType getTransaction(String name) {
        for (TransactionType tt : transactionTypes) {
            if (tt.getShortName().equals(name)) return tt;
            if (tt.getName().equals(name)) return tt;
            if (tt.getId().equals(name)) return tt;
        }
        for (TransactionType tt : httpTransactionTypes) {
           if (tt.getShortName().equals(name)) return tt;
           if (tt.getName().equals(name)) return tt;
           if (tt.getId().equals(name)) return tt;
       }
        return null;
    }


    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append(name).append(" [");
        for (TransactionType tt : transactionTypes)
            buf.append(tt).append(",");
        for (TransactionType tt : httpTransactionTypes)
           buf.append(tt).append(",");
        buf.append("]");

        return buf.toString();
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public List<TransactionType> getTransactions() {
        return transactionTypes;
    }

    public List<TransactionType> getHTTPTransactions() { return httpTransactionTypes; }

   public boolean hasTransaction(TransactionType transType) {
      for (TransactionType transType2 : transactionTypes) {
         if (transType2.equals(transType)) return true;
      }
         for (TransactionType transType2 : httpTransactionTypes) {
            if (transType2.equals(transType)) return true;
         }
      return false;
   }


    public boolean equals(ActorType at) {
        try {
            return name.equals(at.name);
        } catch (Exception e) {
        }
        return false;
    }

    public List<String> getProxyTransforms() {
        return proxyTransforms;
    }

    public String getActorCode() {
       if (Constants.USE_SHORTNAME == actorCode)
           return shortName;
       else
            return actorCode;
    }

    public boolean isFilterProxy() {
        return this.equals(FILTER_PROXY);
    }


    private static class Constants {
        static final String USE_SHORTNAME = null;
    }
}
