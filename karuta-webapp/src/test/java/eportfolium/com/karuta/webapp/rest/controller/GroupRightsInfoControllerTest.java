package eportfolium.com.karuta.webapp.rest.controller;

import eportfolium.com.karuta.business.security.test.AsAdmin;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.webapp.rest.AsUser;
import eportfolium.com.karuta.webapp.rest.ControllerTest;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class GroupRightsInfoControllerTest extends ControllerTest {

    @Test
    @AsAdmin
    public void getAll() throws Exception {
        UUID portfolioId = UUID.randomUUID();

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setId(56L);
        groupRightInfo.setLabel("moderators");
        groupRightInfo.setOwner(45L);

        doReturn(Collections.singletonList(groupRightInfo))
                .when(groupRightInfoRepository)
                .getByPortfolioID(portfolioId);

        get("/groupRightsInfos?portfolioId=" + portfolioId.toString())
                .andExpect(status().isOk())
                .andDo(document("group-rights-infos-by-portfolio"));
    }
}