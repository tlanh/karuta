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

import java.util.UUID;

import eportfolium.com.karuta.document.*;

public interface UserManager {

	CredentialList getUserList(String username, String firstname, String lastname);

	CredentialGroupDocument getUsersByCredentialGroup(Long userGroupId);

	CredentialList getUsersByRole(UUID portfolioId, String role);

	RoleDocument getRole(Long groupRightInfoId);

	CredentialDocument getUserInfos(Long userId);

	Long getUserId(String userLogin);

	Long getUserId(String userLogin, String email);

	ProfileList getUserRolesByUserId(Long userId);

	String getEmailByLogin(String userLogin);

	RoleRightsGroupList getRoleList(UUID portfolioId, Long userId);

	GroupUserList getUserRolesByPortfolio(UUID portfolioId, Long id);

	RoleRightsGroupDocument getUserRole(Long rrgid);

}
