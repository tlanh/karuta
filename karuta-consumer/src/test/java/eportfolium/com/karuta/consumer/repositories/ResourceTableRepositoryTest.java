package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.consumer.RepositoryTest;
import eportfolium.com.karuta.consumer.TestHelpers;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.bean.ResourceTable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@RepositoryTest
public class ResourceTableRepositoryTest extends TestHelpers {
    @Autowired
    private ResourceTableRepository repository;

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
        ResourceTable resource = savableResource();

        assertNull(resource.getModifDate());

        repository.save(resource);

        assertNotNull(resource.getModifDate());
    }

    @Test
    public void getContextResourcesByPortfolioUUID() throws InterruptedException {
        ResourceTable resource = resourceRecord();

        Node node = nodeWithPortfolio();
        node.setContextResource(resource);
        nodeRepository.save(node);

        List<ResourceTable> resources = repository
                .getContextResourcesByPortfolioUUID(node.getPortfolio().getId());

        assertEquals(1, resources.size());
        assertEquals(resource.getId(), resources.get(0).getId());
    }

    @Test
    public void getResourcesByPortfolioUUID() {
        ResourceTable resource = resourceRecord();

        Node node = nodeWithPortfolio();
        node.setResource(resource);
        nodeRepository.save(node);

        List<ResourceTable> resources = repository
                .getResourcesByPortfolioUUID(node.getPortfolio().getId());

        assertEquals(1, resources.size());
        assertEquals(resource.getId(), resources.get(0).getId());
    }

    @Test
    public void getResourcesOfResourceByPortfolioUUID() {
        ResourceTable resource = resourceRecord();

        Node node = nodeWithPortfolio();
        node.setResResource(resource);
        nodeRepository.save(node);

        List<ResourceTable> resources = repository
                .getResourcesOfResourceByPortfolioUUID(node.getPortfolio().getId());

        assertEquals(1, resources.size());
        assertEquals(resource.getId(), resources.get(0).getId());
    }

    @Test
    public void getResourceByParentNodeUuid() {
        ResourceTable resource = resourceRecord();

        Node node = nodeWithPortfolio();
        node.setResource(resource);
        nodeRepository.save(node);

        ResourceTable found = repository.getResourceByParentNodeUuid(node.getId());

        assertEquals(resource.getId(), found.getId());
    }

    @Test
    public void getContextResourceByNodeUuid() {
        ResourceTable resource = resourceRecord();

        Node node = nodeWithPortfolio();
        node.setContextResource(resource);
        nodeRepository.save(node);

        ResourceTable found = repository.getContextResourceByNodeUuid(node.getId());

        assertEquals(resource.getId(), found.getId());
    }

    @Test
    public void getResourceOfResourceByNodeUuid() {
        ResourceTable resource = resourceRecord();

        Node node = nodeWithPortfolio();
        node.setResResource(resource);
        nodeRepository.save(node);

        ResourceTable found = repository.getResourceOfResourceByNodeUuid(node.getId());

        assertEquals(resource.getId(), found.getId());
    }
}