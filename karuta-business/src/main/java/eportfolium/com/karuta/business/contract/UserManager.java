package eportfolium.com.karuta.business.contract;

import java.util.List;

import eportfolium.com.karuta.model.exception.BusinessException;

public interface UserManager {

	/**
	 * @deprecated
	 * 
	 *             Apparently unused, keep getListUsers
	 * 
	 * @param userId
	 * @param username
	 * @param firstname
	 * @param lastname
	 * @return
	 * @throws Exception
	 */
	String getUsers(Long userId, String username, String firstname, String lastname);

	String getListUsers(Long userId, String username, String firstname, String lastname);

	Boolean deleteUsersGroups(Long userGroupId);

	String getUsersByUserGroup(Long userGroupId);

	void addGroups(Long userID, List<Long> groups);

	String getUserGroups(Long userId) throws Exception;

	String getUserGroupList();

	boolean postGroupsUsers(Long user, Long userId, Long groupId) throws BusinessException;

	String getUserGroupByPortfolio(String portfolioUuid, Long userId);

	String getUsersByRole(Long userId, String portfolioUuid, String role);

	String getRole(Long grid);

	String getInfUser(Long userId) throws BusinessException;

	Long getUserID(String username);

	String getUserRolesByUserId(Long userId);

	Long getPublicUserId();

}
