package eportfolium.com.karuta.business.contract;

import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.exception.AuthenticationException;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface SecurityManager {

	/**
	 * 
	 * This method provides a way for security officers to"reset" the userPassword.
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	boolean changePassword(String username, String password);

	/**
	 * This method provides a way for users to change their own userPassword.
	 * 
	 * @param userId
	 * @param currentPassword
	 * @param newPassword
	 * @throws BusinessException
	 */
	void changeUserPassword(Long userId, String currentPassword, String newPassword) throws BusinessException;

	boolean registerUser(String username, String password);

	Long createUser(String username, String email) throws BusinessException;

	boolean createUser(String username, String email, boolean isDesigner, long userId) throws Exception;

	String generatePassword();

	int deleteUser(Long userId) throws BusinessException;

	void deleteUsers(Long userId, Long groupId) throws BusinessException;

	void changeUser(Credential user) throws BusinessException;

	String changeUser(Long userId, Long userId2, String xmlData) throws BusinessException;

	boolean isAdmin(Long id);

	boolean isCreator(Long id);

	/**
	 * Check if customer password is the right one
	 *
	 * @param passwd Password
	 * @return bool result
	 */
	boolean checkPassword(Long userID, String passwd);

	String changeUserInfo(Long userId, long userId2, String xmlUser) throws BusinessException;

	String[] postCredentialFromXml(String login, String password, String substit);

	boolean isUserMemberOfRole(long userId, long roleId);

	String addOrUpdateRole(String xmlRole, Long userId) throws Exception;

	String addUsers(String in, Long userId) throws Exception;

	Long createRole(String portfolioUuid, String role, Long userId) throws BusinessException;

	/**
	 * Add user to a role
	 * 
	 * @param userId
	 * @param grid    roleId
	 * @param userId2
	 * @return
	 * @throws BusinessException
	 */
	String addUserRole(Long userId, Long grid, Long userId2) throws BusinessException;

	/**
	 * Add user to a role
	 * 
	 * @param userId
	 * @param rrgId
	 * @param user
	 * @return
	 * @throws BusinessException
	 */
	String addUserRole2(Long userId, Long rrgId, Long user) throws BusinessException;

	void removeUserRole(Long userId, Long groupRightInfoId) throws BusinessException;

	void removeUsersFromRole(Long userId, String portId) throws Exception;

	void removeRole(Long userId, Long groupRightInfoId) throws Exception;

	void removeRights(Long groupId, Long userId) throws BusinessException;

	void changeRole(Long id, Long rrgId, String data) throws DoesNotExistException, BusinessException, Exception;

	String addUsersToRole(Long id, Long rrgId, String xmlNode) throws BusinessException;

	void addUserToGroup(Long user, Long userId, Long groupId) throws BusinessException;

	Credential authenticateUser(String loginId, String password) throws AuthenticationException;

}
