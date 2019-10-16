package eportfolium.com.karuta.consumer.impl.dao;
// Generated 17 juin 2019 11:33:18 by Hibernate Tools 5.2.10.Final

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import eportfolium.com.karuta.consumer.contract.dao.GroupRightInfoDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupRightsDao;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.GroupRightsId;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.util.PhpUtil;

/**
 * Home object implementation for domain model class GroupRights.
 * 
 * @see dao.GroupRights
 * @author Hibernate Tools
 */
@Repository
public class GroupRightsDaoImpl extends AbstractDaoImpl<GroupRights> implements GroupRightsDao {

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private GroupRightInfoDao groupRightInfoDao;

	public GroupRightsDaoImpl() {
		super();
		setCls(GroupRights.class);
	}

	/**
	 * Récupère la liste des droits sur le noeud donné en paramètre.
	 */
	public List<GroupRights> getRightsById(String uuid) {
		return getRightsById(UUID.fromString(uuid));
	}

	/**
	 * Récupère la liste des droits sur le noeud donné en paramètre.
	 */
	public List<GroupRights> getRightsById(UUID uuid) {
		String sql = "SELECT gr FROM GroupRights gr";
		sql += " WHERE gr.id.id = :uuid";
		TypedQuery<GroupRights> q = em.createQuery(sql, GroupRights.class);
		q.setParameter("uuid", uuid);
		return q.getResultList();
	}

	/**
	 * Regarde si l'utilisateur à un droit sur le noeud en fonction du groupe donné
	 * en parametre.
	 * 
	 * @param uuid
	 * @param userId
	 * @param groupId
	 * @return
	 */
	public GroupRights getRightsByUserAndGroup(String uuid, Long userId, Long groupId) {
		return getRightsByUserAndGroup(UUID.fromString(uuid), userId, groupId);
	}

	/**
	 * Regarde si l'utilisateur à un droit sur le noeud en fonction du groupe donné
	 * en parametre.
	 * 
	 * @param nodeUuid
	 * @param userId
	 * @param groupId
	 * @return
	 */
	public GroupRights getRightsByUserAndGroup(UUID uuid, Long userId, Long groupId) {
		GroupRights res = null;
		String sql = "SELECT gr FROM GroupRights gr";
		sql += " INNER JOIN FETCH gr.id.groupRightInfo gri";
		sql += " INNER JOIN FETCH gri.groupInfo gi";
		sql += " INNER JOIN gi.groupUser gu WITH gu.id.credential.id = :userId AND gu.id.groupInfo.id = :groupId";
		sql += " WHERE gr.id.id = :uuid";

		TypedQuery<GroupRights> q = em.createQuery(sql, GroupRights.class);
		q.setParameter("userId", userId);
		q.setParameter("uuid", uuid);
		q.setParameter("groupId", groupId);
		try {
			res = q.getSingleResult();
		} catch (NoResultException e) {
		}
		return res;
	}

	@Override
	public List<GroupRights> getRightsByIdAndGroup(UUID uuid, Long groupId) {
		String sql = "SELECT gr FROM GroupRights gr";
		sql += " INNER JOIN FETCH gr.id.groupRightInfo gri";
		sql += " INNER JOIN FETCH gri.groupInfo gi WITH gi.id = :groupId";
		sql += " WHERE gr.id.id = :uuid";
		TypedQuery<GroupRights> q = em.createQuery(sql, GroupRights.class);
		q.setParameter("uuid", uuid);
		q.setParameter("groupId", groupId);
		return q.getResultList();
	}

	/**
	 * On recupere d'abord les informations dans la table structure
	 */
	public List<GroupRights> getRightsByGroupId(Long groupId) {
		String sql = "SELECT gr FROM GroupRights gr";
		sql += " INNER JOIN gr.groupRightInfo gri";
		sql += " INNER JOIN gri.groupInfo gi";
		sql += " WHERE gi.id = :groupId";
		TypedQuery<GroupRights> q = em.createQuery(sql, GroupRights.class);
		q.setParameter("groupId", groupId);
		return q.getResultList();
	}

	/**
	 * userId doit être celui de publique, pire cas c'est un autre utilisateur mais
	 * si cette personne n'as pas de droits, il n'y aura rien en retour. <br>
	 * 
	 * A partir du moment ou c'est public, peu importe le groupe d'appartenance le
	 * noeud est accessible
	 *
	 */

	public GroupRights getPublicRightsByUserId(UUID uuid, Long userId) {
		GroupRights gr = null;

		String sql = "SELECT gr FROM GroupRights gr";
		sql += " INNER JOIN gr.id.groupRightInfo gri WITH gri.label='all'";
		sql += " INNER JOIN gri.groupInfo gi";
		sql += " INNER JOIN gi.groupUser gu WITH gu.id.credential.id = :userId";
		sql += " WHERE gr.id.id = :uuid";

		TypedQuery<GroupRights> q = em.createQuery(sql, GroupRights.class);
		q.setParameter("userId", userId);
		q.setParameter("uuid", uuid);

		try {
			gr = q.getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();

		}
		return gr;
	}

	/**
	 * userId doit être celui de publique, pire cas c'est un autre utilisateur mais
	 * si cette personne n'as pas de droits, il n'y aura rien en retour. <br>
	 * 
	 * A partir du moment ou c'est public, peu importe le groupe d'appartenance le
	 * noeud est accessible
	 *
	 */
	public GroupRights getPublicRightsByUserId(String uuid, Long userId) {
		return getPublicRightsByUserId(UUID.fromString(uuid), userId);
	}

	/**
	 * Les droits que l'on a du groupe "all" <br>
	 * NOTE: Pas de vérification si la personne est dans le groupe 'all'.
	 * 
	 */
	public GroupRights getPublicRightsByGroupId(UUID uuid, Long groupId) {
		GroupRights res = null;
		String sql = "SELECT gr FROM GroupRights gr";
		sql += " WHERE gr.id.id = :uuid";
		sql += " AND id.groupRightInfo.id = (";
		sql += " SELECT gri2.id FROM GroupInfo gi, GroupRightInfo gri2";
		sql += " INNER JOIN gi.groupRightInfo gri1";
		sql += " WHERE gri1.portfolio.id = gri2.portfolio.id";
		sql += " AND gi.id = :groupId";
		sql += " AND gri2.label = :label )";
		TypedQuery<GroupRights> query = em.createQuery(sql, GroupRights.class);
		query.setParameter("uuid", uuid);
		query.setParameter("groupId", groupId);
		query.setParameter("label", "all");
		try {
			res = query.getSingleResult();
		} catch (NoResultException e) {
		}
		return res;
	}

	/**
	 * Les droits que l'on a du groupe "all" <br>
	 * NOTE: Pas de vérification si la personne est dans le groupe 'all'.
	 * 
	 */
	public GroupRights getPublicRightsByGroupId(String uuid, Long groupId) {
		return getPublicRightsByGroupId(UUID.fromString(uuid), groupId);
	}

	/**
	 * Regarde si l'utilisateur à un droit sur ce noeud dans l'un des groupes du
	 * portfolio. <br>
	 * 
	 * Pas de sélection de groupe donc le noeud pourrait etre référencé dans
	 * plusieurs groupes. On retourne le premier de la liste.
	 * 
	 * @param uuid
	 * @param userId
	 * @return
	 */
	public GroupRights getRightsByIdAndUser(UUID uuid, Long userId) {
		GroupRights res = null;
		String sql = "SELECT gr FROM GroupRights gr";
		sql += " INNER JOIN FETCH gr.id.groupRightInfo gri";
		sql += " INNER JOIN FETCH gri.groupInfo gi";
		sql += " INNER JOIN gi.groupUser gu WITH gu.id.credential.id = :userId";
		sql += " WHERE gr.id.id = :uuid";

		TypedQuery<GroupRights> q = em.createQuery(sql, GroupRights.class);
		q.setParameter("userId", userId);
		q.setParameter("uuid", uuid);
		List<GroupRights> resList = q.getResultList();
		if (CollectionUtils.isNotEmpty(resList)) {
			res = resList.get(0);
		}
		return res;
	}

	/**
	 * Role et uuid
	 * 
	 * @param uuid
	 * @param label
	 * @return
	 */
	public GroupRights getRightsByIdAndLabel(String uuid, String label) {
		return getRightsByIdAndLabel(UUID.fromString(uuid), label);
	}

	/**
	 * Role et uuid
	 * 
	 * @param uuid
	 * @param label
	 * @return
	 */
	public GroupRights getRightsByIdAndLabel(UUID uuid, String label) {

		GroupRights res = null;
		String sql = "SELECT gr FROM GroupRights gr";
		sql += " INNER JOIN FETCH gr.id.groupRightInfo gri WITH gri.label = :label";
		sql += " WHERE gr.id.id = :uuid";
		TypedQuery<GroupRights> q = em.createQuery(sql, GroupRights.class);
		q.setParameter("uuid", uuid);
		q.setParameter("label", label);
		try {
			res = q.getSingleResult();
		} catch (NoResultException e) {
			// TODO: handle exception
		}
		return res;
	}

	/**
	 * Regarde si l'utilisateur à un droit sur ce noeud dans l'un des groupes du
	 * portfolio. Pas de sélection de groupe donc le noeud pourrait etre référencé
	 * dans plusieurs groupes. On retourne le premier de la liste.
	 * 
	 * @param uuid
	 * @param userId
	 * @return
	 */
	public GroupRights getRightsByIdAndUser(String uuid, Long userId) {
		return getRightsByIdAndUser(UUID.fromString(uuid), userId);
	}

	/**
	 * Regarde si l'utilisateur à un droit sur le noeud en fonction du groupe donné
	 * en parametre.
	 * 
	 * @param uuid
	 * @param userId
	 * @param groupId
	 * @return
	 */
	public GroupRights getRightsByGrid(String uuid, Long grid) {
		return getRightsByGrid(UUID.fromString(uuid), grid);
	}

	/**
	 * Regarde si l'utilisateur à un droit sur le noeud en fonction du groupe donné
	 * en parametre.
	 * 
	 * @param uuid
	 * @param userId
	 * @param groupId
	 * @return
	 */
	public GroupRights getRightsByGrid(UUID uuid, Long grid) {
		GroupRights res = null;
		String sql = "SELECT gr FROM GroupRights gr";
		sql += " INNER JOIN FETCH gr.id.groupRightInfo gri WITH gri.id = :grid";
		sql += " WHERE gr.id.id = :uuid";

		TypedQuery<GroupRights> q = em.createQuery(sql, GroupRights.class);
		q.setParameter("grid", grid);
		q.setParameter("uuid", uuid);
		try {
			res = q.getSingleResult();
		} catch (NoResultException e) {
		}
		return res;
	}

	public List<GroupRights> getRightsByPortfolio(String uuid, String portfolioId) {
		return getRightsByPortfolio(UUID.fromString(uuid), UUID.fromString(portfolioId));
	}

	public List<GroupRights> getRightsByPortfolio(UUID uuid, UUID portfolioUuid) {
		List<GroupRightInfo> pa_implode = groupRightInfoDao.getByPortfolioID(portfolioUuid);

		String sql = "SELECT gr FROM GroupRights gr";
		sql += " WHERE gr.id.id = :uuid";
		sql += " AND gr.id.groupRightInfo.id IN (" + PhpUtil.implode(",", pa_implode) + ")";

		TypedQuery<GroupRights> q = em.createQuery(sql, GroupRights.class);
		q.setParameter("uuid", uuid);
		return q.getResultList();
	}

	/**
	 * Regarde si l'utilisateur à droits spécifiques sur le noeud.
	 * 
	 * @param uuid
	 * @param userId
	 * @return
	 */
	public GroupRights getSpecificRightsForUser(String uuid, Long userId) {
		return getSpecificRightsForUser(UUID.fromString(uuid), userId);

	}

	/**
	 * Regarde si l'utilisateur à droits spécifiques sur le noeud.
	 * 
	 * @param nodeUuid
	 * @param userId
	 * @return
	 */
	public GroupRights getSpecificRightsForUser(UUID nodeUuid, Long userId) {
		GroupRights res = null;
		String sql = "SELECT gr FROM GroupRights gr";
		sql += " WHERE gr.id.id = :uuid";
		sql += " AND id.groupRightInfo.id = (";
		sql += " SELECT gri.id FROM Credential c, GroupRightInfo gri, Node n";
		sql += " WHERE c.login = gri.label";
		sql += " AND c.id = :userId";
		sql += " AND gri.portfolio.id = n.portfolio.id";
		sql += " AND n.id = :uuid";
		sql += ")";
		TypedQuery<GroupRights> q = em.createQuery(sql, GroupRights.class);
		q.setParameter("uuid", nodeUuid);
		q.setParameter("userId", userId);
		try {
			res = q.getSingleResult();
		} catch (NoResultException e) {
		}
		return res;
	}

	/**
	 * Update rights
	 * 
	 * @param nodeUuid
	 * @param labels
	 * @param macroName
	 */
	public void updateNodeRights(String nodeUuid, List<String> labels, String macroName) {
		updateNodeRights(UUID.fromString(nodeUuid), labels, macroName);
	}

	/**
	 * Update rights
	 * 
	 * @param nodeUuid
	 * @param labels
	 * @param macroName
	 */
	public void updateNodeRights(UUID nodeUuid, List<String> labels, String macroName) {
		List<Long> griList = groupRightInfoDao.getByNodeAndLabel(nodeUuid, labels);
		String sql = "SELECT gr FROM GroupRights gr";
		sql += " WHERE gr.id.id = :uuid";
		sql += " AND gr.id.groupRightInfo.id IN (" + PhpUtil.implode(",", griList) + ")";
		TypedQuery<GroupRights> q = em.createQuery(sql, GroupRights.class);
		q.setParameter("uuid", nodeUuid);
		List<GroupRights> grList = q.getResultList();
		for (GroupRights gr : grList) {
			if ("hide".equals(macroName)) {
				gr.setRead(false);
			} else if ("show".equals(macroName)) {
				gr.setRead(true);
			}
			merge(gr);
		}
	}

	/**
	 * Remplace les droits sur les noeuds.
	 * 
	 * @param nodeUuid
	 * @param labels
	 */
	public void updateNodeRights(UUID nodeUuid, List<String> labels) {
		List<Long> griList = groupRightInfoDao.getByNodeAndLabel(nodeUuid, labels);
		String sql = "SELECT gr FROM GroupRights gr";
		sql += " WHERE gr.id.id = :uuid";
		sql += " AND gr.id.groupRightInfo.id IN (" + PhpUtil.implode(",", griList) + ")";
		TypedQuery<GroupRights> q = em.createQuery(sql, GroupRights.class);
		q.setParameter("uuid", nodeUuid);
		List<GroupRights> grList = q.getResultList();
		for (GroupRights gr : grList) {
			gr.setRead(true);
			merge(gr);
		}

	}

	/**
	 * Remplace les droits sur les noeuds.
	 * 
	 */
	public void updateNodeRights(String nodeUuid, List<String> labels) {
		updateNodeRights(UUID.fromString(nodeUuid), labels);
	}

	/**
	 * Insert/replace existing editing related rights
	 */
	public boolean updateNodesRights(List<Node> nodes, Long grid) {
		boolean hasChanges = CollectionUtils.isNotEmpty(nodes);
		GroupRights gr = null;
		for (Node node : nodes) {
			gr = getRightsByGrid(node.getId(), grid);
			if (gr != null) {
				gr.setWrite(false);
				gr.setDelete(false);
				gr.setAdd(false);
				gr.setSubmit(false);
				gr.setTypesId(null);
				gr.setRulesId(null);
				merge(gr);
			} else {
				gr = new GroupRights();
				gr.setId(new GroupRightsId(new GroupRightInfo(grid), node.getId()));
				persist(gr);
			}
		}
		return hasChanges;
	}

	/**
	 * Insert/replace existing editing related rights <br>
	 * NB : we don't limit to user's own group right
	 * 
	 * @param nodes
	 * @param grid
	 */
	public boolean updateAllNodesRights(List<Node> nodes, Long grid) {
		boolean hasChanges = CollectionUtils.isNotEmpty(nodes);
		GroupRights gr = null;
		for (Node node : nodes) {
			List<GroupRights> grList = getRightsById(node.getId());
			if (CollectionUtils.isNotEmpty(grList)) {
				for (GroupRights tmpGr : grList) {
					tmpGr.setWrite(false);
					tmpGr.setDelete(false);
					tmpGr.setAdd(false);
					tmpGr.setSubmit(false);
					tmpGr.setTypesId(null);
					tmpGr.setRulesId(null);
					merge(tmpGr);
				}
			} else {
				gr = new GroupRights();
				gr.setId(new GroupRightsId(new GroupRightInfo(grid), node.getId()));
				persist(gr);
			}
		}
		return hasChanges;
	}

	public Long getUserIdFromNode(String uuid) {
		Long userId = null;
		String sql = "SELECT cr.id FROM GroupRights gr, GroupInfo gi, GroupUser gu";
		sql += " INNER JOIN gu.id.credential cr";
		sql += " INNER JOIN gr.id.groupRightInfo gri";
		sql += " INNER JOIN gi.groupRightInfo gri2";
		sql += " WHERE gr.id.id = :uuid";
		sql += " AND gri.id = gri2.id";
		sql += " AND gi.label LIKE gri.label";
		sql += " AND gu.id.groupInfo.id = gi.id";

		TypedQuery<Long> q = em.createQuery(sql, Long.class);
		q.setParameter("uuid", UUID.fromString(uuid));
		try {
			userId = q.getSingleResult();
		} catch (NoResultException e) {
		}
		return userId;

	}

	@Override
	public void removeById(UUID groupRightsId) throws Exception {
		List<GroupRights> grList = getRightsById(groupRightsId);
		for (GroupRights gr : grList) {
			removeById(gr.getId().getId());
		}

	}

	@Override
	public List<GroupRights> getByPortfolioAndGridList(UUID portfolioUuid, Long grid1, Long grid2, Long grid3) {
		String sql = "SELECT gr FROM GroupRights gr";
		sql += " INNER JOIN FETCH gr.id.groupRightInfo gri";
		sql += " WHERE gri.portfolio.id = :portfolioId";
		sql += " AND (gri.id = :grid1";
		sql += " OR gri.id = :grid2";
		sql += " OR gri.id = :grid3)";

		TypedQuery<GroupRights> q = em.createQuery(sql, GroupRights.class);
		q.setParameter("portfolioId", portfolioUuid);
		q.setParameter("grid1", grid1);
		q.setParameter("grid2", grid2);
		q.setParameter("grid3", grid3);
		return q.getResultList();

	}

	/**
	 * Récupère les droits donnés par le portfolio à 'tout le monde'. et les droits
	 * donnés spécifiquement à un utilisateur
	 * 
	 * @param portfolioUuid
	 * @param userLogin
	 * @param groupId
	 * @return
	 */
	@Override
	public List<GroupRights> getPortfolioAndUserRights(UUID portfolioUuid, String userLogin, Long groupId) {
		String sql = " SELECT gr FROM GroupRights gr";
		sql += " INNER JOIN FETCH gr.id.groupRightInfo gri";
		sql += " INNER JOIN FETCH gri.groupInfo gi";
		sql += " WHERE gri.portfolio.id = :portId";
		sql += " AND (gi.label = 'all' OR gi.groupRightInfo.id = :groupId OR gi.label = :userLogin)";
		TypedQuery<GroupRights> q = em.createQuery(sql, GroupRights.class);
		q.setParameter("portId", portfolioUuid);
		q.setParameter("groupId", groupId);
		q.setParameter("userLogin", userLogin);
		return q.getResultList();
	}

}
