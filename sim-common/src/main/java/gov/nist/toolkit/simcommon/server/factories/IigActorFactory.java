package gov.nist.toolkit.simcommon.server.factories;

import gov.nist.toolkit.actortransaction.shared.ActorType;
import gov.nist.toolkit.actortransaction.client.ParamType;
import gov.nist.toolkit.configDatatypes.server.SimulatorProperties;
import gov.nist.toolkit.configDatatypes.client.TransactionType;
import gov.nist.toolkit.simcommon.client.SimId;
import gov.nist.toolkit.simcommon.client.Simulator;
import gov.nist.toolkit.simcommon.client.SimulatorConfig;
import gov.nist.toolkit.simcommon.server.AbstractActorFactory;
import gov.nist.toolkit.simcommon.server.IActorFactory;
import gov.nist.toolkit.simcommon.server.SimManager;
import gov.nist.toolkit.sitemanagement.client.Site;
import gov.nist.toolkit.sitemanagement.client.TransactionBean;
import gov.nist.toolkit.sitemanagement.client.TransactionBean.RepositoryType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Factory for Initiating Imaging Gateway
 * 
 * @author Ralph Moulton / MIR WUSTL IHE Development Project <a
 * href="mailto:moultonr@mir.wustl.edu">moultonr@mir.wustl.edu</a>
 *
 */
public class IigActorFactory extends AbstractActorFactory implements IActorFactory {
   SimId newID = null;


   static final List <TransactionType> incomingTransactions =
      Arrays.asList(TransactionType.RET_IMG_DOC_SET_GW);

   public Simulator buildNew(SimManager simm, SimId newID, String environment, boolean configureBase) throws Exception {
      this.newID = newID;

      ActorType actorType = ActorType.INITIATING_IMAGING_GATEWAY;
      SimulatorConfig sc;
      if (configureBase) sc = configureBaseElements(actorType, newID, newID.getTestSession(), environment);
      else sc = new SimulatorConfig();
      
      SimId simId = sc.getId();

      addFixedEndpoint(sc, SimulatorProperties.idsrIigEndpoint, actorType,
         TransactionType.RET_IMG_DOC_SET_GW, false);
      addFixedEndpoint(sc, SimulatorProperties.idsrIigTlsEndpoint, actorType,
         TransactionType.RET_IMG_DOC_SET_GW, true);
      addEditableConfig(sc, SimulatorProperties.respondingImagingGateways,
         ParamType.SELECTION, new ArrayList <String>(), true);


      return new Simulator(sc);
   }

   protected void verifyActorConfigurationOptions(SimulatorConfig sc) {

   }

   @Override
   public Site buildActorSite(SimulatorConfig sc, Site site) {
      String siteName = sc.getDefaultName();

      if (site == null) site = new Site(siteName, sc.getId().getTestSession());

      site.setTestSession(sc.getId().getTestSession()); // labels this site as coming from a sim

      boolean isAsync = false;
     
      site.addTransaction(new TransactionBean(
         TransactionType.RET_IMG_DOC_SET_GW.getCode(), RepositoryType.NONE,
         sc.get(SimulatorProperties.idsrIigEndpoint).asString(), false, isAsync));
      site.addTransaction(new TransactionBean(
         TransactionType.RET_IMG_DOC_SET_GW.getCode(), RepositoryType.NONE,
         sc.get(SimulatorProperties.idsrIigTlsEndpoint).asString(), true, isAsync));
      return site;
   }

   public List <TransactionType> getIncomingTransactions() {
      return incomingTransactions;
   }

   @Override
   public ActorType getActorType() {
      return ActorType.INITIATING_IMAGING_GATEWAY;
   }
}
