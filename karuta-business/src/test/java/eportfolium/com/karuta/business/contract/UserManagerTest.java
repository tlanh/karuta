package eportfolium.com.karuta.business.contract;

import eportfolium.com.karuta.business.ServiceTest;
import eportfolium.com.karuta.consumer.repositories.CredentialGroupMembersRepository;
import eportfolium.com.karuta.consumer.repositories.CredentialRepository;
import eportfolium.com.karuta.consumer.repositories.GroupRightInfoRepository;
import eportfolium.com.karuta.consumer.repositories.GroupUserRepository;
import eportfolium.com.karuta.document.*;
import eportfolium.com.karuta.model.bean.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.function.Function;

import static org.mockito.Mockito.doReturn;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ServiceTest
public class UserManagerTest {
    @MockBean
    private CredentialRepository credentialRepository;

    @SpyBean
    private UserManager manager;

    @MockBean
    private CredentialGroupMembersRepository credentialGroupMembersRepository;

    @MockBean
    private GroupRightInfoRepository groupRightInfoRepository;

    @MockBean
    private GroupUserRepository groupUserRepository;

    @Test
    public void getUserList() {
        Credential credential = new Credential();

        credential.setId(42L);
        credential.setLogin("johnd");
        credential.setEmail("john@doe.com");
        credential.setIsAdmin(1);

        List<Credential> credentials = Collections.singletonList(credential);

        doReturn(credentials)
                .when(credentialRepository)
                .getUsers("jo", "Jo", "Do");

        CredentialList list = manager.getUserList("jo", "Jo", "Do");
        assertEquals(1, list.getUsers().size());

        CredentialDocument credentialDocument = list.getUsers().get(0);

        assertEquals(credential.getId(), credentialDocument.getId());
        assertEquals(credential.getEmail(), credentialDocument.getEmail());
        assertEquals(Integer.valueOf(credential.getIsAdmin()), credentialDocument.getAdmin());
    }

    @Test
    public void getUsersByCredentialGroup() {
        Long groupId = 78L;

        CredentialGroupMembers cgm1 = new CredentialGroupMembers();
        CredentialGroupMembers cgm2 = new CredentialGroupMembers();

        cgm1.setId(new CredentialGroupMembersId());
        cgm2.setId(new CredentialGroupMembersId());

        Credential credential1 = new Credential();
        Credential credential2 = new Credential();

        credential1.setLogin("foo");
        credential2.setLogin("bar");

        cgm1.setCredential(credential1);
        cgm2.setCredential(credential2);

        doReturn(Arrays.asList(cgm1, cgm2))
                .when(credentialGroupMembersRepository)
                .findByGroup(groupId);

        CredentialGroupDocument document = manager.getUsersByCredentialGroup(groupId);

        assertEquals(groupId, document.getId());
        assertEquals(2, document.getUsers().size());

        assertEquals(credential1.getLogin(), document.getUsers().get(0).getUsername());
        assertEquals(credential2.getLogin(), document.getUsers().get(1).getUsername());
    }

    @Test
    public void getUsersByRole() {
        UUID portfolioId = UUID.randomUUID();
        String role = "designer";

        Credential credential1 = new Credential();
        Credential credential2 = new Credential();

        credential1.setLogin("foo");
        credential2.setLogin("bar");

        doReturn(Arrays.asList(credential1, credential2))
                .when(credentialRepository)
                .getUsersByRole(portfolioId, role);

        CredentialList credentialList = manager.getUsersByRole(portfolioId, role);

        assertEquals(2, credentialList.getUsers().size());

        assertEquals(credential1.getLogin(), credentialList.getUsers().get(0).getUsername());
        assertEquals(credential2.getLogin(), credentialList.getUsers().get(1).getUsername());
    }

    @Test
    public void getRole() {
        Long id = 87L;

        GroupRightInfo groupRightInfo = new GroupRightInfo();

        groupRightInfo.setLabel("foo");
        groupRightInfo.setOwner(12);

        Portfolio portfolio = new Portfolio();
        groupRightInfo.setPortfolio(portfolio);

        doReturn(Optional.of(groupRightInfo))
                .when(groupRightInfoRepository)
                .findById(id);

        RoleDocument document = manager.getRole(id);

        assertEquals(groupRightInfo.getLabel(), document.getLabel());
        assertEquals(Long.valueOf(groupRightInfo.getOwner()), document.getOwner());
    }

    @Test
    public void getUserInfos() {
        Long id = 42L;

        Credential credential = new Credential();

        credential.setLogin("matthew");
        credential.setIsDesigner(1);

        doReturn(credential)
                .when(credentialRepository)
                .getUserInfos(id);

        CredentialDocument document = manager.getUserInfos(id);

        assertEquals("matthew", document.getUsername());
        assertEquals(Integer.valueOf(1), document.getDesigner());
    }

    @Test
    public void getUserId() {
        Long id = 42L;
        String login = "johnny";

        doReturn(id)
                .when(credentialRepository)
                .getIdByLogin(login);

        assertEquals(id, manager.getUserId(login));
    }

    @Test
    public void getUserId_WithLoginAndEmail() {
        Long id = 42L;
        String login = "michel";
        String email = "michel@mail.com";

        doReturn(id)
                .when(credentialRepository)
                .getIdByLoginAndEmail(login, email);

        assertEquals(id, manager.getUserId(login, email));
    }

    @Test
    public void getUserRolesByUserId() {
        Long userId = 42L;

        GroupUser groupUser = new GroupUser();
        GroupInfo groupInfo = new GroupInfo();
        GroupRightInfo groupRightInfo = new GroupRightInfo();

        groupUser.setId(new GroupUserId());
        groupUser.setGroupInfo(groupInfo);
        groupInfo.setGroupRightInfo(groupRightInfo);

        groupInfo.setLabel("foo");

        groupRightInfo.setLabel("bar");

        doReturn(Collections.singletonList(groupUser))
                .when(groupUserRepository)
                .getByUser(userId);

        ProfileList profiles = manager.getUserRolesByUserId(userId);
        assertEquals(1, profiles.getGroups().size());

        GroupInfoDocument document = profiles.getGroups().get(0);

        assertEquals("foo", document.getLabel());
        assertEquals("bar", document.getRole());
    }

    @Test
    public void getEmailByLogin() {
        String email = "foo@bar.com";
        String login = "foo";

        doReturn(email)
                .when(credentialRepository)
                .getEmailByLogin(login);

        assertEquals(email, manager.getEmailByLogin(login));
    }

    @Test
    public void getRoleList() {
        Function<String, GroupRightInfo> newGroup = (label) -> {
            GroupRightInfo groupRightInfo = new GroupRightInfo();

            groupRightInfo.setLabel(label);
            groupRightInfo.setPortfolio(new Portfolio());

            return  groupRightInfo;
        };

        GroupRightInfo first = newGroup.apply("first");
        GroupRightInfo second = newGroup.apply("second");
        GroupRightInfo third = newGroup.apply("third");

        UUID portfolioId = UUID.randomUUID();
        Long userId = 42L;

        doReturn(Collections.singletonList(first))
                .when(groupRightInfoRepository)
                .getByPortfolioID(portfolioId);

        doReturn(Collections.singletonList(second))
                .when(groupRightInfoRepository)
                .getByUser(userId);

        doReturn(Collections.singletonList(third))
                .when(groupRightInfoRepository)
                .findAll();

        // Triggers getByPortfolioID
        RoleRightsGroupList list = manager.getRoleList(portfolioId, null);

        assertEquals(1, list.getGroups().size());
        assertEquals("first", list.getGroups().get(0).getLabel());

        // Triggers getByUser
        list = manager.getRoleList(null, userId);

        assertEquals(1, list.getGroups().size());
        assertEquals("second", list.getGroups().get(0).getLabel());

        // Triggers findAll
        list = manager.getRoleList(null, null);

        assertEquals(1, list.getGroups().size());
        assertEquals("third", list.getGroups().get(0).getLabel());
    }

    @Test
    public void getUserRolesByPortfolio() {
        UUID portfolioId = UUID.randomUUID();
        Long userId = 42L;

        GroupUser groupUser = new GroupUser();
        GroupInfo groupInfo = new GroupInfo();
        GroupRightInfo groupRightInfo = new GroupRightInfo();

        groupUser.setId(new GroupUserId());
        groupUser.setGroupInfo(groupInfo);
        groupUser.setCredential(new Credential());
        groupInfo.setGroupRightInfo(groupRightInfo);
        groupRightInfo.setPortfolio(new Portfolio());

        groupRightInfo.setLabel("foo");

        doReturn(Collections.singletonList(groupUser))
                .when(groupUserRepository)
                .getByPortfolioAndUser(portfolioId, userId);

        GroupUserList groupUserList = manager.getUserRolesByPortfolio(portfolioId, userId);

        assertEquals(portfolioId, groupUserList.getPortfolioId());
        assertEquals(1, groupUserList.getGroups().size());

        GroupUserDocument groupUserDocument = groupUserList.getGroups().get(0);
        assertEquals("foo", groupUserDocument.getLabel());
    }

    @Test
    public void getUserRole() {
        Long grid = 74L;

        Credential credential = new Credential();
        GroupUser groupUser = new GroupUser();
        GroupInfo groupInfo = new GroupInfo();
        GroupRightInfo groupRightInfo = new GroupRightInfo();

        groupUser.setId(new GroupUserId());
        groupUser.setCredential(credential);
        groupUser.setGroupInfo(groupInfo);
        groupInfo.setGroupRightInfo(groupRightInfo);

        groupRightInfo.setPortfolio(new Portfolio());
        groupRightInfo.setLabel("foo");

        doReturn(Collections.singletonList(groupUser))
                .when(groupUserRepository)
                .getByRole(grid);

        RoleRightsGroupDocument document = manager.getUserRole(grid);

        assertEquals(grid, document.getId());
        assertEquals(1, document.getGroups().size());
        assertEquals("foo", document.getGroups().get(0).getLabel());
    }
}
