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
import java.util.UUID;

import eportfolium.com.karuta.model.bean.CredentialGroup;
import eportfolium.com.karuta.model.exception.BusinessException;

public interface GroupManager {

	String addGroup(String name);

	String getCredentialGroupByUser(Long userId);

	String getGroupsByRole(String portfolioUuid, String role);

	boolean changeNotifyRoles(Long userId, String portfolioUuid, String nodeUuid, String notify)
			throws BusinessException;

	boolean changeNotifyRoles(Long userId, UUID portfolioUuid, UUID nodeUuid, String notify) throws BusinessException;

	boolean setPublicState(Long userId, String portfolioUuid, boolean isPublic) throws BusinessException;

	Long addCredentialGroup(String credentialGroupName) throws Exception;

	boolean renameCredentialGroup(Long credentialGroupId, String newName);

	CredentialGroup getCredentialGroupByName(String name);

	String getCredentialGroupList();

	/**
	 * Get groups from a user
	 * 
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	String getUserGroups(Long userId) throws Exception;

	Boolean removeCredentialGroup(Long credentialGroupId);

	String addUserGroup(String xmlGroup, Long userId) throws BusinessException, Exception;

	void changeUserGroup(Long grid, Long groupId, Long userId) throws BusinessException;

	String getGroupRights(Long userId, Long groupId) throws Exception;

	/**
	 * Ajout des droits du portfolio dans GroupRightInfo et GroupRights
	 * 
	 * @param label
	 * @param uuid
	 * @param right
	 * @param portfolioUuid
	 * @param userId
	 * @return
	 */
	boolean addGroupRights(String label, String nodeUuid, String right, String portfolioUuid, Long userId);

	void removeRights(long groupId, Long userId) throws BusinessException;

	/*******************************************************
	 * Fcts. pour migration de base de données
	 *******************************************************/
	void transferGroupGroupTable(Connection con, Map<Long, Long> giIds) throws SQLException;

	Map<Long, Long> transferGroupRightInfoTable(Connection con, Map<String, String> portIds) throws SQLException;

	Map<Long, Long> transferGroupInfoTable(Connection con, Map<Long, Long> griIds) throws SQLException;

	void transferGroupRightsTable(Connection con, Map<Long, Long> griIds) throws SQLException;

	void transferGroupUserTable(Connection con, Map<Long, Long> giIds, Map<Long, Long> userIds) throws SQLException;

	Map<Long, Long> transferCredentialGroupTable(Connection con) throws SQLException;

	void removeGroups();

}
