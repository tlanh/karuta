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

import java.util.Set;
import java.util.UUID;

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

	String getUserGroupByPortfolio(UUID portfolioId, Long userId);

	String getUsersByRole(Long userId, UUID portfolioId, String role);

	String getRole(Long groupRightInfoId) throws BusinessException;

	String getUserInfos(Long userId) throws DoesNotExistException;

	Long getUserId(String userLogin);

	String getUserRolesByUserId(Long userId);

	Long getPublicUserId();

	String getEmailByLogin(String userLogin);

	Long getUserId(String userLogin, String email);


	String getRoleList(UUID portfolioId, Long userId, String role) throws BusinessException;

	/**
	 * Liste des RRG utilisateurs d'un portfolio donné
	 * 
	 * @param portfolioId
	 * @param id
	 * @return
	 * @throws Exception
	 */
	String getUserRolesByPortfolio(UUID portfolioId, Long id) throws Exception;

	String getUserRole(Long rrgid);

	Set<String[]> getNotificationUserList(Long userId, Long groupId, UUID groupRightId);

	Credential getUser(Long userId) throws DoesNotExistException;

}
