package eportfolium.com.karuta.webapp.rest.controller;

import eportfolium.com.karuta.model.bean.*;
import eportfolium.com.karuta.webapp.rest.AsAdmin;
import eportfolium.com.karuta.webapp.rest.AsUser;
import eportfolium.com.karuta.webapp.rest.ControllerTest;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class NodesControllerTest extends ControllerTest {
    @Test
    @AsUser
    public void getNode() throws Exception {
        UUID nodeId = UUID.randomUUID();

        Portfolio portfolio = new Portfolio();

        Node node = new Node();

        node.setId(nodeId);
        node.setPortfolio(portfolio);
        node.setAsmType("asmContext");
        node.setXsiType("");
        node.setModifDate(new Date());
        node.setMetadataWad("public=\"false\"");

        doReturn(Optional.of(node))
                .when(nodeRepository)
                .findById(nodeId);

        GroupRightInfo groupRightInfo = new GroupRightInfo();

        GroupRights groupRights = new GroupRights();
        groupRights.setRead(true);
        groupRights.setId(new GroupRightsId());
        groupRights.setGroupRightInfo(groupRightInfo);

        doReturn(groupRights)
                .when(nodeManager)
                .getRights(userId, nodeId);

        GroupUser groupUser = new GroupUser();
        GroupInfo groupInfo = new GroupInfo();

        groupUser.setId(new GroupUserId());
        groupUser.setGroupInfo(groupInfo);

        groupInfo.setGroupRightInfo(groupRightInfo);

        doReturn(groupUser)
                .when(groupUserRepository)
                .getUniqueByUser(userId);

        doReturn(groupRightInfo)
                .when(groupRightInfoRepository)
                .getByPortfolioAndLabel(null, "all");

        doReturn(true)
                .when(credentialRepository)
                .isDesigner(userId, nodeId);

        doReturn(Collections.singletonList(node))
                .when(nodeRepository)
                .getNodes(anyList());

        get("/nodes/node/" + nodeId)
                .andExpect(status().isOk())
                .andDo(document("get-node"));
    }

    @Test
    @AsUser
    public void getNodeRights() throws Exception {
        UUID nodeId = UUID.randomUUID();

        GroupRights groupRights = new GroupRights();

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setLabel("designer");
        groupRights.setId(new GroupRightsId());
        groupRights.setGroupRightInfo(groupRightInfo);

        groupRights.setRead(true);
        groupRights.setWrite(true);

        doReturn(groupRights)
                .when(nodeManager)
                .getRights(userId, nodeId);

        get("/nodes/node/" + nodeId + "/rights")
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<node uuid=\"" + nodeId)))
                .andDo(document("get-node-rights"));
    }

    @Test
    @AsUser
    public void getPortfolioId_AsUser_WithoutRights() throws Exception {
        UUID nodeId = UUID.randomUUID();

        get("/nodes/node/" + nodeId + "/portfolioid")
                .andExpect(status().isForbidden());
    }

    @Test
    @AsUser
    public void getPortfolioId_AsUser_WithRights() throws Exception {
        UUID nodeId = UUID.randomUUID();

        Portfolio portfolio = new Portfolio();
        portfolio.setId(UUID.randomUUID());

        Node node = new Node();
        node.setId(nodeId);
        node.setPortfolio(portfolio);

        doReturn(Optional.of(node))
            .when(nodeRepository)
            .findById(nodeId);

        doReturn(true)
            .when(nodeManager)
            .hasRight(userId, nodeId, GroupRights.READ);

        get("/nodes/node/" + nodeId + "/portfolioid")
                .andExpect(status().isOk())
                .andExpect(content().string(portfolio.getId().toString()))
                .andDo(document("get-node-portfolio-id"));
    }

    @Test
    @AsUser
    public void changeRights_AsUser() throws Exception {
        UUID nodeId = UUID.randomUUID();

        post("/nodes/node/" + nodeId + "/rights", "<node></node>")
                .andExpect(status().isForbidden());
    }

    @Test
    @AsAdmin
    public void changeRights_AsAdmin() throws Exception {
        UUID nodeId = UUID.randomUUID();

        String xml = "<node><role name=\"all\">" +
                "<right RD=\"true\" WR=\"true\" DL=\"true\" SB=\"true\"/>" +
                "</role></node>";

        post("/nodes/node/" + nodeId + "/rights", xml)
                .andExpect(status().isOk())
                .andExpect(content().string(""))
                .andDo(document("change-node-rights"));

        ArgumentCaptor<GroupRights> captor = ArgumentCaptor.forClass(GroupRights.class);

        verify(nodeManager).changeRights(eq(nodeId), eq("all"), captor.capture());

        GroupRights groupRights = captor.getValue();

        assertTrue(groupRights.isRead());
        assertTrue(groupRights.isWrite());
        assertTrue(groupRights.isDelete());
        assertTrue(groupRights.isSubmit());
    }

    @Test
    @AsUser
    public void updateMetadata_WithoutRights() throws Exception {
        UUID nodeId = UUID.randomUUID();

        mvc.perform(putBuilder("/nodes/node/" + nodeId + "/metadata")
                .content("<metadata />"))
                .andExpect(status().isForbidden());
    }

    @Test
    @AsUser
    public void updateMetadata_WithRights() throws Exception {
        UUID nodeId = UUID.randomUUID();

        doReturn(true)
                .when(nodeManager)
                .hasRight(userId, nodeId, GroupRights.WRITE);

        Node node = new Node();

        doReturn(Optional.of(node))
                .when(nodeRepository)
                .findById(nodeId);

        mvc.perform(putBuilder("/nodes/node/" + nodeId + "/metadata")
                .content("<metadata semantictag=\"hello\" foo=\"bar\" baz=\"quux\"/>"))
                .andExpect(status().isOk())
                .andExpect(content().string("editer"))
                .andDo(document("change-node-metadata"));

        assertEquals("semantictag=\"hello\" foo=\"bar\" baz=\"quux\"", node.getMetadata());
        assertEquals("hello", node.getSemantictag());
    }

    @Test
    @AsUser
    public void updateMetadatawad_WithoutRights() throws Exception {
        UUID nodeId = UUID.randomUUID();

        mvc.perform(putBuilder("/nodes/node/" + nodeId + "/metadatawad")
                .content("<metadata-wad />"))
                .andExpect(status().isForbidden());
    }

    @Test
    @AsUser
    public void updateMetadatawad_WithRights() throws Exception {
        UUID nodeId = UUID.randomUUID();

        doReturn(true)
                .when(nodeManager)
                .hasRight(userId, nodeId, GroupRights.WRITE);

        Node node = new Node();

        doReturn(Optional.of(node))
                .when(nodeRepository)
                .findById(nodeId);

        mvc.perform(putBuilder("/nodes/node/" + nodeId + "/metadatawad")
                .content("<metadata-wad foo=\"bar\" baz=\"quux\"/>"))
                .andExpect(status().isOk())
                .andExpect(content().string("editer"))
                .andDo(document("change-node-metadatawad"));


        assertEquals(" foo=\"bar\" baz=\"quux\"", node.getMetadataWad());
    }

    @Test
    @AsUser
    public void updateMetadataepm_WithoutRights() throws Exception {
        UUID nodeId = UUID.randomUUID();

        mvc.perform(putBuilder("/nodes/node/" + nodeId + "/metadataepm")
                .content("<metadata-epm />"))
                .andExpect(status().isForbidden());
    }

    @Test
    @AsUser
    public void updateMetadataepm_WithRights() throws Exception {
        UUID nodeId = UUID.randomUUID();

        doReturn(true)
                .when(nodeManager)
                .hasRight(userId, nodeId, GroupRights.WRITE);

        Node node = new Node();

        doReturn(Optional.of(node))
                .when(nodeRepository)
                .findById(nodeId);

        mvc.perform(putBuilder("/nodes/node/" + nodeId + "/metadataepm")
                .content("<metadata-epm foo=\"bar\" baz=\"quux\"/>"))
                .andExpect(status().isOk())
                .andExpect(content().string("editer"))
                .andDo(document("change-node-metadataepm"));

        assertEquals(" foo=\"bar\" baz=\"quux\"", node.getMetadataEpm());
    }

    @Test
    @AsUser
    public void updateNodeContext_WithoutRights() throws Exception {
        UUID nodeId = UUID.randomUUID();

        mvc.perform(putBuilder("/nodes/node/" + nodeId + "/nodecontext")
                .content("<asmResource></asmResource>"))
                .andExpect(status().isForbidden());
    }

    @Test
    @AsUser
    public void updateNodeContext_WithRights() throws Exception {
        UUID nodeId = UUID.randomUUID();

        doReturn(true)
                .when(nodeManager)
                .hasRight(userId, nodeId, GroupRights.WRITE);

        Resource resource = new Resource();

        Node node = new Node();
        node.setContextResource(resource);

        doReturn(Optional.of(node))
                .when(nodeRepository)
                .findById(nodeId);

        mvc.perform(putBuilder("/nodes/node/" + nodeId + "/nodecontext")
                .content("<asmResource><foo>bar</foo></asmResource>"))
                .andExpect(status().isOk())
                .andExpect(content().string("editer"))
                .andDo(document("change-node-context"));

        assertEquals("<foo>bar</foo>", resource.getContent());
    }

    @Test
    @AsUser
    public void updateNodeResource_WithoutRights() throws Exception {
        UUID nodeId = UUID.randomUUID();

        mvc.perform(putBuilder("/nodes/node/" + nodeId + "/noderesource")
                .content("<asmResource></asmResource>"))
                .andExpect(status().isForbidden());
    }

    @Test
    @AsUser
    public void updateNodeResource_WithRights() throws Exception {
        UUID nodeId = UUID.randomUUID();

        doReturn(true)
                .when(nodeManager)
                .hasRight(userId, nodeId, GroupRights.WRITE);

        Resource resource = new Resource();

        Node node = new Node();
        node.setResource(resource);

        doReturn(Optional.of(node))
                .when(nodeRepository)
                .findById(nodeId);

        mvc.perform(putBuilder("/nodes/node/" + nodeId + "/noderesource")
                .content("<asmResource><code>hello.world</code><foo>bar</foo></asmResource>"))
                .andExpect(status().isOk())
                .andDo(document("change-node-resource"));

        assertEquals("hello.world", node.getCode());
        assertEquals("<code>hello.world</code><foo>bar</foo>", resource.getContent());
    }

    @Test
    @AsUser
    public void importNode_WithoutRights() throws Exception {
        UUID parentId = UUID.randomUUID();

        mvc.perform(postBuilder("/nodes/node/import/" + parentId))
                .andExpect(status().isForbidden());
    }

    @Test
    @AsUser
    public void importNode_WithRights() throws Exception {
        UUID parentId = UUID.randomUUID();

        String srcetag = "foo";
        String srcecode = "bar";

        doReturn(true)
                .when(nodeManager)
                .hasRight(userId, parentId, GroupRights.READ);

        Portfolio portfolio = new Portfolio();
        portfolio.setId(UUID.randomUUID());

        doReturn(portfolio)
                .when(portfolioRepository)
                .getPortfolioFromNodeCode(srcecode);

        ArgumentCaptor<Node> captor = ArgumentCaptor.forClass(Node.class);

        doAnswer(invocationOnMock -> {
            Node node = invocationOnMock.getArgument(0);

            if (node.getId() == null)
                node.setId(UUID.randomUUID());

            return node;
        }).when(nodeRepository)
                .save(captor.capture());

        Node node = new Node();
        node.setId(UUID.randomUUID());
        node.setPortfolio(portfolio);
        node.setSemantictag(srcetag);
        node.setCode(srcecode);

        doReturn(node)
                .when(portfolioRepository)
                .getPortfolioRootNode(portfolio.getId());

        doReturn(Collections.singletonList(node))
                .when(nodeRepository)
                .getNodes(portfolio.getId());

        Node source = new Node();
        source.setId(parentId);

        doReturn(Optional.of(source))
                .when(nodeRepository)
                .findById(parentId);

        mvc.perform(postBuilder("/nodes/node/import/" + parentId)
                .param("srcetag", srcetag)
                .param("srcecode", srcecode))
                .andExpect(status().isOk())
                .andExpect(content().string(captor.getAllValues().get(0).getId().toString()))
                .andDo(document("import-node"));
    }

    @Test
    @AsUser
    public void copyNode_WithoutRights() throws Exception {
        UUID parentId = UUID.randomUUID();

        mvc.perform(postBuilder("/nodes/node/copy/" + parentId))
                .andExpect(status().isForbidden());
    }

    @Test
    @AsUser
    public void moveNodeUp_EveythingOk() throws Exception {
        UUID nodeId = UUID.randomUUID();

        doReturn(0L)
                .when(nodeManager)
                .moveNodeUp(nodeId);

        mvc.perform(postBuilder("/nodes/node/" + nodeId + "/moveup"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andDo(document("move-node-up"));
    }

    @Test
    @AsUser
    public void moveNodeUp_FirstNode() throws Exception {
        UUID nodeId = UUID.randomUUID();

        doReturn(-2L)
                .when(nodeManager)
                .moveNodeUp(nodeId);

        mvc.perform(postBuilder("/nodes/node/" + nodeId + "/moveup"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Cannot move first node"));
    }

    @Test
    @AsUser
    public void moveNodeUp_MissingNode() throws Exception {
        UUID nodeId = UUID.randomUUID();

        doReturn(-1L)
                .when(nodeManager)
                .moveNodeUp(nodeId);

        mvc.perform(postBuilder("/nodes/node/" + nodeId + "/moveup"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Non-existing node"));
    }

    @Test
    @AsUser
    public void changeParent_AsUser() throws Exception {
        UUID nodeId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();

        mvc.perform(postBuilder("/nodes/node/" + nodeId + "/parentof/" + parentId))
                .andExpect(status().isForbidden());
    }

    @Test
    @AsAdmin
    public void changeParent_AsAdmin_WithError() throws Exception {
        UUID nodeId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();

        doReturn(false)
                .when(nodeManager)
                .changeParentNode(nodeId, parentId);

        mvc.perform(postBuilder("/nodes/node/" + nodeId + "/parentof/" + parentId))
                .andExpect(status().isConflict())
                .andExpect(content().string("Cannot move"));
    }

    @Test
    @AsAdmin
    public void changeParent_AsAdmin_WithoutError() throws Exception {
        UUID nodeId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();

        doReturn(true)
                .when(nodeManager)
                .changeParentNode(nodeId, parentId);

        mvc.perform(postBuilder("/nodes/node/" + nodeId + "/parentof/" + parentId))
                .andExpect(status().isOk())
                .andDo(document("change-node-parent"));
    }

    @Test
    @AsUser
    public void executeMacro() throws Exception {
        UUID nodeId = UUID.randomUUID();

        doReturn("")
                .when(nodeManager)
                .executeMacroOnNode(userId, nodeId, "reset");

        mvc.perform(postBuilder("/nodes/node/" + nodeId + "/action/reset"))
                .andExpect(status().isOk())
                .andExpect(content().string(""))
                .andDo(document("execute-macro"));

        verify(nodeManager).executeMacroOnNode(userId, nodeId, "reset");
    }

    @Test
    @AsUser
    public void deleteNode_AsUser_WithoutRights() throws Exception {
        UUID nodeId = UUID.randomUUID();

        mvc.perform(deleteBuilder("/nodes/node/" + nodeId))
                .andExpect(status().isForbidden());
    }

    @Test
    @AsUser
    public void deleteNode_AsUser_WithRights() throws Exception {
        UUID nodeId = UUID.randomUUID();

        GroupRights groupRights = new GroupRights();
        groupRights.setDelete(true);

        doReturn(groupRights)
                .when(nodeManager)
                .getRights(userId, nodeId);

        mvc.perform(deleteBuilder("/nodes/node/" + nodeId))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(nodeManager).removeNode(nodeId);
    }

    @Test
    @AsAdmin
    public void deleteNode_AsAdmin() throws Exception {
        UUID nodeId = UUID.randomUUID();

        mvc.perform(deleteBuilder("/nodes/node/" + nodeId))
                .andExpect(status().isOk())
                .andExpect(content().string(""))
                .andDo(document("delete-node"));

        verify(nodeManager).removeNode(nodeId);
    }
}
