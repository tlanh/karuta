/* =======================================================
	Copyright 2020 - ePortfolium - Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
   ======================================================= */

package eportfolium.com.karuta.business.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import eportfolium.com.karuta.consumer.repositories.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.consumer.util.DomUtils;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.CredentialGroup;
import eportfolium.com.karuta.model.bean.CredentialGroupMembers;
import eportfolium.com.karuta.model.bean.GroupInfo;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.GroupRightsId;
import eportfolium.com.karuta.model.bean.GroupUser;
import eportfolium.com.karuta.model.bean.GroupUserId;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.GenericBusinessException;

@Service
@Transactional
public class GroupManagerImpl implements GroupManager {

	@Autowired
	private GroupRightInfoRepository groupRightInfoRepository;

	@Autowired
	private CredentialRepository credentialRepository;

	@Autowired
	private GroupInfoRepository groupInfoRepository;

	@Autowired
	private GroupRightsRepository groupRightsRepository;

	@Autowired
	private CredentialGroupMembersRepository credentialGroupMembersRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private GroupUserRepository groupUserRepository;

	@Autowired
	private NodeRepository nodeRepository;

	@Autowired
	private CredentialGroupRepository credentialGroupRepository;

	public String addGroup(String name) {
		Long retval = 0L;
		try {
			GroupRightInfo gri = new GroupRightInfo();
			gri.setOwner(1);
			gri.setLabel(name);
			groupRightInfoRepository.save(gri);

			retval = gri.getId();

			groupInfoRepository.save(new GroupInfo(gri, 1L, name));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return String.valueOf(retval);
	}

	public String getCredentialGroupByUser(Long userId) {
		List<CredentialGroupMembers> cgmList = credentialGroupMembersRepository.findByUser(userId);
		String result = "<groups>";

		for (CredentialGroupMembers cgm : cgmList) {
			result += "<group ";
			result += DomUtils.getXmlAttributeOutput("id", "" + cgm.getCredentialGroup().getId()) + " ";
			result += ">";
			result += "<label>" + cgm.getCredentialGroup().getLabel() + "</label>";
			result += "</group>";
		}
		result += "</groups>";
		return result;
	}

	public boolean changeNotifyRoles(Long userId, UUID portfolioId, UUID nodeId, String notify)
			throws GenericBusinessException {

		if (!credentialRepository.isAdmin(userId))
			throw new GenericBusinessException("No admin right");

		List<GroupRights> grList = groupRightsRepository.getRightsByPortfolio(nodeId, portfolioId);

		grList.forEach(gr -> gr.setNotifyRoles(notify));
		groupRightsRepository.saveAll(grList);

		return true;
	}

	public boolean setPublicState(Long userId, UUID portfolioId, boolean isPublic) throws BusinessException {
		boolean ret = false;
		if (!credentialRepository.isAdmin(userId)
				&& !portfolioRepository.isOwner(portfolioId, userId)
				&& !credentialRepository.isDesigner(userId, portfolioId)
				&& !credentialRepository.isCreator(userId))
			throw new GenericBusinessException("No admin right");

		try {
			// S'assure qu'il y ait au moins un groupe de base
			List<GroupRightInfo> rs = groupRightInfoRepository.getDefaultByPortfolio(portfolioId);
			long gid = 0;
			if (CollectionUtils.isNotEmpty(rs))
				gid = rs.get(0).getGroupInfo().getId();

			if (gid == 0) // If not exist, create 'all' groups
			{
//				c.setAutoCommit(false);
				GroupRightInfo gri = new GroupRightInfo();
				gri.setOwner(userId);
				gri.setLabel("all");
				gri.setPortfolio(new Portfolio(portfolioId));

				groupRightInfoRepository.save(gri);

				// Insert all nodes into rights
				// TODO: Might need updates on additional nodes too
				List<Node> nodes = nodeRepository.getNodes(portfolioId);
				Iterator<Node> it = nodes.iterator();
				Node current = null;
				GroupRights gr = null;
				while (it.hasNext()) {
					current = it.next();
					gr = new GroupRights();
					gr.setId(new GroupRightsId());
					gr.setGroupRightInfo(gri);
					gr.setGroupRightsId(current.getId());

					groupRightsRepository.save(gr);
				}

				groupInfoRepository.save(new GroupInfo(gri, userId, "all"));
			}

			Long publicUid = credentialRepository.getPublicId();
			GroupUserId id = new GroupUserId();
			id.setGroupInfo(new GroupInfo(gid));
			id.setCredential(new Credential(publicUid));
			if (isPublic) // Insère ou retire 'sys_public' dans le groupe 'all' du portfolio
			{
				if (!groupUserRepository.existsById(id)) {
					groupUserRepository.save(new GroupUser(id));
				}
			} else {
				groupUserRepository.deleteById(id);
			}

			ret = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;

	}

	public String getGroupsByRole(UUID portfolioId, String role) {
		List<GroupInfo> giList = groupInfoRepository.getGroupsByRole(portfolioId, role);

		String result = "<groups>";

		for (GroupInfo gi : giList) {
			result += DomUtils.getXmlElementOutput("group", String.valueOf(gi.getId()));
		}

		result += "</groups>";

		return result;
	}

	public String getUserGroups(Long userId) throws Exception {
		List<GroupUser> res = groupUserRepository.getByUser(userId);
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

	public void changeUserGroup(Long grid, Long groupId, Long userId) throws BusinessException {
		if (!credentialRepository.isAdmin(userId))
			throw new GenericBusinessException("403 FORBIDDEN : No admin right");


		Optional<GroupInfo> gi = groupInfoRepository.findById(groupId);

		if (gi.isPresent()) {
			GroupInfo groupInfo = gi.get();

			groupInfo.setGroupRightInfo(new GroupRightInfo(grid));
			groupInfoRepository.save(groupInfo);
		}
	}

	/**
	 * Ajout des droits du portfolio dans GroupRightInfo et GroupRights
	 * 
	 * @param label
	 * @param nodeId
	 * @param right
	 * @param portfolioId
	 * @param userId
	 * @return
	 */
	public boolean addGroupRights(String label, UUID nodeId, String right, UUID portfolioId, Long userId) {
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

		if (GroupRights.READ.equalsIgnoreCase(right)) {
			RD = 1;
		} else if (GroupRights.WRITE.equalsIgnoreCase(right)) {
			WR = 1;
		} else if (GroupRights.DELETE.equalsIgnoreCase(right)) {
			DL = 1;
		} else if (GroupRights.SUBMIT.equalsIgnoreCase(right)) {
			SB = 1;
		} else if (GroupRights.ADD.equalsIgnoreCase(right)) {
			AD = 1;
		}

		try {

			if (StringUtils.isNotBlank(label) && right != null) {
				// Si le nom de group est 'user'. Le remplacer par le rôle de l'utilisateur
				// (voir pour juste le nom plus tard)
				if ("user".equals(label)) {
					res = groupUserRepository.getByPortfolioAndUser(portfolioId, userId);

				} else if (portfolioId != null) { /// Rôle et portfolio

					gri = groupRightInfoRepository.getByPortfolioAndLabel(portfolioId, label);
					if (gri == null) // Groupe non-existant
					{
						gri = new GroupRightInfo();
						gri.setOwner(userId);
						gri.setLabel(label);
						gri.setChangeRights(false);
						gri.setPortfolio(new Portfolio(portfolioId));
						groupRightInfoRepository.save(gri);

						/// Crée une copie dans group_info, le temps de re-organiser tout ça.
						groupInfoRepository.save(new GroupInfo(gri, userId, label));
					}

				} else { // Role et uuid
					gr = groupRightsRepository.getRightsByIdAndLabel(nodeId, label);
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

					res2 = groupRightsRepository.getRightsByGrid(nodeId, grid);

					//// FIXME Pas de noeud existant. Il me semble qu'il y a un UPDATE OR INSERT
					//// dans MySQL. A verifier et arranger au besoin.
					if (res2 == null) {
						res2 = new GroupRights();
						res2.setId(new GroupRightsId());
						res2.setGroupRightInfo(groupRightInfoRepository.findById(grid).get());
						res2.setGroupRightsId(nodeId);
					}
					if (GroupRights.READ.equalsIgnoreCase(right)) {
						res2.setRead(BooleanUtils.toBoolean(RD));
					} else if (GroupRights.WRITE.equalsIgnoreCase(right)) {
						res2.setWrite(BooleanUtils.toBoolean(WR));
					} else if (GroupRights.DELETE.equalsIgnoreCase(right)) {
						res2.setDelete(BooleanUtils.toBoolean(DL));
					} else if (GroupRights.SUBMIT.equalsIgnoreCase(right)) {
						//// FIXME: ajoute le rules_id prÃ©-cannÃ© pour certaine valeurs
						res2.setSubmit(BooleanUtils.toBoolean(SB));
					} else if (GroupRights.ADD.equalsIgnoreCase(right)) {
						res2.setAdd(BooleanUtils.toBoolean(AD));
					} else {
						// Le droit d'executer des actions.
						// FIXME Pas propre, à changer plus tard.
						res2.setRulesId(right);
					}

					groupRightsRepository.save(res2);
				}
			}
		} catch (Exception ex) {
			reponse = false;
		}
		return reponse;
	}

	public String getGroupRights(Long userId, Long groupId) throws Exception {
		if (!credentialRepository.isAdmin(userId))
			throw new GenericBusinessException("403 FORBIDDEN : No admin right");

		List<GroupRights> resList = groupRightsRepository.getRightsByGroupId(groupId);
		boolean AD, SB, WR, DL, RD;
		AD = SB = WR = DL = RD = true;

		String result = "<groupRights>";
		for (GroupRights res : resList) {
			result += "<groupRight ";
			result += DomUtils.getXmlAttributeOutput("gid", res.getGroupRightInfo().getGroupInfo().getId().toString())
					+ " ";
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
		if (!credentialRepository.isAdmin(userId))
			throw new GenericBusinessException("403 FORBIDDEN : no admin right");

		groupInfoRepository.deleteById(groupId);
	}

	/*******************************************************
	 * Fcts. pour groupes utilisateurs
	 *******************************************************/
	public String getCredentialGroupList() {
		String result = "<groups>";
		Iterable<CredentialGroup> res = credentialGroupRepository.findAll();

		for (CredentialGroup currentCredential : res) {
			result += "<group ";
			result += DomUtils.getXmlAttributeOutput("id", String.valueOf(currentCredential.getId()) + " ");
			result += ">";
			result += DomUtils.getXmlElementOutput("label", currentCredential.getLabel());
			result += "</group>";
		}
		result += "</groups>";

		return result;
	}

	public Long addCredentialGroup(String credentialGroupName) {
		CredentialGroup cg = new CredentialGroup();
		cg.setLabel(credentialGroupName);

		credentialGroupRepository.save(cg);

		return cg.getId();
	}

	public boolean renameCredentialGroup(Long credentialGroupId, String newName) {
		Optional<CredentialGroup> credentialGroup = credentialGroupRepository.findById(credentialGroupId);

		if (credentialGroup.isPresent()) {
			CredentialGroup cg = credentialGroup.get();
			cg.setLabel(newName);

			credentialGroupRepository.save(cg);

			return true;
		} else {
			return false;
		}
	}

	public CredentialGroup getCredentialGroupByName(String name) {
		return credentialGroupRepository.findByLabel(name);
	}

	public Boolean removeCredentialGroup(Long credentialGroupId) {
		try {
			credentialGroupMembersRepository.deleteAll(credentialGroupMembersRepository.findByGroup(credentialGroupId));
			credentialGroupRepository.deleteById(credentialGroupId);
			return true;

		} catch (Exception e) {
			e.printStackTrace();

			return false;
		}
	}
}
