package eportfolium.com.karuta.webapp.rest.controller;

import eportfolium.com.karuta.model.bean.*;
import eportfolium.com.karuta.webapp.rest.AsUser;
import eportfolium.com.karuta.webapp.rest.ControllerTest;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class GroupsControllerTest extends ControllerTest {

    @Test
    @AsUser
    public void getUserGroups() throws Exception {
        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setId(87L);
        groupRightInfo.setLabel("moderators");

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setId(userId);
        groupInfo.setOwner(45L);
        groupInfo.setLabel("super-group");
        groupInfo.setGroupRightInfo(groupRightInfo);

        GroupUser groupUser = new GroupUser();
        groupUser.setId(new GroupUserId());
        groupUser.setGroupInfo(groupInfo);

        doReturn(Collections.singletonList(groupUser))
                .when(groupUserRepository)
                .getByUser(userId);

        get("/groups")
                .andExpect(status().isOk())
                .andDo(document("groups-for-user", relaxedResponseFields(
                        fieldWithPath("//group/label").type(JsonFieldType.STRING)
                                .description("The group info's label"),
                        fieldWithPath("//group/role").type(JsonFieldType.STRING)
                                .description("The group info's group right info's label"),
                        fieldWithPath("//group/@templateId").type(JsonFieldType.NUMBER)
                                .description("The group info's group right info's ID"),
                        fieldWithPath("//group/roleId").type(JsonFieldType.NUMBER)
                                .description("Same as templateId, kept for backward compatibility.")
                )));
    }

    @Test
    @AsUser
    public void getRoles() throws Exception {
        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setId(87L);
        groupRightInfo.setLabel("moderators");

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setId(userId);
        groupInfo.setOwner(45L);
        groupInfo.setLabel("super-group");
        groupInfo.setGroupRightInfo(groupRightInfo);

        UUID portfolioId = UUID.randomUUID();

        GroupRights groupRights = new GroupRights();
        groupRights.setRead(true);

        doReturn(groupRights)
                .when(portfolioManager)
                .getRightsOnPortfolio(userId, 0L, portfolioId);

        doReturn(Collections.singletonList(groupInfo))
               .when(groupInfoRepository)
               .getByPortfolio(portfolioId);

        get("/groups/" + portfolioId)
                .andExpect(status().isOk())
                .andDo(document("groups-by-portfolio"));
    }
}