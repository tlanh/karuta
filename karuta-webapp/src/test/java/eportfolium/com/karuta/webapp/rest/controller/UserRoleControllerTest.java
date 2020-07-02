package eportfolium.com.karuta.webapp.rest.controller;

import eportfolium.com.karuta.business.security.test.AsAdmin;
import eportfolium.com.karuta.model.bean.GroupInfo;
import eportfolium.com.karuta.webapp.rest.AsUser;
import eportfolium.com.karuta.webapp.rest.ControllerTest;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class UserRoleControllerTest extends ControllerTest {
    @Test
    @AsAdmin
    public void addUserToRole() throws Exception {
        Long grid = 68L;
        Long userId = 49L;

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setId(78L);

        doReturn(groupInfo)
                .when(groupInfoRepository)
                .getGroupByGrid(grid);

        mvc.perform(postBuilder("/roleUser")
                    .param("grid", grid.toString())
                    .param("user-id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string(String.format("user %d rajoute au groupd gid %d pour correspondre au groupRight grid %d",
                                userId, groupInfo.getId(), grid)))
                .andDo(document("add-user-to-role"));

        verify(securityManager).addUserRole(grid, userId);
    }

    @Test
    @AsUser
    public void addUserToRole_AsRegularUser() throws Exception {
        mvc.perform(postBuilder("/roleUser")
                    .param("grid", "89")
                    .param("user-id", "45"))
                .andExpect(status().isForbidden());
    }
}