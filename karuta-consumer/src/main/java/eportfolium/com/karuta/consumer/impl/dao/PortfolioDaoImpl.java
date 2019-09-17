package eportfolium.com.karuta.consumer.impl.dao;
// Generated 17 juin 2019 11:33:18 by Hibernate Tools 5.2.10.Final

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.activation.MimeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import eportfolium.com.karuta.consumer.contract.dao.CredentialDao;
import eportfolium.com.karuta.consumer.contract.dao.NodeDao;
import eportfolium.com.karuta.consumer.contract.dao.PortfolioDao;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.util.PhpUtil;

/**
 * Home object implementation for domain model class Portfolio.
 * 
 * @see dao.Portfolio
 * @author Hibernate Tools
 */
@Repository
public class PortfolioDaoImpl extends AbstractDaoImpl<Portfolio> implements PortfolioDao {

	private static final Log log = LogFactory.getLog(PortfolioDaoImpl.class);

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private CredentialDao credentialDao;

	@Autowired
	private NodeDao nodeDao;

	public PortfolioDaoImpl() {
		super();
		setCls(Portfolio.class);
	}

	public Portfolio getPortfolio(UUID portfolioUuid) {
		Portfolio result = null;
		try {
			String sql = "SELECT p FROM Portfolio p";
			sql += " WHERE p.id = :portfolioID ";
			TypedQuery<Portfolio> q = em.createQuery(sql, Portfolio.class);
			q.setParameter("portfolioID", portfolioUuid);
			result = q.getSingleResult();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	public Portfolio getPortfolio(String portfolioUuid) {
		return getPortfolio(UUID.fromString(portfolioUuid));
	}

	public Node getPortfolioRootNode(UUID portfolioUuid) {
		Node root_node = null;
		String hql = "SELECT rn FROM Portfolio p";
		hql += " LEFT JOIN FETCH p.rootNode as rn";
		hql += " WHERE p.id = :portfolioID ";
		TypedQuery<Node> q = em.createQuery(hql, Node.class);
		q.setParameter("portfolioID", portfolioUuid);
		try {
			root_node = q.getSingleResult();
		} catch (NoResultException e) {
			log.error("getPortfolioRootNode failed", e);
		}
		return root_node;
	}

	public Node getPortfolioRootNode(String portfolioUuid) {
		return getPortfolioRootNode(UUID.fromString(portfolioUuid));
	}

	public UUID getPortfolioUuidByNodeUuid(String nodeUuid) {
		return getPortfolioUuidByNodeUuid(UUID.fromString(nodeUuid));
	}

	public UUID getPortfolioUuidByNodeUuid(UUID nodeUuid) {
		UUID res = null;

		String sql = "SELECT p.id FROM Node n";
		sql += " INNER JOIN n.portfolio p";
		sql += " WHERE n.id = :nodeUuid";
		TypedQuery<UUID> q = em.createQuery(sql, UUID.class);
		q.setParameter("nodeUuid", nodeUuid);
		try {
			res = q.getSingleResult();
		} catch (NoResultException e) {
		}
		return res;
	}

	public Portfolio getPortfolioZip(MimeType mimeType, String portfolioUuid, int userId, int groupId, String label,
			Boolean resource, Boolean files) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getPortfolioShared(Long userId) {
		String sql = "SELECT new map(gi.id AS gid, p.id AS portfolio)";
		sql += " FROM GroupUser gu";
		sql += " INNER JOIN gu.id.groupInfo gi";
		sql += " INNER JOIN gi.groupRightInfo gri";
		sql += " INNER JOIN gri.portfolio p";
		sql += " AND gu.id.credential.id = :userId";
		Query q = em.createQuery(sql);
		List<Map<String, Object>> result = q.getResultList();
		return result;
	}

	public List<Portfolio> getPortfolios(Long userId, Long substId, Boolean portfolioActive) {
		if (PhpUtil.empty(userId) && PhpUtil.empty(substId)) {
			return Arrays.asList();
		}
		TypedQuery<Portfolio> q;
		String sql = "";

		// Ordering by code. A bit hackish but it work as intended
		// Si on est admin, on recupere la liste complete
		if (credentialDao.isAdmin(String.valueOf(userId))) {
			sql = "SELECT p FROM portfolio p";
			sql += "INNER JOIN p.rootNode n";
			sql += "INNER JOIN n.resResourceTable r";
			if (portfolioActive)
				sql += "  AND p.active = 1 ";
			else
				sql += "  AND p.active = 0 ";

			sql += " ORDER BY r.content";

			q = em.createQuery(sql, Portfolio.class);
			return q.getResultList();
		}

		if (credentialDao.isAdmin(String.valueOf(substId))) {
			// If root wants to debug user UI
			substId = 0L;
		}

		// On récupère d'abord les informations dans la table structures
		// Récupération des portfolios :

		// Étape 1 : ceux qui appartiennent à l'utilisateur
		sql = " SELECT p FROM portfolio p";
		sql += " INNER JOIN FETCH p.rootNnode n";
		sql += " INNER JOIN FETCH n.resResourceTable r";
		sql += " WHERE p.modifUserId = :modifUserId";
		if (portfolioActive)
			sql += "AND p.active = 1 ";
		else
			sql += "AND p.active = 0 ";

		q = em.createQuery(sql, Portfolio.class);
		q.setParameter("modifUserId", userId);
		Set<Portfolio> portfolios = new HashSet<Portfolio>(q.getResultList());

		// Étape 2 : Ceux qu'il peut modifier, sur lesquels l'utilisateur a reçu des
		// droits spécifiques.
		sql = " SELECT p FROM portfolio p";
		sql += " INNER JOIN FETCH p.rootNnode n";
		sql += " INNER JOIN FETCH n.resResourceTable r";
		sql += " INNER JOIN p.credential cr";
		sql += " INNER JOIN cr.groups gu";
//			sql = " SELECT gu FROM GroupUser gu";
//			sql+= " INNER JOIN gu.groupInfo gi;
//		    sql+= " INNER JOIN gi.groupRightInfo gri";
//			sql+= " INNER JOIN FETCH gri.portfolio p";
//			sql += " INNER JOIN FETCH p.rootNnode n";
//			sql += " INNER JOIN FETCH n.resResourceTable r";
//			sql += " WHERE gu.id.credential.id = : userId;

		// Étape 2 : portfolios auxquels l'utilisateur standard et celui de substitution
		// peuvent accéder.
		sql += " WHERE (gu.id.credential.id = :userId OR gu.id.credential.id = :substUserId)";

		// check active from substitute too
		if (portfolioActive)
			sql += " AND p.active = 1 ";
		else
			sql += " AND p.active = 0 ";

		/// Closing top level query and sorting
		sql += " GROUP BY p.id ORDER BY content";

		q = em.createQuery(sql, Portfolio.class);
		q.setParameter("userId", userId);

		if (PhpUtil.empty(substId)) {
			q.setParameter("substUserId", userId);
		} else {
			q.setParameter("substUserId", substId);
		}

		Set<Portfolio> step2 = new HashSet<Portfolio>(q.getResultList());
		portfolios.addAll(step2);
		return new ArrayList<Portfolio>(portfolios);
	}

	public UUID getPortfolioUuidByPortfolioCode(String portfolioCode) {
		Portfolio p = getPortfolioByPortfolioCode(portfolioCode);
		return p != null ? p.getId() : null;
	}

	public Portfolio getPortfolioByPortfolioCode(String portfolioCode) {
		Portfolio res = null;

		String sql = "SELECT p FROM Node n";
		sql += " INNER JOIN n.portfolio p WITH p.active = 1";
		sql += " WHERE n.asmType = 'asmRoot'";
		sql += " AND n.code = :code ";

		TypedQuery<Portfolio> q = em.createQuery(sql, Portfolio.class);
		q.setParameter("portfolioCode", portfolioCode);
		try {
			res = q.getSingleResult();
		} catch (NoResultException ex) {
			ex.printStackTrace();
		}
		return res;
	}

	public Long getPortfolioUserId(String portfolioUuid) {
		String sql = "SELECT cr.id FROM Portfolio p";
		sql += " INNER JOIN p.credential cr";
		sql += " WHERE p.id = :portfolioUuid";
		TypedQuery<Long> q = em.createQuery(sql, Long.class);
		q.setParameter("portfolioUuid", portfolioUuid);
		Long res = null;
		try {
			res = q.getSingleResult();
		} catch (NoResultException e) {
		}
		return res;
	}

	public int updatePortfolioModelId(UUID portfolioUuid, UUID portfolioModelId) {
		int result = 0;
		String sql = "SELECT p FROM Portfolio p";
		sql += " WHERE p.id = :portfolioUuid";
		TypedQuery<Portfolio> q = em.createQuery(sql, Portfolio.class);
		q.setParameter("portfolioUuid", portfolioUuid);
		try {
			Portfolio p = q.getSingleResult();
			p.setModelId(portfolioModelId);
			merge(p);
		} catch (Exception ex) {
			result = 1;
			ex.printStackTrace();
		}
		return result;
	}

	public int updatePortfolioModelId(String portfolioUuid, String portfolioModelId) {
		return updatePortfolioModelId(UUID.fromString(portfolioUuid), UUID.fromString(portfolioModelId));
	}

	public UUID getPortfolioModelUuid(String portfolioUuid) {
		return getPortfolioModelUuid(UUID.fromString(portfolioUuid));
	}

	public UUID getPortfolioModelUuid(UUID portfolioUuid) {
		UUID modelID = null;
		String sql = "SELECT p.modelId FROM Portfolio p";
		sql += " WHERE p.id = :portfolioUuid";
		TypedQuery<UUID> q = em.createQuery(sql, UUID.class);
		q.setParameter("portfolioUuid", portfolioUuid);
		try {
			modelID = q.getSingleResult();
		} catch (Exception e) {
		}
		return modelID;
	}

	public Object putPortfolio(MimeType inMimeType, MimeType outMimeType, String in, String portfolioUuid, int userId,
			Boolean portfolioActive, int groupId, String modelId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object postPortfolio(MimeType inMimeType, MimeType outMimeType, String in, int userId, int groupId,
			String modelId, int substid, boolean parseRights, String projectName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object postInstanciatePortfolio(MimeType inMimeType, String portfolioUuid, String srcCode, String newCode,
			int userId, int groupId, boolean copyshared, String portfGroupName, boolean setOwner) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object postCopyPortfolio(MimeType inMimeType, String portfolioUuid, String srcCode, String newCode,
			int userId, boolean setOwner) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object postPortfolioParserights(String portfolioUuid, int userId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getModels(MimeType mimeType, int userId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getModel(MimeType mimeType, Integer modelId, int userId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object postModels(MimeType mimeType, String xmlModel, int userId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Portfolio getPortfolio(MimeType outMimeType, String portfolioUuid, int userId, int groupId, String label,
			String resource, String files, int substid, Integer cutoff) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Long getOwner(String portfolioId) {
		return getOwner(UUID.fromString(portfolioId));
	}

	public Long getOwner(UUID portfolioId) {
		Long result = null;
		String query = "SELECT p.modifUserId FROM Portfolio p";
		query += " WHERE p.id = :id";
		TypedQuery<Long> q = em.createQuery(query, Long.class);
		q.setParameter("id", portfolioId);
		try {
			result = q.getSingleResult();
		} catch (NoResultException e) {
		}
		return result;
	}

	public boolean isOwner(Long userId, String portfolioUuid) {
		return isOwner(userId, UUID.fromString(portfolioUuid));
	}

	public boolean isOwner(Long userId, UUID portfolioUuid) {
		boolean result = false;
		if (!PhpUtil.empty(userId)) {
			String sql = "SELECT n.modifUserId FROM Node n";
			sql += " INNER JOIN n.portfolio p";
			sql += " WHERE n.modifUserId = :userId";
			sql += " AND p.id = :portfolioUuid";
			Query query = em.createQuery(sql);
			query.setParameter("userId", userId);
			query.setParameter("portfolioUuid", portfolioUuid);
			try {
				query.getSingleResult();
				result = true;
			} catch (NoResultException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public boolean putPortfolioOwner(String portfolioUuid, Long ownerId) {
		return putPortfolioOwner(UUID.fromString(portfolioUuid), ownerId);
	}

	/**
	 * Change portfolio owner
	 * 
	 * @param portfolioUuid
	 * @param ownerId
	 * @return
	 * @throws Exception
	 */
	public boolean putPortfolioOwner(UUID portfolioUuid, Long ownerId) {
		boolean retval = false;

		try {
			String sql = "SELECT p FROM Portfolio p";
			sql += " INNER JOIN FETCH p.rootNode";
			sql += " WHERE p.id = :portfolioUuid";
			TypedQuery<Portfolio> query2 = em.createQuery(sql, Portfolio.class);
			query2.setParameter("portfolioUuid", portfolioUuid);
			Portfolio p = query2.getSingleResult();
			p.setModifUserId(ownerId);
			Node n = p.getRootNode();
			n.setModifUserId(ownerId);
			nodeDao.merge(n);
			merge(p);
			retval = true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return retval;
	}

	public Portfolio putPortfolioConfiguration(UUID portfolioUuid, Boolean portfolioActive) {
		Portfolio result = null;
		try {
			Portfolio p = findById(portfolioUuid);
			p.setActive(BooleanUtils.toInteger(portfolioActive));
			result = merge(p);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	public Portfolio putPortfolioConfiguration(String portfolioUuid, Boolean portfolioActive) {
		return putPortfolioConfiguration(UUID.fromString(portfolioUuid), portfolioActive);
	}

	@Override
	public Portfolio addPortfolio(String rootNodeUuid, UUID modelId, Long userId, Portfolio porfolio)
			throws BusinessException {
		if (porfolio.getRootNode() != null) {
			throw new IllegalArgumentException();
		}
		if (porfolio.getCredential() != null) {
			throw new IllegalArgumentException();
		}

		porfolio.setModelId(modelId);
		Node rootNode = nodeDao.findById(UUID.fromString(rootNodeUuid));
		Credential credential = credentialDao.findById(userId);

		return addPortfolio(rootNode, credential, porfolio);
	}

	private Portfolio addPortfolio(Node rootNode, Credential credential, Portfolio portfolio) throws BusinessException {
		if (portfolio.getRootNode() != null) {
			throw new IllegalArgumentException();
		}
		if (portfolio.getCredential() != null) {
			throw new IllegalArgumentException();
		}

		Node n = nodeDao.merge(rootNode);
		Credential cr = credentialDao.merge(credential);

		// If id is different it means the person did not exist so merge has created a
		// new one.
		if (!n.getId().equals(rootNode.getId())) {
			throw new DoesNotExistException(Node.class, rootNode.getId());
		}

		// If id is different it means the person did not exist so merge has created a
		// new one.
		if (!cr.getId().equals(credential.getId())) {
			throw new DoesNotExistException(Credential.class, credential.getId());
		}

		n.addPortfolio(portfolio);
		cr.addPortfolio(portfolio);

		persist(portfolio);
		return portfolio;
	}

	public void changePortfolio(Portfolio portfolio) throws BusinessException {
		Portfolio p = merge(portfolio);

		// If id is different it means the person did not exist so merge has created a
		// new one.
		if (!p.getId().equals(portfolio.getId())) {
			throw new DoesNotExistException(Portfolio.class, portfolio.getId());
		}

	}

}
