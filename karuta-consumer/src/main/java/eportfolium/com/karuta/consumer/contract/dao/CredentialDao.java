package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.CredentialGroupMembers;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

/**
 * @author mlengagne
 *         <p>
 *         Relatif à l'authentification
 *         </p>
 *
 */
public interface CredentialDao {

	void persist(Credential transientInstance);

	void remove(Credential persistentInstance);

	void removeById(final Serializable id) throws DoesNotExistException;

	Credential merge(Credential detachedInstance);

	Credential findById(Serializable id) throws DoesNotExistException;

	/**
	 * Check if user is already registered in database
	 *
	 * @param string login login
	 * @return true if found, false otherwise
	 */
	boolean userExists(String login);

	/**
	 * Return credential instance from its login (does not check if user is active
	 * or not)
	 *
	 * @param string login login
	 * @return credential instance
	 */

	Credential getUserByLogin(String login);

	/**
	 * Return credential instance from its login (check if user is active)
	 *
	 * @param string login login
	 * @return credential instance
	 */
	Credential getByLogin(String login);

	/**
	 * Return credential instance from its login (optionnaly check password) (check
	 * if user is active)
	 *
	 * @param string login login
	 * @param string passwd Password is also checked if specified
	 * @return credential instance
	 */
	Credential getByLogin(String login, String passwd);

	/**
	 * Return credential instance from its login. <br>
	 * Check if user is admin or not.
	 * 
	 * @param login
	 * @param isAdmin
	 * @return Credential instance
	 */
	Credential getByLogin(String login, boolean isAdmin);

	/**
	 * Return credential instance from its e-mail
	 *
	 * @param string email e-mail
	 * @return Credential instance
	 */
	Credential getByEmail(String email);

	/**
	 * Return credential instance from its e-mail (optionnaly check password)
	 *
	 * @param string email e-mail
	 * @param string passwd Password is also checked if specified
	 * @return Credential instance
	 */
	Credential getByEmail(String email, String passwd);

	List<CredentialGroupMembers> getUsersByUserGroup(Long userGroupId);

	Credential getUser(Long userId);

	Credential getActiveByUserId(Long userID);

	String getUserUid(String login);

	Long getUserId(String username);

	Long getUserId(String username, String email);

	Long getPublicUid();

	List<Credential> getUsersByRole(Long userId, String portfolioUuid, String role);

	int updateCredentialToken(Long userId, String token);

	boolean isAdmin(String uid);

	boolean isAdmin(Long uid);

	boolean isDesigner(Long userId, String nodeId);

	boolean isDesigner(Long userId, UUID nodeId);

	boolean isCreator(Long userId);
	
	boolean isUserMemberOfRole(Long userId, Long roleId);
	
	String getEmailByLogin(String login);

	String getLoginById(Long userId);
	
	// ----------------------------------------------------------------------

	String[] postCredentialFromXml(Integer userId, String username, String password, String substitute)
			throws BusinessException;

	String getUserUidByTokenAndLogin(String login, String token) throws Exception;

	int deleteCredential(int userId);

	List<Credential> getUsers(Long userId, String username, String firstname, String lastname);

	String getListUsers(Long userId, String username, String firstname, String lastname);

	Credential getInfUser(Long userId);

	String getUserGroupByPortfolio(String portfolioUuid, int userId);

	Object putUser(int userId, String oAuthToken, String oAuthSecret) throws Exception;

	Object postUser(String xmluser, int userId) throws SQLException, Exception;

	Object deleteUser(int userid, int userId1);

	int deleteUsers(Integer userId, Integer userid2);


	

}