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
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.document.*;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.exception.BusinessException;

public interface NodeManager {

	NodeDocument getNode(UUID nodeId, boolean withChildren, Long userId, Long groupId, String label,
			Integer cutoff) throws BusinessException;

	GroupRights getRights(Long userId, Long groupId, UUID nodeId);

	void resetRights(List<Node> children) throws JsonProcessingException;

	String changeRights(Long userId, UUID nodeId, String label, GroupRights rights);

	UUID getPortfolioIdFromNode(Long userId, UUID nodeId) throws BusinessException;

	NodeDocument getNode(UUID nodeId, boolean withChildren, String withChildrenOfXsiType, Long userId,
			Long groupId, String label, boolean checkSecurity);

	NodeDocument getNodeBySemanticTag(UUID portfolioId, String semantictag, Long userId, Long groupId)
			throws BusinessException;

	NodeList getNodesBySemanticTag(Long userId, Long groupId, UUID portfolioId,
								   String semanticTag) throws BusinessException;

	/**
	 * forcedParentUuid permet de forcer l'uuid parent, independamment de l'attribut
	 * du noeud fourni
	 *
	 * @param node
	 * @param portfolioId
	 * @param portfolioModelId
	 * @param userId
	 * @param ordrer
	 * @param forcedId
	 * @param forcedParentId
	 * @param sharedResParent
	 * @param sharedNodeResParent
	 * @param rewriteId
	 * @param resolve
	 * @param parseRights
	 * @return
	 * @throws BusinessException
	 */
	UUID writeNode(NodeDocument node, UUID portfolioId, UUID portfolioModelId, Long userId, int ordrer,
				   UUID forcedId, UUID forcedParentId, boolean sharedResParent, boolean sharedNodeResParent,
				   boolean rewriteId, Map<UUID, UUID> resolve, boolean parseRights)
			throws BusinessException, JsonProcessingException;

	boolean isCodeExist(String code, UUID uuid);

	boolean isCodeExist(String code);

	String executeMacroOnNode(long userId, UUID nodeId, String macroName)
			throws JsonProcessingException, BusinessException;

	MetadataWadDocument getNodeMetadataWad(UUID nodeId, Long userId, Long groupId)
			throws BusinessException;

	Integer changeNode(UUID nodeId, NodeDocument node, Long userId, Long groupId)
			throws Exception;

	void removeNode(UUID nodeId, Long userId, long groupId) throws BusinessException;

	long getRoleByNode(Long userId, UUID nodeUuid, String role) throws BusinessException;

	String changeNodeMetadataWad(UUID nodeId, MetadataWadDocument metadata, Long userId, Long groupId)
			throws BusinessException, JsonProcessingException;

	boolean changeParentNode(Long userid, UUID id, UUID parentId) throws BusinessException;

	Long moveNodeUp(UUID nodeId);

	NodeList addNodeFromModelBySemanticTag(UUID nodeId, String semanticTag, Long userId,
			Long groupId) throws BusinessException, JsonProcessingException;

	String changeNodeMetadataEpm(UUID nodeId, MetadataEpmDocument metadata, Long id, long groupId)
			throws BusinessException, JsonProcessingException;

	String changeNodeMetadata(UUID nodeId, MetadataDocument metadata, Long id, long groupId)
			throws BusinessException, JsonProcessingException;

	String changeNodeContext(UUID nodeId, ResourceDocument resource, Long userId, Long groupId)
			throws BusinessException;

	String changeNodeResource(UUID nodeId, ResourceDocument resource, Long id, Long groupId)
			throws BusinessException;

	NodeList addNode(UUID parentNodeId, NodeDocument node, Long userId, Long groupId,
			boolean forcedUuid) throws JsonProcessingException, BusinessException;

	NodeDocument getNodeWithXSL(UUID nodeId, String xslFile, String parameters, Long id, Long groupId)
			throws BusinessException;

	NodeList getNodes(String rootNodeCode, String childSemtag, Long userId, Long groupId,
			String parentSemtag, String parentNodeCode, Integer cutoff) throws BusinessException;

	String executeAction(Long userId, UUID nodeId, String action, String role);

	UUID copyNode(UUID destId, String tag, String code, UUID sourceId, Long userId,
			Long groupId) throws BusinessException, JsonProcessingException;

	UUID importNode(UUID parentId, String semtag, String code, UUID sourceId, Long id, long groupId)
			throws BusinessException, JsonProcessingException;

	int updateNodeCode(UUID nodeId, String code);

	UUID getChildUuidBySemtag(UUID rootId, String semantictag);

	List<Node> getChildren(UUID nodeId);
}
