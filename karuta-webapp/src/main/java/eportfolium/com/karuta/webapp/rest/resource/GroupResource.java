package eportfolium.com.karuta.webapp.rest.resource;

import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.business.contract.GroupManager;

public class GroupResource extends AbstractResource {

	@Autowired
	private GroupManager groupManager;
	
	private GroupResource() {
	}

	

}
