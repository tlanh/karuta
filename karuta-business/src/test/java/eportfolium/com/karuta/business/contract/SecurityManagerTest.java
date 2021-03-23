package eportfolium.com.karuta.business.contract;

import eportfolium.com.karuta.business.ServiceTest;
import eportfolium.com.karuta.business.security.test.AsAdmin;
import eportfolium.com.karuta.consumer.repositories.*;
import eportfolium.com.karuta.document.CredentialDocument;
import eportfolium.com.karuta.document.CredentialList;
import eportfolium.com.karuta.document.LoginDocument;
import eportfolium.com.karuta.document.RoleDocument;
import eportfolium.com.karuta.model.bean.*;
import eportfolium.com.karuta.model.exception.BusinessException;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ServiceTest
public class SecurityManagerTest {
    @MockBean
    private CredentialRepository credentialRepository;

    @MockBean
    private GroupUserRepository groupUserRepository;

    @MockBean
    private CredentialGroupMembersRepository credentialGroupMembersRepository;

    @MockBean
    private CredentialSubstitutionRepository credentialSubstitutionRepository;

    @MockBean
    private GroupRightInfoRepository groupRightInfoRepository;

    @MockBean
    private PortfolioRepository portfolioRepository;

    @MockBean
    private GroupInfoRepository groupInfoRepository;

    @SpyBean
    private SecurityManager manager;

    @MockBean
    private EmailManager emailManager;

    private final PasswordEncoder passwordEncoder = new Pbkdf2PasswordEncoder();

    // It is important to clean-up the context *after* the test executes, otherwise
    // the `@WithMockUser`-like annotations (e.g. `@AsAdmin`) are no-op.
    @After
    public void setup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void changePassword() {
        String login = "jdoe";
        Credential credential = new Credential();

        doReturn(credential)
                .when(credentialRepository)
                .findByLogin(login);

        // With an empty password.
        assertFalse(manager.changePassword(login, ""));
        assertNull(credential.getPassword());
        verify(credentialRepository, times(0)).save(credential);

        // With a valid password.
        assertTrue(manager.changePassword(login, "s3cr3t"));
        assertNotNull(credential.getPassword());
        verify(credentialRepository).save(credential);
    }

    @Test
    public void generatePassword() {
        String password = manager.generatePassword();

        assertEquals(12, password.length());
    }

    @Test
    @AsAdmin
    public void removeUsers() {
        Long userId = 42L;

        doReturn(Collections.emptyList())
                .when(groupUserRepository)
                .getByUser(userId);

        manager.removeUsers(userId);

        verify(groupUserRepository).getByUser(userId);
        verify(groupUserRepository).deleteAll(Collections.emptyList());
        verify(credentialRepository).deleteById(userId);
    }

    @Test
    @AsAdmin
    public void addUsers() {
    	
        Credential credential = new Credential();
        credential.setLogin("jdoe");
        
        // Configure how save behave, otherwise it returns null
        when(credentialRepository.save(any(Credential.class))).thenReturn(credential);

        CredentialDocument document1 = new CredentialDocument(credential);
        CredentialDocument document2 = new CredentialDocument();

        document1.setPassword("foobarbaz");

        CredentialList list = new CredentialList(Arrays.asList(document1, document2));
        CredentialList processed = manager.addUsers(list);

        assertEquals(1, processed.getUsers().size());
        assertEquals("jdoe", processed.getUsers().get(0).getUsername());

        verify(credentialRepository, times(1))
                .save(any(Credential.class));
    }

    @Test
    public void changeUser_WithUnexistingUser() {
        Long byUserId = 0L;
        Long forUserId = 42L;

        doReturn(Optional.empty())
                .when(credentialRepository)
                .findById(forUserId);

        CredentialDocument credentialDocument = new CredentialDocument();

        try {
            manager.changeUser(byUserId, forUserId, credentialDocument);
            fail("Unexisting user can't be changed.");
        } catch (BusinessException ignored) { }
    }

    @Test
    public void changeUser_WithWrongPassword() {
        Long byUserId = 41L;
        Long forUserId = 42L;

        String rawPassword = "s3cr3t";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Credential credential = new Credential();
        credential.setPassword(encodedPassword);

        doReturn(Optional.of(credential))
                .when(credentialRepository)
                .findById(forUserId);

        CredentialDocument credentialDocument = new CredentialDocument();
        credentialDocument.setPrevpass("wrongpassword");

        try {
            manager.changeUser(byUserId, forUserId, credentialDocument);
            fail("Giving a wrong password for editing a user must fail.");
        } catch (BusinessException ignored) { }
    }

    @Test
    public void changeUser_WithRightPassword() throws BusinessException {
        Long byUserId = 41L;
        Long forUserId = 42L;

        String rawPassword = "s3cr3t";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Credential credential = new Credential();
        credential.setPassword(encodedPassword);

        doReturn(Optional.of(credential))
                .when(credentialRepository)
                .findById(forUserId);

        CredentialDocument credentialDocument = mock(CredentialDocument.class);

        when(credentialDocument.getUsername()).thenReturn("jdoe");
        when(credentialDocument.getPrevpass()).thenReturn(rawPassword);

        manager.changeUser(byUserId, forUserId, credentialDocument);

        assertEquals(credentialDocument.getUsername(), credential.getLogin());

        verify(credentialRepository).save(credential);
    }

    @Test
    public void changeUser_BeingAdmin() throws BusinessException {
        Long byUserId = 41L;
        Long forUserId = 42L;

        doReturn(true)
                .when(credentialRepository)
                .isAdmin(byUserId);

        Credential credential = new Credential();

        doReturn(Optional.of(credential))
                .when(credentialRepository)
                .findById(forUserId);

        CredentialDocument credentialDocument = mock(CredentialDocument.class);
        when(credentialDocument.getUsername()).thenReturn("jdoe");

        manager.changeUser(byUserId, forUserId, credentialDocument);

        assertEquals(credentialDocument.getUsername(), credential.getLogin());

        verify(credentialRepository).save(credential);
    }

    @Test
    public void changeUser_AdminAttribute_WithoutAdminRole() throws BusinessException {
        Long byUserId = 42L;
        Long forUserId = 45L;

        String password = "s3cr3t";
        String encodedPassword = passwordEncoder.encode(password);

        Credential credential = new Credential();
        credential.setPassword(encodedPassword);

        doReturn(Optional.of(credential))
                .when(credentialRepository)
                .findById(forUserId);

        Collection<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_DESIGNER"));
        Authentication authentication = mock(Authentication.class);

        doReturn(authorities)
                .when(authentication)
                .getAuthorities();

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);

        CredentialDocument document = mock(CredentialDocument.class);
        when(document.getAdmin()).thenReturn(1);
        when(document.getPrevpass()).thenReturn(password);

        manager.changeUser(byUserId, forUserId, document);

        assertEquals(0, credential.getIsAdmin());

        verify(credentialRepository).findById(forUserId);
        verify(credentialRepository).save(credential);
    }

    @Test
    public void changeUser_AdminAttribute_WithAdminRole() throws BusinessException {
        Long byUserId = 42L;
        Long forUserId = 45L;

        Credential credential = new Credential();

        doReturn(Optional.of(credential))
                .when(credentialRepository)
                .findById(forUserId);

        doReturn(true)
                .when(credentialRepository)
                .isAdmin(byUserId);

        Collection<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"));
        Authentication authentication = mock(Authentication.class);

        doReturn(authorities)
                .when(authentication)
                .getAuthorities();

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);

        CredentialDocument document = mock(CredentialDocument.class);
        when(document.getAdmin()).thenReturn(1);

        manager.changeUser(byUserId, forUserId, document);

        assertEquals(1, credential.getIsAdmin());

        verify(credentialRepository).findById(forUserId);
        verify(credentialRepository).save(credential);
    }

    @Test
    public void isAdmin() {
        Long userId = 42L;

        doReturn(true)
                .when(credentialRepository)
                .isAdmin(userId);

        assertTrue(manager.isAdmin(userId));

        doReturn(false)
                .when(credentialRepository)
                .isAdmin(userId);

        assertFalse(manager.isAdmin(userId));
    }

    @Test
    public void isCreator() {
        Long userId = 42L;

        doReturn(true)
                .when(credentialRepository)
                .isCreator(userId);

        assertTrue(manager.isCreator(userId));

        doReturn(false)
                .when(credentialRepository)
                .isCreator(userId);

        assertFalse(manager.isCreator(userId));
    }

    @Test
    public void changeUserInfo() throws BusinessException {
        Long userId = 42L;

        String rawPassword = "foobarbaz";
        String newPassword = "s3cr3t";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Credential credential = new Credential();
        credential.setPassword(encodedPassword);

        CredentialDocument document = mock(CredentialDocument.class);

        when(document.getEmail()).thenReturn("foo@bar.com");
        when(document.getFirstname()).thenReturn("John");
        when(document.getLastname()).thenReturn("Doe");
        when(document.getPrevpass()).thenReturn(rawPassword);
        when(document.getPassword()).thenReturn(newPassword);

        doReturn(Optional.of(credential))
                .when(credentialRepository)
                .findById(userId);

        manager.changeUserInfo(userId, userId, document);

        assertEquals(document.getEmail(), credential.getEmail());
        assertEquals(document.getFirstname(), credential.getDisplayFirstname());
        assertEquals(document.getLastname(), credential.getDisplayLastname());

        assertTrue(passwordEncoder.matches(newPassword, credential.getPassword()));

        verify(credentialRepository).save(credential);
    }

    @Test
    public void changeUserInfo_WithWrongId() {
        Long byId = 122L;
        Long forId = 42L;

        try {
            manager.changeUserInfo(byId, forId, null);
            fail("A user should not be able to edit another one's info");
        } catch (BusinessException ignored) { }
    }

    @Test
    public void changeUserInfo_WithWrongPassword() {
        Long userId = 42L;

        String rawPassword = "s3cr3t";

        Credential credential = new Credential();
        credential.setPassword(passwordEncoder.encode(rawPassword));

        CredentialDocument document = mock(CredentialDocument.class);
        when(document.getPrevpass()).thenReturn("wrongpassword");

        try {
            manager.changeUserInfo(userId, userId, document);
            fail("Giving a wrong password should throw an error.");
        } catch (BusinessException ignored) { }
    }

    @Test
    public void addRole() throws BusinessException {
        UUID portfolioId = UUID.randomUUID();
        String label = "designer";
        Long userId = 42L;
        Long groupInfoId = 345L;

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setId(groupInfoId);

        groupRightInfo.setGroupInfo(groupInfo);

        doReturn(true)
                .when(credentialRepository)
                .isAdmin(userId);

        doReturn(groupRightInfo)
                .when(groupRightInfoRepository)
                .getByPortfolioAndLabel(portfolioId, label);

        manager.addRole(portfolioId, label, userId);

        verify(groupRightInfoRepository).getByPortfolioAndLabel(portfolioId, label);
        verifyNoMoreInteractions(groupRightInfoRepository);

        verifyNoInteractions(groupInfoRepository);
    }

    @Test
    public void addRole_WithUnexistingGroup() throws BusinessException {
        UUID portfolioId = UUID.randomUUID();
        String label = "designer";
        Long userId = 42L;

        doReturn(true)
                .when(credentialRepository)
                .isAdmin(userId);

        doReturn(null)
                .when(groupRightInfoRepository)
                .getByPortfolioAndLabel(portfolioId, label);

        manager.addRole(portfolioId, label, userId);

        verify(groupRightInfoRepository, times(2)).save(any(GroupRightInfo.class));
        verify(groupInfoRepository).save(any(GroupInfo.class));
    }

    @Test
    @AsAdmin
    public void addUserToGroup() {
        Long userId = 42L;
        Long groupId = 74L;

        GroupUserId gid = new GroupUserId();
        gid.setCredential(new Credential(userId));
        gid.setGroupInfo(new GroupInfo(groupId));

        doReturn(true)
                .when(groupUserRepository)
                .existsById(gid);

        manager.addUserToGroup(userId, groupId);

        verify(groupUserRepository, times(0))
                .save(any(GroupUser.class));

        doReturn(false)
                .when(groupUserRepository)
                .existsById(gid);

        manager.addUserToGroup(userId, groupId);

        verify(groupUserRepository)
                .save(any(GroupUser.class));
    }

    @Test
    public void addUserInCredentialGroups() {
        Long userId = 42L;
        List<Long> groupIds = Arrays.asList(12L, 74L);

        manager.addUserInCredentialGroups(userId, groupIds);

        groupIds.forEach(id -> {
            CredentialGroupMembersId cgmId = new CredentialGroupMembersId();
            cgmId.setCredential(new Credential(userId));
            cgmId.setCredentialGroup(new CredentialGroup(id));

            CredentialGroupMembers cgm = new CredentialGroupMembers(cgmId);

            verify(credentialGroupMembersRepository).save(cgm);
        });
    }

    @Test
    @AsAdmin
    public void addUserRole_WithUnexistingGroupInfo() {
        Long groupId = 89L;
        Long userId = 42L;

        GroupRightInfo groupRightInfo = mock(GroupRightInfo.class);

        doReturn(null)
                .when(groupInfoRepository)
                .getGroupByGrid(groupId);

        doReturn(Optional.of(groupRightInfo))
                .when(groupRightInfoRepository)
                .findById(groupId);

        manager.addUserRole(groupId, userId);

        verify(groupInfoRepository).getGroupByGrid(groupId);
        verify(groupRightInfoRepository).findById(groupId);
        verify(groupInfoRepository).save(any(GroupInfo.class));
        verify(manager).addUserToGroup(userId, null);
    }

    @Test
    public void login_WithUnexistingUser() {
        String login = "jdoe";

        doReturn(null)
                .when(credentialRepository)
                .findByLogin(login);

        LoginDocument loginDocument = new LoginDocument();
        loginDocument.setLogin(login);

        assertNull(manager.login(loginDocument));
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(credentialRepository).findByLogin(login);
        verifyNoMoreInteractions(credentialRepository);
    }

    @Test
    public void login_WithWrongPassword() {
        String login = "jdoe";
        String rawPassword = "s3cr3t";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Credential credential = new Credential();
        credential.setPassword(encodedPassword);

        LoginDocument loginDocument = new LoginDocument();
        loginDocument.setLogin(login);
        loginDocument.setPassword("foobarbaz");

        doReturn(credential)
                .when(credentialRepository)
                .findByLogin(login);

        assertNull(manager.login(loginDocument));
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(credentialRepository).findByLogin(login);
        verifyNoMoreInteractions(credentialRepository);
    }

    @Test
    public void login() {
        String login = "jdoe";
        String rawPassword = "s3cr3t";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Credential credential = new Credential();
        credential.setLogin(login);
        credential.setPassword(encodedPassword);

        LoginDocument loginDocument = new LoginDocument();
        loginDocument.setLogin(login);
        loginDocument.setPassword(rawPassword);

        doReturn(credential)
                .when(credentialRepository)
                .findByLogin(login);

        CredentialDocument credentialDocument = manager.login(loginDocument);

        assertEquals(login, credentialDocument.getUsername());
        assertEquals(login, SecurityContextHolder.getContext().getAuthentication().getName());

        verify(credentialRepository).findByLogin(login);
        verifyNoMoreInteractions(credentialRepository);
    }

    @Test
    public void login_WithSubstitutionWhileNotAuthorized() {
        String principal = "jdoe";
        String substitute = "other";
        String login = String.format("%s#%s", principal, substitute);

        String rawPassword = "s3cr3t";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Credential credential = new Credential();
        credential.setId(42L);
        credential.setLogin(principal);
        credential.setPassword(encodedPassword);

        LoginDocument loginDocument = new LoginDocument();
        loginDocument.setLogin(login);
        loginDocument.setPassword(rawPassword);

        doReturn(credential)
                .when(credentialRepository)
                .findByLogin(principal);

        CredentialDocument credentialDocument = manager.login(loginDocument);

        assertEquals(principal, credentialDocument.getUsername());
        assertEquals(principal, SecurityContextHolder.getContext().getAuthentication().getName());

        verify(credentialRepository).findByLogin(principal);
        verifyNoMoreInteractions(credentialRepository);

        verify(credentialSubstitutionRepository).getFor(credential.getId(), "USER");
    }

    @Test
    public void login_WithSubstitution() {
        String principal = "jdoe";
        String substitute = "other";
        String login = String.format("%s#%s", principal, substitute);

        String rawPassword = "s3cr3t";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Credential principalCredential = new Credential();
        principalCredential.setId(42L);
        principalCredential.setPassword(encodedPassword);

        Credential substituteCredential = new Credential();
        substituteCredential.setLogin(substitute);

        LoginDocument loginDocument = new LoginDocument();
        loginDocument.setLogin(login);
        loginDocument.setPassword(rawPassword);

        doReturn(principalCredential)
                .when(credentialRepository)
                .findByLogin(principal);

        doReturn(new CredentialSubstitution())
                .when(credentialSubstitutionRepository)
                .getFor(principalCredential.getId(), "USER");

        doReturn(substituteCredential)
                .when(credentialRepository)
                .findByLoginAndAdmin(substitute, 0);

        CredentialDocument credentialDocument = manager.login(loginDocument);

        assertEquals(substitute, credentialDocument.getUsername());
        assertEquals(substitute, SecurityContextHolder.getContext().getAuthentication().getName());

        verify(credentialRepository).findByLogin(principal);
        verify(credentialRepository).findByLoginAndAdmin(substitute, 0);
        verifyNoMoreInteractions(credentialRepository);

        verify(credentialSubstitutionRepository).getFor(principalCredential.getId(), "USER");
    }

    @Test
    public void userHasRole() {
        long userId = 42L;
        long roleId = 98L;

        doReturn(true)
                .when(groupUserRepository)
                .hasRole(userId, roleId);

        assertTrue(manager.userHasRole(userId, roleId));

        doReturn(false)
                .when(groupUserRepository)
                .hasRole(userId, roleId);

        assertFalse(manager.userHasRole(userId, roleId));
    }

    @Test
    @AsAdmin
    public void removeRole() {
        Long roleId = 79L;

        manager.removeRole(roleId);

        verify(groupRightInfoRepository).deleteById(roleId);
    }

    @Test
    @AsAdmin
    public void removeUserRole() {
        Long userId = 42L;
        Long roleId = 37L;

        GroupUser groupUser = new GroupUser();

        doReturn(groupUser)
                .when(groupUserRepository)
                .getByUserAndRole(userId, roleId);

        manager.removeUserRole(userId, roleId);

        verify(groupUserRepository).getByUserAndRole(userId, roleId);
        verify(groupUserRepository).delete(groupUser);
    }

    @Test
    @AsAdmin
    public void removeUsersFromRole() {
        UUID portfolioId = UUID.randomUUID();

        manager.removeUsersFromRole(portfolioId);

        verify(groupUserRepository).deleteByPortfolio(portfolioId);
    }

    @Test
    @AsAdmin
    public void changeRole() {
        UUID portfolioId = UUID.randomUUID();
        String label = "foo";

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setLabel(label);
        groupRightInfo.setPortfolio(new Portfolio(portfolioId));

        Long groupId = 98L;
        RoleDocument document = new RoleDocument(groupRightInfo);

        GroupRightInfo returned = mock(GroupRightInfo.class);

        doReturn(groupId)
                .when(returned)
                .getId();

        doReturn(Optional.of(returned))
                .when(groupRightInfoRepository)
                .findById(groupId);

        Long returnedId = manager.changeRole(groupId, document);

        assertEquals(groupId, returnedId);

        verify(groupRightInfoRepository).findById(groupId);

        verify(returned).setLabel(label);
        verify(returned).setPortfolio(new Portfolio(portfolioId));

        verify(groupRightInfoRepository).save(returned);
    }

    @Test
    @AsAdmin
    public void addUsersToRole() {
        Long groupId = 74L;
        Long user1Id = 42L;
        long user2Id = 43L;

        doReturn("")
                .when(manager)
                .addUserRole(eq(groupId), anyLong());

        Credential credential1 = new Credential();
        Credential credential2 = new Credential();

        credential1.setId(user1Id);
        credential2.setId(user2Id);

        CredentialDocument document1 = new CredentialDocument(credential1);
        CredentialDocument document2 = new CredentialDocument(credential2);

        CredentialList list = new CredentialList(Arrays.asList(document1, document2));

        manager.addUsersToRole(groupId, list);

        verify(manager, times(1)).addUserRole(groupId, user1Id);
        verify(manager, times(1)).addUserRole(groupId, user2Id);
    }

    @Test
    public void deleteUserFromCredentialGroup() {
        Long userId = 42L;
        Long groupId = 74L;

        manager.deleteUserFromCredentialGroup(userId, groupId);

        verify(credentialGroupMembersRepository, times(1))
                .deleteUserFromGroup(groupId, userId);
    }
}
