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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import eportfolium.com.karuta.business.UserInfo;
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
import eportfolium.com.karuta.model.exception.AuthenticationException;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.GenericBusinessException;
import eportfolium.com.karuta.model.exception.ValueRequiredException;

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

	/**
	 * Each token produced by this class uses this identifier as a prefix.
	 */
	public static final String ID = "$31$";

	/**
	 * The minimum recommended cost, used by default
	 */
	public static final int DEFAULT_COST = 16;

	private static final String ALGORITHM = "PBKDF2WithHmacSHA512";

	/**
	 * A keyLength of 256 would be safer :).
	 */
	private static final int SIZE = 128;

	private static final Pattern layout = Pattern.compile("\\$31\\$(\\d\\d?)\\$(.{43})");

	private final SecureRandom random;

	private final int cost;

	private static final Logger log = LoggerFactory.getLogger(SecurityManagerImpl.class);

	public SecurityManagerImpl() {
		this(DEFAULT_COST);
	}

	/**
	 * Create a SecurityManager with a specified cost
	 * 
	 * @param cost the exponential computational cost of hashing a password, 0 to 30
	 */
	private SecurityManagerImpl(int cost) {
		iterations(cost); /* Validate cost */
		this.cost = cost;
		this.random = new SecureRandom();
	}

	/**
	 * The token generated is stored at the server, and should be associated with
	 * the user identity. For example, a user table with id, login name and/or email
	 * address, and token. When someone logs in with login and password, lookup the
	 * stored token with the login and pass that to the authenticate() method with
	 * the password.
	 */
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

	/**
	 * This method provides a way for users to change their own userPassword.
	 * 
	 * @param userId
	 * @param currentPassword
	 * @param newPassword
	 * @throws BusinessException
	 */
	public void changeUserPassword(Long userId, String currentPassword, String newPassword) throws BusinessException {
		Credential user = credentialRepository
							.findById(userId)
							.orElse(new Credential());

		if (!authenticate(currentPassword.toCharArray(), user.getPassword())) {
			throw new AuthenticationException("User_password_incorrect");
		}

		if (user.getPassword() != null && authenticate(newPassword.toCharArray(), user.getPassword())) {
			throw new GenericBusinessException("User_newpassword_is_same");
		}

		setPassword(newPassword, user);
		credentialRepository.save(user);
	}

	public String generatePassword() {
		List<CharacterRule> rules = Arrays.asList(
				// at least one upper-case character
				new CharacterRule(EnglishCharacterData.UpperCase, 1),

				// at least one lower-case character
				new CharacterRule(EnglishCharacterData.LowerCase, 1),

				// at least one digit character
				new CharacterRule(EnglishCharacterData.Digit, 1),

				// at least one symbol (special character)
				new CharacterRule(EnglishCharacterData.Special, 1));

		PasswordGenerator generator = new PasswordGenerator();

		// Generated password is 12 characters long, which complies with policy
		String password = generator.generatePassword(12, rules);
		return password;
	}

	/**
	 * Do all tests of userPassword size, content, history, etc. here...
	 * 
	 * @param newPassword
	 * @param credential
	 */
	private void setPassword(String newPassword, Credential credential) throws BusinessException {
		if (StringUtils.isEmpty(newPassword)) {
			throw new ValueRequiredException(credential, "User_newpassword_is_required");
		}

		credential.setPassword(hash(newPassword.toCharArray()));
	}

	private static int iterations(int cost) {
		if ((cost < 0) || (cost > 31))
			throw new IllegalArgumentException("cost: " + cost);
		return 1 << cost;
	}

	/**
	 * Hash a password for storage. *
	 * 
	 * <p>
	 * Passwords should be stored in a {@code char[]} so that it can be filled with
	 * zeros after use instead of lingering on the heap and elsewhere.
	 * 
	 * @return a secure authentication token to be stored for later authentication
	 */
	private String hash(char[] password) {
		byte[] salt = new byte[SIZE / 8];
		random.nextBytes(salt);
		byte[] dk = pbkdf2(password, salt, 1 << cost);
		byte[] hash = new byte[salt.length + dk.length];
		System.arraycopy(salt, 0, hash, 0, salt.length);
		System.arraycopy(dk, 0, hash, salt.length, dk.length);
		Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding();
		return ID + cost + '$' + enc.encodeToString(hash);
	}

	/**
	 * Authenticate with a password and a stored password token.
	 * 
	 * @return true if the password and token match
	 */
	private boolean authenticate(char[] password, String token) {
		Matcher m = layout.matcher(token);
		if (!m.matches())
			throw new IllegalArgumentException("Invalid token format");
		int iterations = iterations(Integer.parseInt(m.group(1)));
		byte[] hash = Base64.getUrlDecoder().decode(m.group(2));
		byte[] salt = Arrays.copyOfRange(hash, 0, SIZE / 8);
		byte[] check = pbkdf2(password, salt, iterations);
		int zero = 0;
		for (int idx = 0; idx < check.length; ++idx)
			zero |= hash[salt.length + idx] ^ check[idx];
		return zero == 0;
	}

	private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
		KeySpec spec = new PBEKeySpec(password, salt, iterations, SIZE);
		try {
			SecretKeyFactory f = SecretKeyFactory.getInstance(ALGORITHM);
			return f.generateSecret(spec).getEncoded();
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("Missing algorithm: " + ALGORITHM, ex);
		} catch (InvalidKeySpecException ex) {
			throw new IllegalStateException("Invalid SecretKeyFactory", ex);
		}
	}

	@Override
	@PreAuthorize("hasRole('admin') or principal.id == #forUser")
	public void removeUsers(Long forUser) {
		groupUserRepository.deleteAll(groupUserRepository.getByUser(forUser));
		credentialRepository.deleteById(forUser);
	}

	@Override
	@PreAuthorize("hasRole('admin') or hasRole('designer')")
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
			credential.setPassword(hash(credential.getPassword().toCharArray()));

			credential.setLogin(document.getUsername());
			credential.setOther(document.getOther());
			credential.setEmail(document.getEmail());
			credential.setDisplayFirstname(document.getFirstname());
			credential.setDisplayLastname(document.getLastname());

			credentialRepository.save(credential);

			/// FIXME: More complete rule to use
			CredentialSubstitutionId csId = new CredentialSubstitutionId();
			// id = 0, ne pas vérifier qui cette personne peut remplacer (sauf root)
			csId.setId(0L);
			csId.setCredential(credential);
			csId.setType("USER");

			if (credential.getCanSubstitute() == 1) {
				CredentialSubstitution subst = new CredentialSubstitution();
				subst.setId(csId);

				credentialSubstitutionRepository.save(subst);
				credential.setCredentialSubstitution(subst);
			} else {
				credentialSubstitutionRepository.deleteById(csId);
			}

			processed.add(document);
		}

		return new CredentialList(processed);
	}

	@Override
	public boolean addUser(String username, String email) {
		if (!credentialRepository.existsByLogin(username)) {

			try {
				Credential newUser = new Credential();
				String passwd = generatePassword();
				/// Credential checking use hashing, we'll never reach this.
				setPassword(passwd, newUser);
				newUser.setLogin(username);
				newUser.setEmail(email);
				newUser.setActive(1);
				newUser.setDisplayFirstname("");
				newUser.setOther("");
				newUser.setDisplayLastname("");
				newUser.setIsDesigner(1);

				credentialRepository.save(newUser);

				final Map<String, String> locals = new HashMap<>();

				locals.put("firstname", username);
				locals.put("lastname", "");
				locals.put("email", email);
				locals.put("passwd", passwd);

				try {
					// Envoie d'un e-mail à l'utilisateur
					emailManager.send("account", "Welcome!", locals, email, username);
				} catch (Exception e) {
					e.printStackTrace();
				}

				return true;
			} catch (BusinessException e) {
				e.printStackTrace();

				return false;
			}
		}

		return false;
	}

	@Override
	public Long changeUser(Long byUserId, Long forUserId, CredentialDocument user) throws BusinessException {
		if (!checkPassword(byUserId, user.getPrevpass()) && !credentialRepository.isAdmin(byUserId)) {
			throw new GenericBusinessException("Not authorized");
		}

		Credential credential = credentialRepository.findById(forUserId)
									.orElseThrow(() -> new GenericBusinessException("Unexisting user"));

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

		if (user.getAdmin() != null)
			credential.setIsAdmin(user.getAdmin());

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

	/**
	 * Check if user password is the right one
	 *
	 * @param passwd Password
	 * @return bool result
	 */
	public boolean checkPassword(Long userId, String passwd) {
		if (userId == null || passwd.length() < CredentialRepository.PASSWORD_LENGTH) {
			log.error("Fatal Error : illegal checkPassword parameters");
			throw new RuntimeException();
		}
		Credential cr = credentialRepository.findActiveById(userId);
		return cr != null ? authenticate(passwd.toCharArray(), cr.getPassword()) : false;
	}

	@Override
	public Long changeUserInfo(Long byUserId, Long forUserId, CredentialDocument user) throws BusinessException {
		if (byUserId != forUserId)
			throw new GenericBusinessException("Not authorized");

		Credential credential = credentialRepository.findById(forUserId)
				.orElseThrow(() -> new GenericBusinessException("Unknown user id"));

		try {
			changeUserPassword(forUserId, user.getPrevpass(), user.getPassword());
			log.info(String.format("User with id  [%s] has changed his password\n", forUserId));
		} catch (BusinessException e) {
			// L'utilisation du même mot de passe dans cette méthode n'est pas interdite
			// donc on continue.
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
		Long groupId = 0L;
		Node rootNode = portfolioRepository.getPortfolioRootNode(portfolioId);

		if (!credentialRepository.isAdmin(userId)
				&& !credentialRepository.isDesigner(userId, rootNode.getId())
				&& !credentialRepository.isCreator(userId))
			throw new GenericBusinessException("No admin right");

		try {
			GroupRightInfo gri = groupRightInfoRepository.getByPortfolioAndLabel(portfolioId, role);

			if (gri != null) {
				groupId = gri.getGroupInfo().getId();
			} else {
				GroupRightInfo ngri = new GroupRightInfo(new Portfolio(portfolioId), role);
				groupRightInfoRepository.save(ngri);

				GroupInfo gi = new GroupInfo(ngri.getId(), 1L, role);
				groupInfoRepository.save(gi);

				groupId = gi.getId();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return groupId;
	}

	@Override
	@PreAuthorize("hasRole('admin')")
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
		boolean added = true;
		CredentialGroupMembers cgm = null;
		try {
			for (Long credentialGroupId : credentialGroupIds) {
				cgm = new CredentialGroupMembers(
						new CredentialGroupMembersId(new CredentialGroup(credentialGroupId), new Credential(userId)));
				credentialGroupMembersRepository.save(cgm);
			}
		} catch (Exception e) {
			added = false;
		}
		return added;
	}

	@Override
	@PreAuthorize("hasRole('admin')")
	public String addUserRole(Long rrgid, Long forUser) {

		// Vérifie si un group_info/grid existe
		GroupInfo gi = groupInfoRepository.getGroupByGrid(rrgid);

		if (gi == null) {
			// Copie de RRG vers group_info
			GroupRightInfo gri = groupRightInfoRepository.findById(rrgid).get();

			groupInfoRepository.save(new GroupInfo(gri, gri.getOwner(), gri.getLabel()));
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
		else if (!authenticate(credentials.getPassword().toCharArray(), user.getPassword()))
			return null;

		Credential credential = user;
		String substitute = credentials.getSubstitute();

		if (substitute != null &&
				credentialSubstitutionRepository.getFor(user.getId(), "USER") != null) {
			// User can get "any" account, except admin one
			credential = credentialRepository.findByLoginAndAdmin(substitute, 0);
		}

		// Apply the login as per se
		UserInfo userInfo = new UserInfo(credential);

		SecurityContext context = SecurityContextHolder.createEmptyContext();
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			userInfo, user.getPassword(), userInfo.getAuthorities());
		context.setAuthentication(authentication);

		SecurityContextHolder.setContext(context);

		return new CredentialDocument(credential, true);
	}

	@Override
	public boolean userHasRole(long userId, long roleId) {
		return groupUserRepository.hasRole(userId, roleId);
	}

	@Override
	@PreAuthorize("hasRole('admin')")
	public void removeRole(Long groupRightInfoId) {
		groupRightInfoRepository.deleteById(groupRightInfoId);
	}

	@Override
	@PreAuthorize("hasRole('admin')")
	public void removeUserRole(Long userId, Long groupRightInfoId) {
		groupUserRepository.delete(groupUserRepository.getByUserAndRole(userId, groupRightInfoId));
	}

	@Override
	@PreAuthorize("hasRole('admin')")
	public void removeUsersFromRole(UUID portfolioId) {
		groupUserRepository.deleteByPortfolio(portfolioId);
	}

	@Override
	@PreAuthorize("hasRole('admin')")
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
	@PreAuthorize("hasRole('admin')")
	public String addUsersToRole(Long rrgid, CredentialList users) {
		for (CredentialDocument user : users.getUsers()) {
			addUserRole(rrgid, user.getId());
		}

		return "";
	}

	@Override
	public void deleteUserFromCredentialGroup(Long userId, Long credentialGroupId) {
		credentialGroupMembersRepository.deleteUserFromGroup(userId, credentialGroupId);
	}

}
