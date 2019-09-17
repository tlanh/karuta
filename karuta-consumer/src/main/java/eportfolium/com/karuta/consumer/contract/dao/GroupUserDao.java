package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import eportfolium.com.karuta.model.bean.GroupUser;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface GroupUserDao {

	void persist(GroupUser transientInstance);

	void remove(GroupUser persistentInstance);

	GroupUser merge(GroupUser detachedInstance);

	GroupUser findById(Serializable id) throws DoesNotExistException;

	void removeById(final Serializable id) throws DoesNotExistException;

	List<GroupUser> getUserGroups();

	List<GroupUser> getUserGroups(final Long userId);

	boolean isUserInGroup(String uid, String gid);

	boolean isUserMemberOfGroup(Long userId, Long groupId);

	boolean postGroupsUsers(int user, int userId, int groupId);

	List<GroupUser> getUserGroupByPortfolio(String portfolioUuid);

	List<GroupUser> getUserGroupByPortfolio(UUID portfolioUuid);

	List<GroupUser> getUserGroupByPortfolioAndUser(String portfolioUuid, Long userId);

	List<GroupUser> getUserGroupByPortfolioAndUser(UUID portfolioUuid, Long userId);

	Integer putUserGroup(String usergroup, String userPut);

	/**
	 * Supprime les utilisateurs des RRG d'un portfolio donné
	 * 
	 * @param portId
	 * @return
	 */
	int deleteUserGroupByPortfolio(String portId);

	int deleteUserGroupByPortfolio(UUID portId);

}