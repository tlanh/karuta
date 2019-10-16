package eportfolium.com.karuta.business.contract;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.util.MimeType;

import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface NodeManager {

	String getNode(MimeType outMimeType, String nodeUuid, boolean withChildren, Long userId, Long groupId, String label,
			Integer cutoff) throws DoesNotExistException, BusinessException, Exception;

	String getParentNodes(String portfoliocode, String semtag, String semtag_parent, String code_parent)
			throws Exception;

	GroupRights getRights(Long userId, Long groupId, UUID nodeUuid);

	GroupRights getRights(Long userId, Long groupId, String nodeUuid);

	void removeRights(long groupId, Long groupRightId, Long id) throws BusinessException;

	void resetRights(List<Node> children) throws ParserConfigurationException;

	/**
	 * /** <node uuid=""> <role name=""> <right RD="" WR="" DL="" />
	 * <action>reset</action> </role> </node> ====== <portfolio uuid="">
	 * <xpath>XPATH</xpath> <role name=""> <right RD="" WR="" DL="" />
	 * <action>reset</action> </role> </portfolio> ====== <portfoliogroup name="">
	 * <xpath>XPATH</xpath> <role name=""> <right RD="" WR="" DL="" />
	 * <action>reset</action> </role> </portfoliogroup>
	 * 
	 * @param xmlNode
	 * @param userId
	 * @param subId
	 * @param label
	 * @throws BusinessException
	 * @throws Exception
	 */
	void changeRights(String xmlNode, Long id, Long credentialSubstitutionId, String label)
			throws BusinessException, Exception;

	String changeRights(Long userId, String nodeUuid, String role, GroupRights rights) throws BusinessException;

	UUID getPortfolioIdFromNode(Long userId, String nodeUuid) throws DoesNotExistException, BusinessException;

	StringBuffer getNodeXmlOutput(String nodeUuid, boolean withChildren, String withChildrenOfXsiType, Long userId,
			Long groupId, String label, boolean checkSecurity);

	String getNodeBySemanticTag(MimeType textXml, String portfolioUuid, String semantictag, Long userId, Long groupId)
			throws BusinessException;

	String getNodesBySemanticTag(MimeType outMimeType, Long userId, Long groupId, String portfolioUuid,
			String semanticTag) throws BusinessException;

	/**
	 * forcedParentUuid permet de forcer l'uuid parent, independamment de l'attribut
	 * du noeud fourni
	 * 
	 * @param c
	 * @param node
	 * @param portfolioUuid
	 * @param portfolioModelId
	 * @param userId
	 * @param ordrer
	 * @param forcedUuid
	 * @param forcedUuidParent
	 * @param sharedResParent
	 * @param sharedNodeResParent
	 * @param rewriteId
	 * @param resolve
	 * @param parseRights
	 * @return
	 * @throws BusinessException
	 */
	String writeNode(org.w3c.dom.Node node, String portfolioUuid, UUID portfolioModelId, Long userId, int ordrer,
			String forcedUuid, String forcedUuidParent, boolean sharedResParent, boolean sharedNodeResParent,
			boolean rewriteId, Map<String, String> resolve, boolean parseRights) throws BusinessException;

	boolean isCodeExist(String code, String uuid);

	String executeMacroOnNode(long userId, String nodeUuid, String macroName);

	String getNodeMetadataWad(MimeType mimeType, String nodeUuid, boolean b, Long userId, Long groupId, String label)
			throws DoesNotExistException, BusinessException;

	Integer changeNode(MimeType inMimeType, String nodeUuid, String in, Long userId, Long groupId) throws Exception;

	int deleteNode(String nodeUuid, Long id, long groupId);

	long getRoleByNode(Long userId, String nodeUuid, String role) throws BusinessException;

	String changeNodeMetadataWad(MimeType mimeType, String nodeUuid, String xmlNode, Long userId, Long groupId)
			throws Exception;

	boolean changeParentNode(Long userid, String uuid, String uuidParent) throws BusinessException;

	Long moveNodeUp(Long id, String nodeId) throws BusinessException;

	String addNodeFromModelBySemanticTag(MimeType inMimeType, String parentNodeUuid, String semanticTag, Long userId,
			Long groupId) throws Exception;

	String changeNodeMetadataEpm(MimeType mimeType, String nodeUuid, String xmlNode, Long id, long groupId)
			throws Exception;

	String changeNodeMetadata(MimeType mimeType, String nodeUuid, String xmlNode, Long id, long groupId)
			throws DoesNotExistException, BusinessException, Exception;

	String changeNodeContext(MimeType mimeType, String nodeUuid, String xmlNode, Long userId, Long groupId)
			throws BusinessException, Exception;

	String changeNodeResource(MimeType mimeType, String nodeUuid, String xmlNode, Long id, Long groupId)
			throws BusinessException, Exception;

	String addNode(MimeType inMimeType, String parentNodeUuid, String in, Long userId, Long groupId, boolean forcedUuid)
			throws Exception;

	String getNodeWithXSL(MimeType textXml, String nodeUuid, String xslFile, String parameters, Long id, Long groupId)
			throws BusinessException, Exception;

	String getNodes(MimeType mimeType, String portfoliocode, String semtag, Long id, long groupId, String semtag_parent,
			String code_parent, Integer cutoff) throws BusinessException;

	String executeAction(Long userId, String nodeUuid, String action, String role);

	String copyNode(MimeType inMimeType, String destUuid, String tag, String code, String srcuuid, Long userId,
			Long groupId) throws Exception;

	String importNode(MimeType mimeType, String parentId, String semtag, String code, String srcuuid, Long id,
			long groupId) throws BusinessException, Exception;

}
