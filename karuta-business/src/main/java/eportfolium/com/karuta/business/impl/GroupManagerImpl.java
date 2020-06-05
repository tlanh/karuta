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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import eportfolium.com.karuta.consumer.repositories.*;
import eportfolium.com.karuta.document.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eportfolium.com.karuta.business.contract.GroupManager;
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

	@Override
	public CredentialGroupList getCredentialGroupByUser(Long userId) {
		List<CredentialGroupMembers> cgmList = credentialGroupMembersRepository.findByUser(userId);

		return new CredentialGroupList(cgmList.stream()
				.map(CredentialGroupMembers::getCredentialGroup)
				.map(CredentialGroupDocument::new)
				.collect(Collectors.toList()));
	}

	@Override
	public boolean changeNotifyRoles(Long userId, UUID portfolioId, UUID nodeId, String notify)
			throws GenericBusinessException {

		if (!credentialRepository.isAdmin(userId))
			throw new GenericBusinessException("No admin right");

		List<GroupRights> grList = groupRightsRepository.getRightsByPortfolio(nodeId, portfolioId);

		grList.forEach(gr -> gr.setNotifyRoles(notify));
		groupRightsRepository.saveAll(grList);

		return true;
	}

	@Override
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
			if (!rs.isEmpty())
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

	@Override
	public RoleGroupList getGroupsByRole(UUID portfolioId, String role) {
		List<GroupInfo> giList = groupInfoRepository.getGroupsByRole(portfolioId, role);

		return new RoleGroupList(giList.stream()
				.map(GroupInfo::getId)
				.collect(Collectors.toList()));
	}

	@Override
	public GroupInfoList getUserGroups(Long userId) {
		List<GroupUser> groups = groupUserRepository.getByUser(userId);

		return new GroupInfoList(groups.stream()
				.map(GroupUser::getGroupInfo)
				.map(GroupInfoDocument::new)
				.collect(Collectors.toList()));
	}

	@Override
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
		Long grid = -1L;
		boolean reponse = true;

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
						res2.setRead(true);
					} else if (GroupRights.WRITE.equalsIgnoreCase(right)) {
						res2.setWrite(true);
					} else if (GroupRights.DELETE.equalsIgnoreCase(right)) {
						res2.setDelete(true);
					} else if (GroupRights.SUBMIT.equalsIgnoreCase(right)) {
						//// FIXME: ajoute le rules_id prÃ©-cannÃ© pour certaine valeurs
						res2.setSubmit(true);
					} else if (GroupRights.ADD.equalsIgnoreCase(right)) {
						res2.setAdd(true);
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

	@Override
	public GroupRightsList getGroupRights(Long userId, Long groupId) throws BusinessException {
		if (!credentialRepository.isAdmin(userId))
			throw new GenericBusinessException("403 FORBIDDEN : No admin right");

		List<GroupRights> groupRightsList = groupRightsRepository.getRightsByGroupId(groupId);

		return new GroupRightsList(groupRightsList.stream()
				.map(GroupRightsDocument::new)
				.collect(Collectors.toList()));
	}

	public void removeRights(long groupId, Long userId) throws BusinessException {
		if (!credentialRepository.isAdmin(userId))
			throw new GenericBusinessException("403 FORBIDDEN : no admin right");

		groupInfoRepository.deleteById(groupId);
	}

	@Override
	public CredentialGroupList getCredentialGroupList() {
		Iterable<CredentialGroup> groups = credentialGroupRepository.findAll();

		return new CredentialGroupList(StreamSupport.stream(groups.spliterator(), false)
				.map(CredentialGroupDocument::new)
				.collect(Collectors.toList()));
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

	@Override
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

	@Override
	public GroupRightInfo getByPortfolioAndLabel(UUID portfolioId, String label) {
		return groupRightInfoRepository.getByPortfolioAndLabel(portfolioId, label);
	}
}
