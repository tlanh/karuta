package eportfolium.com.karuta.webapp.rest.controller;

import eportfolium.com.karuta.webapp.rest.AsAdmin;
import eportfolium.com.karuta.webapp.rest.AsUser;
import eportfolium.com.karuta.webapp.rest.ControllerTest;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class UserGroupControllerTest extends ControllerTest {
    @Test
    @AsAdmin
    public void addUserToGroup() throws Exception {
        Long groupId = 75L;
        Long userId = 49L;

        mvc.perform(postBuilder("/groupsUsers")
                    .param("userId", userId.toString())
                    .param("group", groupId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("<ok/>"))
                .andDo(document("add-user-to-group"));

        verify(securityManager).addUserToGroup(userId, groupId);
    }

    @Test
    @AsUser
    public void addUserToGroup_AsRegularUser() throws Exception {
        mvc.perform(postBuilder("/groupsUsers")
                    .param("userId", "47")
                    .param("group", "78"))
                .andExpect(status().isForbidden());
    }
}