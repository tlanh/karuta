package eportfolium.com.karuta.consumer.impl.dao;
// Generated 17 juin 2019 11:33:18 by Hibernate Tools 5.2.10.Final

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.activation.MimeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import eportfolium.com.karuta.consumer.contract.dao.NodeDao;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.Portfolio;

/**
 * Home object implementation for domain model class Node.
 * 
 * @see dao.Node
 * @author Hibernate Tools
 */
@Repository
public class NodeDaoImpl extends AbstractDaoImpl<Node> implements NodeDao {

	@PersistenceContext
	private EntityManager entityManager;

	public NodeDaoImpl() {
		super();
		setCls(Node.class);

	}

	public Node getNode(UUID nodeUuid) {
		Node n = null;
		String sql = "SELECT n FROM Node n";
		sql += " WHERE n.id = :nodeUuid";
		try {
			TypedQuery<Node> q = entityManager.createQuery(sql, Node.class);
			q.setParameter("nodeUuid", nodeUuid);
			n = q.getSingleResult();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return n;
	}

	public Node getNode(String nodeUuid) {
		return getNode(nodeUuid, null, null);
	}

	public Node getNode(String nodeUuid, Long userId, Long groupId) {
		Node result = null;

		// On recupere d'abord les informations dans la table structures
		String sql = "SELECT n FROM Node WHERE n.id = :nodeID ";
		TypedQuery<Node> q = entityManager.createQuery(sql, Node.class);
		q.setParameter("nodeID", UUID.fromString(nodeUuid));

		try {
			result = q.getSingleResult();
		} catch (NoResultException e) {
		}
		return result;
	}

	public UUID getPortfolioIdFromNode(String nodeUuid) throws Exception {
		UUID result = null;
		String hql = "SELECT p.id FROM Node n";
		hql += " LEFT JOIN FETCH n.portfolio p";
		hql = " WHERE n.id = :nodeUUID";

		TypedQuery<UUID> q = entityManager.createQuery(hql, UUID.class);
		q.setParameter("nodeUUID", UUID.fromString(nodeUuid));
		try {
			result = q.getSingleResult();
		} catch (NoResultException e) {
		}
		return result;
	}

	public Node getNodeBySemanticTag(UUID nodeUuid, String semantictag) {
		Node n = null;
		String sql = "SELECT n FROM Node";
		sql += " WHERE n.semantictag = :semantictag";
		sql += " AND n.id = :id";
		TypedQuery<Node> q = entityManager.createQuery(sql, Node.class);
		q.setParameter("semantictag", semantictag);
		q.setParameter("id", nodeUuid);
		try {
			n = q.getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
		}
		return n;
	}

	public Node getNodeBySemanticTag(String nodeUuid, String semantictag) {
		return getNodeBySemanticTag(UUID.fromString(nodeUuid), semantictag);
	}

	public List<Node> getNodesBySemanticTag(UUID portfolioUuid, String semantictag) {
		String regexSemantictag = "%" + semantictag + "%";
		String sql = "SELECT n FROM Node n";
		sql += " INNER JOIN n.portfolio p WITH p.id = :portfolioUuid";
		sql += " WHERE n.semantictag LIKE :semantictag";
		sql += " ORDER BY n.code, n.nodeOrder";
		TypedQuery<Node> q = entityManager.createQuery(sql, Node.class);
		q.setParameter("portfolioUuid", portfolioUuid);
		q.setParameter("semantictag", regexSemantictag);
		return q.getResultList();
	}

	public List<Node> getNodesBySemanticTag(String portfolioUuid, String semantictag) {
		return getNodesBySemanticTag(UUID.fromString(portfolioUuid), semantictag);
	}

	public Integer getNodeOrderByNodeUuid(String nodeUuid) {
		return getNodeOrderByNodeUuid(UUID.fromString(nodeUuid));
	}

	public Integer getNodeOrderByNodeUuid(UUID nodeUuid) {
		Integer res = null;
		try {
			String sql = "SELECT n.nodeOrder FROM Node n";
			sql += " WHERE n.id = :nodeUuid";
			TypedQuery<Integer> q = entityManager.createQuery(sql, Integer.class);
			q.setParameter("nodeUuid", nodeUuid);
			res = q.getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
		}
		return res;

	}

	public int create(String nodeUuid, String nodeParentUuid, String nodeChildrenUuid, String asmType, String xsiType,
			boolean sharedRes, boolean sharedNode, boolean sharedNodeRes, String sharedResUuid, String sharedNodeUuid,
			String sharedNodeResUuid, String metadata, String metadataWad, String metadataEpm, String semtag,
			String semanticTag, String label, String code, String descr, String format, int order, Long modifUserId,
			String portfolioUuid) {
		Node node = new Node();
		try {
			node = findById(UUID.fromString(nodeUuid));
		} catch (Exception ex) {

		}
		node.setParentNode(new Node(UUID.fromString(nodeParentUuid)));
		if (nodeChildrenUuid != null) {
			node.setChildrenStr(nodeChildrenUuid);
		}
		node.setNodeOrder(order);
		node.setAsmType(asmType);
		node.setXsiType(xsiType);
		node.setSharedRes(sharedRes);
		node.setSharedNode(sharedNode);
		node.setSharedNodeRes(sharedNodeRes);
		node.setSharedResUuid(UUID.fromString(sharedResUuid));
		node.setSharedNodeUuid(UUID.fromString(sharedNodeUuid));
		node.setSharedNodeResUuid(UUID.fromString(sharedNodeResUuid));
		node.setMetadata(metadata);
		node.setMetadataWad(metadataWad);
		node.setMetadataEpm(metadataEpm);
		node.setSemtag(semtag);
		node.setSemantictag(semanticTag);
		node.setLabel(label);
		node.setCode(code);
		node.setDescr(descr);
		node.setFormat(format);
		node.setModifUserId(modifUserId);
		node.setPortfolio(new Portfolio(UUID.fromString(portfolioUuid)));
		try {
			merge(node);
		} catch (Exception ex) {
			ex.printStackTrace();
			return 1;
		}
		return 0;
	}

	public int update(String nodeUuid, String asmType, String xsiType, String semantictag, String label, String code,
			String descr, String format, String metadata, String metadataWad, String metadataEpm, boolean sharedRes,
			boolean sharedNode, boolean sharedNodeRes, Long modifUserId) {
		Node node = null;
		try {
			node = findById(UUID.fromString(nodeUuid));
		} catch (Exception ex) {
			return 1;
		}
		node.setAsmType(asmType);
		node.setXsiType(xsiType);
		node.setSemantictag(semantictag);
		node.setLabel(label);
		node.setCode(code);
		node.setDescr(descr);
		node.setMetadata(metadata);
		node.setMetadataWad(metadataWad);
		node.setMetadataEpm(metadataEpm);
		node.setSharedRes(sharedRes);
		node.setSharedNode(sharedNode);
		node.setSharedNodeRes(sharedNodeRes);
		node.setModifUserId(modifUserId);
		try {
			merge(node);
		} catch (Exception e) {
			return 1;
		}

		return 0;
	}

	public int updateNodeOrder(String nodeUuid, int order) {
		int result = 0;
		try {
			Node n = findById(UUID.fromString(nodeUuid));
			n.setNodeOrder(order);
			merge(n);
		} catch (Exception e) {
			result = 1;
		}
		return result;
	}

	public int updateNodeCode(String nodeUuid, String code) {
		int result = 0;
		try {
			Node n = findById(UUID.fromString(nodeUuid));
			n.setCode(code);
			merge(n);
		} catch (Exception e) {
			result = 1;
		}
		return result;
	}

	public Node getParentNode(UUID portfolioUuid, String semtag_parent, String code_parent) throws Exception {
		Node node = null;
		String sql = "SELECT n FROM Node n";
		sql += " WHERE n.portfolio.id = :Uuid";
		sql += " AND n.metadata LIKE :metadata ";
		sql += " AND n.code = :code";
		TypedQuery<Node> q = entityManager.createQuery(sql, Node.class);
		q.setParameter("Uuid", portfolioUuid);
		q.setParameter("metadata", "%semantictag=%" + semtag_parent + "%");
		q.setParameter("code", code_parent);
		try {
			node = q.getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
		}
		return node;
	}

	public Node getParentNodeByNodeUuid(String nodeUuid) {
		return getParentNodeByNodeUuid(UUID.fromString(nodeUuid));
	}

	public Node getParentNodeByNodeUuid(UUID nodeUuid) {
		Node result = null;
		String sql = "SELECT pn FROM Node n";
		sql += " INNER JOIN FETCH n.parentNode pn";
		sql += " WHERE n.id = :nodeUuid";
		TypedQuery<Node> q = entityManager.createQuery(sql, Node.class);
		q.setParameter("nodeUuid", nodeUuid);
		try {
			result = q.getSingleResult();
		} catch (NoResultException ex) {
			ex.printStackTrace();
		}
		return result;
	}

	public UUID getParentNodeUuidByNodeUuid(UUID nodeUuid) {
		Node res = getParentNodeByNodeUuid(nodeUuid);
		return res != null ? res.getId() : null;
	}

	public UUID getParentNodeUuidByNodeUuid(String nodeUuid) {
		return getParentNodeUuidByNodeUuid(UUID.fromString(nodeUuid));
	}

	public List<Node> getNodes(String portfolioUuid) {
		return getNodes(UUID.fromString(portfolioUuid));
	}

	public List<Node> getNodes(UUID portfolioUuid) {
		return entityManager.createQuery("SELECT n FROM Node n WHERE n.portfolio.id = :portfolioUuid", Node.class)
				.setParameter("portfolioUuid", portfolioUuid).getResultList();
	}

	public boolean isPublic(UUID nodeUuid, String portfolioUuid) {
		Query q = null;
		String sql = null;
		boolean val = false;
		if (nodeUuid != null) {
			sql = "SELECT n FROM Node n";
			sql += " INNER JOIN n.portfolio p";
			sql += " INNER JOIN p.groupRightInfo gri WITH gri.label='all'";
			sql += " INNER JOIN gri.groupInfo gi";
			sql += " INNER JOIN gi.groupUser gu";
			sql += " INNER JOIN gu.id.credential c WITH c.login='sys_public'";
			sql += " WHERE n.id = :nodeUuid";
			q = entityManager.createQuery(sql);
			q.setParameter("nodeUuid", nodeUuid);
		} else {
			sql = "SELECT p FROM Portfolio p";
			sql += " INNER JOIN p.groupRightInfo gri WITH gri.label='all'";
			sql += " INNER JOIN gri.groupInfo gi";
			sql += " INNER JOIN gi.groupUser gu";
			sql += " INNER JOIN gu.id.credential c WITH c.login='sys_public'";
			sql += " WHERE p.id = :portfolioUuid";
			q = entityManager.createQuery(sql);
			q.setParameter("portfolioUuid", UUID.fromString(portfolioUuid));
		}
		try {
			q.getSingleResult();
			val = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return val;
	}

	public boolean isPublic(String nodeUuid, String portfolioUuid) {
		return isPublic(UUID.fromString(nodeUuid), portfolioUuid);
	}

	public List<Node> getNodes(MimeType outMimeType, String portfolioUuid, int userId, int groupId, String semtag,
			String parentUuid, String filterId, String filterParameters, String sortId, Integer cutoff)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Node> getNodes(MimeType mimeType, String portfoliocode, String semtag, int userId, int groupId,
			String semtag_parent, String code_parent, Integer cutoff) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getNodesBySemanticTag(MimeType outMimeType, int userId, int groupId, String portfolioUuid,
			String semanticTag) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Node getNodeWithXSL(MimeType mimeType, String nodeUuid, String xslFile, String parameters, int userId,
			int groupId) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Node> getNodesParent(MimeType mimeType, String portfoliocode, String semtag, int userId, int groupId,
			String semtag_parent, String code_parent) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getNodeMetadataWad(MimeType mimeType, String nodeUuid, boolean b, int userId, int groupId,
			String label) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getResNode(String contextUuid, int userId, int groupId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Node> getNodeRights(String nodeUuid, int userId, int groupId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object putNode(MimeType inMimeType, String nodeUuid, String in, int userId, int groupId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object putNodeMetadata(MimeType mimeType, String nodeUuid, String xmlNode, int userId, int groupId)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object putNodeMetadataWad(MimeType mimeType, String nodeUuid, String xmlNode, int userId, int groupId)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object putNodeMetadataEpm(MimeType mimeType, String nodeUuid, String xmlNode, int userId, int groupId)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object putNodeNodeContext(MimeType mimeType, String nodeUuid, String xmlNode, int userId, int groupId)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object putNodeNodeResource(MimeType mimeType, String nodeUuid, String xmlNode, int userId, int groupId)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object postNode(MimeType inMimeType, String parentNodeUuid, String in, int userId, int groupId,
			boolean forcedUuid) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object postNodeFromModelBySemanticTag(MimeType mimeType, String nodeUuid, String semantictag, int userId,
			int groupId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object postImportNode(MimeType inMimeType, String destUuid, String tag, String code, String srcuuid,
			int userId, int groupId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object postCopyNode(MimeType inMimeType, String destUuid, String tag, String code, String srcuuid,
			int userId, int groupId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public int postMoveNodeUp(int userid, String uuid) {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean postChangeNodeParent(int userid, String uuid, String uuidParent) {
		// TODO Auto-generated method stub
		return false;
	}

	public Object deleteNode(String nodeUuid, int userId, int groupId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Node> getNodeBySemanticTag(MimeType mimeType, String portfolioUuid, String semantictag, int userId,
			int groupId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Check if same code allowed with nodes in different portfolio, and not root
	 * node
	 *
	 *
	 */
	public boolean isCodeExist(String code, UUID nodeuuid) {
		boolean response = false;
		String sql;
		TypedQuery<UUID> q = null;
		sql = "SELECT p.id FROM Node n";
		sql += " INNER JOIN n.portfolio p";
		sql += " WHERE n.asmType = :asmType";
		sql += " AND n.code = :code";
		if (nodeuuid != null) {
			sql += " AND n.id != :nodeuuid";
			sql += " AND p.id = (SELECT n.portfolio.id FROM Node n WHERE n.id = :nodeuuid)";
		}
		q = entityManager.createQuery(sql, UUID.class);
		q.setParameter("asmType", "asmRoot");
		q.setParameter("code", code);
		if (nodeuuid != null) {
			q.setParameter("nodeuuid", nodeuuid);
		}
		try {
			q.getSingleResult();
			response = true;
		} catch (NoResultException ex) {

		} catch (NonUniqueResultException ex) {
			response = true;
		}
		return response;
	}

	/**
	 * Check if same code allowed with nodes in different portfolio, and not root
	 * node
	 *
	 */
	public boolean isCodeExist(String nodeuuid, String code) {
		return isCodeExist(code, UUID.fromString(nodeuuid));
	}

}
