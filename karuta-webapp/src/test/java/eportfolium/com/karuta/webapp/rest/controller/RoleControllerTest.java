package eportfolium.com.karuta.webapp.rest.controller;

import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.webapp.rest.AsUser;
import eportfolium.com.karuta.webapp.rest.ControllerTest;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class RoleControllerTest extends ControllerTest {
    @Test
    @AsUser
    public void getRole() throws Exception {
        Long roleId = 121L;

        Portfolio portfolio = new Portfolio();
        portfolio.setId(UUID.randomUUID());

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setId(roleId);
        groupRightInfo.setPortfolio(portfolio);

        doReturn(Optional.of(groupRightInfo))
                .when(groupRightInfoRepository)
                .findById(roleId);

        get("/roles/role/" + roleId)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<role id=\"" + roleId +"\" ")))
                .andDo(document("get-role"));

        verify(userManager).getRole(roleId);
    }

    @Test
    @AsUser
    public void getRolePortfolio_WithPresentRole() throws Exception {
        UUID portfolioId = UUID.randomUUID();
        String role = "designer";

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setId(121L);

        doReturn(groupRightInfo)
            .when(groupRightInfoRepository)
            .getByPortfolioAndLabel(portfolioId, role);

        get("/roles/portfolio/" + portfolioId + "?role=" + role)
                .andExpect(status().isOk())
                .andExpect(content().string("grid = " + groupRightInfo.getId()))
                .andDo(document("get-portfolio-role-present"));
    }

    @Test
    @AsUser
    public void getRolePortfolio_WithMissingRole() throws Exception {
        UUID portfolioId = UUID.randomUUID();
        String role = "designer";

        doReturn(null)
                .when(groupRightInfoRepository)
                .getByPortfolioAndLabel(portfolioId, role);

        get("/roles/portfolio/" + portfolioId + "?role=" + role)
                .andExpect(status().isOk())
                .andExpect(content().string("Le grid n'existe pas"))
                .andDo(document("get-portfolio-role-missing"));
    }
}
