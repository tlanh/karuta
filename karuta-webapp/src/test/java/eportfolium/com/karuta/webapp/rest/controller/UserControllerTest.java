package eportfolium.com.karuta.webapp.rest.controller;

import eportfolium.com.karuta.document.CredentialDocument;
import eportfolium.com.karuta.document.CredentialList;
import eportfolium.com.karuta.model.bean.*;
import eportfolium.com.karuta.webapp.rest.AsAdmin;
import eportfolium.com.karuta.webapp.rest.AsDesigner;
import eportfolium.com.karuta.webapp.rest.AsUser;
import eportfolium.com.karuta.webapp.rest.ControllerTest;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class UserControllerTest extends ControllerTest {

    private final String registerXml = "<users>" +
            "<user>" +
                "<username>jdoe</username>" +
                "<password>s3cr3t</password>" +
                "<email>foo@bar.com</email>" +
                "<firstname>John</firstname>" +
                "<lastname>Doe</lastname>" +
                "</user>" +
            "</users>";

    @Test
    @AsUser
    public void register_AsUser() throws Exception {
        post("/users", registerXml)
                .andExpect(status().isForbidden());

        verify(securityManager, times(0)).addUsers(any(CredentialList.class));
    }

    @Test
    @AsDesigner
    public void register_AsDesigner() throws Exception {
        post("/users", registerXml)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<user><username>jdoe</username>")));
    }

    @Test
    @AsAdmin
    public void register_AsAdmin() throws Exception {
        post("/users", registerXml)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<user><username>jdoe</username>")))
                .andDo(document("user-register"));
    }

    @Test
    @AsAdmin
    public void getUsers_AsAdmin() throws Exception {
        Credential credential = new Credential();
        credential.setLogin("jdoe");
        credential.setDisplayFirstname("John");
        credential.setDisplayLastname("Doe");

        CredentialDocument credentialDocument = new CredentialDocument(credential);
        CredentialList list = new CredentialList(Collections.singletonList(credentialDocument));

        doReturn(list)
                .when(userManager)
                .getUserList(null, "oh", null);

        get("/users?firstname=oh")
                .andExpect(status().isOk())
                .andExpect(content()
                        .string(containsString("<users><user><username>jdoe</username>")))
                .andDo(document("get-users-admin"));

        verify(userManager).getUserList(null, "oh", null);
        verify(userManager, times(0)).getUserInfos(userId);
    }

    @Test
    @AsUser
    public void getUsers_AsUser() throws Exception {
        Credential credential = new Credential();
        credential.setLogin("jdoe");
        credential.setDisplayFirstname("John");
        credential.setDisplayLastname("Doe");

        CredentialDocument credentialDocument = new CredentialDocument(credential);

        doReturn(credentialDocument)
                .when(userManager)
                .getUserInfos(userId);

        get("/users")
                .andExpect(status().isOk())
                .andExpect(content()
                        .string(containsString("<user><username>jdoe</username>")))
                .andExpect(content()
                        .string(not(containsString("<users>"))))
                .andDo(document("get-users-regular"));

        verify(userManager, times(0)).getUserList(anyString(), anyString(), anyString());
        verify(userManager).getUserInfos(userId);
    }

    @Test
    @AsUser
    public void getUser() throws Exception {
        Long userId = 56L;

        Credential credential = new Credential();
        credential.setLogin("jsmith");
        credential.setDisplayFirstname("Johnny");
        credential.setDisplayLastname("Smith");

        CredentialDocument credentialDocument = new CredentialDocument(credential);

        doReturn(credentialDocument)
                .when(userManager)
                .getUserInfos(userId);

        get("/users/user/" + userId)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<user><username>jsmith</username>")))
                .andDo(document("get-user"));
    }

    @Test
    @AsUser
    public void getUserId_WithExistingUser() throws Exception {
        String username = "jdoe";

        doReturn(userId)
                .when(userManager)
                .getUserId(username);

        get("/users/user/username/" + username)
                .andExpect(status().isOk())
                .andExpect(content().string(userId.toString()))
                .andDo(document("get-user-id"));
    }

    @Test
    @AsUser
    public void getUserId_WithMissingUser() throws Exception {
        String username = "jdoe";

        doReturn(null)
                .when(userManager)
                .getUserId(username);

        get("/users/user/username/" + username)
                .andExpect(status().isNotFound())
                .andDo(document("get-user-id-missing"));
    }

    @Test
    @AsUser
    public void getUserGroups() throws Exception {
        Long userId = 46L;

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setId(84L);
        groupRightInfo.setLabel("all");

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupRightInfo(groupRightInfo);
        groupInfo.setId(74L);
        groupInfo.setOwner(42L);
        groupInfo.setLabel("designer");

        GroupUser groupUser = new GroupUser(new GroupUserId());
        groupUser.setGroupInfo(groupInfo);

        doReturn(Collections.singletonList(groupUser))
                .when(groupUserRepository)
                .getByUser(userId);

        get("/users/user/" + userId + "/groups")
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<profiles><profile>")))
                .andDo(document("get-user-groups", relaxedResponseFields(
                        fieldWithPath("//group/@templateId").type(JsonFieldType.NUMBER)
                                .description("The group info's group right info's id"),
                        fieldWithPath("//group/roleId").type(JsonFieldType.NUMBER)
                                .description("Same as @templateId"),
                        fieldWithPath("//group/label").type(JsonFieldType.STRING)
                                .description("The group info's label"),
                        fieldWithPath("//group/role").type(JsonFieldType.STRING)
                                .description("The group info's group right info's label")
                )));
    }

    @Test
    @AsUser
    public void deleteUser_AsOwner() throws Exception {
        mvc.perform(deleteBuilder("/users?userId=" + userId))
                .andExpect(status().isOk())
                .andExpect(content().string("user " + userId + " deleted"))
                .andDo(document("delete-user"));

        verify(securityManager).removeUsers(userId);
    }

    @Test
    @AsUser
    public void deleteUser_AsRegularSomeoneElse() throws Exception {
        long otherId = 46L;

        mvc.perform(deleteBuilder("/users?userId=" + otherId))
                .andExpect(status().isForbidden());

        verifyNoInteractions(securityManager);
    }

    @Test
    @AsAdmin
    public void deleteUser_AsAdmin() throws Exception {
        long userId = 46L;

        mvc.perform(deleteBuilder("/users?userId=" + userId))
                .andExpect(status().isOk())
                .andExpect(content().string("user " + userId + " deleted"));

        verify(securityManager).removeUsers(userId);
    }

    @Test
    @AsUser
    public void getUsersByRole() throws Exception {
        UUID portfolioId = UUID.randomUUID();
        String role = "designer";

        Credential credential = new Credential();
        credential.setLogin("jdoe");
        credential.setDisplayFirstname("John");
        credential.setDisplayLastname("Doe");

        doReturn(Collections.singletonList(credential))
                .when(credentialRepository)
                .getUsersByRole(portfolioId, role);

        get("/users/Portfolio/" + portfolioId.toString() + "/Role/" + role + "/users")
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<users><user><username>jdoe")))
                .andDo(document("get-users-in-role"));
    }

    @Test
    @AsUser
    public void getGroupsByRole() throws Exception {
        UUID portfolioId = UUID.randomUUID();
        String role = "designer";

        GroupInfo groupInfo1 = new GroupInfo();
        GroupInfo groupInfo2 = new GroupInfo();

        groupInfo1.setId(82L);
        groupInfo2.setId(89L);

        doReturn(Arrays.asList(groupInfo1, groupInfo2))
                .when(groupInfoRepository)
                .getGroupsByRole(portfolioId, role);

        get("/users/Portfolio/" + portfolioId + "/Role/" + role + "/groups")
                .andExpect(status().isOk())
                .andExpect(content().string("<groups><group>82</group><group>89</group></groups>"))
                .andDo(document("get-groups-in-role"));
    }

    @Test
    @AsAdmin
    public void updateUser_AsAdmin() throws Exception {
        long otherId = 46L;

        String xml = "<user>" +
                    "<username>jdoe</username>" +
                    "<prevpass>s3cr3t</prevpass>" +
                    "<firstname>John</firstname>" +
                    "<lastname>Doe</lastname>" +
                    "<email>foo@bar.com</email>" +
                    "<admin>1</admin>" +
                    "<designer>0</designer>" +
                    "<active>1</active>" +
                    "<substitute>0</substitute>" +
                "</user>";

        Credential credential = new Credential();
        credential.setPassword(passwordEncoder.encode("s3cr3t"));

        doReturn(Optional.of(credential))
                .when(credentialRepository)
                .findActiveById(otherId);

        mvc.perform(putBuilder("/users/user/" + otherId)
                    .content(xml))
                .andExpect(status().isOk())
                .andExpect(content().string(Long.toString(otherId)))
                .andDo(document("update-user-as-admin"));

        assertEquals(1, credential.getIsAdmin());

        verify(securityManager).changeUser(eq(userId), eq(otherId), any(CredentialDocument.class));
    }

    @Test
    @AsUser
    public void updateUser_AsSelf() throws Exception {
        String xml = "<user>" +
                    "<prevpass>s3cr3t</prevpass>" +
                    "<password>s3cr3t</password>" +
                    "<firstname>John</firstname>" +
                    "<lastname>Doe</lastname>" +
                    "<email>foo@bar.com</email>" +
                "</user>";

        Credential credential = new Credential();
        credential.setPassword(passwordEncoder.encode("s3cr3t"));

        doReturn(Optional.of(credential))
                .when(credentialRepository)
                .findById(userId);

        mvc.perform(putBuilder("/users/user/" + userId)
                    .content(xml))
                .andExpect(status().isOk())
                .andExpect(content().string(userId.toString()))
                .andDo(document("update-user-as-owner"));

        verify(securityManager).changeUserInfo(eq(userId), eq(userId), any(CredentialDocument.class));
    }

    @Test
    @AsUser
    public void updateUser_OtherAccount() throws Exception {
        long otherId = 46L;

        String xml = "<user>" +
                "<prevpass>s3cr3t</prevpass>" +
                "<password>s3cr3t</password>" +
                "<firstname>John</firstname>" +
                "<lastname>Doe</lastname>" +
                "<email>foo@bar.com</email>" +
                "</user>";

        mvc.perform(putBuilder("/users/user/" + otherId)
                    .content(xml))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Not authorized"));
    }
}
