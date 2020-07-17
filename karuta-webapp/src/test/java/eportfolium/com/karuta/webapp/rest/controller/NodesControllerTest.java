package eportfolium.com.karuta.webapp.rest.controller;

import eportfolium.com.karuta.webapp.rest.AsUser;
import eportfolium.com.karuta.webapp.rest.ControllerTest;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class NodesControllerTest extends ControllerTest {
    @Test
    @AsUser
    public void getNode() throws Exception {
        UUID nodeId = UUID.randomUUID();

        /*get("/nodes/node/" + nodeId)
                .andExpect(status().isOk())
                .andDo(document("get-node"));*/
    }

    @Test
    @AsUser
    public void getNodeWithChildren() throws Exception {
        UUID nodeId = UUID.randomUUID();

        /*get("/nodes/node/" + nodeId + "/children")
                .andExpect(status().isOk())
                .andDo(document("get-node-with-children"));*/
    }

    @Test
    @AsUser
    public void getNodeMetadataWad() throws Exception {
        UUID nodeId = UUID.randomUUID();

        /*get("/nodes/nodes/" + nodeId + "/metadatawad")
                .andExpect(status().isOk())
                .andDo(document("get-node-metadatawad"));*/
    }

    @Test
    @AsUser
    public void getNodeRights() throws Exception {
        UUID nodeId = UUID.randomUUID();

        /*get("/nodes/node/" + nodeId + "/rights")
                .andExpect(status().isOk())
                .andDo(document("get-node-rights"));*/
    }
}