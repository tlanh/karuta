package eportfolium.com.karuta.webapp.rest.controller;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eportfolium.com.karuta.document.PortfolioDocument;
import eportfolium.com.karuta.document.PortfolioList;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.webapp.rest.AsAdmin;
import eportfolium.com.karuta.webapp.rest.AsUser;
import eportfolium.com.karuta.webapp.rest.ControllerTest;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
public class PortfolioControllerTest extends ControllerTest {
    private final XmlMapper xmlMapper = new XmlMapper();

    @Test
    @AsUser
    public void getPortfolioById_WithoutAnyParameters() throws Exception {
        Node node = new Node();
        node.setId(UUID.randomUUID());

        Portfolio portfolio = new Portfolio();
        portfolio.setId(UUID.randomUUID());
        portfolio.setRootNode(node);

        doReturn(xmlMapper.writeValueAsString(new PortfolioDocument(portfolio, false)))
                .when(portfolioManager)
                .getPortfolio(portfolio.getId(), userId, null);

        get("/portfolios/portfolio/" + portfolio.getId())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/xml;charset=UTF-8"))
                .andDo(document("get-portfolio-by-id-default"));
    }

    @Test
    @AsUser
    public void getPortfolioById_WithExport() throws Exception {
        Node rootNode = new Node();
        rootNode.setCode("my-super-portfolio");

        Portfolio portfolio = new Portfolio();
        portfolio.setId(UUID.randomUUID());
        portfolio.setRootNode(rootNode);

        doReturn(xmlMapper.writeValueAsString(new PortfolioDocument(portfolio, false)))
                .when(portfolioManager)
                .getPortfolio(portfolio.getId(), userId, null);

        get("/portfolios/portfolio/" + portfolio.getId() + "?export=true")
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=")))
                .andDo(document("get-portfolio-by-id-export"));
    }

    @Test
    @AsUser
    public void getPortfolioById_WithFiles() throws Exception {
        Node rootNode = new Node();
        rootNode.setCode("my-super-portfolio");

        Portfolio portfolio = new Portfolio();
        portfolio.setId(UUID.randomUUID());
        portfolio.setRootNode(rootNode);

        doReturn(xmlMapper.writeValueAsString(new PortfolioDocument(portfolio, false)))
                .when(portfolioManager)
                .getPortfolio(portfolio.getId(), userId, null);

        doReturn(new ByteArrayOutputStream())
                .when(portfolioManager)
                .getZippedPortfolio(any(PortfolioDocument.class), eq(null), eq(""));

        get("/portfolios/portfolio/" + portfolio.getId() + "?files=true")
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=")))
                .andExpect(header().string("Content-Type", "application/octet-stream"))
                .andDo(document("get-portfolio-by-id-files"));
    }

    @Test
    @AsUser
    public void getPortfolioByCode_WithoutResources() throws Exception {
        String code = "karuta.project";

        doReturn("<portfolio code=\"" + code + "\"></portfolio>")
                .when(portfolioManager)
                .getPortfolioByCode(code, userId, false);

        get("/portfolios/portfolio/code/" + code)
                .andExpect(status().isOk())
                .andDo(document("get-portfolio-by-code"));
    }

    @Test
    @AsUser
    public void getPortfolioByCode_WithMissingPortfolio() throws Exception {
        String code = "karuta.project";

        doReturn(null)
                .when(portfolioManager)
                .getPortfolioByCode(code, userId, false);

        get("/portfolios/portfolio/code/" + code)
                .andExpect(status().isNotFound());
    }

    @Test
    @AsAdmin
    public void setPortfolioOwner_AsAdmin() throws Exception {
        UUID portfolioId = UUID.randomUUID();
        long ownerId = 46L;

        doReturn(true)
                .when(securityManager)
                .isAdmin(userId);

        Portfolio portfolio = new Portfolio();
        portfolio.setRootNode(new Node());

        doReturn(Optional.of(portfolio))
                .when(portfolioRepository)
                .findById(portfolioId);

        mvc.perform(putBuilder("/portfolios/portfolio/" + portfolioId + "/setOwner/" + ownerId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"))
                .andDo(document("set-portfolio-owner"));

        verify(portfolioManager).changePortfolioOwner(portfolioId, ownerId);
    }

    @Test
    @AsUser
    public void setPortfolioOwner_AsOwner() throws Exception {
        UUID portfolioId = UUID.randomUUID();
        long ownerId = 46L;

        doReturn(true)
                .when(portfolioRepository)
                .isOwner(portfolioId, userId);

        Portfolio portfolio = new Portfolio();
        portfolio.setRootNode(new Node());

        doReturn(Optional.of(portfolio))
                .when(portfolioRepository)
                .findById(portfolioId);

        mvc.perform(putBuilder("/portfolios/portfolio/" + portfolioId + "/setOwner/" + ownerId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(portfolioManager).changePortfolioOwner(portfolioId, ownerId);
    }

    @Test
    @AsUser
    public void setPortfolioOwner_AsRegularUser() throws Exception {
        UUID portfolioId = UUID.randomUUID();
        long ownerId = 46L;

        mvc.perform(putBuilder("/portfolios/portfolio/" + portfolioId + "/setOwner/" + ownerId))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(portfolioManager, times(0)).changePortfolioOwner(portfolioId, ownerId);
    }

    @Test
    @AsAdmin
    public void definePortfolioAsActive_AsAdmin() throws Exception {
        UUID portfolioId = UUID.randomUUID();

        mvc.perform(putBuilder("/portfolios/portfolio/" + portfolioId)
                    .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(""))
                .andDo(document("define-portfolio-as-active"));

        verify(portfolioManager).changePortfolioConfiguration(portfolioId, true);
    }

    @Test
    @AsAdmin
    public void definePortfolioAsInactive_AsAdmin() throws Exception {
        UUID portfolioId = UUID.randomUUID();

        mvc.perform(putBuilder("/portfolios/portfolio/" + portfolioId)
                    .param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(portfolioManager).changePortfolioConfiguration(portfolioId, false);
    }

    @Test
    @AsUser
    public void definePortfolioAsActive_AsUser() throws Exception {
        UUID portfolioId = UUID.randomUUID();

        mvc.perform(putBuilder("/portfolios/portfolio/" + portfolioId)
                    .param("active", "true"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(portfolioManager);
    }

    @Test
    @AsUser
    public void instanciate_AsUser() throws Exception {
        mvc.perform(postBuilder("/portfolios/instanciate/null")
                .param("targetcode", "foo"))
                .andExpect(status().isForbidden());
    }

    @Test
    @AsAdmin
    public void instanciate_AsAdmin_WithAvailableCode() throws Exception {
        String targetcode = "foobar";

        UUID baseId = UUID.randomUUID();
        UUID newId = UUID.randomUUID();

        doReturn(false)
                .when(nodeManager)
                .isCodeExist(targetcode);

        doReturn(newId.toString())
                .when(portfolioManager)
                .instanciatePortfolio(baseId.toString(), null, targetcode);

        mvc.perform(postBuilder("/portfolios/instanciate/" + baseId.toString())
                .param("targetcode", targetcode))
                .andExpect(status().isOk())
                .andExpect(content().string(newId.toString()))
                .andDo(document("instanciate-with-id"));

        verify(portfolioManager).instanciatePortfolio(baseId.toString(), null, targetcode);
    }

    @Test
    @AsAdmin
    public void instanciate_AsAdmin_WithExistingCode() throws Exception {
        String targetcode = "foobar";
        String newcode = "foobar (1)";

        UUID baseId = UUID.randomUUID();
        UUID newId = UUID.randomUUID();

        doReturn(true)
                .when(nodeManager)
                .isCodeExist(targetcode);

        doReturn(newId.toString())
                .when(portfolioManager)
                .instanciatePortfolio(baseId.toString(), null, newcode);

        mvc.perform(postBuilder("/portfolios/instanciate/" + baseId.toString())
                .param("targetcode", targetcode))
                .andExpect(status().isOk())
                .andExpect(content().string(newId.toString()));

        verify(portfolioManager).instanciatePortfolio(baseId.toString(), null, newcode);
    }

    @Test
    @AsAdmin
    public void instanciate_AsAdmin_WithSourceCode_AndAvailableTarget() throws Exception {
        String sourcecode = "my-portfolio";
        String targetcode = "foobar";

        UUID newId = UUID.randomUUID();

        doReturn(false)
                .when(nodeManager)
                .isCodeExist(targetcode);

        doReturn(newId.toString())
                .when(portfolioManager)
                .instanciatePortfolio("null", sourcecode, targetcode);

        mvc.perform(postBuilder("/portfolios/instanciate/null")
                .param("sourcecode", sourcecode)
                .param("targetcode", targetcode))
                .andExpect(status().isOk())
                .andExpect(content().string(newId.toString()))
                .andDo(document("instanciate-with-code"));

        verify(portfolioManager).instanciatePortfolio("null", sourcecode, targetcode);
    }

    @Test
    @AsAdmin
    public void instanciate_AsAdmin_WithSourceCode_AndExistingTarget() throws Exception {
        String sourcecode = "my-portfolio";
        String targetcode = "foobar";
        String newcode = "foobar (1)";

        UUID newId = UUID.randomUUID();

        doReturn(true)
                .when(nodeManager)
                .isCodeExist(targetcode);

        doReturn(newId.toString())
                .when(portfolioManager)
                .instanciatePortfolio("null", sourcecode, newcode);

        mvc.perform(postBuilder("/portfolios/instanciate/null")
                .param("sourcecode", sourcecode)
                .param("targetcode", targetcode))
                .andExpect(status().isOk())
                .andExpect(content().string(newId.toString()));

        verify(portfolioManager).instanciatePortfolio("null", sourcecode, newcode);
    }

    @Test
    @AsUser
    public void copy_AsUser() throws Exception {
        this.mvc.perform(postBuilder("/portfolios/copy/null")
                .param("targetcode", "foobar"))
                .andExpect(status().isForbidden());
    }

    @Test
    @AsAdmin
    public void copy_AsAdmin_WithId_AndAvailableTarget() throws Exception {
        String targetcode = "foobar";

        UUID baseId = UUID.randomUUID();
        UUID newId = UUID.randomUUID();

        doReturn(false)
                .when(nodeManager)
                .isCodeExist(targetcode);

        doReturn(newId)
                .when(portfolioManager)
                .copyPortfolio(baseId.toString(), null, targetcode, userId);

        this.mvc.perform(postBuilder("/portfolios/copy/" + baseId.toString())
                .param("targetcode", targetcode))
                .andExpect(status().isOk())
                .andExpect(content().string(newId.toString()))
                .andDo(document("copy-with-id"));

        verify(portfolioManager).copyPortfolio(baseId.toString(), null, targetcode, userId);
    }

    @Test
    @AsAdmin
    public void copy_AsAdmin_WithId_AndExistingTarget() throws Exception {
        String targetcode = "foobar";

        UUID baseId = UUID.randomUUID();

        doReturn(true)
                .when(nodeManager)
                .isCodeExist(targetcode);

        this.mvc.perform(postBuilder("/portfolios/copy/" + baseId.toString())
                .param("targetcode", targetcode))
                .andExpect(status().isConflict());

        verifyNoInteractions(portfolioManager);
    }

    @Test
    @AsAdmin
    public void copy_AsAdmin_WithCode_AndAvailableTarget() throws Exception {
        String sourcecode = "my-portfolio";
        String targetcode = "foobar";

        UUID newId = UUID.randomUUID();

        doReturn(false)
                .when(nodeManager)
                .isCodeExist(targetcode);

        doReturn(newId)
                .when(portfolioManager)
                .copyPortfolio("null", sourcecode, targetcode, userId);

        this.mvc.perform(postBuilder("/portfolios/copy/null")
                .param("sourcecode", sourcecode)
                .param("targetcode", targetcode))
                .andExpect(status().isOk())
                .andExpect(content().string(newId.toString()))
                .andDo(document("copy-with-code"));

        verify(portfolioManager).copyPortfolio("null", sourcecode, targetcode, userId);
    }

    @Test
    @AsAdmin
    public void copy_AsAdmin_WithCode_AndExistingTarget() throws Exception {
        String sourcecode = "my-portfolio";
        String targetcode = "foobar";

        doReturn(true)
                .when(nodeManager)
                .isCodeExist(targetcode);

        this.mvc.perform(postBuilder("/portfolios/copy/null")
                .param("sourcecode", sourcecode)
                .param("targetcode", targetcode))
                .andExpect(status().isConflict());

        verifyNoInteractions(portfolioManager);
    }

    @Test
    @AsAdmin
    public void getPortfolioShared_AsAdmin() throws Exception {
        Long userId = 46L;

        Map<String, Object> first = new HashMap<>();
        Map<String, Object> second = new HashMap<>();

        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();

        first.put("portfolio", firstId);
        first.put("gid", 89L);

        second.put("portfolio", secondId);
        second.put("gid", 112L);

        doReturn(Arrays.asList(first, second))
                .when(portfolioRepository)
                .getPortfolioShared(userId);

        mvc.perform(postBuilder("/portfolios/shared/" + userId))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string(containsString("<portfolios count=\"2\">")))
                .andExpect(content()
                        .string(containsString("<portfolio id=\"" + firstId +"\" gid=\"89\" ")))
                .andExpect(content()
                        .string(containsString("<portfolio id=\"" + secondId +"\" gid=\"112\" ")))
                .andDo(document("get-portfolios-shared"));

        verify(portfolioManager).getPortfolioShared(userId);
    }

    @Test
    @AsUser
    public void getPortfolioShared_AsUser() throws Exception {
        long userId = 46L;

        mvc.perform(postBuilder("/portfolios/shared/" + userId))
                .andExpect(status().isForbidden());

        verifyNoInteractions(portfolioManager);
    }

    @Test
    @AsUser
    public void importPortfolio_AsUser() throws Exception {
        MockMultipartFile emptyZip = new MockMultipartFile("uploadfile", "portfolios.zip", "application/zip",
                "".getBytes());

        mvc.perform(multipart("/portfolios/zip")
                .file(emptyZip))
                .andExpect(status().isForbidden());
    }

    @Test
    @AsAdmin
    public void importPortfolio_AsAdmin() throws Exception {
        MockMultipartFile emptyZip = new MockMultipartFile("uploadfile", "portfolios.zip", "application/zip",
                "".getBytes());

        UUID returnedId = UUID.randomUUID();

        doReturn(returnedId)
                .when(portfolioManager)
                .importPortfolio(any(InputStream.class), eq(userId), eq(""));

        mvc.perform(multipart("/portfolios/zip")
                .file(emptyZip))
                .andExpect(status().isOk())
                .andExpect(content().string(returnedId.toString()))
                .andDo(document("import-zip"));
    }

    @Test
    @AsUser
    public void deletePortfolio_AsUser_WithoutRights() throws Exception {
        UUID portfolioId = UUID.randomUUID();

        GroupRights groupRights = new GroupRights();
        groupRights.setDelete(false);

        doReturn(groupRights)
                .when(portfolioManager)
                .getRightsOnPortfolio(userId, portfolioId);

        mvc.perform(deleteBuilder("/portfolios/portfolio/" + portfolioId.toString()))
                .andExpect(status().isNotFound());

        verifyNoInteractions(groupRightInfoRepository);
        verifyNoInteractions(resourceRepository);
        verifyNoInteractions(nodeRepository);
        verifyNoInteractions(portfolioRepository);
    }

    @Test
    @AsUser
    public void deletePortfolio_AsUser_WithRights() throws Exception {
        UUID portfolioId = UUID.randomUUID();

        GroupRights groupRights = new GroupRights();
        groupRights.setDelete(true);

        doReturn(groupRights)
                .when(portfolioManager)
                .getRightsOnPortfolio(userId, portfolioId);

        mvc.perform(deleteBuilder("/portfolios/portfolio/" + portfolioId.toString()))
                .andExpect(status().isOk());

        verify(groupRightInfoRepository).deleteAll(anyIterable());
        verify(resourceRepository, times(3)).deleteAll(anyIterable());
        verify(nodeRepository).deleteAll(anyIterable());

        verify(portfolioRepository).deleteById(portfolioId);
    }

    @Test
    @AsAdmin
    public void deletePortfolio_AsAdmin() throws Exception {
        UUID portfolioId = UUID.randomUUID();

        doReturn(true)
                .when(credentialRepository)
                .isAdmin(userId);

        mvc.perform(deleteBuilder("/portfolios/portfolio/" + portfolioId.toString()))
                .andExpect(status().isOk())
                .andDo(document("delete-portfolio"));

        verify(groupRightInfoRepository).deleteAll(anyIterable());
        verify(resourceRepository, times(3)).deleteAll(anyIterable());
        verify(nodeRepository).deleteAll(anyIterable());

        verify(portfolioRepository).deleteById(portfolioId);
    }

    @Test
    @AsUser
    public void postPortfolio_AsUser() throws Exception {
        MockMultipartFile xmlFile = new MockMultipartFile("uploadfile", "portfolio.xml", "",
                "<portfolio code=\"foobar\"></portfolio>".getBytes());

        mvc.perform(multipart("/portfolios")
                .file(xmlFile))
                .andExpect(status().isForbidden());
    }

    @Test
    @AsAdmin
    public void postPortfolio_AsAdmin() throws Exception {
        ArgumentCaptor<PortfolioDocument> captor = ArgumentCaptor.forClass(PortfolioDocument.class);

        MockMultipartFile xmlFile = new MockMultipartFile("uploadfile", "portfolio.xml", "",
                "<portfolio code=\"foobar\"></portfolio>".getBytes());

        doReturn(new PortfolioList(1L))
                .when(portfolioManager)
                .addPortfolio(any(PortfolioDocument.class), eq(userId), eq(""));

        mvc.perform(multipart("/portfolios")
                .file(xmlFile))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<portfolios")))
                .andDo(document("add-portfolio"));

        verify(portfolioManager).addPortfolio(captor.capture(), eq(userId), eq(""));

        assertEquals("foobar", captor.getValue().getCode());
    }
}
