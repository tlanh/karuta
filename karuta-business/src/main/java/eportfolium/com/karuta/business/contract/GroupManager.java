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

import eportfolium.com.karuta.document.CredentialGroupList;
import eportfolium.com.karuta.document.GroupInfoList;
import eportfolium.com.karuta.document.GroupRightsList;
import eportfolium.com.karuta.document.RoleGroupList;
import eportfolium.com.karuta.model.bean.CredentialGroup;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.exception.BusinessException;

import java.util.UUID;

public interface GroupManager {

	CredentialGroupList getCredentialGroupByUser(Long userId);

	RoleGroupList getGroupsByRole(UUID portfolioId, String role);

	void changeNotifyRoles(UUID portfolioId, UUID nodeId, String notify);

	void setPublicState(Long userId, UUID portfolioId, boolean isPublic) throws BusinessException;

	Long addCredentialGroup(String credentialGroupName);

	boolean renameCredentialGroup(Long credentialGroupId, String newName);

	CredentialGroup getCredentialGroupByName(String name);

	CredentialGroupList getCredentialGroupList();

	GroupInfoList getUserGroups(Long userId);

	void removeCredentialGroup(Long credentialGroupId);

	void changeUserGroup(Long grid, Long groupId);

	GroupRightsList getGroupRights(Long groupId);

	void addGroupRights(String label, UUID nodeUuid, String right, UUID portfolioId, Long userId);

	void removeRights(long groupId);

	GroupRightInfo getByPortfolioAndLabel(UUID portfolioId, String role);

}
