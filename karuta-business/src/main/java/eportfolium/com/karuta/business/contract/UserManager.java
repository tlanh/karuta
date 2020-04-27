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
	String getUserRolesByPortfolio(String portId, Long id) throws Exception;

	String getUserRole(Long rrgid);

	Set<String[]> getNotificationUserList(Long userId, Long groupId, String uuid);

	Credential getUser(Long userId) throws DoesNotExistException;

	// -----------------------------------------------------------------------------------------------------------------

	void transferCredentialGroupMembersTable(Connection con, Map<Long, Long> userIds, Map<Long, Long> cgIds)
			throws SQLException;

	Map<Long, Long> transferCredentialTable(Connection con) throws SQLException;

	void transferCredentialSubstitutionTable(Connection con, Map<Long, Long> userIds) throws SQLException;

	void removeUsers();

}
