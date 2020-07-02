package eportfolium.com.karuta.webapp.rest.controller;

import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.business.security.test.AsAdmin;
import eportfolium.com.karuta.model.bean.GroupInfo;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.GroupRightsId;
import eportfolium.com.karuta.webapp.rest.AsUser;
import eportfolium.com.karuta.webapp.rest.ControllerTest;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class GroupRightsControllerTest extends ControllerTest {

    @Test
    @AsUser
    public void getAll_AsUser() throws Exception {
        get("/groupRights?group=74")
                .andExpect(status().isForbidden());

        verifyNoInteractions(groupManager);
    }

    @Test
    @AsAdmin
    public void getAll() throws Exception {
        Long groupId = 74L;

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setId(85L);

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setId(98L);
        groupRightInfo.setGroupInfo(groupInfo);

        GroupRights groupRights = new GroupRights();
        groupRights.setId(new GroupRightsId());
        groupRights.setGroupRightInfo(groupRightInfo);

        doReturn(Collections.singletonList(groupRights))
                .when(groupRightsRepository)
                .getRightsByGroupId(groupId);

        get("/groupRights?group=" + groupId)
                .andExpect(status().isOk())
                .andDo(document("group-rights-all", relaxedResponseFields(
                        fieldWithPath("//groupRight/@gid").type(JsonFieldType.NUMBER)
                                .description("The group right info's group info's ID"),
                        fieldWithPath("//groupRight/@templateId").type(JsonFieldType.NUMBER)
                                .description("The group right info's ID")
                )));
    }

    @Test
    @AsUser
    public void delete_AsUser() throws Exception {
        mvc.perform(deleteBuilder("/groupRights")
                    .param("group", Long.toString(74L)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(groupManager);
    }

    @Test
    @AsAdmin
    public void delete() throws Exception {
        long groupId = 74L;

        doNothing()
                .when(groupManager)
                .removeRights(groupId);

        mvc.perform(deleteBuilder("/groupRights")
                    .param("group", Long.toString(groupId)))
                .andExpect(status().isOk())
                .andExpect(content().string("supprimé"))
                .andDo(document("group-rights-delete"));
    }
}