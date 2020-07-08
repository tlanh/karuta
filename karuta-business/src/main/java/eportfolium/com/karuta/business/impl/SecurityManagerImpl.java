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

import eportfolium.com.karuta.business.UserInfo;
import eportfolium.com.karuta.business.security.IsAdmin;
import eportfolium.com.karuta.business.security.IsAdminOrDesigner;
import eportfolium.com.karuta.consumer.repositories.*;
import eportfolium.com.karuta.document.CredentialDocument;
import eportfolium.com.karuta.document.CredentialList;
import eportfolium.com.karuta.document.LoginDocument;
import eportfolium.com.karuta.document.RoleDocument;
import org.apache.commons.lang3.StringUtils;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eportfolium.com.karuta.business.contract.EmailManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.CredentialGroup;
import eportfolium.com.karuta.model.bean.CredentialGroupMembers;
import eportfolium.com.karuta.model.bean.CredentialGroupMembersId;
import eportfolium.com.karuta.model.bean.CredentialSubstitution;
import eportfolium.com.karuta.model.bean.CredentialSubstitutionId;
import eportfolium.com.karuta.model.bean.GroupInfo;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.bean.GroupUser;
import eportfolium.com.karuta.model.bean.GroupUserId;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.GenericBusinessException;

@Service
@Transactional
public class SecurityManagerImpl implements SecurityManager {

	@Autowired
	private EmailManager emailManager;

	@Autowired
	private CredentialRepository credentialRepository;

	@Autowired
	private CredentialSubstitutionRepository credentialSubstitutionRepository;

	@Autowired
	private CredentialGroupMembersRepository credentialGroupMembersRepository;

	@Autowired
	private GroupUserRepository groupUserRepository;

	@Autowired
	private GroupRightInfoRepository groupRightInfoRepository;

	@Autowired
	private GroupInfoRepository groupInfoRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	private static final Logger log = LoggerFactory.getLogger(SecurityManagerImpl.class);

	private final PasswordEncoder passwordEncoder = new Pbkdf2PasswordEncoder();

	@Override
	public boolean changePassword(String username, String password) {
		try {
			Credential credential = credentialRepository.findByLogin(username);
			setPassword(password, credential);

			credentialRepository.save(credential);

			return true;
		} catch (BusinessException e) {
			return false;
		}
	}

	@Override
	public String generatePassword() {
		List<CharacterRule> rules = Arrays.asList(
				new CharacterRule(EnglishCharacterData.UpperCase, 4),
				new CharacterRule(EnglishCharacterData.LowerCase, 5),
				new CharacterRule(EnglishCharacterData.Digit, 2),
				new CharacterRule(EnglishCharacterData.Special, 1));

		PasswordGenerator generator = new PasswordGenerator();

		return generator.generatePassword(12, rules);
	}

	private void setPassword(String newPassword, Credential credential) throws BusinessException {
		if (StringUtils.isEmpty(newPassword)) {
			throw new GenericBusinessException("New password is required.");
		}

		credential.setPassword(passwordEncoder.encode(newPassword));
	}

	@Override
	@PreAuthorize("hasRole('ADMIN') or principal.id == #forUser")
	public void removeUsers(@P("forUser") Long forUser) {
		groupUserRepository.deleteAll(groupUserRepository.getByUser(forUser));
		credentialRepository.deleteById(forUser);
	}

	@Override
	@IsAdminOrDesigner
	public CredentialList addUsers(CredentialList users) {
		List<CredentialDocument> processed = new ArrayList<>();

		for (CredentialDocument document : users.getUsers()) {
			// Password is required
			if (StringUtils.isEmpty(document.getPassword())) {
				continue;
			}

			Credential credential = new Credential();

			credential.setActive(document.getActive());
			credential.setIsDesigner(document.getDesigner());
			credential.setCanSubstitute(document.getSubstitute());
			credential.setPassword(passwordEncoder.encode(document.getPassword()));

			credential.setLogin(document.getUsername());
			credential.setOther(document.getOther());
			credential.setEmail(document.getEmail());
			credential.setDisplayFirstname(document.getFirstname());
			credential.setDisplayLastname(document.getLastname());

			credential = credentialRepository.save(credential);

			/// FIXME: More complete rule to use
			CredentialSubstitutionId csId = new CredentialSubstitutionId();
			// id = 0, ne pas vérifier qui cette personne peut remplacer (sauf root)
			csId.setId(0L);
			csId.setCredential(credential);
			csId.setType("USER");

			if (credential.getCanSubstitute() == 1) {
				CredentialSubstitution subst = new CredentialSubstitution();
				subst.setId(csId);

				subst = credentialSubstitutionRepository.save(subst);
				credential.setCredentialSubstitution(subst);
				credentialRepository.save(credential);
			} else {
				credentialSubstitutionRepository.deleteById(csId);
			}

			processed.add(document);
		}

		return new CredentialList(processed);
	}

	@Override
	public Long changeUser(Long byUserId, Long forUserId, CredentialDocument user) throws BusinessException {
		Credential credential = credentialRepository.findActiveById(forUserId)
									.orElseThrow(() -> new GenericBusinessException("Unexisting user"));

		if (!credentialRepository.isAdmin(byUserId) &&
				!passwordEncoder.matches(user.getPrevpass(), credential.getPassword())) {
			throw new GenericBusinessException("Not authorized");
		}

		if (user.getUsername() != null)
			credential.setLogin(user.getUsername());

		if (user.getPassword() != null)
			setPassword(user.getPassword(), credential);

		if (user.getFirstname() != null)
			credential.setDisplayFirstname(user.getFirstname());

		if (user.getLastname() != null)
			credential.setDisplayLastname(user.getLastname());

		if (user.getEmail() != null)
			credential.setEmail(user.getEmail());

		if (user.getAdmin() != null) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if (authentication != null &&
					authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"))) {
				credential.setIsAdmin(user.getAdmin());
			}
		}

		if (user.getDesigner() != null)
			credential.setIsDesigner(user.getDesigner());

		if (user.getActive() != null)
			credential.setActive(user.getActive());

		if (user.getOther() != null)
			credential.setOther(user.getOther());

		credentialRepository.save(credential);

		if (user.getSubstitute() != null) {
			/// FIXME: More complete rule to use
			CredentialSubstitutionId csId = new CredentialSubstitutionId();
			// id=0, don't check who this person can substitute (except root)
			csId.setId(0L);
			csId.setCredential(credential);
			csId.setType("USER");

			if (user.getSubstitute() == 1) {
				CredentialSubstitution subst = new CredentialSubstitution();
				subst.setId(csId);

				credentialSubstitutionRepository.save(subst);
			} else {
				credentialSubstitutionRepository.deleteById(csId);
			}
		}

		return forUserId;
	}

	@Override
	public boolean isAdmin(Long userId) {
		return credentialRepository.isAdmin(userId);
	}

	@Override
	public boolean isCreator(Long userId) {
		return credentialRepository.isCreator(userId);
	}

	@Override
	public Long changeUserInfo(Long byUserId, Long forUserId, CredentialDocument user) throws BusinessException {
		if (!byUserId.equals(forUserId))
			throw new GenericBusinessException("Not authorized");

		Credential credential = credentialRepository.findById(forUserId)
				.orElseThrow(() -> new GenericBusinessException("Unknown user id"));

		if (!passwordEncoder.matches(user.getPrevpass(), credential.getPassword())) {
			throw new GenericBusinessException("Password is not correct.");
		}

		setPassword(user.getPassword(), credential);

		if (!user.getPassword().equals(user.getPrevpass())) {
			log.info(String.format("User with id  [%s] has changed his password\n", forUserId));
		}

		if (user.getEmail() != null)
			credential.setEmail(user.getEmail());

		if (user.getFirstname() != null)
			credential.setDisplayFirstname(user.getFirstname());

		if (user.getLastname() != null)
			credential.setDisplayLastname(user.getLastname());

		credentialRepository.save(credential);

		return forUserId;
	}

	@Override
	public Long addRole(UUID portfolioId, String role, Long userId) throws BusinessException {
		Node rootNode = portfolioRepository.getPortfolioRootNode(portfolioId);

		if (!credentialRepository.isAdmin(userId)
				&& !credentialRepository.isDesigner(userId, rootNode.getId())
				&& !credentialRepository.isCreator(userId))
			throw new GenericBusinessException("No admin right");

		GroupRightInfo gri = groupRightInfoRepository.getByPortfolioAndLabel(portfolioId, role);

		if (gri != null) {
			return gri.getGroupInfo().getId();
		} else {
			GroupRightInfo ngri = new GroupRightInfo(new Portfolio(portfolioId), role);
			groupRightInfoRepository.save(ngri);

			GroupInfo gi = new GroupInfo(ngri.getId(), 1L, role);
			groupInfoRepository.save(gi);

			return gi.getId();
		}
	}

	@Override
	@IsAdmin
	public void addUserToGroup(Long forUser, Long groupId) {
		GroupUserId gid = new GroupUserId();
		gid.setCredential(new Credential(forUser));
		gid.setGroupInfo(new GroupInfo(groupId));

		if (!groupUserRepository.existsById(gid)) {
			groupUserRepository.save(new GroupUser(gid));
		}
	}

	@Override
	public boolean addUserInCredentialGroups(Long userId, List<Long> credentialGroupIds) {
		for (Long credentialGroupId : credentialGroupIds) {
			CredentialGroupMembers cgm = new CredentialGroupMembers(
					new CredentialGroupMembersId(new CredentialGroup(credentialGroupId), new Credential(userId)));
			credentialGroupMembersRepository.save(cgm);
		}

		return true;
	}

	@Override
	@IsAdmin
	public String addUserRole(Long rrgid, Long forUser) {
		// Vérifie si un group_info/grid existe
		GroupInfo gi = groupInfoRepository.getGroupByGrid(rrgid);

		if (gi == null) {
			// Copie de RRG vers group_info
			GroupRightInfo gri = groupRightInfoRepository.findById(rrgid).get();

			gi = new GroupInfo(gri, gri.getOwner(), gri.getLabel());
			groupInfoRepository.save(gi);
		}

		// Ajout de l'utilisateur
		addUserToGroup(forUser, gi.getId());
		return "user " + forUser + " rajoute au groupd gid " + gi.getId() + " pour correspondre au groupRight grid "
				+ rrgid;
	}

	@Override
	public CredentialDocument login(LoginDocument credentials) {
		Credential user = credentialRepository.findByLogin(credentials.getLogin());

		if (user == null)
			return null;
		else if (!passwordEncoder.matches(credentials.getPassword(), user.getPassword()))
			return null;

		Credential credential = user;
		String substitute = credentials.getSubstitute();

		if (substitute != null &&
				credentialSubstitutionRepository.getFor(user.getId(), "USER") != null) {
			// User can get "any" account, except admin one
			credential = credentialRepository.findByLoginAndAdmin(substitute, 0);
		}

		// Apply the login as per se
		login(credential);

		return new CredentialDocument(credential, true);
	}

	@Override
	public void login(Credential credential) {
		UserInfo userInfo = new UserInfo(credential);

		SecurityContext context = SecurityContextHolder.createEmptyContext();
		Authentication authentication = new UsernamePasswordAuthenticationToken(
				userInfo, credential.getPassword(), userInfo.getAuthorities());
		context.setAuthentication(authentication);

		SecurityContextHolder.setContext(context);
	}

	@Override
	public boolean userHasRole(long userId, long roleId) {
		return groupUserRepository.hasRole(userId, roleId);
	}

	@Override
	@IsAdmin
	public void removeRole(Long groupRightInfoId) {
		groupRightInfoRepository.deleteById(groupRightInfoId);
	}

	@Override
	@IsAdmin
	public void removeUserRole(Long userId, Long groupRightInfoId) {
		groupUserRepository.delete(groupUserRepository.getByUserAndRole(userId, groupRightInfoId));
	}

	@Override
	@IsAdmin
	public void removeUsersFromRole(UUID portfolioId) {
		groupUserRepository.deleteByPortfolio(portfolioId);
	}

	@Override
	@IsAdmin
	public Long changeRole(Long rrgId, RoleDocument role) {
		GroupRightInfo gri = groupRightInfoRepository.findById(rrgId).get();

		if (role.getLabel() != null) {
			gri.setLabel(role.getLabel());
		}

		if (role.getPortfolioId() != null) {
			gri.setPortfolio(new Portfolio(role.getPortfolioId()));
		}

		groupRightInfoRepository.save(gri);

		return gri.getId();
	}

	@Override
	@IsAdmin
	public String addUsersToRole(Long rrgid, CredentialList users) {
		for (CredentialDocument user : users.getUsers()) {
			addUserRole(rrgid, user.getId());
		}
		return null;
	}

	@Override
	public void deleteUserFromCredentialGroup(Long userId, Long credentialGroupId) {
		credentialGroupMembersRepository.deleteUserFromGroup(credentialGroupId, userId);
	}

}
