/* =======================================================
	Copyright 2020 - ePortfolium - Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
   ======================================================= */

package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

/**
 * @author mlengagne
 *         <p>
 *         Relatif à l'authentification
 *         </p>
 *
 */
/**
 * @author mlengagne
 *
 */
public interface CredentialDao {

	List<Credential> findAll();

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

	Credential getUser(Long userId);

	List<Credential> getUsers(String username, String firstname, String lastname);

	Credential getActiveByUserId(Long userID);

	String getUserUid(String login);

	Long getUserId(String userLogin);

	Long getUserId(String userLogin, String email);

	Long getPublicUid();

	List<Credential> getUsersByRole(Long userId, String portfolioUuid, String role);

	int updateCredentialToken(Long userId, String token);

	boolean isAdmin(Long userId);

	boolean isCreator(Long userId);

	boolean userHasRole(Long userId, Long roleId);

	String getEmailByLogin(String login);

	String getLoginById(Long userId);

	/**
	 * Requête permettant de récupérer toutes les informations dans la table
	 * credential pour un utilisateur particulier.
	 * 
	 * @param userId id de l'utilisateur.
	 * @return
	 */
	Credential getUserInfos(Long userId);

	ResultSet getMysqlUsers(Connection con, String username, String firstname, String lastname);

	ResultSet findAll(String table, Connection con);

	void removeAll();

	boolean isDesigner(Long userId, String nodeId);
}