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
import eportfolium.com.karuta.model.exception.BusinessException;

public interface SecurityManager {
	/**
	 * This method provides a way for security officers to"reset" the userPassword.
	 */
	boolean changePassword(String username, String password);

	/**
	 * This method provides a way for users to change their own userPassword.
	 */
	void changeUserPassword(Long userId, String currentPassword, String newPassword) throws BusinessException;

	/**
	 * This method provides a way for security officers to change info of other
	 * users.
	 */
	Long changeUser(Long byUserId, Long forUserId, CredentialDocument user) throws BusinessException;

	/**
	 * This method provides a way for users to change their personal info.
	 */
	Long changeUserInfo(Long byUserId, Long forUserId, CredentialDocument user) throws BusinessException;

	boolean addUser(String username, String email);

	String generatePassword();

	void removeUsers(Long forUser);

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

	CredentialList addUsers(CredentialList users);

	Long addRole(UUID portfolioId, String role, Long userId) throws BusinessException;

	String addUserRole(Long grid, Long forUserId);

	void removeUserRole(Long userId, Long groupRightInfoId);

	void removeUsersFromRole(UUID portfolioId);

	void removeRole(Long groupRightInfoId);

	Long changeRole(Long rrgId, RoleDocument role);

	String addUsersToRole(Long rrgId, CredentialList users);

	void addUserToGroup(Long forUser, Long groupId);

	boolean addUserInCredentialGroups(Long userId, List<Long> credentialGroupIds);

	void deleteUserFromCredentialGroup(Long userId, Long credentialGroupId);

}
