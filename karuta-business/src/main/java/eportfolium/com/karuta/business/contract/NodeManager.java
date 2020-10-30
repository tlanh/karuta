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

public interface NodeManager extends BaseManager {

	String getNode(UUID nodeId, Long userId, Integer cutoff) throws JsonProcessingException;

	void resetRights(List<Node> children) throws JsonProcessingException;

	void changeRights(UUID nodeId, String label, GroupRights rights);

	UUID getPortfolioIdFromNode(UUID nodeId);

	NodeDocument getNode(UUID nodeId, boolean withChildren, String withChildrenOfXsiType, Long userId,
			String label, boolean checkSecurity) throws JsonProcessingException;

	String getNodeWithXSL(UUID nodeUuid, String folder, String xslFile, String parameters, Long userId );
	
	/**
	 * forcedParentUuid permet de forcer l'uuid parent, independamment de l'attribut
	 * du noeud fourni
	 */
	Node writeNode(NodeDocument node, UUID portfolioId, Long userId, int ordrer, UUID forcedId, Node parentNode,
				   boolean rewriteId, Map<UUID, UUID> resolve, boolean parseRights)
			throws BusinessException, JsonProcessingException;

	boolean isCodeExist(String code);

	String executeMacroOnNode(long userId, UUID nodeId, String macroName)
			throws JsonProcessingException;

	void removeNode(UUID nodeId);

	long getRoleByNode(Long userId, UUID nodeUuid, String role);

	String changeNodeMetadataWad(UUID nodeId, MetadataWadDocument metadata)
			throws JsonProcessingException;

	boolean changeParentNode(UUID id, UUID parentId);

	Long moveNodeUp(UUID nodeId);

	String changeNodeMetadataEpm(UUID nodeId, MetadataEpmDocument metadata)
			throws JsonProcessingException;

	String changeNodeMetadata(UUID nodeId, MetadataDocument metadata, Long id)
			throws BusinessException, JsonProcessingException;

	String changeNodeContext(UUID nodeId, ResourceDocument resource, Long userId)
			throws BusinessException;

	String changeNodeResource(UUID nodeId, ResourceDocument resource, Long id)
			throws BusinessException;

	NodeList getNodes(String rootNodeCode, String childSemtag, Long userId,
			String parentSemtag, String parentNodeCode, Integer cutoff) throws BusinessException;

	UUID copyNode(UUID destId, String tag, String code, UUID sourceId, Long userId)
			throws BusinessException, JsonProcessingException;

	UUID importNode(UUID parentId, String semtag, String code, UUID sourceId, Long id)
			throws BusinessException, JsonProcessingException;

	List<Node> getChildren(UUID nodeId);

	NodeRightsDocument getRights(UUID nodeId, Long userId);
}
