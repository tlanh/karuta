package eportfolium.com.karuta.consumer.impl.dao;
// Generated 17 juin 2019 11:33:18 by Hibernate Tools 5.2.10.Final

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.activation.MimeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import eportfolium.com.karuta.consumer.contract.dao.GroupInfoDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupRightInfoDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupRightsDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupUserDao;
import eportfolium.com.karuta.model.bean.GroupInfo;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.GroupRightsId;
import eportfolium.com.karuta.model.bean.GroupUser;
import eportfolium.com.karuta.model.bean.Portfolio;
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
	private GroupUserDao groupUserDao;

	@Autowired
	private GroupRightInfoDao groupRightInfoDao;

	@Autowired
	private GroupInfoDao groupInfoDao;

	public GroupRightsDaoImpl() {
		super();
		setCls(GroupRights.class);
	}

	public List<GroupRights> getGroupRights(Long groupId) throws Exception {
		// On recupere d'abord les informations dans la table structures
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
	public GroupRights getPublicRightsByUserId(Long userId, UUID nodeUUID) {
		String sql;
		GroupRights gr = null;

		sql = "SELECT gr FROM GroupRights gr";
		sql += " INNER JOIN gr.id.groupRightInfo gri WITH gri.label='all'";
		sql += " INNER JOIN gri.groupInfo gi";
		sql += " INNER JOIN gi.groupUser gu WITH gu.id.credential.id = :userID";
		sql += " WHERE gr.id.id = :nodeUUID";

		TypedQuery<GroupRights> q = em.createQuery(sql, GroupRights.class);
		q.setParameter("userID", userId);
		q.setParameter("nodeUUID", nodeUUID);

		try {
			gr = q.getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();

		}
		return gr;
	}

	/**
	 * Les droits que l'on a du groupe "all" NOTE: Pas de vérification si la
	 * personne est dans le groupe 'all'. Le fonctionnement voulu est différent de
	 * ce que j'avais prévu, mais ca marche aussi
	 * 
	 */
	public GroupRights getPublicRightsByGroupId(UUID nodeUuid, Long groupId) {
		GroupRights res = null;
		String sql = "SELECT gr FROM GroupRights gr";
		sql += " WHERE gr.id.id = :nodeUuid";
		sql += " AND id.groupRightInfo.id = (";
		sql += " SELECT gri2.id FROM GroupInfo gi, GroupRightInfo gri2";
		sql += " INNER JOIN gi.groupRightInfo gri1";
		sql += " WHERE gri1.portfolio.id = gri2.portfolio.id";
		sql += " AND gi.id = :groupId";
		sql += " AND gri2.label = :label )";
		TypedQuery<GroupRights> st = em.createQuery(sql, GroupRights.class);
		st.setParameter("nodeUuid", nodeUuid);
		st.setParameter("groupId", groupId);
		try {
			res = st.getSingleResult();
		} catch (NoResultException e) {
		}
		return res;
	}

	/**
	 * Les droits que l'on a du groupe "all" NOTE: Pas de vérification si la
	 * personne est dans le groupe 'all'. Le fonctionnement voulu est différent de
	 * ce que j'avais prévu, mais ca marche aussi
	 * 
	 */
	public GroupRights getPublicRightsByGroupId(String nodeUuid, Long groupId) {
		return getPublicRightsByGroupId(UUID.fromString(nodeUuid), groupId);
	}

	public String getGroupRightsInfos(int userId, String portfolioId) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRolePortfolio(MimeType mimeType, String role, String portfolioId, int userId) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRole(MimeType mimeType, int grid, int userId) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object putRole(String xmlRole, int userId, int roleId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public String postRoleUser(int userId, int grid, Integer userid2) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean postNodeRight(int userId, String nodeUuid) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean postRightGroup(int groupRightId, int groupId, Integer userId) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean postNotifyRoles(int userId, String portfolio, String uuid, String notify) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean setPublicState(int userId, String portfolio, boolean isPublic) {
		// TODO Auto-generated method stub
		return false;
	}

	public int postShareGroup(String portfolio, int user, Integer userId, String write) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int deleteShareGroup(String portfolio, Integer userId) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int deleteSharePerson(String portfolio, int user, Integer userId) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Object deleteGroupRights(Integer groupId, Integer groupRightId, Integer userId) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Regarde si l'utilisateur à un droit sur ce noeud dans l'un des groupes du
	 * portfolio. Pas de sélection de groupe donc le noeud pourrait etre référencé
	 * dans plusieurs groupes. On retourne le premier de la liste.
	 * 
	 * @param nodeUuid
	 * @param userId
	 * @return
	 */
	public GroupRights getRightsFromGroups(UUID nodeUuid, Long userId) {
		GroupRights res = null;
		String sql = "SELECT gr FROM GroupRights gr";
		sql += " INNER JOIN FETCH gr.id.groupRightInfo gri";
		sql += " INNER JOIN FETCH gri.portfolio p";
		sql += " INNER JOIN FETCH gri.groupInfo gi";
		sql += " INNER JOIN gi.groupUser gu WITH gu.id.credential.id = :userId";
		sql += " WHERE gr.id.id = :nodeUuid";

		TypedQuery<GroupRights> q = em.createQuery(sql, GroupRights.class);
		q.setParameter("userId", userId);
		q.setParameter("nodeUuid", nodeUuid);
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
		sql += " WHERE gr.id.id = :nodeUuid";
		TypedQuery<GroupRights> q = em.createQuery(sql, GroupRights.class);
		q.setParameter("nodeUuid", uuid);
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
	 * @param nodeUuid
	 * @param userId
	 * @return
	 */
	public GroupRights getRightsFromGroups(String nodeUuid, Long userId) {
		return getRightsFromGroups(UUID.fromString(nodeUuid), userId);
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
	public GroupRights getRightsByGroupId(String nodeUuid, Long userId, Long groupId) {
		return getRightsByGroupId(UUID.fromString(nodeUuid), userId, groupId);
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
	public GroupRights getRightsByGroupId(UUID nodeUuid, Long userId, Long groupId) {
		GroupRights res = null;
		String sql = "SELECT gr FROM GroupRights gr";
		sql += " INNER JOIN FETCH gr.id.groupRightInfo gri";
		sql += " INNER JOIN FETCH gri.groupInfo gi";
		sql += " INNER JOIN gi.groupUser gu WITH gu.id.credential.id = :userId AND gu.id.groupInfo.id = :groupId";
		sql += " WHERE gr.id.id = :nodeUuid";

		TypedQuery<GroupRights> q = em.createQuery(sql, GroupRights.class);
		q.setParameter("userId", userId);
		q.setParameter("nodeUuid", nodeUuid);
		q.setParameter("groupId", groupId);
		try {
			res = q.getSingleResult();
		} catch (NoResultException e) {
		}
		return res;
	}

	/**
	 * Regarde si l'utilisateur à un droit sur le noeud en fonction du groupe
	 * donnéen parametre.
	 * 
	 * @param nodeUuid
	 * @param userId
	 * @param groupId
	 * @return
	 */
	public GroupRights getRightsByGrid(UUID nodeUuid, Long grid) {
		GroupRights res = null;
		String sql = "SELECT gr FROM GroupRights gr";
		sql += " INNER JOIN FETCH gr.id.groupRightInfo gri WITH gri.id = :grid";
		sql += " WHERE gr.id.id = :nodeUuid";

		TypedQuery<GroupRights> q = em.createQuery(sql, GroupRights.class);
		q.setParameter("grid", grid);
		q.setParameter("nodeUuid", nodeUuid);
		try {
			res = q.getSingleResult();
		} catch (NoResultException e) {
		}
		return res;
	}

	public List<GroupRights> getRightsByPortfolio(UUID nodeUuid, UUID portfolioUuid) {
		List<GroupRightInfo> pa_implode = groupRightInfoDao.getByPortfolioID(portfolioUuid);

		String sql = "SELECT gr FROM GroupRights gr";
		sql += " WHERE gr.id.id = :nodeUuid";
		sql += " AND gr.id.groupRightInfo.id IN (" + PhpUtil.implode(",", pa_implode) + ")";

		TypedQuery<GroupRights> q = em.createQuery(sql, GroupRights.class);
		q.setParameter("nodeUuid", nodeUuid);
		return q.getResultList();
	}

	public List<GroupRights> getRightsByPortfolio(String uuid, String portfolioId) {
		return getRightsByPortfolio(UUID.fromString(uuid), UUID.fromString(portfolioId));
	}

	public GroupRights getRightsByGrid(String uuid, Long grid) {
		return getRightsByGrid(UUID.fromString(uuid), grid);
	}

	/**
	 * Regarde si l'utilisateur à droits spécifiques sur le noeud.
	 * 
	 * @param nodeUuid
	 * @param userId
	 * @return
	 */
	public GroupRights getSpecificRightsForUser(String nodeUuid, Long userId) {
		return getSpecificRightsForUser(UUID.fromString(nodeUuid), userId);

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
		sql += " WHERE gr.id.id = :nodeUuid";
		sql += " AND id.groupRightInfo.id = (";
		sql += " SELECT gri.id FROM Credential c, GroupRightInfo gri, Node n";
		sql += " WHERE c.login = gri.label";
		sql += " AND c.id = :userId";
		sql += " AND gri.portfolio.id = n.portfolio.id";
		sql += " AND n.id = :nodeUuid";
		sql += ")";
		TypedQuery<GroupRights> q = em.createQuery(sql, GroupRights.class);
		q.setParameter("nodeUuid", nodeUuid);
		q.setParameter("userId", userId);
		try {
			res = q.getSingleResult();
		} catch (NoResultException e) {
		}
		return res;
	}

	@Override
	public boolean postGroupRight(String label, String uuid, String droit, String portfolioUuid, Long userId) {
		List<GroupUser> res = null;
		GroupRights res2 = null;
		GroupRightInfo gri = null;
		GroupRights gr = null;
		int RD = 0;
		int WR = 0;
		int DL = 0;
		int SB = 0;
		int AD = 0;
		Long grid = -1L;
		boolean reponse = true;

		if (GroupRights.READ.equals(droit)) {
			RD = 1;
		} else if (GroupRights.WRITE.equals(droit)) {
			WR = 1;
		} else if (GroupRights.DELETE.equals(droit)) {
			DL = 1;
		} else if (GroupRights.SUBMIT.equals(droit)) {
			SB = 1;
		} else if (GroupRights.ADD.equals(droit)) {
			AD = 1;
		}

		try {

			if (StringUtils.isNotBlank(label) && droit != null) {
				if ("user".equals(label)) // Si le nom de group est 'user'. Le remplacer par le role de l'utilisateur
											// (voir pour juste le nom plus tard)
				{
					res = groupUserDao.getUserGroupByPortfolioAndUser(portfolioUuid, userId);

				} else if (!"".equals(portfolioUuid)) /// Role et portfolio
				{
					gri = groupRightInfoDao.getByPortfolioAndLabel(portfolioUuid, label);
					if (gri == null) // Groupe non-existant
					{

						gri = new GroupRightInfo();
						gri.setOwner(userId);
						gri.setLabel(label);
						gri.setChangeRights(false);
						gri.setPortfolio(new Portfolio(UUID.fromString(portfolioUuid)));
						groupRightInfoDao.persist(gri);

						/// Cree une copie dans group_info, le temps de re-organiser tout ca
						GroupInfo gi = new GroupInfo();
						gi.setId(grid);
						gi.setOwner(userId);
						gi.setLabel(label);
						groupInfoDao.persist(gi);
					}

				} else // Role et uuid
				{
					gr = getRightsByIdAndLabel(uuid, label);
				}

				if (res != null || gri != null || gr != null) /// On a trouve notre groupe
				{
					if (grid == -1) {
						if (res != null) {
							grid = res.get(0).getGroupInfo().getGroupRightInfo().getId();
						} else if (gri != null) {
							grid = gri.getId();
						} else if (gr != null) {
							grid = gr.getGroupRightInfo().getId();
						}
					}

					res2 = getRightsByGrid(uuid, grid);

					//// FIXME Pas de noeud existant. Il me semble qu'il y a un UPDATE OR INSERT
					//// dans
					// MySQL. A verifier et arranger au besoin.
					if (res2 == null) {
						res2 = new GroupRights();
						res2.setId(new GroupRightsId());
						res2.setGroupRightInfo(groupRightInfoDao.findById(grid));
						res2.setGroupRightsId(UUID.fromString(uuid));
					}
					if (GroupRights.READ.equals(droit)) {
						res2.setRead(BooleanUtils.toBoolean(RD));
					} else if (GroupRights.WRITE.equals(droit)) {
						res2.setWrite(BooleanUtils.toBoolean(WR));
					} else if (GroupRights.DELETE.equals(droit)) {
						res2.setDelete(BooleanUtils.toBoolean(DL));
					} else if (GroupRights.SUBMIT.equals(droit)) {
						//// FIXME: ajoute le rules_id prÃ©-cannÃ© pour certaine valeurs
						res2.setSubmit(BooleanUtils.toBoolean(SB));
					} else if (GroupRights.ADD.equals(droit)) {
						res2.setAdd(BooleanUtils.toBoolean(AD));
					} else {
						// Le droit d'executer des actions.
						// FIXME Pas propre, à changer plus tard.
						res2.setRulesId(droit);
					}
					merge(res2);
				}
			}
		} catch (Exception ex) {
			reponse = false;
		}
		return reponse;
	}

}
