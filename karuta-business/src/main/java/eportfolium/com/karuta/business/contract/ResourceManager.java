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

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.document.ResourceDocument;
import eportfolium.com.karuta.document.ResourceList;
import eportfolium.com.karuta.model.exception.BusinessException;

public interface ResourceManager extends BaseManager {

	ResourceDocument getResource(UUID parentNodeId, Long userId, Long groupId) throws BusinessException;

	ResourceList getResources(UUID portfolioId, Long userId, Long groupId);

	Integer changeResource(UUID parentNodeId, ResourceDocument resource, Long userId, Long groupId)
			throws BusinessException, JsonProcessingException;

	String addResource(UUID parentNodeId, ResourceDocument resource, Long userId, Long groupId)
			throws BusinessException;

	void removeResource(UUID resourceId, Long userId, Long groupId) throws BusinessException;

	void changeResourceByXsiType(UUID nodeId, String xsiType, ResourceDocument resource, Long userId)
			throws BusinessException;

	int addResource(UUID id, UUID parentId, String xsiType, String content, UUID portfolioModelId,
						   boolean sharedNodeRes, boolean sharedRes, Long userId);

	int updateResource(UUID nodeUuid, String content, Long userId);

	int updateResource(UUID id, String xsiType, String content, Long userId);

	int updateContextResource(UUID nodeUuid, String content, Long userId);
}
