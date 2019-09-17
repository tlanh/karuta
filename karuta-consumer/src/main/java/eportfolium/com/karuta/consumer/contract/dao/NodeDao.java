package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.activation.MimeType;

import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface NodeDao {

	void persist(Node transientInstance);

	void remove(Node persistentInstance);

	Node merge(Node detachedInstance);

	Node findById(Serializable id) throws DoesNotExistException;

	Node getNode(String nodeUuid);

	Node getNode(UUID nodeUuid);

	Node getNode(String nodeUuid, Long userId, Long groupId);

	Integer getNodeOrderByNodeUuid(String nodeUuid);

	Integer getNodeOrderByNodeUuid(UUID nodeUuid);

	UUID getPortfolioIdFromNode(String nodeUuid) throws Exception;

	Node getParentNode(UUID portfolioUuid, String semtag_parent, String code_parent) throws Exception;

	Node getParentNodeByNodeUuid(String nodeUuid);

	Node getParentNodeByNodeUuid(UUID nodeUuid);

	UUID getParentNodeUuidByNodeUuid(UUID nodeUuid);

	UUID getParentNodeUuidByNodeUuid(String nodeUuid);

	Node getNodeBySemanticTag(UUID nodeUuid, String semantictag);

	Node getNodeBySemanticTag(String nodeUuid, String semantictag);

	List<Node> getNodesBySemanticTag(UUID portfolioUuid, String semantictag);

	List<Node> getNodesBySemanticTag(String portfolioUuid, String semantictag);

	List<Node> getNodes(UUID portfolioUuid);

	List<Node> getNodes(String portfolioUuid);

	/**
	 * From node, check if portfolio has user 'sys_public' in group 'all' <br>
	 * To differentiate between 'public' to the world, and 'public' to people with
	 * an account
	 * 
	 * @param node_uuid
	 * @param portfolio_uuid
	 * @return
	 */
	boolean isPublic(String nodeUuid, String portfolioUuid);

	/**
	 * From node, check if portfolio has user 'sys_public' in group 'all' <br>
	 * To differentiate between 'public' to the world, and 'public' to people with
	 * an account
	 * 
	 * @param node_uuid
	 * @param portfolio_uuid
	 * @return
	 */
	boolean isPublic(UUID nodeUuid, String portfolioUuid);

	/**
	 * Ecrit le noeud en base
	 * 
	 * @param nodeUuid
	 * @param nodeParentUuid
	 * @param nodeChildrenUuid
	 * @param asmType
	 * @param xsiType
	 * @param sharedRes
	 * @param sharedNode
	 * @param sharedNodeRes
	 * @param sharedResUuid
	 * @param sharedNodeUuid
	 * @param sharedNodeResUuid
	 * @param metadata
	 * @param metadataWad
	 * @param metadataEpm
	 * @param semtag
	 * @param semanticTag
	 * @param label
	 * @param code
	 * @param descr
	 * @param format
	 * @param order
	 * @param modifUserId
	 * @param portfolioUuid
	 * @return
	 */
	int create(String nodeUuid, String nodeParentUuid, String nodeChildrenUuid, String asmType, String xsiType,
			boolean sharedRes, boolean sharedNode, boolean sharedNodeRes, String sharedResUuid, String sharedNodeUuid,
			String sharedNodeResUuid, String metadata, String metadataWad, String metadataEpm, String semtag,
			String semanticTag, String label, String code, String descr, String format, int order, Long modifUserId,
			String portfolioUuid);

	int update(String nodeUuid, String asmType, String xsiType, String semantictag, String label, String code,
			String descr, String format, String metadata, String metadataWad, String metadataEpm, boolean sharedRes,
			boolean sharedNode, boolean sharedNodeRes, Long modifUserId);

	int updateNodeOrder(String nodeUuid, int order);

	int updateNodeCode(String nodeUuid, String order);

	/**
	 * Same code allowed with nodes in different portfolio, and not root node
	 * 
	 * @param nodeuuid
	 * @param code
	 * @return
	 */
	boolean isCodeExist(String nodeuuid, String code);

	/**
	 * Same code allowed with nodes in different portfolio, and not root node
	 * 
	 * @param code
	 * @param nodeuuid
	 * @return
	 */
	boolean isCodeExist(String code, UUID nodeuuid);
//	----------------------------------------------------------------------------------------------------------------------------

	List<Node> getNodes(MimeType outMimeType, String portfolioUuid, int userId, int groupId, String semtag,
			String parentUuid, String filterId, String filterParameters, String sortId, Integer cutoff)
			throws Exception;

	List<Node> getNodes(MimeType mimeType, String portfoliocode, String semtag, int userId, int groupId,
			String semtag_parent, String code_parent, Integer cutoff) throws SQLException;

	Object getNodesBySemanticTag(MimeType outMimeType, int userId, int groupId, String portfolioUuid,
			String semanticTag) throws SQLException;

	Node getNodeWithXSL(MimeType mimeType, String nodeUuid, String xslFile, String parameters, int userId, int groupId);

	List<Node> getNodesParent(MimeType mimeType, String portfoliocode, String semtag, int userId, int groupId,
			String semtag_parent, String code_parent) throws Exception;

	Object getNodeMetadataWad(MimeType mimeType, String nodeUuid, boolean b, int userId, int groupId, String label)
			throws SQLException;

	String getResNode(String contextUuid, int userId, int groupId) throws Exception;

	List<Node> getNodeRights(String nodeUuid, int userId, int groupId) throws Exception;

	Object putNode(MimeType inMimeType, String nodeUuid, String in, int userId, int groupId) throws Exception;

	Object putNodeMetadata(MimeType mimeType, String nodeUuid, String xmlNode, int userId, int groupId)
			throws Exception;

	Object putNodeMetadataWad(MimeType mimeType, String nodeUuid, String xmlNode, int userId, int groupId)
			throws Exception;

	Object putNodeMetadataEpm(MimeType mimeType, String nodeUuid, String xmlNode, int userId, int groupId)
			throws Exception;

	Object putNodeNodeContext(MimeType mimeType, String nodeUuid, String xmlNode, int userId, int groupId)
			throws Exception;

	Object putNodeNodeResource(MimeType mimeType, String nodeUuid, String xmlNode, int userId, int groupId)
			throws Exception;

	Object postNode(MimeType inMimeType, String parentNodeUuid, String in, int userId, int groupId, boolean forcedUuid)
			throws Exception;

	Object postNodeFromModelBySemanticTag(MimeType mimeType, String nodeUuid, String semantictag, int userId,
			int groupId) throws Exception;

	Object postImportNode(MimeType inMimeType, String destUuid, String tag, String code, String srcuuid, int userId,
			int groupId) throws Exception;

	Object postCopyNode(MimeType inMimeType, String destUuid, String tag, String code, String srcuuid, int userId,
			int groupId) throws Exception;

	/**
	 * @return: 0: OK -1: Invalid uuid -2: First node, can't move
	 */
	int postMoveNodeUp(int userid, String uuid);

	boolean postChangeNodeParent(int userid, String uuid, String uuidParent);

	Object deleteNode(String nodeUuid, int userId, int groupId) throws Exception;

}