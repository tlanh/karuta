package eportfolium.com.karuta.business.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.consumer.contract.dao.CredentialDao;
import eportfolium.com.karuta.consumer.contract.dao.CredentialGroupDao;
import eportfolium.com.karuta.consumer.contract.dao.CredentialGroupMembersDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupGroupDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupInfoDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupRightInfoDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupRightsDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupUserDao;
import eportfolium.com.karuta.consumer.contract.dao.NodeDao;
import eportfolium.com.karuta.consumer.contract.dao.PortfolioDao;
import eportfolium.com.karuta.consumer.util.DomUtils;
import eportfolium.com.karuta.model.bean.CredentialGroup;
import eportfolium.com.karuta.model.bean.CredentialGroupMembers;
import eportfolium.com.karuta.model.bean.GroupGroup;
import eportfolium.com.karuta.model.bean.GroupGroupId;
import eportfolium.com.karuta.model.bean.GroupInfo;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.GroupRightsId;
import eportfolium.com.karuta.model.bean.GroupUser;
import eportfolium.com.karuta.model.bean.GroupUserId;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.model.exception.GenericBusinessException;

@Service
@Transactional
public class GroupManagerImpl implements GroupManager {

	@Autowired
	private GroupRightInfoDao groupRightInfoDao;

	@Autowired
	private CredentialDao credentialDao;

	@Autowired
	private GroupInfoDao groupInfoDao;

	@Autowired
	private GroupRightsDao groupRightsDao;

	@Autowired
	private CredentialGroupMembersDao credentialGroupMembersDao;

	@Autowired
	private PortfolioDao portfolioDao;

	@Autowired
	private GroupUserDao groupUserDao;

	@Autowired
	private NodeDao nodeDao;

	@Autowired
	private CredentialGroupDao credentialGroupDao;

	@Autowired
	private GroupGroupDao groupGroupDao;

	public String addGroup(String name) {
		Long retval = 0L;
		try {
			GroupRightInfo gri = new GroupRightInfo();
			gri.setOwner(1);
			gri.setLabel(name);
			groupRightInfoDao.persist(gri);
			retval = gri.getId();

			GroupInfo groupInfo = new GroupInfo();
			groupInfo.setGroupRightInfo(gri);
			groupInfo.setOwner(1);
			groupInfo.setLabel(name);
			groupInfoDao.persist(groupInfo);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return String.valueOf(retval);
	}

	public String getGroupByUser(Long userId) {
		List<CredentialGroupMembers> cgmList = credentialGroupMembersDao.getGroupByUser(userId);
		Iterator<CredentialGroupMembers> it = cgmList.iterator();
		String result = "<groups>";
		CredentialGroupMembers cgm = null;
		while (it.hasNext()) {
			cgm = it.next();
			result += "<group ";
			result += DomUtils.getXmlAttributeOutput("id", "" + cgm.getCredentialGroup().getId()) + " ";
			result += ">";
			result += "<label>" + cgm.getCredentialGroup().getLabel() + "</label>";
			result += "</group>";
		}
		result += "</groups>";
		return result;
	}

	public String getGroupsByUser(Long id) {
		return null;
	}

	public boolean postNotifyRoles(Long userId, String portfolioId, String uuid, String notify)
			throws GenericBusinessException {

		boolean ret = false;
		if (!credentialDao.isAdmin(userId))
			throw new GenericBusinessException("No admin right");
		List<GroupRights> grList = groupRightsDao.getRightsByPortfolio(uuid, portfolioId);

		try {
			GroupRights gr = null;
			for (Iterator<GroupRights> it = grList.iterator(); it.hasNext();) {
				gr = it.next();
				gr.setNotifyRoles(notify);
				groupRightsDao.merge(gr);
			}
			ret = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	public boolean postNotifyRoles(Long userId, UUID portfolioId, UUID uuid, String notify)
			throws GenericBusinessException {

		boolean ret = false;
		if (!credentialDao.isAdmin(userId))
			throw new GenericBusinessException("No admin right");
		List<GroupRights> grList = groupRightsDao.getRightsByPortfolio(uuid, portfolioId);

		try {
			GroupRights gr = null;
			for (Iterator<GroupRights> it = grList.iterator(); it.hasNext();) {
				gr = it.next();
				gr.setNotifyRoles(notify);
				groupRightsDao.merge(gr);
			}
			ret = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	public boolean setPublicState(Long userId, String portfolioUuid, boolean isPublic) throws BusinessException {
		boolean ret = false;
		if (!credentialDao.isAdmin(userId) && !portfolioDao.isOwner(userId, portfolioUuid)
				&& !credentialDao.isDesigner(userId, portfolioUuid) && !credentialDao.isCreator(userId))
			throw new GenericBusinessException("No admin right");

		try {
			// S'assure qu'il y ait au moins un groupe de base
			List<GroupRightInfo> rs = groupRightInfoDao.getDefaultByPortfolio(portfolioUuid);
			long gid = 0;
			if (CollectionUtils.isNotEmpty(rs))
				gid = rs.get(0).getGroupInfo().getId();

			if (gid == 0) // If not exist, create 'all' groups
			{
//				c.setAutoCommit(false);
				GroupRightInfo gri = new GroupRightInfo();
				gri.setOwner(userId);
				gri.setLabel("all");
				gri.setPortfolio(new Portfolio(portfolioUuid));

				groupRightInfoDao.persist(gri);

				// Insert all nodes into rights
				// TODO: Might need updates on additional nodes too
				List<Node> nodes = nodeDao.getNodes(portfolioUuid);
				Iterator<Node> it = nodes.iterator();
				Node current = null;
				GroupRights gr = null;
				while (it.hasNext()) {
					current = it.next();
					gr = new GroupRights();
					gr.setGroupRightInfo(gri);
					gr.setId(new GroupRightsId());
					gr.setGroupRightsId(current.getId());
					groupRightsDao.persist(gr);
				}

				GroupInfo gi = new GroupInfo();
				gi.setGroupRightInfo(gri);
				gi.setOwner(userId);
				gi.setLabel("all");
				groupInfoDao.persist(gi);
			}

			Long publicUid = credentialDao.getPublicUid();
			GroupUserId id = new GroupUserId();
			id.setGroupInfoId(gid);
			id.setCredentialId(publicUid);
			if (isPublic) // Insère ou retire 'sys_public' dans le groupe 'all' du portfolio
			{
				GroupUser gu = null;
				try {
					gu = groupUserDao.findById(id);
				} catch (DoesNotExistException e) {
					gu = new GroupUser();
					gu.setId(id);
					groupUserDao.persist(gu);
				}

			} else {
				groupUserDao.removeById(id);
			}

			ret = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;

	}

	public String getGroupsByRole(String portfolioUuid, String role) {
		List<GroupInfo> giList = groupInfoDao.getGroupsByRole(portfolioUuid, role);
		Iterator<GroupInfo> it = giList.iterator();
		String result = "<groups>";
		while (it.hasNext()) {
			result += DomUtils.getXmlElementOutput("group", String.valueOf(it.next().getId()));
		}
		result += "</groups>";
		return result;
	}

	public String getUserGroupList() {
		String result = "<groups>";
		List<CredentialGroup> res = credentialGroupDao.findAll();
		Iterator<CredentialGroup> it = res.iterator();
		CredentialGroup currentCredential;
		while (it.hasNext()) {
			currentCredential = it.next();
			result += "<group ";
			result += DomUtils.getXmlAttributeOutput("id", String.valueOf(currentCredential.getId()) + " ");
			result += ">";
			result += DomUtils.getXmlElementOutput("label", currentCredential.getLabel());
			result += "</group>";
		}
		result += "</groups>";

		return result;
	}

	public String getUserGroups(Long userId) throws Exception {
		List<GroupUser> res = groupUserDao.getByUser(userId);
		GroupUser current = null;

		Iterator<GroupUser> it = res.iterator();
		String result = "<groups>";
		while (it.hasNext()) {
			current = it.next();
			result += "<group ";
			result += DomUtils.getXmlAttributeOutput("id", String.valueOf(current.getGroupInfo().getId())) + " ";
			result += DomUtils.getXmlAttributeOutput("owner", String.valueOf(current.getGroupInfo().getOwner())) + " ";
			result += DomUtils.getXmlAttributeOutput("templateId",
					String.valueOf(current.getGroupInfo().getGroupRightInfo().getId())) + " ";
			result += ">";
			result += DomUtils.getXmlElementOutput("label", current.getGroupInfo().getLabel());
			result += "</group>";
		}

		result += "</groups>";

		return result;
	}

	public Long addUserGroup(String groupName) throws Exception {
		return credentialGroupDao.createCredentialGroup(groupName);
	}

	public boolean renameUserGroup(Long groupId, String newName) {
		return credentialGroupDao.renameCredentialGroup(groupId, newName);
	}

	public CredentialGroup getGroupByName(String name) {
		return credentialGroupDao.getGroupByName(name);
	}

	public Boolean deleteUsersGroups(Long userGroupId) {
		Boolean isOK = Boolean.TRUE;
		try {
			final List<CredentialGroupMembers> userGroupList = credentialDao.getUsersByUserGroup(userGroupId);
			final Iterator<CredentialGroupMembers> it = userGroupList.iterator();
			while (it.hasNext()) {
				credentialGroupMembersDao.remove(it.next());
			}
			credentialGroupDao.removeById(userGroupId);
		} catch (Exception e) {
			e.printStackTrace();
			isOK = Boolean.FALSE;
		}
		return isOK;
	}

	public String addUserGroup(String in, Long userId) throws Exception {
		if (!credentialDao.isAdmin(userId))
			throw new GenericBusinessException("403 FORBIDDEN, No admin right");

		String result = null;
		Long grid = 0L;
		Long owner = 0L;
		String label = null;

		// On récupère le body
		Document doc = DomUtils.xmlString2Document(in, new StringBuffer());
		Element etu = doc.getDocumentElement();

		// On vérifie le bon format
		if (etu.getNodeName().equals("group")) {
			// On récupère les attributs
			try {
				if (etu.getAttributes().getNamedItem("grid") != null) {
					grid = Long.parseLong(etu.getAttributes().getNamedItem("grid").getNodeValue());
				} else {
					grid = null;
				}
			} catch (Exception ex) {
			}

			try {
				if (etu.getAttributes().getNamedItem("owner") != null) {
					owner = Long.parseLong(etu.getAttributes().getNamedItem("owner").getNodeValue());
					if (owner == 0)
						owner = userId;
				} else {
					owner = userId;
				}
			} catch (Exception ex) {
			}

			try {
				if (etu.getAttributes().getNamedItem("label") != null) {
					label = etu.getAttributes().getNamedItem("label").getNodeValue();
				}
			} catch (Exception ex) {
			}

		} else {
			result = "Erreur lors de la recuperation des attributs du groupe dans le XML";
		}

		if (grid == null)
			return "";

		// On ajoute le groupe dans la base de données
		groupInfoDao.add(new GroupRightInfo(grid), owner, label);

		// On renvoie le body pour qu'il soit stocke dans le log
		result = "<group ";
		result += DomUtils.getXmlAttributeOutputInt("grid", grid.intValue()) + " ";
		result += DomUtils.getXmlAttributeOutputInt("owner", owner.intValue()) + " ";
		result += DomUtils.getXmlAttributeOutput("label", label) + " ";
		result += ">";
		result += "</group>";
		return result;
	}

	public void changeUserGroup(Long groupRightId, Long groupId, Long userId) throws BusinessException {
		if (!credentialDao.isAdmin(userId))
			throw new GenericBusinessException("403 FORBIDDEN : No admin right");

		GroupInfo gi = groupInfoDao.findById(groupId);
		gi.setGroupRightInfo(new GroupRightInfo(groupRightId));
		gi = groupInfoDao.merge(gi);
	}

	/**
	 * Ajout des droits du portfolio dans group_right_info, group_rights
	 * 
	 * @param label
	 * @param uuid
	 * @param droit
	 * @param portfolioUuid
	 * @param userId
	 * @return
	 */
	public boolean addGroupRights(String label, String nodeUuid, String droit, String portfolioUuid, Long userId) {
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
				// Si le nom de group est 'user'. Le remplacer par le rôle de l'utilisateur
				// (voir pour juste le nom plus tard)
				if ("user".equals(label)) {
					res = groupUserDao.getByPortfolioAndUser(portfolioUuid, userId);

				} else if (!"".equals(portfolioUuid)) /// Rôle et portfolio
				{
					gri = groupRightInfoDao.getByPortfolioAndLabel(portfolioUuid, label);
					if (gri == null) // Groupe non-existant
					{

						gri = new GroupRightInfo();
						gri.setOwner(userId);
						gri.setLabel(label);
						gri.setChangeRights(false);
						gri.setPortfolio(new Portfolio(portfolioUuid));
						groupRightInfoDao.persist(gri);

						/// Crée une copie dans group_info, le temps de re-organiser tout ça.
						GroupInfo gi = new GroupInfo();
						gi.setId(grid);
						gi.setOwner(userId);
						gi.setLabel(label);
						groupInfoDao.persist(gi);
					}

				} else // Role et uuid
				{
					gr = groupRightsDao.getRightsByIdAndLabel(nodeUuid, label);
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

					res2 = groupRightsDao.getRightsByGrid(nodeUuid, grid);

					//// FIXME Pas de noeud existant. Il me semble qu'il y a un UPDATE OR INSERT
					//// dans MySQL. A verifier et arranger au besoin.
					if (res2 == null) {
						res2 = new GroupRights();
						res2.setId(new GroupRightsId());
						res2.setGroupRightInfo(groupRightInfoDao.findById(grid));
						res2.setGroupRightsId(nodeUuid);
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
					groupRightsDao.merge(res2);
				}
			}
		} catch (Exception ex) {
			reponse = false;
		}
		return reponse;
	}

	public String getGroupRights(Long userId, Long groupId) throws Exception {
		if (!credentialDao.isAdmin(userId))
			throw new GenericBusinessException("403 FORBIDDEN : No admin right");

		List<GroupRights> resList = groupRightsDao.getRightsByGroupId(groupId);
		boolean AD, SB, WR, DL, RD;
		AD = SB = WR = DL = RD = true;

		String result = "<groupRights>";
		for (GroupRights res : resList) {
			result += "<groupRight ";
			result += DomUtils.getXmlAttributeOutput("gid", res.getId().toString()) + " ";
			result += DomUtils.getXmlAttributeOutput("templateId", res.getGroupRightInfo().getId().toString()) + " ";
			result += ">";

			result += "<item ";
			if (AD == res.isAdd()) {
				result += DomUtils.getXmlAttributeOutput("add", "True") + " ";
			} else {
				result += DomUtils.getXmlAttributeOutput("add", "False") + " ";
			}
			result += DomUtils.getXmlAttributeOutput("creator",
					String.valueOf(res.getGroupRightInfo().getGroupInfo().getOwner())) + " ";
			result += DomUtils.getXmlAttributeOutput("date", String.valueOf(res.isDelete())) + " ";
			if (DL == res.isDelete()) {
				result += DomUtils.getXmlAttributeOutput("del", "True") + " ";
			} else {
				result += DomUtils.getXmlAttributeOutput("del", "False") + " ";
			}
			result += DomUtils.getXmlAttributeOutput("id", res.getGroupRightsId().toString()) + " ";
			result += DomUtils.getXmlAttributeOutput("owner", String.valueOf(res.getGroupRightInfo().getOwner())) + " ";
			if (RD == res.isRead()) {
				result += DomUtils.getXmlAttributeOutput("read", "True") + " ";
			} else {
				result += DomUtils.getXmlAttributeOutput("read", "False") + " ";
			}
			if (SB == res.isSubmit()) {
				result += DomUtils.getXmlAttributeOutput("submit", "True") + " ";
			} else {
				result += DomUtils.getXmlAttributeOutput("submit", "False") + " ";
			}
			result += DomUtils.getXmlAttributeOutput("typeId", res.getTypesId()) + " ";
			if (WR == res.isWrite()) {
				result += DomUtils.getXmlAttributeOutput("write", "True") + " ";
			} else {
				result += DomUtils.getXmlAttributeOutput("write", "False") + " ";
			}
			result += "/>";

			result += "</groupRight>";
		}
		result += "</groupRights>";
		return result;
	}

	public void removeRights(long groupId, Long userId) throws BusinessException {
		if (!credentialDao.isAdmin(userId))
			throw new GenericBusinessException("403 FORBIDDEN : no admin right");

		groupInfoDao.removeById(groupId);
	}

	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
	public Map<Long, Long> transferGroupRightInfoTable(Connection con, Map<String, String> portIds)
			throws SQLException {
		ResultSet res = groupInfoDao.getMysqlGroupRightsInfos(con);
		Map<Long, Long> groupRightIds = new HashMap<Long, Long>();
		GroupRightInfo gri = null;
		while (res.next()) {
			try {
				gri = new GroupRightInfo();
				gri.setOwner(res.getLong("owner"));
				gri.setLabel(res.getString("label"));
				if (StringUtils.isNotEmpty(res.getString("portfolio_id")))
					gri.setPortfolio(portfolioDao.findById(MapUtils.getString(portIds, res.getString("portfolio_id"))));
				gri.setChangeRights(res.getBoolean("change_rights"));
				gri = groupRightInfoDao.merge(gri);
				groupRightIds.put(res.getLong("grid"), gri.getId());
			} catch (DoesNotExistException e) {
				System.err.println("GroupManagerImpl.transferGroupRightInfoTable()" + " portfolio not found :"
						+ res.getString("portfolio_id"));
			}
		}
		return groupRightIds;
	}

	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
	public Map<Long, Long> transferGroupInfoTable(Connection con, Map<Long, Long> griIds) throws SQLException {
		ResultSet res = groupInfoDao.findAll("group_info", con);
		GroupInfo gi = null;
		final Map<Long, Long> groupInfoIds = new HashMap<Long, Long>();

		while (res.next()) {
			try {
				gi = new GroupInfo();
				gi.setId(res.getLong("gid"));
				long grid = res.getLong("grid");
				if (grid != 0) {
					gi.setGroupRightInfo(groupRightInfoDao.findById(MapUtils.getLong(griIds, grid)));
				}
				gi.setOwner(res.getLong("owner"));
				gi.setLabel(res.getString("label"));
				gi = groupInfoDao.merge(gi);
				groupInfoIds.put(grid, gi.getId());
			} catch (DoesNotExistException e) {
				System.err.println("GroupManagerImpl.transferGroupInfoTable()" + " GroupRightInfo not found :"
						+ res.getLong("grid"));
			}
		}
		return groupInfoIds;
	}

	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
	public void transferGroupGroupTable(Connection con, Map<Long, Long> giIds) throws SQLException {
		ResultSet res = groupGroupDao.findAll("group_group", con);
		GroupGroup gg = null;
		while (res.next()) {
			gg = new GroupGroup(new GroupGroupId(giIds.get(res.getLong("gid")), giIds.get(res.getLong("child_gid"))));
			groupGroupDao.persist(gg);
		}
	}

	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
	public Map<Long, Long> transferCredentialGroupTable(Connection con) throws SQLException {
		ResultSet res = credentialGroupDao.findAll("credential_group", con);
		CredentialGroup cg = null;
		Map<Long, Long> cgIds = new HashMap<Long, Long>();
		while (res.next()) {
			cg = new CredentialGroup();
			cg.setLabel(res.getString("label"));
			cg = credentialGroupDao.merge(cg);
			cgIds.put(res.getLong("cg"), cg.getId());
		}
		return cgIds;
	}

	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
	public void transferGroupRightsTable(Connection con, Map<Long, Long> griIds) throws SQLException {
		ResultSet res = groupRightsDao.getMysqlGroupRights(con);
		GroupRights gr = null;
		GroupRightsId grId = null;
		try {
			while (res.next()) {
				gr = new GroupRights();
				grId = new GroupRightsId(griIds.get(res.getLong("grid")), res.getString("id"));
				gr.setId(grId);
				gr.setRead(res.getBoolean("RD"));
				gr.setWrite(res.getBoolean("WR"));
				gr.setDelete(res.getBoolean("DL"));
				gr.setSubmit(res.getBoolean("SB"));
				gr.setAdd(res.getBoolean("AD"));
				gr.setTypesId(res.getString("types_id"));
				gr.setRulesId(res.getString("rules_id"));
				gr.setNotifyRoles(res.getString("notify_roles"));
				groupRightsDao.persist(gr);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
	public void transferGroupUserTable(Connection con, Map<Long, Long> giIds, Map<Long, Long> userIds)
			throws SQLException {
		ResultSet res = groupUserDao.findAll("group_user", con);
		// gid, grid, owner, label
		GroupUser gu = null;
		while (res.next()) {
			gu = new GroupUser(new GroupUserId(giIds.get(res.getLong("gid")), userIds.get(res.getLong("userid"))));
			groupUserDao.persist(gu);
		}
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
	public void removeGroups() {
		groupGroupDao.removeAll();
		groupUserDao.removeAll();
		groupRightInfoDao.removeAll();
		credentialGroupDao.removeAll();
		groupInfoDao.removeAll();
	}

}
