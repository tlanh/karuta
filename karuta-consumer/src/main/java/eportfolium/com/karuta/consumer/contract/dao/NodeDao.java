package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface NodeDao {

	void persist(Node transientInstance);

	void remove(Node persistentInstance);

	Node merge(Node detachedInstance);

	Node findById(Serializable id) throws DoesNotExistException;

	Integer getNodeOrderByNodeUuid(String nodeUuid);

	String getPortfolioIdFromNode(String nodeUuid);

	Node getParentNode(String portfolioUuid, String semtag_parent, String code_parent) throws Exception;

	Node getParentNodeByNodeUuid(String nodeUuid);

	String getParentNodeUuidByNodeUuid(String nodeUuid);

	Node getNodeBySemanticTag(UUID nodeUuid, String semantictag);

	Node getNodeBySemanticTag(String nodeUuid, String semantictag);

	List<Node> getNodesBySemanticTag(UUID portfolioUuid, String semantictag);

	List<Node> getNodesBySemanticTag(String portfolioUuid, String semantictag);

	List<Node> getNodes(String portfolioUuid);

	/**
	 * Récupère les noeuds partages dans un portfolio
	 * 
	 * @param portfolioUuid
	 * @return
	 */
	List<Node> getSharedNodes(String portfolioUuid);

	List<Node> getNodesWithResources(String portfolioUuid);

	List<Node> getNodes(Collection<String> nodeIds);

	/**
	 * From node, check if portfolio has user 'sys_public' in group 'all' <br>
	 * To differentiate between 'public' to the world, and 'public' to people with
	 * an account
	 * 
	 * @param node_uuid
	 * 
	 * @return
	 */
	boolean isPublic(String nodeUuid);

	/**
	 * From node, check if portfolio has user 'sys_public' in group 'all' <br>
	 * To differentiate between 'public' to the world, and 'public' to people with
	 * an account
	 * 
	 * @param node_uuid
	 * @return
	 */
	boolean isPublic(UUID nodeUuid);

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
	 * @param code
	 * @param nodeuuid
	 * 
	 * @return
	 */
	boolean isCodeExist(String code, String nodeuuid);

	/**
	 * Same code allowed with nodes in different portfolio, and not root node
	 * 
	 * @param code
	 * @param nodeuuid
	 * @return
	 */
	boolean isCodeExist(char[] code, UUID nodeuuid);

	int updateNode(String nodeUuid);

	String getMetadataWad(String nodeUuid);

	String getMetadataWad(UUID nodeUuid);

	List<Node> getFirstLevelChildren(String parentNodeUuid);

	/**
	 * Pour retrouver les enfants du noeud et affecter les droits.
	 * 
	 * @param nodeUuid
	 * @return
	 * @throws DoesNotExistException
	 */
	List<Node> getChildren(String nodeUuid) throws DoesNotExistException;

	String getNodeUuidBySemtag(String string, String nodeUuid) throws DoesNotExistException;

	Node getParentNode(String parentUuid, String semantictag);

	Integer getNodeNextOrderChildren(String nodeUuid);

	String getNodeUuidByPortfolioModelAndSemanticTag(String portfolioModelId, String semanticTag);

	List<Node> getNodesByOrder(String nodeUuid, int order);

	ResultSet getMysqlNodes(Connection con);

	void removeAll();

	ResultSet getMysqlChildrenNodes(Connection con, String parentNodeUuid);

	ResultSet getMysqlRootNodes(Connection con);

	ResultSet getMysqlRootNode(Connection con, String portfolioUuid);

	ResultSet getMysqlNode(Connection c, String nodeUuid);

}