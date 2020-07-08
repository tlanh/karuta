package eportfolium.com.karuta.webapp.rest.controller;

import eportfolium.com.karuta.document.ResourceDocument;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.Resource;
import eportfolium.com.karuta.webapp.rest.AsUser;
import eportfolium.com.karuta.webapp.rest.ControllerTest;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.UUID;

import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
public class ResourcesControllerTest extends ControllerTest {
    @Test
    @AsUser
    public void getResource_WithRights() throws Exception {
        UUID nodeId = UUID.randomUUID();

        doReturn(true)
                .when(resourceManager)
                .hasRight(userId, nodeId, GroupRights.READ);

        Resource resource = new Resource();
        resource.setId(UUID.randomUUID());
        resource.setXsiType("nodeRes");

        doReturn(resource)
                .when(resourceRepository)
                .getResourceByParentNodeUuid(nodeId);

        get("/resources/resource/" + nodeId)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<asmResource id=\"" + resource.getId() + "\"")))
                .andExpect(content().string(containsString(" contextid=\"" + nodeId +"\"")))
                .andExpect(content().string(containsString("<content>nodeRes</content></asmResource>")))
                .andDo(document("get-resource"));
    }

    @Test
    @AsUser
    public void getResource_WithoutRights() throws Exception {
        UUID nodeId = UUID.randomUUID();

        doReturn(false)
                .when(resourceManager)
                .hasRight(userId, nodeId, GroupRights.READ);

        get("/resources/resource/" + nodeId)
                .andExpect(status().isForbidden());
    }

    @Test
    @AsUser
    public void getResourceFile_WithRights() throws Exception {
        UUID nodeId = UUID.randomUUID();

        doReturn(true)
                .when(resourceManager)
                .hasRight(userId, nodeId, GroupRights.READ);

        CloseableHttpClient client = mock(CloseableHttpClient.class);

        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        HttpEntity entity = mock(HttpEntity.class);

        doReturn(new ByteArrayInputStream("Hello world!".getBytes()))
                .when(entity)
                .getContent();

        doReturn(entity)
                .when(response)
                .getEntity();

        doReturn(response)
                .when(client)
                .execute(any(HttpGet.class));

        doReturn(client)
                .when(fileManager)
                .createClient();

        Node node = new Node();
        node.setId(nodeId);

        Resource resource = new Resource();
        resource.setNode(node);
        resource.setXsiType("nodeRes");
        resource.setModifDate(new Date());
        resource.setContent("<filename lang=\"fr\" value=\"foo.txt\"/>" +
                "<filename lang=\"en\" value=\"bar.txt\" />" +
                "<type lang=\"fr\" value=\"text/plain\" />");

        doReturn(resource)
                .when(resourceRepository)
                .findByNodeId(nodeId);

        get("/resources/resource/file/" + nodeId)
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/plain"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"foo.txt\""))
                .andDo(document("get-resource-file"));
    }

    @Test
    @AsUser
    public void updateResourceFile() throws Exception {
        UUID parentNodeId = UUID.randomUUID();

        doReturn(true)
                .when(resourceManager)
                .hasRight(userId, parentNodeId, GroupRights.WRITE);

        Node node = new Node();

        Resource resource = new Resource();
        resource.setNode(node);

        doReturn(resource)
                .when(resourceRepository)
                .findByNodeId(parentNodeId);

        CloseableHttpClient client = mock(CloseableHttpClient.class);

        doReturn(mock(CloseableHttpResponse.class))
                .when(client)
                .execute(any(HttpPut.class));

        doReturn(client)
                .when(fileManager)
                .createClient();

        mvc.perform(multipart("/resources/resource/file/" + parentNodeId)
                    .file("foo.txt", "Hello world !".getBytes())
                    .with(request -> {
                        request.setMethod("PUT");
                        return request;
                    }))
                .andExpect(status().isOk())
                .andExpect(content().string("Updated"))
                .andDo(document("update-resource-file"));
    }

    @Test
    @AsUser
    public void updateResource() throws Exception {
        UUID parentNodeId = UUID.randomUUID();

        doReturn(true)
                .when(resourceManager)
                .hasRight(userId, parentNodeId, GroupRights.WRITE);

        doReturn(new Resource())
                .when(resourceRepository)
                .getResourceByParentNodeUuid(parentNodeId);

        String xml = "<asmResource id=\"" + parentNodeId +"\" xsi_type=\"nodeRes\">" +
                    "<label lang=\"fr\">Nouvelle section</label>" +
                    "<label lang=\"en\">New section</label>" +
                "</asmResource>";

        mvc.perform(putBuilder("/resources/resource/" + parentNodeId)
                    .content(xml))
                .andExpect(status().isOk())
                .andExpect(content().string("0"))
                .andDo(document("update-resource"));

        verify(resourceManager).changeResource(eq(parentNodeId), any(ResourceDocument.class), eq(userId));
    }

    @Test
    @AsUser
    public void addResource() throws Exception {
        UUID parentNodeId = UUID.randomUUID();

        doReturn(true)
                .when(resourceManager)
                .hasRight(userId, parentNodeId, GroupRights.WRITE);

        String xml = "<asmResource id=\"" + parentNodeId +"\" xsi_type=\"nodeRes\">" +
                    "<label lang=\"fr\">Nouvelle section</label>" +
                    "<label lang=\"en\">New section</label>" +
                "</asmResource>";

        post("/resources/" + parentNodeId, xml)
                .andExpect(status().isOk())
                .andExpect(content().string(""))
                .andDo(document("add-resource"));

        verify(resourceManager).addResource(eq(parentNodeId), any(ResourceDocument.class), eq(userId));
    }

    @Test
    @AsUser
    public void addResourceAlternative() throws Exception {
        UUID parentNodeId = UUID.randomUUID();

        String xml = "<asmResource id=\"" + parentNodeId +"\" xsi_type=\"nodeRes\">" +
                    "<label lang=\"fr\">Nouvelle section</label>" +
                    "<label lang=\"en\">New section</label>" +
                "</asmResource>";

        doReturn(true)
                .when(resourceManager)
                .hasRight(userId, parentNodeId, GroupRights.WRITE);

        mvc.perform(postBuilder("/resources")
                    .param("resource", parentNodeId.toString())
                    .content(xml)
                    .contentType(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andDo(document("add-resource-alternative"));

        verify(resourceManager).addResource(eq(parentNodeId), any(ResourceDocument.class), eq(userId));
    }

    @Test
    @AsUser
    public void deleteResource() throws Exception {
        UUID id = UUID.randomUUID();

        doReturn(true)
                .when(resourceManager)
                .hasRight(userId, id, GroupRights.DELETE);

        mvc.perform(deleteBuilder("/resources/" + id))
                .andExpect(status().isOk())
                .andExpect(content().string(""))
                .andDo(document("delete-resource"));

        verify(resourceRepository).deleteById(id);
    }
}
