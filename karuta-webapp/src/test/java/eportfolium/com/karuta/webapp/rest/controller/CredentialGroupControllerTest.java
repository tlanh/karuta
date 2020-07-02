package eportfolium.com.karuta.webapp.rest.controller;

import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.CredentialGroup;
import eportfolium.com.karuta.model.bean.CredentialGroupMembers;
import eportfolium.com.karuta.model.bean.CredentialGroupMembersId;
import eportfolium.com.karuta.webapp.rest.AsUser;
import eportfolium.com.karuta.webapp.rest.ControllerTest;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class CredentialGroupControllerTest extends ControllerTest {

    @Test
    @AsUser
    public void add() throws Exception {
        String label = "phd-students";

        doReturn(74L)
            .when(groupManager)
            .addCredentialGroup(label);

        mvc.perform(postBuilder("/usergroups")
                    .param("label", label))
                .andExpect(status().isOk())
                .andExpect(content().string("74"))
                .andDo(document("credential-groups-add"));
    }

    @Test
    @AsUser
    public void addUser_Fail() throws Exception {
        Long groupId = 74L;

        doReturn(false)
                .when(securityManager)
                .addUserInCredentialGroups(userId, Collections.singletonList(groupId));

        mvc.perform(putBuilder("/usergroups")
                    .param("group", groupId.toString())
                    .param("user", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("Not OK"))
                .andDo(document("add-user-fail"));
    }

    @Test
    @AsUser
    public void addUser_Ok() throws Exception {
        Long groupId = 74L;

        doReturn(true)
            .when(securityManager)
            .addUserInCredentialGroups(userId, Collections.singletonList(groupId));

        mvc.perform(putBuilder("/usergroups")
                    .param("group", groupId.toString())
                    .param("user", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("Changed"))
                .andDo(document("add-user-ok"));
    }

    @Test
    @AsUser
    public void renameGroup_Fail() throws Exception {
        String label = "new-group-name";
        Long groupId = 74L;

        doReturn(false)
                .when(groupManager)
                .renameCredentialGroup(groupId, label);

        mvc.perform(putBuilder("/usergroups")
                    .param("group", groupId.toString())
                    .param("label", label))
                .andExpect(status().isOk())
                .andExpect(content().string("Not OK"))
                .andDo(document("rename-group-fail"));
    }

    @Test
    @AsUser
    public void renameGroup_Ok() throws Exception {
        String label = "new-group-name";
        Long groupId = 74L;

        doReturn(true)
                .when(groupManager)
                .renameCredentialGroup(groupId, label);

        mvc.perform(putBuilder("/usergroups")
                    .param("group", groupId.toString())
                    .param("label", label))
                .andExpect(status().isOk())
                .andExpect(content().string("Changed"))
                .andDo(document("rename-group-ok"));
    }

    @Test
    @AsUser
    public void getGroup_ByLabel() throws Exception {
        String label = "group-label";

        CredentialGroup credentialGroup = new CredentialGroup();
        credentialGroup.setId(74L);

        doReturn(credentialGroup)
                .when(groupManager)
                .getCredentialGroupByName(label);

        get("/usergroups?label=" + label)
                .andExpect(status().isOk())
                .andExpect(content().string(credentialGroup.getId().toString()))
                .andDo(document("credential-group-by-label"));
    }

    @Test
    @AsUser
    public void getGroups_ByUser() throws Exception {
        CredentialGroup credentialGroup = new CredentialGroup();
        credentialGroup.setId(74L);
        credentialGroup.setLabel("super-group");

        CredentialGroupMembers cgm = new CredentialGroupMembers();

        cgm.setId(new CredentialGroupMembersId());
        cgm.setCredentialGroup(credentialGroup);

        doReturn(Collections.singletonList(cgm))
                .when(credentialGroupMembersRepository)
                .findByUser(userId);

        get("/usergroups?user=" + userId)
                .andExpect(status().isOk())
                .andDo(document("credential-group-by-user"));
    }

    @Test
    @AsUser
    public void getGroup_ById() throws Exception {
        Long groupId = 74L;

        Credential credential = new Credential();

        CredentialGroupMembers cgm = new CredentialGroupMembers();
        cgm.setId(new CredentialGroupMembersId());
        cgm.setCredential(credential);

        doReturn(Collections.singletonList(cgm))
                .when(credentialGroupMembersRepository)
                .findByGroup(groupId);

        get("/usergroups?group=" + groupId)
                .andExpect(status().isOk())
                .andDo(document("credential-group-by-group"));
    }

    @Test
    @AsUser
    public void getGroups() throws Exception {
        CredentialGroup credentialGroup = new CredentialGroup();

        credentialGroup.setId(74L);
        credentialGroup.setLabel("super-group");

        doReturn(Collections.singletonList(credentialGroup))
                .when(credentialGroupRepository)
                .findAll();

        get("/usergroups")
                .andExpect(status().isOk())
                .andDo(document("credential-groups"));
    }

    @Test
    @AsUser
    public void removeGroup() throws Exception {
        Long groupId = 74L;

        doNothing()
                .when(groupManager)
                .removeCredentialGroup(groupId);

        mvc.perform(deleteBuilder("/usergroups")
                    .param("group", groupId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted"))
                .andDo(document("credential-group-delete"));

        verify(groupManager).removeCredentialGroup(groupId);
        verifyNoInteractions(securityManager);
    }

    @Test
    @AsUser
    public void removeUserFromGroup() throws Exception {
        Long groupId = 74L;

        doNothing()
                .when(securityManager)
                .deleteUserFromCredentialGroup(userId, groupId);

        mvc.perform(deleteBuilder("/usergroups")
                    .param("group", groupId.toString())
                    .param("user", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted"))
                .andDo(document("credential-group-remove-user"));

        verify(securityManager).deleteUserFromCredentialGroup(userId, groupId);
        verifyNoInteractions(groupManager);
    }

}