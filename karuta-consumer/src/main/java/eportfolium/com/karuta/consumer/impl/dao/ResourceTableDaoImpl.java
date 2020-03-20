package eportfolium.com.karuta.consumer.impl.dao;
// Generated 17 juin 2019 11:33:18 by Hibernate Tools 5.2.10.Final

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import eportfolium.com.karuta.consumer.contract.dao.NodeDao;
import eportfolium.com.karuta.consumer.contract.dao.ResourceTableDao;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.ResourceTable;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.util.JavaTimeUtil;

/**
 * Home object implementation for domain model class ResourceTable.
 * 
 * @see dao.ResourceTable
 * @author Hibernate Tools
 */
@Repository
public class ResourceTableDaoImpl extends AbstractDaoImpl<ResourceTable> implements ResourceTableDao {

	@Autowired
	private NodeDao nodeDao;

	private static final Log log = LogFactory.getLog(ResourceTableDaoImpl.class);

	public ResourceTableDaoImpl() {
		super();
		setCls(ResourceTable.class);
	}

	public ResourceTable getResource(String resUuid) {
		ResourceTable result = null;
		// On récupère d'abord les informations dans la table structures
		String sql = "FROM ResourceTable r";
		sql += " WHERE r.id = :resourceID ";
		TypedQuery<ResourceTable> q = em.createQuery(sql, ResourceTable.class);
		q.setParameter("resourceID", UUID.fromString(resUuid));
		try {
			result = q.getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
		}
		return result;
	}

	public ResourceTable getResourceByXsiType(UUID resUuid, String xsiType) {
		ResourceTable result = null;
		// On récupère d'abord les informations dans la table structures
		String sql = "SELECT r FROM ResourceTable r";
		sql += " WHERE r.xsiType LIKE :xsiType";
		sql += " AND r.id = :resourceID ";
		TypedQuery<ResourceTable> q = em.createQuery(sql, ResourceTable.class);
		q.setParameter("xsiType", xsiType);
		q.setParameter("resourceID", resUuid);
		try {
			result = q.getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
		}
		return result;
	}

	public ResourceTable getResourceByXsiType(String resUuid, String xsiType) {
		return getResourceByXsiType(UUID.fromString(resUuid), xsiType);
	}

	public UUID getResourceUuidByParentNodeUuid(String parentNodeUuid) throws DoesNotExistException {
		return getResourceByParentNodeUuid(parentNodeUuid).getId();
	}

	public ResourceTable getResourceByParentNodeUuid(String parentNodeUuid) throws DoesNotExistException {
		return getResourceByNodeParentUuid(UUID.fromString(parentNodeUuid));
	}

	public ResourceTable getResourceByNodeParentUuid(UUID parentNodeUuid) throws DoesNotExistException {
		ResourceTable res = null;
		// On récupère d'abord les informations dans la table structures
		String sql = "SELECT r FROM ResourceTable r";
		sql += " INNER JOIN r.node n WITH n.id = :parentNodeID";
		TypedQuery<ResourceTable> q = em.createQuery(sql, ResourceTable.class);
		q.setParameter("parentNodeID", parentNodeUuid);
		try {
			res = q.getSingleResult();
		} catch (NoResultException e) {
			throw new DoesNotExistException(ResourceTable.class, parentNodeUuid);
		}
		return res;
	}

	/**
	 * Replace was designed to ease the following case:
	 * 
	 * Check, if record with same PK exists If yes, delete the row and insert a new
	 * row for the record with the given one If no, insert a new record As per
	 * documentation REPLACE is equivalent to INSERT, apart from deleting any
	 * existing record having the PK is beeing deleted before.
	 */
	public int updateResource(String uuid, String xsiType, String content, Long userId) {
		int result = 0;
		ResourceTable rt = null;
		try {
			if (xsiType != null) {
				try {
					removeById(UUID.fromString(uuid));
				} catch (DoesNotExistException e) {
				}
				Date now = JavaTimeUtil.toJavaDate(LocalDateTime.now(JavaTimeUtil.date_default_timezone));
				rt = new ResourceTable(UUID.fromString(uuid), xsiType, content, new Credential(userId), userId, now);
				persist(rt);
			} else {
				rt = findById(UUID.fromString(uuid));
				rt.setContent(content);
				rt.setCredential(new Credential(userId));
				rt.setModifUserId(userId);
				merge(rt);
			}
		} catch (Exception e) {
			log.error("");
			result = 1;
		}
		return result;
	}

	public ResourceTable getResourceOfResourceByNodeUuid(String nodeUuid) {
		return getResourceOfResourceByNodeUuid(UUID.fromString(nodeUuid));
	}

	public ResourceTable getResourceOfResourceByNodeUuid(UUID nodeUuid) {
		ResourceTable result = null;
		String sql = " SELECT r FROM ResourceTable r";
		sql += " INNER JOIN r.resNode resNode WITH resNode.id = :nodeUuid";
		TypedQuery<ResourceTable> q = em.createQuery(sql, ResourceTable.class);
		q.setParameter("nodeUuid", nodeUuid);
		try {
			result = q.getSingleResult();
		} catch (NoResultException e) {
		}
		return result;
	}

	public ResourceTable getContextResourceByNodeUuid(String nodeUuid) {
		return getContextResourceByNodeUuid(UUID.fromString(nodeUuid));
	}

	public ResourceTable getContextResourceByNodeUuid(UUID nodeUuid) {
		ResourceTable result = null;
		String sql = " SELECT r FROM ResourceTable r";
		sql += " INNER JOIN r.contextNode contextNode WITH contextNode.id = :nodeUuid";
		TypedQuery<ResourceTable> q = em.createQuery(sql, ResourceTable.class);
		q.setParameter("nodeUuid", nodeUuid);
		try {
			result = q.getSingleResult();
		} catch (NoResultException e) {
		}
		return result;
	}

	public ResourceTable getResourceByNodeUuid(String nodeUuid) {
		return getResourceByNodeUuid(UUID.fromString(nodeUuid));
	}

	public ResourceTable getResourceByNodeUuid(UUID nodeUuid) {
		ResourceTable resourceTable = null;
		String sql = "SELECT r FROM ResourceTable r";
		sql += " INNER JOIN r.node n WITH n.id = :nodeUuid";
		TypedQuery<ResourceTable> q = em.createQuery(sql, ResourceTable.class);
		q.setParameter("nodeUuid", nodeUuid);
		try {
			resourceTable = q.getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
		}
		return resourceTable;
	}

	public String getResNodeContentByNodeUuid(String nodeUuid) {
		ResourceTable rt = getResourceByNodeUuid(nodeUuid);
		return rt != null ? rt.getContent() : "";
	}

	public List<ResourceTable> getResourcesByPortfolioUUID(String portfolioUuid) {
		// On récupère d'abord les informations dans la table structures
		String sql = "SELECT r FROM ResourceTable r";
		sql += " INNER JOIN r.node n WITH n.portfolio.id = :portfolioUuid";
		TypedQuery<ResourceTable> q = em.createQuery(sql, ResourceTable.class);
		q.setParameter("portfolioUuid", UUID.fromString(portfolioUuid));
		List<ResourceTable> results = q.getResultList();
		return results;
	}

	public List<ResourceTable> getResourcesOfResourceByPortfolioUUID(String portfolioUuid) {
		String sql = "SELECT r FROM ResourceTable r";
		sql += " INNER JOIN r.resNode n WITH n.portfolio.id = :portfolioUuid";
		TypedQuery<ResourceTable> q = em.createQuery(sql, ResourceTable.class);
		q.setParameter("portfolioUuid", UUID.fromString(portfolioUuid));
		return q.getResultList();
	}

	public List<ResourceTable> getContextResourcesByPortfolioUUID(String portfolioUuid) {
		String sql = "SELECT r FROM ResourceTable r";
		sql += " INNER JOIN r.contextNode n WITH n.portfolio.id = :portfolioUuid";
		TypedQuery<ResourceTable> q = em.createQuery(sql, ResourceTable.class);
		q.setParameter("portfolioUuid", UUID.fromString(portfolioUuid));
		return q.getResultList();
	}

	public int updateResource(String nodeUuid, String content, Long userId) {
		return updateResource(UUID.fromString(nodeUuid), content, userId);
	}

	public int updateResource(UUID nodeUuid, String content, Long userId) {
		int result = 0;
		String sql = " SELECT rt FROM ResourceTable rt";
		sql += " INNER JOIN rt.node n WITH n.id = :nodeUuid";
		TypedQuery<ResourceTable> q = em.createQuery(sql, ResourceTable.class);
		q.setParameter("nodeUuid", nodeUuid);
		try {
			ResourceTable rt = q.getSingleResult();
			rt.setContent(content);
			rt.setCredential(new Credential(userId));
			rt.setModifUserId(userId);
			merge(rt);
		} catch (Exception e) {
			result = 1;
		}
		return result;

	}

	public int updateResResource(String nodeUuid, String content, Long userId) {
		return updateResResource(UUID.fromString(nodeUuid), content, userId);
	}

	public int updateResResource(UUID nodeUuid, String content, Long userId) {
		int result = 0;
		try {
			ResourceTable rt = getResourceOfResourceByNodeUuid(nodeUuid);
			rt.setContent(content);
			rt.setCredential(new Credential(userId));
			rt.setModifUserId(userId);
			merge(rt);
		} catch (Exception e) {
			log.error("merge failed", e);
			result = 1;
		}
		return result;
	}

	public int updateContextResource(String nodeUuid, String content, Long userId) {
		return updateContextResource(UUID.fromString(nodeUuid), content, userId);
	}

	public int updateContextResource(UUID nodeUuid, String content, Long userId) {
		int result = 0;
		try {
			ResourceTable rt = getContextResourceByNodeUuid(nodeUuid);
			rt.setContent(content);
			rt.setCredential(new Credential(userId));
			rt.setModifUserId(userId);
			merge(rt);
		} catch (Exception e) {
			result = 1;
		}
		return result;
	}

	public int addResource(String uuid, String parentUuid, String xsiType, String content, String portfolioModelId,
			boolean sharedNodeRes, boolean sharedRes, Long userId) {

		int status = 0;
		final UUID uuidObj = UUID.fromString(uuid);
		final UUID parentUuidObj = UUID.fromString(parentUuid);

		ResourceTable rt = null;
		if (((xsiType.equals("nodeRes") && sharedNodeRes)
				|| (!xsiType.equals("context") && !xsiType.equals("nodeRes") && sharedRes))
				&& portfolioModelId != null) {
			// On ne fait rien
		} else {
			try {
				rt = findById(uuidObj);
			} catch (DoesNotExistException e) {
				rt = new ResourceTable();
				rt.setId(uuidObj);
			}
			rt.setXsiType(xsiType);
			rt.setContent(content);
			rt.setCredential(new Credential(userId));
			rt.setModifUserId(userId);
			rt = merge(rt);	// Updated version of object is returned
		}

		String sql = "SELECT n FROM Node n";
		sql += " WHERE n.id = :nodeUuid";
		final TypedQuery<Node> q = em.createQuery(sql, Node.class);
		q.setParameter("nodeUuid", parentUuidObj);

		try {
			final Node n = q.getSingleResult();

			// Ensuite on met à jour les id ressource au niveau du noeud parent
			if (xsiType.equals("nodeRes")) {
				n.setResResource(rt);
				if (sharedNodeRes && portfolioModelId != null) {
					n.setSharedNodeResUuid(uuidObj);
				} else {
					n.setSharedNodeResUuid(null);
				}
			} else if (xsiType.equals("context")) {
				n.setContextResource(rt);
			} else {
				n.setResource(rt);
				if (sharedRes && portfolioModelId != null) {
					n.setSharedResUuid(uuidObj);
				} else {
					n.setSharedResUuid(null);
				}
			}
			nodeDao.merge(n);
		} catch (Exception ex) {
			ex.printStackTrace();
			status = 1;
		}
		return status;
	}

	/*****************************************************************************************************************/

	@Override
	public ResultSet getMysqlResources(Connection con) throws SQLException {
		PreparedStatement st;
		String sql;
		try {
			// On récupère d'abord les informations dans la table structures
			sql = "SELECT bin2uuid(node_uuid) AS node_uuid, xsi_type, content, user_id, modif_user_id, modif_date FROM resource_table";
			st = con.prepareStatement(sql);
			return st.executeQuery();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
