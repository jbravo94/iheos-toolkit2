package gov.nist.toolkit.xdstools2.client.tabs.actorConfigTab;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import gov.nist.toolkit.xdstools2.client.LoadGazelleConfigs;

class ReloadSystemFromGazelleClickHandler implements ClickHandler {

	/**
	 * 
	 */
	private ActorConfigTab actorConfigTab;

	/**
	 * @param actorConfigTab
	 */
	ReloadSystemFromGazelleClickHandler(ActorConfigTab actorConfigTab) {
		this.actorConfigTab = actorConfigTab;
	}

	public void onClick(ClickEvent event) {
		String systemName = this.actorConfigTab.currentEditSite.getName();
		if (systemName == null || systemName.equals(""))
			return;
		new LoadGazelleConfigs(actorConfigTab, systemName).load();
	}
	
}