package eportfolium.com.karuta.business.contract;

import java.util.HashMap;
import java.util.UUID;

import javax.activation.MimeType;

import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.exception.BusinessException;

public interface NodeManager {

	String getNode(MimeType outMimeType, String nodeUuid, boolean withChildren, Long userId, Long groupId, String label,
			Integer cutoff);

	String getNodesParent(String portfoliocode, String semtag, String semtag_parent, String code_parent)
			throws Exception;

	String getResource(String nodeUuid);

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
	String writeNode(Node node, String portfolioUuid, UUID portfolioModelId, Long userId, int ordrer, String forcedUuid,
			String forcedUuidParent, int sharedResParent, int sharedNodeResParent, boolean rewriteId,
			HashMap<String, String> resolve, boolean parseRights) throws BusinessException;

	GroupRights getRights(Long userId, Long groupId, UUID nodeUuid);

	GroupRights getRights(Long userId, Long groupId, String nodeUuid);

	StringBuffer getNodeXmlOutput(String nodeUuid, boolean withChildren, String withChildrenOfXsiType, Long userId,
			Long groupId, String label, boolean checkSecurity);

	Object getNodeBySemanticTag(MimeType outMimeType, String portfolioUuid, String semantictag, Long userId,
			Long groupId);

	Object getNodesBySemanticTag(MimeType outMimeType, Long userId, Long groupId, String portfolioUuid,
			String semanticTag);

	boolean hasRight(Long userId, Long groupId, String nodeUuid, String right);

	int deleteResource(String resourceUuid, Long userId, Long groupId);

	int updateResourceByXsiType(String nodeUuid, String xsiType, String content, Long userId);

}
