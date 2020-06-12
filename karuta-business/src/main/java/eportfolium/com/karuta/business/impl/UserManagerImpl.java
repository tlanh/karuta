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

import eportfolium.com.karuta.consumer.repositories.CredentialGroupMembersRepository;
import eportfolium.com.karuta.consumer.repositories.CredentialRepository;
import eportfolium.com.karuta.consumer.repositories.GroupRightInfoRepository;
import eportfolium.com.karuta.consumer.repositories.GroupUserRepository;
import eportfolium.com.karuta.document.*;
import eportfolium.com.karuta.model.bean.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import eportfolium.com.karuta.business.contract.UserManager;

@Service
@Transactional
public class UserManagerImpl implements UserManager {

	@Autowired
	private CredentialRepository credentialRepository;

	@Autowired
	private CredentialGroupMembersRepository credentialGroupMembersRepository;

	@Autowired
	private GroupUserRepository groupUserRepository;

	@Autowired
	private GroupRightInfoRepository groupRightInfoRepository;

	@Override
	public CredentialList getUserList(String username, String firstname, String lastname) {
		List<Credential> credentials = credentialRepository.getUsers(username, firstname, lastname);

		return new CredentialList(credentials.stream()
				.map(c -> new CredentialDocument(c, true))
				.collect(Collectors.toList()));
	}

	@Override
	public CredentialGroupDocument getUsersByCredentialGroup(Long userGroupId) {
		List<CredentialGroupMembers> userGroupList = credentialGroupMembersRepository.findByGroup(userGroupId);

		List<CredentialDocument> users = userGroupList.stream()
				.map(CredentialGroupMembers::getCredential)
				.map(CredentialDocument::new)
				.collect(Collectors.toList());

		return new CredentialGroupDocument(userGroupId, users);
	}

	@Override
	public CredentialList getUsersByRole(UUID portfolioId, String role) {
		List<Credential> users = credentialRepository.getUsersByRole(portfolioId, role);

		return new CredentialList(users.stream()
				.map(CredentialDocument::new)
				.collect(Collectors.toList()));
	}

	@Override
	public RoleDocument getRole(Long groupRightInfoId) {
		return groupRightInfoRepository
				.findById(groupRightInfoId)
				.map(RoleDocument::new)
				.orElse(null);
	}

	@Override
	public CredentialDocument getUserInfos(Long userId) {
		return new CredentialDocument(credentialRepository.getUserInfos(userId), true);
	}

	@Override
	public Long getUserId(String userLogin) {
		return credentialRepository.getIdByLogin(userLogin);
	}

	@Override
	public Long getUserId(String userLogin, String email) {
		return credentialRepository.getIdByLoginAndEmail(userLogin, email);
	}

	@Override
	public ProfileList getUserRolesByUserId(Long userId) {
		List<GroupUser> groups = groupUserRepository.getByUser(userId);

		return new ProfileList(groups.stream()
				.map(GroupUser::getGroupInfo)
				.map(GroupInfoDocument::new)
				.collect(Collectors.toList()));
	}

	@Override
	public String getEmailByLogin(String userLogin) {
		return credentialRepository.getEmailByLogin(userLogin);
	}

	@Override
	public RoleRightsGroupList getRoleList(UUID portfolioId, Long userId) {
		Iterable<GroupRightInfo> griList;

		if (portfolioId != null) {
			griList = groupRightInfoRepository.getByPortfolioID(portfolioId);
		} else if (userId != null) {
			griList = groupRightInfoRepository.getByUser(userId);
		} else {
			griList = groupRightInfoRepository.findAll();
		}

		return new RoleRightsGroupList(StreamSupport.stream(griList.spliterator(), false)
				.map(RoleRightsGroupDocument::new)
				.collect(Collectors.toList()));
	}

	@Override
	public GroupUserList getUserRolesByPortfolio(UUID portfolioId, Long userId) {
		// group_right_info pid:grid -> group_info grid:gid -> group_user gid:userid
		List<GroupUser> groupUsers = groupUserRepository.getByPortfolioAndUser(portfolioId, userId);

		return new GroupUserList(portfolioId, groupUsers.stream()
				.map(GroupUserDocument::new)
				.collect(Collectors.toList()));
	}

	@Override
	public RoleRightsGroupDocument getUserRole(Long rrgid) {
		List<GroupUser> groupUsers = groupUserRepository.getByRole(rrgid);

		return new RoleRightsGroupDocument(rrgid, groupUsers.stream()
				.map(GroupUserDocument::new)
				.collect(Collectors.toList()));
	}

}
