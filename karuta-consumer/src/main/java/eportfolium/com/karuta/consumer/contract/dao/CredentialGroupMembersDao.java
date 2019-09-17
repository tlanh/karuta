package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
import java.util.List;

import eportfolium.com.karuta.model.bean.CredentialGroupMembers;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface CredentialGroupMembersDao {

	void persist(CredentialGroupMembers transientInstance);

	void remove(CredentialGroupMembers persistentInstance);

	CredentialGroupMembers merge(CredentialGroupMembers detachedInstance);

	CredentialGroupMembers findById(Serializable id) throws DoesNotExistException;

	boolean isUserMemberOfGroup(int userId, int groupId);

	String getRoleUser(int userId, int userid2);

	List<CredentialGroupMembers> getUserGroupList(Long userId);

	int getGroupByGroupLabel(String groupLabel, int userId);

	List<CredentialGroupMembers> getGroupByUser(Long userId);

	String getUsersByUserGroup(int userGroupId, int userId);

	String getGroupsByRole(int userId, String portfolioUuid, String role);

	String getGroupsPortfolio(String portfolioUuid, int userId);

	Integer getRoleByNode(int userId, String nodeUuid, String role);

	Boolean putUserGroupLabel(Integer user, int siteGroupId, String label);

	Integer putUserGroup(String siteGroupId, String userId);

	Boolean putUserInUserGroup(int user, int siteGroupId, int currentUid);

	Object postGroup(String xmlgroup, int userId) throws Exception;

	boolean postGroupsUsers(int user, int userId, int groupId);

	int postUserGroup(String label, int userid);

	Boolean deleteUsersGroups(int usersgroup, int currentUid);

	Boolean deleteUsersFromUserGroups(Long userId, Long usersgroupId);

}