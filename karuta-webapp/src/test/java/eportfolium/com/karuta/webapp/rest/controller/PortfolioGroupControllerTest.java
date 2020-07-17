package eportfolium.com.karuta.webapp.rest.controller;

import eportfolium.com.karuta.document.PortfolioGroupDocument;
import eportfolium.com.karuta.document.PortfolioGroupList;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.bean.PortfolioGroup;
import eportfolium.com.karuta.webapp.rest.AsUser;
import eportfolium.com.karuta.webapp.rest.ControllerTest;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class PortfolioGroupControllerTest extends ControllerTest {
    @Test
    @AsUser
    public void getPortfolioGroupId() throws Exception {
        String label = "my-group";
        Long groupId = 96L;

        doReturn(groupId)
                .when(portfolioManager)
                .getPortfolioGroupIdFromLabel(label);

        get("/portfoliogroups?label=" + label)
                .andExpect(status().isOk())
                .andExpect(content().string(groupId.toString()))
                .andDo(document("portfolio-group-id"));
    }

    @Test
    @AsUser
    public void getPortfolioGroupsForPortfolio() throws Exception {
        UUID portfolioId = UUID.randomUUID();

        PortfolioGroup portfolioGroup = new PortfolioGroup();
        portfolioGroup.setId(89L);
        portfolioGroup.setLabel("my-group");
        portfolioGroup.setType("portfolio");

        PortfolioGroupDocument portfolioGroupDocument = new PortfolioGroupDocument(portfolioGroup);

        doReturn(new PortfolioGroupList(Collections.singletonList(portfolioGroupDocument)))
                .when(portfolioManager)
                .getPortfolioGroupListFromPortfolio(portfolioId);

        get("/portfoliogroups?uuid=" + portfolioId.toString())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<portfolio><group id=\"89\" ")))
                .andDo(document("portfolio-groups-for-portfolio"));
    }

    @Test
    @AsUser
    public void getPortfoliosForGroup() throws Exception {
        Long groupId = 89L;

        UUID portfolioId1 = UUID.randomUUID();
        UUID portfolioId2 = UUID.randomUUID();

        Portfolio portfolio1 = new Portfolio();
        portfolio1.setId(portfolioId1);

        Portfolio portfolio2 = new Portfolio();
        portfolio2.setId(portfolioId2);

        doReturn(Arrays.asList(portfolio1, portfolio2))
                .when(portfolioRepository)
                .findByPortfolioGroup(groupId);

        get("/portfoliogroups?group=" + groupId)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<portfolio id=\"" + portfolioId1.toString())))
                .andExpect(content().string(containsString("<portfolio id=\"" + portfolioId2.toString())))
                .andDo(document("portfolios-for-portfolio-group"));
    }

    @Test
    @AsUser
    public void getAllPortfolioGroups() throws Exception {
        PortfolioGroup portfolioGroup1 = new PortfolioGroup();
        portfolioGroup1.setId(89L);
        portfolioGroup1.setType("portfolio");
        portfolioGroup1.setLabel("My group");

        PortfolioGroup portfolioGroup2 = new PortfolioGroup();
        portfolioGroup2.setId(110L);
        portfolioGroup2.setType("portfolio");
        portfolioGroup2.setLabel("A sub group");
        portfolioGroup2.setParent(portfolioGroup1);

        doReturn(Arrays.asList(portfolioGroup1, portfolioGroup2))
                .when(portfolioGroupRepository)
                .findAll();

        get("/portfoliogroups")
                .andExpect(status().isOk())
                .andExpect(content()
                        .string(containsString("<label>My group</label><group type=\"portfolio\" id=\"110\">")))
                .andDo(document("all-portfolio-groups"));
    }

    @Test
    @AsUser
    public void addPortfolioGroup() throws Exception {
        String label = "my-group";
        String type = "portfolio";
        long parentId = 96L;

        long returned = 99L;

        doReturn(returned)
                .when(portfolioManager)
                .addPortfolioGroup(label, type, parentId);

        mvc.perform(postBuilder("/portfoliogroups")
                    .param("label", label)
                    .param("type", type)
                    .param("parent", Long.toString(parentId)))
                .andExpect(status().isOk())
                .andExpect(content().string(Long.toString(returned)))
                .andDo(document("add-portfolio-group"));
    }

    @Test
    @AsUser
    public void updatePortfolioGroup() throws Exception {
        Long groupId = 96L;
        String label = "my-group";

        PortfolioGroup portfolioGroup = new PortfolioGroup();

        doReturn(Optional.of(portfolioGroup))
                .when(portfolioGroupRepository)
                .findById(groupId);

        mvc.perform(putBuilder("/portfoliogroups")
                    .param("group", groupId.toString())
                    .param("label", label))
                .andExpect(status().isOk())
                .andExpect(content().string("0"))
                .andDo(document("update-portfolio-group"));

        assertEquals(label, portfolioGroup.getLabel());
        verify(portfolioGroupRepository).save(portfolioGroup);
    }

    @Test
    @AsUser
    public void addPortfolioInGroup() throws Exception {
        UUID portfolioId = UUID.randomUUID();
        Long groupId = 96L;

        doReturn(0)
                .when(portfolioManager)
                .addPortfolioInGroup(portfolioId, groupId, null);

        mvc.perform(putBuilder("/portfoliogroups")
                    .param("group", groupId.toString())
                    .param("uuid", portfolioId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("0"))
                .andDo(document("add-portfolio-in-group"));
    }

    @Test
    @AsUser
    public void deleteGroup() throws Exception {
        long groupId = 96L;

        doReturn(true)
                .when(portfolioManager)
                .removePortfolioGroups(groupId);

        mvc.perform(deleteBuilder("/portfoliogroups")
                    .param("group", Long.toString(groupId)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"))
                .andDo(document("delete-portfolio-group"));
    }

    @Test
    @AsUser
    public void removingPortfolioFromGroup() throws Exception {
        long groupId = 96L;
        UUID portfolioId = UUID.randomUUID();

        doReturn(true)
                .when(portfolioManager)
                .removePortfolioFromPortfolioGroups(portfolioId, groupId);

        mvc.perform(deleteBuilder("/portfoliogroups")
                    .param("group", Long.toString(groupId))
                    .param("uuid", portfolioId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"))
                .andDo(document("remove-portfolio-from-group"));
    }
}