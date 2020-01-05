package eportfolium.com.karuta.business.contract;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

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

	String getUserList(Long userId, String username, String firstname, String lastname);

	String getUsersByCredentialGroup(Long userGroupId);

	String getUserGroupByPortfolio(String portfolioUuid, Long userId);

	String getUsersByRole(Long userId, String portfolioUuid, String role);

	String getRole(Long groupRightInfoId) throws BusinessException;

	String getUserInfos(Long userId) throws DoesNotExistException;

	Long getUserId(String userLogin);

	String getUserRolesByUserId(Long userId);

	Long getPublicUserId();

	String getEmailByLogin(String userLogin);

	Long getUserId(String userLogin, String email);


	String getRoleList(String portfolio, Long userId, String role) throws BusinessException;

	/**
	 * Liste des RRG utilisateurs d'un portfolio donné
	 * 
	 * @param portId
	 * @param id
	 * @return
	 * @throws Exception
	 */
	String findUserRolesByPortfolio(String portId, Long id) throws Exception;

	String findUserRole(Long rrgid);

	Set<String[]> getNotificationUserList(Long userId, Long groupId, String uuid);

	Credential getUser(Long userId) throws DoesNotExistException;

	// -----------------------------------------------------------------------------------------------------------------

	void transferCredentialGroupMembersTable(Connection con, Map<Long, Long> userIds, Map<Long, Long> cgIds)
			throws SQLException;

	Map<Long, Long> transferCredentialTable(Connection con) throws SQLException;

	void transferCredentialSubstitutionTable(Connection con, Map<Long, Long> userIds) throws SQLException;

	void removeUsers();

}
