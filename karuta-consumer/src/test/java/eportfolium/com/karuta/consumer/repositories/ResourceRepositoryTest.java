package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.consumer.RepositoryTest;
import eportfolium.com.karuta.consumer.TestHelpers;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.bean.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@RepositoryTest
public class ResourceRepositoryTest extends TestHelpers {
    @Autowired
    private ResourceRepository repository;

    @Autowired
    private NodeRepository nodeRepository;

    private Node nodeWithPortfolio() {
        Portfolio portfolio = portfolioRecord();

        Node node = savableNode();
        node.setPortfolio(portfolio);

        return node;
    }

    @Test
    public void modifDateIsUpdatedOnSave() {
        Resource resource = savableResource();

        assertNull(resource.getModifDate());

        repository.save(resource);

        assertNotNull(resource.getModifDate());
    }

    @Test
    public void getContextResourcesByPortfolioUUID() throws InterruptedException {
        Resource resource = resourceRecord();

        Node node = nodeWithPortfolio();
        node.setContextResource(resource);
        nodeRepository.save(node);

        List<Resource> resources = repository
                .getContextResourcesByPortfolioUUID(node.getPortfolio().getId());

        assertEquals(1, resources.size());
        assertEquals(resource.getId(), resources.get(0).getId());
    }

    @Test
    public void getResourcesByPortfolioUUID() {
        Resource resource = resourceRecord();

        Node node = nodeWithPortfolio();
        node.setResource(resource);
        nodeRepository.save(node);

        List<Resource> resources = repository
                .getResourcesByPortfolioUUID(node.getPortfolio().getId());

        assertEquals(1, resources.size());
        assertEquals(resource.getId(), resources.get(0).getId());
    }

    @Test
    public void getResourcesOfResourceByPortfolioUUID() {
        Resource resource = resourceRecord();

        Node node = nodeWithPortfolio();
        node.setResResource(resource);
        nodeRepository.save(node);

        List<Resource> resources = repository
                .getResourcesOfResourceByPortfolioUUID(node.getPortfolio().getId());

        assertEquals(1, resources.size());
        assertEquals(resource.getId(), resources.get(0).getId());
    }

    @Test
    public void getResourceByParentNodeUuid() {
        Resource resource = resourceRecord();

        Node node = nodeWithPortfolio();
        node.setResource(resource);
        nodeRepository.save(node);

        Resource found = repository.getResourceByParentNodeUuid(node.getId());

        assertEquals(resource.getId(), found.getId());
    }

    @Test
    public void getContextResourceByNodeUuid() {
        Resource resource = resourceRecord();

        Node node = nodeWithPortfolio();
        node.setContextResource(resource);
        nodeRepository.save(node);

        Resource found = repository.getContextResourceByNodeUuid(node.getId());

        assertEquals(resource.getId(), found.getId());
    }

    @Test
    public void getResourceOfResourceByNodeUuid() {
        Resource resource = resourceRecord();

        Node node = nodeWithPortfolio();
        node.setResResource(resource);
        nodeRepository.save(node);

        Resource found = repository.getResourceOfResourceByNodeUuid(node.getId());

        assertEquals(resource.getId(), found.getId());
    }

    @Test
    public void findByNodeId() {
        Resource resource = resourceRecord();

        Node node = nodeWithPortfolio();
        node.setResource(resource);
        nodeRepository.save(node);

        Resource found = repository.findByNodeId(node.getId());

        assertEquals(resource.getId(), found.getId());
    }
}