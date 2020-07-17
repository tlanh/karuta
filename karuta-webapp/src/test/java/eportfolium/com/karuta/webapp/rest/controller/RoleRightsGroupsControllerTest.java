package eportfolium.com.karuta.webapp.rest.controller;

import eportfolium.com.karuta.business.security.test.AsAdmin;
import eportfolium.com.karuta.model.bean.*;
import eportfolium.com.karuta.webapp.rest.AsUser;
import eportfolium.com.karuta.webapp.rest.ControllerTest;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class RoleRightsGroupsControllerTest extends ControllerTest {
    @Test
    @AsUser
    public void getRolesInPortfolio() throws Exception {
        UUID portfolioId = UUID.randomUUID();

        Portfolio portfolio = new Portfolio();
        portfolio.setId(portfolioId);

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setId(123L);
        groupRightInfo.setLabel("designer");
        groupRightInfo.setPortfolio(portfolio);

        doReturn(Collections.singletonList(groupRightInfo))
                .when(groupRightInfoRepository)
                .getByPortfolioID(portfolioId);

        get("/rolerightsgroups?portfolio=" + portfolioId)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<rolerightsgroups>")))
                .andExpect(content().string(containsString("<rolerightgroup id=\"123\">")))
                .andExpect(content().string(containsString("<label>designer</label>")))
                .andExpect(content().string(containsString("<portfolio>" + portfolioId + "</portfolio>")))
                .andDo(document("get-roles-in-portfolio"));
    }

    @Test
    @AsUser
    public void getRoleId() throws Exception {
        UUID portfolioId = UUID.randomUUID();
        String role = "designer";

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setId(123L);

        doReturn(groupRightInfo)
                .when(groupRightInfoRepository)
                .getByPortfolioAndLabel(portfolioId, role);

        get("/rolerightsgroups?portfolio=" + portfolioId + "&role=" + role)
                .andExpect(status().isOk())
                .andExpect(content().string(groupRightInfo.getId().toString()))
                .andDo(document("get-role-id-in-portfolio"));
    }

    @Test
    @AsUser
    public void getUsersRole() throws Exception {
        UUID portfolioId = UUID.randomUUID();

        Portfolio portfolio = new Portfolio();
        portfolio.setId(portfolioId);

        Credential credential = new Credential();
        credential.setLogin("jdoe");
        credential.setDisplayFirstname("John");
        credential.setDisplayLastname("Doe");

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setId(123L);
        groupRightInfo.setLabel("designer");
        groupRightInfo.setPortfolio(portfolio);

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupRightInfo(groupRightInfo);

        GroupUser groupUser = new GroupUser();
        groupUser.setId(new GroupUserId());
        groupUser.setGroupInfo(groupInfo);
        groupUser.setCredential(credential);

        doReturn(Collections.singletonList(groupUser))
                .when(groupUserRepository)
                .getByPortfolio(portfolioId);

        get("/rolerightsgroups/all/users?portfolio=" + portfolioId)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<portfolio id=\"" + portfolioId +"\">")))
                .andExpect(content().string(containsString("")))
                .andDo(document("get-users-in-portfolio", relaxedResponseFields(
                        fieldWithPath("//rrg/@id").type(JsonFieldType.NUMBER)
                                .description("The role (group right info)'s id"),
                        fieldWithPath("//rrg/label").type(JsonFieldType.STRING)
                                .description("The role's label")
                )));
    }

    @Test
    @AsUser
    public void getRole() throws Exception {
        Long roleId = 123L;

        UUID portfolioId = UUID.randomUUID();

        Portfolio portfolio = new Portfolio();
        portfolio.setId(portfolioId);

        Credential credential = new Credential();
        credential.setLogin("jdoe");
        credential.setDisplayFirstname("John");
        credential.setDisplayLastname("Doe");

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setId(roleId);
        groupRightInfo.setLabel("designer");
        groupRightInfo.setPortfolio(portfolio);

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupRightInfo(groupRightInfo);

        GroupUser groupUser = new GroupUser();
        groupUser.setId(new GroupUserId());
        groupUser.setGroupInfo(groupInfo);
        groupUser.setCredential(credential);

        doReturn(Collections.singletonList(groupUser))
                .when(groupUserRepository)
                .getByRole(roleId);

        get("/rolerightsgroups/rolerightsgroup/" + roleId)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<rolerightsgroup id=\"123\">")))
                .andExpect(content().string(containsString("<group id=\"123\">")))
                .andExpect(content().string(containsString("<users><user><username>jdoe</username>")))
                .andDo(document("get-role"));
    }

    @Test
    @AsAdmin
    public void addUserInRole_AsAdmin() throws Exception {
        Long roleId = 68L;
        Long userId = 49L;

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setId(78L);

        doReturn(groupInfo)
                .when(groupInfoRepository)
                .getGroupByGrid(roleId);

        mvc.perform(postBuilder("/rolerightsgroups/rolerightsgroup/" + roleId + "/users/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string(String.format("user %d rajoute au groupd gid %d pour correspondre au groupRight grid %d",
                                userId, groupInfo.getId(), roleId)))
                .andDo(document("add-user-in-role"));

        verify(securityManager).addUserRole(roleId, userId);
    }

    @Test
    @AsUser
    public void addUserInRole_AsRegularUser() throws Exception {
        mvc.perform(postBuilder("/rolerightsgroups/rolerightsgroup/123/users/user/48"))
                .andExpect(status().isForbidden());
    }

    @Test
    @AsAdmin
    public void addSeveralUsersInRole_AsAdmin() throws Exception {
        Long roleId = 123L;

        String xml = "<users>" +
                    "<user id=\"45\" />" +
                    "<user id=\"48\" />" +
                "</users>";

        GroupRightInfo groupRightInfo = new GroupRightInfo();

        doReturn(Optional.of(groupRightInfo))
                .when(groupRightInfoRepository)
                .findById(roleId);

        post("/rolerightsgroups/rolerightsgroup/" + roleId + "/users", xml)
                .andExpect(status().isOk())
                .andDo(document("add-users-in-role"));

        verify(securityManager).addUserRole(roleId, 45L);
        verify(securityManager).addUserRole(roleId, 48L);
    }

    @Test
    @AsUser
    public void addSeveralUsers_AsRegularUser() throws Exception {
        String xml = "<users>" +
                    "<user id=\"45\" />" +
                "</users>";

        post("/rolerightsgroups/rolerightsgroup/129/users", xml)
                .andExpect(status().isForbidden());
    }

    @Test
    @AsAdmin
    public void removeRole_AsAdmin() throws Exception {
        Long roleId = 127L;

        mvc.perform(deleteBuilder("/rolerightsgroups/rolerightsgroup/" + roleId))
                .andExpect(status().isOk())
                .andExpect(content().string(""))
                .andDo(document("remove-role"));

        verify(securityManager).removeRole(roleId);
    }

    @Test
    @AsUser
    public void removeRole_AsRegularUser() throws Exception {
        mvc.perform(deleteBuilder("/rolerightsgroups/rolerightsgroup/129"))
                .andExpect(status().isForbidden());
    }

    @Test
    @AsAdmin
    public void removeUserFromRole_AsAdmin() throws Exception {
        Long userId = 49L;
        Long roleId = 125L;

        mvc.perform(deleteBuilder("/rolerightsgroups/rolerightsgroup/" + roleId + "/users/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(content().string(""))
                .andDo(document("remove-user-from-role"));

        verify(securityManager).removeUserRole(userId, roleId);
    }

    @Test
    @AsUser
    public void removeUserFromRole_AsRegularUser() throws Exception {
        mvc.perform(deleteBuilder("/rolerightsgroups/rolerightsgroup/125/users/user/48"))
                .andExpect(status().isForbidden());
    }

    @Test
    @AsAdmin
    public void removeAllUsersFromRole_AsAdmin() throws Exception {
        UUID portfolioId = UUID.randomUUID();

        mvc.perform(deleteBuilder("/rolerightsgroups/all/users")
                    .param("portfolio", portfolioId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(""))
                .andDo(document("remove-all-users-from-role"));

        verify(securityManager).removeUsersFromRole(portfolioId);
    }

    @Test
    @AsUser
    public void removeAllUsersFromRole_AsRegularUser() throws Exception {
        UUID portfolioId = UUID.randomUUID();

        mvc.perform(deleteBuilder("/rolerightsgroups/all/users")
                .param("portfolio", portfolioId.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @AsAdmin
    public void changeRightInRole_AsAdmin() throws Exception {
        UUID portfolioId = UUID.randomUUID();
        Long roleId = 121L;

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setId(roleId);

        doReturn(Optional.of(groupRightInfo))
                .when(groupRightInfoRepository)
                .findById(roleId);

        String xml = "<role>" +
                "<label>designer</label>" +
                "<portfolio_id>" + portfolioId + "</portfolio_id>" +
                "</role>";

        mvc.perform(putBuilder("/rolerightsgroups/rolerightsgroup/" + roleId)
                    .content(xml))
                .andExpect(status().isOk())
                .andExpect(content().string(""))
                .andDo(document("update-role"));

        assertEquals("designer", groupRightInfo.getLabel());
        assertEquals(portfolioId, groupRightInfo.getPortfolio().getId());

        verify(groupRightInfoRepository).save(groupRightInfo);
    }

    @Test
    @AsUser
    public void changeRightInRole_AsRegularUser() throws Exception {
        String xml = "<role>" +
                "<label>designer</label>" +
                "</role>";

        mvc.perform(putBuilder("/rolerightsgroups/rolerightsgroup/126")
                .content(xml))
                .andExpect(status().isForbidden());
    }
}
