package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
import java.util.List;

import eportfolium.com.karuta.model.bean.GroupInfo;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface GroupInfoDao {

	void persist(GroupInfo transientInstance);

	void remove(GroupInfo persistentInstance);

	GroupInfo merge(GroupInfo detachedInstance);

	GroupInfo findById(Serializable id) throws DoesNotExistException;

	GroupInfo getGroupByName(String name);

	Long add(GroupRightInfo gri, long owner, String label);

	List<GroupInfo> getGroupsByRole(String portfolioUuid, String role);

	/**
	 * Recupere une instance de group_info associé à un grid.
	 * 
	 * @param grid
	 * @return
	 */
	GroupInfo getGroupByGrid(Long grid);

	void removeById(final Serializable id) throws DoesNotExistException;

	List<GroupInfo> getByPortfolio(String portfolioUuid);

	boolean exists(GroupRightInfo gri, long owner, String label);

}