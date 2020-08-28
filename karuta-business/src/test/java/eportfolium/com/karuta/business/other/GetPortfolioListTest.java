package eportfolium.com.karuta.business.other;

import eportfolium.com.karuta.business.contract.PortfolioManager;
import eportfolium.com.karuta.consumer.repositories.CredentialRepository;
import eportfolium.com.karuta.consumer.repositories.NodeRepository;
import eportfolium.com.karuta.consumer.repositories.PortfolioRepository;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.Portfolio;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * This test is a bit particular. Since we are building queries dynamically, we can't rely on
 * a `@Query` annotation on the repository side so we are at the service layer but we need
 * actual database queries to be executed to test the exactness of the test.
 *
 * On the other hand, since we either fully mock repositories or use the underlying implementation
 * (i.e. `@SpyBean` is not usable), we need to separate these tests from the ones that require
 * a mock.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class GetPortfolioListTest {
    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private NodeRepository nodeRepository;

    @SpyBean
    private PortfolioManager manager;

    private Credential credential;
    private Portfolio portfolio1, portfolio2, portfolio3, portfolio4;

    private Portfolio portfolioRecord() {
        Portfolio portfolio = new Portfolio();
        portfolio.setCredential(credential);
        portfolio.setModifUserId(credential.getId());

        return portfolio;
    }

    private Node nodeRecord() {
        Node node = new Node();
        node.setMetadata("");
        node.setMetadataWad("");
        node.setMetadataEpm("");
        node.setModifUserId(credential.getId());
        node.setSharedNode(false);
        node.setSharedNodeRes(false);
        node.setSharedRes(false);

        return node;
    }

    private void isAdmin() {
        credential.setIsAdmin(1);
        credentialRepository.save(credential);
    }

    @Before
    public void setup() {
        credential = new Credential();

        credential.setLogin("jdoe");
        credential.setDisplayFirstname("John");
        credential.setDisplayLastname("Doe");
        credential.setPassword("s3cr3t");

        credentialRepository.save(credential);

        Credential otherUser = new Credential();

        otherUser.setLogin("jsmith");
        otherUser.setDisplayFirstname("Johnny");
        otherUser.setDisplayLastname("Smith");
        otherUser.setPassword("foobarbaz");

        credentialRepository.save(otherUser);

        portfolio1 = portfolioRecord();
        portfolio1.setActive(1);

        portfolio2 = portfolioRecord();
        portfolio2.setActive(1);

        portfolio3 = portfolioRecord();
        portfolio3.setCredential(otherUser);
        portfolio3.setModifUserId(otherUser.getId());
        portfolio3.setActive(1);

        portfolio4 = portfolioRecord();
        portfolio4.setActive(0);

        portfolioRepository.saveAll(Arrays.asList(portfolio1, portfolio2, portfolio3, portfolio4));

        // Set a rootNode with a code for `portfolio1`
        Node codeNode = nodeRecord();
        codeNode.setPortfolio(portfolio1);
        codeNode.setCode("foo-bar-baz");

        // Set a rootNode with a semantic tag for `portfolio2`
        Node projectNode = nodeRecord();
        projectNode.setPortfolio(portfolio2);
        projectNode.setSemantictag("it-is-a-karuta-project-thing");

        nodeRepository.saveAll(Arrays.asList(codeNode, projectNode));

        portfolio1.setRootNode(codeNode);
        portfolio2.setRootNode(projectNode);
        portfolioRepository.saveAll(Arrays.asList(portfolio1, portfolio2));
    }

    @Test
    public void getPortfolioList_AsAdmin() {
        isAdmin();

        List<Portfolio> portfolios = manager.getPortfolioList(credential.getId(), true, null, null);

        assertEquals(3, portfolios.size());

        assertNotNull(portfolio1.getId());
        assertNotNull(portfolio2.getId());
        assertNotNull(portfolio3.getId());

        // We can't realy check the result order because `modifDate` on portfolio
        // is set automatically on save (c.f. AuditListener in karuta-model).
        List<UUID> ids = Stream.of(portfolio1, portfolio2, portfolio3).map(Portfolio::getId)
                .collect(Collectors.toList());

        assertTrue(portfolios.stream()
                .map(Portfolio::getId).collect(Collectors.toList()).containsAll(ids));
    }

    @Test
    public void getPortfolioList_AsRegularUser() {
        List<Portfolio> portfolios = manager.getPortfolioList(credential.getId(), true, null, null);

        assertEquals(2, portfolios.size());

        assertNotNull(portfolio1.getId());
        assertNotNull(portfolio2.getId());

        List<UUID> ids = Stream.of(portfolio1, portfolio2).map(Portfolio::getId)
                .collect(Collectors.toList());

        assertTrue(portfolios.stream()
                .map(Portfolio::getId).collect(Collectors.toList()).containsAll(ids));
    }

    @Test
    public void getPortfolioList_Inactive_AsAdmin() {
        isAdmin();

        List<Portfolio> portfolios = manager.getPortfolioList(credential.getId(), false, null, null);

        assertEquals(1, portfolios.size());
        assertEquals(portfolio4.getId(), portfolios.get(0).getId());
    }

    @Test
    public void getPortfolioList_Inactive_AsRegularUser() {
        List<Portfolio> portfolios = manager.getPortfolioList(credential.getId(), false, null, null);

        assertEquals(1, portfolios.size());
        assertEquals(portfolio4.getId(), portfolios.get(0).getId());
    }

    @Test
    public void getPortfolioList_SpecialProject_AsAdmin() {
        isAdmin();

        List<Portfolio> portfolios = manager.getPortfolioList(credential.getId(), true, true, null);

        assertEquals(1, portfolios.size());
        assertEquals(portfolio2.getId(), portfolios.get(0).getId());
    }

    @Test
    public void getPortfolioList_SpecialProject_AsRegularUser() {
        List<Portfolio> portfolios = manager.getPortfolioList(credential.getId(), true, true, null);

        assertEquals(1, portfolios.size());
        assertEquals(portfolio2.getId(), portfolios.get(0).getId());
    }

    @Test
    public void getPortfolioList_WithCode_AsAdmin() {
        isAdmin();

        List<Portfolio> portfolios = manager.getPortfolioList(credential.getId(), true, null, "bar");

        assertEquals(1, portfolios.size());
        assertEquals(portfolio1.getId(), portfolios.get(0).getId());
    }

    @Test
    public void getPortfolioList_WithCode_AsRegularUser() {
        List<Portfolio> portfolios = manager.getPortfolioList(credential.getId(), true, null, "bar");

        assertEquals(1, portfolios.size());
        assertEquals(portfolio1.getId(), portfolios.get(0).getId());
    }
}
