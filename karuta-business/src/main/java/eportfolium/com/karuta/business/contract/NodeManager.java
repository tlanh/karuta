package eportfolium.com.karuta.business.contract;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.util.MimeType;

import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface NodeManager {

	String getNode(MimeType outMimeType, String nodeUuid, boolean withChildren, Long userId, Long groupId, String label,
			Integer cutoff) throws DoesNotExistException, BusinessException, Exception;

	String getChildNodes(String parentNodeCode, String parentSemtag, String semtag) throws Exception;

	GroupRights getRights(Long userId, Long groupId, String nodeUuid);

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
	 * @param userId
	 * @param subId
	 * @param label
	 * @throws BusinessException
	 * @throws Exception
	 */
	void changeRights(String xmlNode, Long id, Long credentialSubstitutionId, String label)
			throws BusinessException, Exception;

	String changeRights(Long userId, String nodeUuid, String role, GroupRights rights) throws BusinessException;

	String getPortfolioIdFromNode(Long userId, String nodeUuid) throws DoesNotExistException, BusinessException;

	String getNodeXmlOutput(String nodeUuid, boolean withChildren, String withChildrenOfXsiType, Long userId,
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
	String writeNode(org.w3c.dom.Node node, String portfolioUuid, String portfolioModelId, Long userId, int ordrer,
			String forcedUuid, String forcedUuidParent, boolean sharedResParent, boolean sharedNodeResParent,
			boolean rewriteId, Map<String, String> resolve, boolean parseRights) throws BusinessException;

	boolean isCodeExist(String code, String uuid);

	String executeMacroOnNode(long userId, String nodeUuid, String macroName) throws BusinessException;

	String getNodeMetadataWad(MimeType mimeType, String nodeUuid, Long userId, Long groupId)
			throws DoesNotExistException, BusinessException;

	Integer changeNode(MimeType inMimeType, String nodeUuid, String xmlNode, Long userId, Long groupId)
			throws Exception;

	void removeNode(String nodeUuid, Long userId, long groupId) throws BusinessException;

	long getRoleByNode(Long userId, String nodeUuid, String role) throws BusinessException;

	String changeNodeMetadataWad(MimeType mimeType, String nodeUuid, String xmlMetawad, Long userId, Long groupId)
			throws Exception;

	boolean changeParentNode(Long userid, String uuid, String uuidParent) throws BusinessException;

	Long moveNodeUp(String nodeUuid) throws BusinessException;

	String addNodeFromModelBySemanticTag(MimeType inMimeType, String nodeUuid, String semanticTag, Long userId,
			Long groupId) throws Exception;

	String changeNodeMetadataEpm(MimeType mimeType, String nodeUuid, String xmlMetadataEpm, Long id, long groupId)
			throws Exception;

	String changeNodeMetadata(MimeType mimeType, String nodeUuid, String xmlNode, Long id, long groupId)
			throws DoesNotExistException, BusinessException, Exception;

	String changeNodeContext(MimeType mimeType, String nodeUuid, String xmlNode, Long userId, Long groupId)
			throws BusinessException, Exception;

	String changeNodeResource(MimeType mimeType, String nodeUuid, String xmlNode, Long id, Long groupId)
			throws BusinessException, Exception;

	String addNode(MimeType inMimeType, String parentNodeUuid, String xmlNode, Long userId, Long groupId,
			boolean forcedUuid) throws Exception;

	String getNodeWithXSL(MimeType textXml, String nodeUuid, String xslFile, String parameters, Long id, Long groupId)
			throws BusinessException, Exception;

	String getNodes(MimeType mimeType, String rootNodeCode, String childSemtag, Long userId, Long groupId,
			String parentSemtag, String parentNodeCode, Integer cutoff) throws BusinessException;

	String executeAction(Long userId, String nodeUuid, String action, String role);

	String copyNode(MimeType inMimeType, String destUuid, String tag, String code, String srcuuid, Long userId,
			Long groupId) throws Exception;

	String importNode(MimeType mimeType, String parentId, String semtag, String code, String srcuuid, Long id,
			long groupId) throws BusinessException, Exception;

	void transferAnnotationTable(Connection con, Map<String, String> nodesIds) throws SQLException;

	void removeAnnotations();

	void removeNodes();

	Map<String, String> transferNodeTable(Connection con, Map<String, String> rtIds, Map<String, String> portIds,
			Map<Long, Long> userIds) throws SQLException;

}
