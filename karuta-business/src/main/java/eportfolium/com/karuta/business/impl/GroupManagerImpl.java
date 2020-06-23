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

import eportfolium.com.karuta.business.security.IsAdmin;
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
	@IsAdmin
	public void changeNotifyRoles(UUID portfolioId, UUID nodeId, String notify) {
		List<GroupRights> grList = groupRightsRepository.getRightsByPortfolio(nodeId, portfolioId);

		grList.forEach(gr -> gr.setNotifyRoles(notify));
		groupRightsRepository.saveAll(grList);
	}

	@Override
	public void setPublicState(Long userId, UUID portfolioId, boolean isPublic) throws BusinessException {
		if (!credentialRepository.isAdmin(userId)
				&& !portfolioRepository.isOwner(portfolioId, userId)
				&& !credentialRepository.isDesigner(userId, portfolioId)
				&& !credentialRepository.isCreator(userId))
			throw new GenericBusinessException("No admin right");

		// S'assure qu'il y ait au moins un groupe de base
		GroupRightInfo defaultGroup = groupRightInfoRepository.getDefaultByPortfolio(portfolioId);
		GroupInfo groupInfo = defaultGroup != null ? defaultGroup.getGroupInfo() : null;

		// If the default group doesn't exist, create it (with 'all' label).
		if (groupInfo == null) {
			GroupRightInfo gri = new GroupRightInfo();
			gri.setOwner(userId);
			gri.setLabel("all");
			gri.setPortfolio(new Portfolio(portfolioId));

			groupRightInfoRepository.save(gri);

			// Insert all nodes into rights
			// TODO: Might need updates on additional nodes too
			List<GroupRights> groupRights = nodeRepository.getNodes(portfolioId)
					.stream()
					.map(node -> {
						GroupRights gr = new GroupRights();
						gr.setId(new GroupRightsId());
						gr.setGroupRightInfo(gri);
						gr.setGroupRightsId(node.getId());

						return gr;
					}).collect(Collectors.toList());

			groupRightsRepository.saveAll(groupRights);

			groupInfo = new GroupInfo(gri, userId, "all");
			groupInfoRepository.save(groupInfo);
		}

		Long publicUid = credentialRepository.getPublicId();
		GroupUserId id = new GroupUserId();
		id.setGroupInfo(new GroupInfo(groupInfo.getId()));
		id.setCredential(new Credential(publicUid));

		// Insert or remove the public account from the default group.
		if (isPublic) {
			if (!groupUserRepository.existsById(id)) {
				groupUserRepository.save(new GroupUser(id));
			}
		} else {
			groupUserRepository.deleteById(id);
		}
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
	@IsAdmin
	public void changeUserGroup(Long grid, Long groupId) {
		groupInfoRepository.findById(groupId)
				.ifPresent(gi -> {
					gi.setGroupRightInfo(new GroupRightInfo(grid));
					groupInfoRepository.save(gi);
				});
	}

	/**
	 * Ajout des droits du portfolio dans GroupRightInfo et GroupRights
	 */
	@Override
	public void addGroupRights(String label, UUID nodeId, String right, UUID portfolioId, Long userId) {
		GroupRightInfo groupRightInfo;
		GroupRights groupRights;

		if (StringUtils.isBlank(label) || right == null) {
			return;
		}

		// Si le nom de group est 'user'. Le remplacer par le rôle de l'utilisateur
		// (voir pour juste le nom plus tard)
		if ("user".equals(label)) {
			List<GroupUser> groups = groupUserRepository.getByPortfolioAndUser(portfolioId, userId);

			if (groups != null && !groups.isEmpty()) {
				groupRightInfo = groups.get(0).getGroupInfo().getGroupRightInfo();
				groupRights = groupRightsRepository.getRightsByGrid(nodeId, groupRightInfo.getId());
			} else {
				return;
			}

		} else if (portfolioId != null) { /// Rôle et portfolio

			groupRightInfo = groupRightInfoRepository.getByPortfolioAndLabel(portfolioId, label);

			if (groupRightInfo == null) // Groupe non-existant
			{
				groupRightInfo = new GroupRightInfo();
				groupRightInfo.setOwner(userId);
				groupRightInfo.setLabel(label);
				groupRightInfo.setChangeRights(false);
				groupRightInfo.setPortfolio(new Portfolio(portfolioId));
				groupRightInfoRepository.save(groupRightInfo);

				/// Crée une copie dans group_info, le temps de re-organiser tout ça.
				groupInfoRepository.save(new GroupInfo(groupRightInfo, userId, label));
			}

			groupRights = groupRightsRepository.getRightsByGrid(nodeId, groupRightInfo.getId());

		} else { // Role et uuid
			groupRights = groupRightsRepository.getRightsByIdAndLabel(nodeId, label);

			if (groupRights != null)
				groupRightInfo = groupRights.getGroupRightInfo();
			else
				return;
		}

		if (groupRights == null) {
			groupRights = new GroupRights();
			groupRights.setId(new GroupRightsId());
			groupRights.setGroupRightInfo(groupRightInfo);
			groupRights.setGroupRightsId(nodeId);
		}

		if (GroupRights.READ.equalsIgnoreCase(right)) {
			groupRights.setRead(true);
		} else if (GroupRights.WRITE.equalsIgnoreCase(right)) {
			groupRights.setWrite(true);
		} else if (GroupRights.DELETE.equalsIgnoreCase(right)) {
			groupRights.setDelete(true);
		} else if (GroupRights.SUBMIT.equalsIgnoreCase(right)) {
			//// FIXME: ajoute le rules_id prÃ©-cannÃ© pour certaine valeurs
			groupRights.setSubmit(true);
		} else if (GroupRights.ADD.equalsIgnoreCase(right)) {
			groupRights.setAdd(true);
		} else {
			// Le droit d'executer des actions.
			// FIXME Pas propre, à changer plus tard.
			groupRights.setRulesId(right);
		}

		groupRightsRepository.save(groupRights);
	}

	@Override
	@IsAdmin
	public GroupRightsList getGroupRights(Long groupId) {
		List<GroupRights> groupRightsList = groupRightsRepository.getRightsByGroupId(groupId);

		return new GroupRightsList(groupRightsList.stream()
				.map(GroupRightsDocument::new)
				.collect(Collectors.toList()));
	}

	@IsAdmin
	public void removeRights(long groupId) {
		groupInfoRepository.deleteById(groupId);
	}

	@Override
	public CredentialGroupList getCredentialGroupList() {
		Iterable<CredentialGroup> groups = credentialGroupRepository.findAll();

		return new CredentialGroupList(StreamSupport.stream(groups.spliterator(), false)
				.map(CredentialGroupDocument::new)
				.collect(Collectors.toList()));
	}

	@Override
	public Long addCredentialGroup(String credentialGroupName) {
		CredentialGroup cg = new CredentialGroup();
		cg.setLabel(credentialGroupName);

		credentialGroupRepository.save(cg);

		return cg.getId();
	}

	@Override
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

	@Override
	public CredentialGroup getCredentialGroupByName(String name) {
		return credentialGroupRepository.findByLabel(name);
	}

	@Override
	public void removeCredentialGroup(Long credentialGroupId) {
		credentialGroupMembersRepository.deleteAll(
				credentialGroupMembersRepository.findByGroup(credentialGroupId));
		credentialGroupRepository.deleteById(credentialGroupId);
	}

	@Override
	public GroupRightInfo getByPortfolioAndLabel(UUID portfolioId, String label) {
		return groupRightInfoRepository.getByPortfolioAndLabel(portfolioId, label);
	}
}
