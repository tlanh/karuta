package eportfolium.com.karuta.webapp.rest.controller;

import eportfolium.com.karuta.business.security.test.AsAdmin;
import eportfolium.com.karuta.webapp.rest.AsUser;
import eportfolium.com.karuta.webapp.rest.ControllerTest;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class RightGroupControllerTest extends ControllerTest {
    @Test
    @AsAdmin
    public void changeRightGroupUser_AsAdmin() throws Exception {
        Long groupId = 89L;
        Long groupRightId = 112L;

        mvc.perform(postBuilder("/RightGroup")
                    .param("group", groupId.toString())
                    .param("groupRightId", groupRightId.toString()))
                .andExpect(status().isOk())
                .andDo(document("change-right-group"));

        verify(groupManager).changeUserGroup(groupRightId, groupId);
    }

    @Test
    @AsUser
    public void changeRightGroupUser_AsRegularUser() throws Exception {
        mvc.perform(postBuilder("/RightGroup")
                .param("group", "89")
                .param("groupRightId", "112"))
                .andExpect(status().isForbidden());
    }
}