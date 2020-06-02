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
import java.util.UUID;

import eportfolium.com.karuta.document.CredentialDocument;
import eportfolium.com.karuta.document.CredentialList;
import eportfolium.com.karuta.document.LoginDocument;
import eportfolium.com.karuta.document.RoleDocument;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.exception.AuthenticationException;
import eportfolium.com.karuta.model.exception.BusinessException;

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
	 * @param user
	 * @return
	 * @throws BusinessException
	 */
	Long changeUser(Long byUserId, Long forUserId, CredentialDocument user) throws BusinessException;

	/**
	 * This method provides a way for users to change their personal info.
	 * 
	 * @param byUserId
	 * @param forUserId
	 * @param user
	 * @return
	 * @throws BusinessException
	 */
	Long changeUserInfo(Long byUserId, Long forUserId, CredentialDocument user) throws BusinessException;

	boolean addUser(String username, String email);

	String generatePassword();

	void removeUsers(Long byUser, Long forUser) throws BusinessException;

	boolean isAdmin(Long userId);

	boolean isCreator(Long userId);

	/**
	 * Check if customer password is the right one
	 *
	 * @param passwd Password
	 * @return bool result
	 */
	boolean checkPassword(Long userID, String passwd);

	CredentialDocument login(LoginDocument credentials);

	boolean userHasRole(long userId, long roleId);

	CredentialList addUsers(CredentialList users, Long userId) throws BusinessException;

	Long addRole(UUID portfolioId, String role, Long userId) throws BusinessException;

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

	void removeUsersFromRole(Long userId, UUID portfolioId) throws Exception;

	void removeRole(Long userId, Long groupRightInfoId) throws Exception;

	Long changeRole(Long userId, Long rrgId, RoleDocument role) throws BusinessException;

	String addUsersToRole(Long id, Long rrgId, CredentialList users) throws BusinessException;

	void addUserToGroup(Long byUser, Long forUser, Long groupId) throws BusinessException;

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
	void deleteUserFromCredentialGroup(Long userId, Long credentialGroupId);

}
