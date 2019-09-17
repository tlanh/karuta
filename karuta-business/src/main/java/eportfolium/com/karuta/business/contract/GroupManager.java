package eportfolium.com.karuta.business.contract;

import eportfolium.com.karuta.model.exception.GenericBusinessException;

public interface GroupManager {

	String createGroup(String name);

	String getGroupByUser(Long userId);

	String getGroupsByUser(Long id);

	boolean postNotifyRoles(Long userId, String portfolioId, String uuid, String notify)
			throws GenericBusinessException;

}
