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

import javax.xml.parsers.ParserConfigurationException;

import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.exception.BusinessException;

public interface NodeManager {

	String getNode(UUID nodeId, boolean withChildren, Long userId, Long groupId, String label,
			Integer cutoff) throws BusinessException, ParserConfigurationException;

	String getChildNodes(String parentNodeCode, String parentSemtag, String semtag) throws Exception;

	GroupRights getRights(Long userId, Long groupId, UUID nodeId);

	void resetRights(List<Node> children) throws ParserConfigurationException;

	/**
	 * <node uuid=""> <role name=""> <right RD="" WR="" DL="" />
	 * <action>reset</action> </role> </node> ====== <portfolio uuid="">
	 * <xpath>XPATH</xpath> <role name=""> <right RD="" WR="" DL="" />
	 * <action>reset</action> </role> </portfolio> ====== <portfoliogroup name="">
	 * <xpath>XPATH</xpath> <role name=""> <right RD="" WR="" DL="" />
	 * <action>reset</action> </role> </portfoliogroup>
	 * 
	 * @param xmlNode
	 * @param label
	 * @throws BusinessException
	 * @throws Exception
	 */
	void changeRights(String xmlNode, Long id, Long credentialSubstitutionId, String label)
			throws BusinessException, Exception;

	String changeRights(Long userId, UUID nodeId, String role, GroupRights rights) throws BusinessException;

	UUID getPortfolioIdFromNode(Long userId, UUID nodeId) throws BusinessException;

	String getNodeXmlOutput(UUID nodeId, boolean withChildren, String withChildrenOfXsiType, Long userId,
			Long groupId, String label, boolean checkSecurity);

	String getNodeBySemanticTag(UUID portfolioId, String semantictag, Long userId, Long groupId)
			throws BusinessException;

	String getNodesBySemanticTag(Long userId, Long groupId, UUID portfolioId,
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
	UUID writeNode(org.w3c.dom.Node node, UUID portfolioId, UUID portfolioModelId, Long userId, int ordrer,
					 UUID forcedId, UUID forcedParentId, boolean sharedResParent, boolean sharedNodeResParent,
					 boolean rewriteId, Map<UUID, UUID> resolve, boolean parseRights) throws BusinessException;

	boolean isCodeExist(String code, UUID uuid);

	boolean isCodeExist(String code);

	String executeMacroOnNode(long userId, UUID nodeId, String macroName);

	String getNodeMetadataWad(UUID nodeId, Long userId, Long groupId)
			throws BusinessException;

	Integer changeNode(UUID nodeId, String xmlNode, Long userId, Long groupId)
			throws Exception;

	void removeNode(UUID nodeId, Long userId, long groupId) throws BusinessException;

	long getRoleByNode(Long userId, UUID nodeUuid, String role) throws BusinessException;

	String changeNodeMetadataWad(UUID nodeId, String xmlMetawad, Long userId, Long groupId)
			throws Exception;

	boolean changeParentNode(Long userid, UUID id, UUID parentId) throws BusinessException;

	Long moveNodeUp(UUID nodeId);

	String addNodeFromModelBySemanticTag(UUID nodeId, String semanticTag, Long userId,
			Long groupId) throws Exception;

	String changeNodeMetadataEpm(UUID nodeId, String xmlMetadataEpm, Long id, long groupId)
			throws Exception;

	String changeNodeMetadata(UUID nodeId, String xmlNode, Long id, long groupId)
			throws Exception;

	String changeNodeContext(UUID nodeId, String xmlNode, Long userId, Long groupId)
			throws BusinessException, Exception;

	String changeNodeResource(UUID nodeId, String xmlNode, Long id, Long groupId)
			throws BusinessException, Exception;

	String addNode(UUID parentNodeId, String xmlNode, Long userId, Long groupId,
			boolean forcedUuid) throws Exception;

	String getNodeWithXSL(UUID nodeId, String xslFile, String parameters, Long id, Long groupId)
			throws Exception;

	String getNodes(String rootNodeCode, String childSemtag, Long userId, Long groupId,
			String parentSemtag, String parentNodeCode, Integer cutoff) throws BusinessException;

	String executeAction(Long userId, UUID nodeId, String action, String role);

	String copyNode(UUID destId, String tag, String code, UUID sourceId, Long userId,
			Long groupId);

	UUID importNode(UUID parentId, String semtag, String code, UUID sourceId, Long id, long groupId);

	int updateNodeCode(UUID nodeId, String code);

	UUID getChildUuidBySemtag(UUID rootId, String semantictag);

	List<Node> getChildren(UUID nodeId);
}
