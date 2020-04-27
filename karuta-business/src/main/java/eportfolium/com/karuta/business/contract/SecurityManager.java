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

package eportfolium.com.karuta.business.contract;

import java.util.List;

import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.exception.AuthenticationException;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

/**
 * Cette classe rassemble la gestion et la modification des utilisateurs, des
 * groupes et des rôles. Le cycle de vie entier de l’utilisateur, de la création
 * à la suppression de son identité au sein du système, est alors contrôlé en un
 * seul endroit.
 * 
 * @author mlengagne
 *
 */
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

	/**
	 * 
	 * This method provides a way for security officers to change info of other
	 * users.
	 * 
	 * @param byUserId
	 * @param forUserId
	 * @param xmlUser
	 * @return
	 * @throws BusinessException
	 */
	String changeUser(Long byUserId, Long forUserId, String xmlUser) throws BusinessException;

	/**
	 * This method provides a way for users to change their personal info.
	 * 
	 * @param byUserId
	 * @param forUserId
	 * @param xmlUser
	 * @return
	 * @throws BusinessException
	 */
	String changeUserInfo(Long byUserId, Long forUserId, String xmlUser) throws BusinessException;

	boolean registerUser(String username, String password);

	Long addUser(String username, String email) throws BusinessException;

	boolean addUser(String username, String email, boolean isDesigner, long userId) throws Exception;

	String generatePassword();

	void removeUser(Long byUser, Long forUser) throws BusinessException;

	void removeUsers(Long byUser, Long forUser) throws BusinessException;

	void changeUser(Credential user) throws BusinessException;

	boolean isAdmin(Long userId);

	boolean isCreator(Long userId);

	/**
	 * Check if customer password is the right one
	 *
	 * @param passwd Password
	 * @return bool result
	 */
	boolean checkPassword(Long userID, String passwd);

	String[] postCredentialFromXml(String login, String password, String substit);

	boolean userHasRole(long userId, long roleId);

	String addUsers(String xmlUsers, Long userId) throws Exception;

	Long addRole(String portfolioUuid, String role, Long userId) throws BusinessException;

	/**
	 * Add user to a role
	 * 
	 * @param byUserId
	 * @param grid      roleId
	 * @param forUserId
	 * @return
	 * @throws BusinessException
	 */
	String addUserRole(Long byUserId, Long grid, Long forUserId) throws BusinessException;

	void removeUserRole(Long userId, Long groupRightInfoId) throws BusinessException;

	void removeUsersFromRole(Long userId, String portId) throws Exception;

	void removeRole(Long userId, Long groupRightInfoId) throws Exception;

	void removeRights(Long userId, Long groupId) throws BusinessException;

	Long changeRole(Long userId, Long rrgId, String xmlRole) throws DoesNotExistException, BusinessException, Exception;

	String addUsersToRole(Long id, Long rrgId, String xmlUser) throws BusinessException;

	void addUserToGroup(Long byUser, Long forUser, Long groupId) throws BusinessException;

	Credential authenticateUser(String login, String password) throws AuthenticationException;

	/**
	 * Add user in user groups.
	 * 
	 * @param userId
	 * @param credentialGroupIds
	 * @return
	 */
	boolean addUserInCredentialGroups(Long userId, List<Long> credentialGroupIds);

	/**
	 * Remove a user from a user group,
	 * 
	 * @param userId
	 * @param credentialGroupId
	 * @return
	 */
	Boolean deleteUserFromCredentialGroup(Long userId, Long credentialGroupId);

}
