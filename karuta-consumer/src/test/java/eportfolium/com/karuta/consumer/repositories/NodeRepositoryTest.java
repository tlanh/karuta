package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.consumer.RepositoryTest;
import eportfolium.com.karuta.consumer.TestHelpers;
import eportfolium.com.karuta.model.bean.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@RepositoryTest
public class NodeRepositoryTest extends TestHelpers {
    @Autowired
    private NodeRepository repository;

    @Autowired
    private GroupRightInfoRepository groupRightInfoRepository;

    @Autowired
    private GroupInfoRepository groupInfoRepository;

    @Autowired
    private GroupUserRepository groupUserRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    public void modifDateIsUpdatedOnSave() {
        Node node = savableNode();

        assertNull(node.getModifDate());

        repository.save(node);

        assertNotNull(node.getModifDate());
    }

    @Test
    public void isPublic() {
        Credential credential = credentialRecord("sys_public");
        Portfolio portfolio = portfolioRecord();

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setPortfolio(portfolio);
        groupRightInfo.setLabel("all");
        groupRightInfoRepository.save(groupRightInfo);

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setLabel("");
        groupInfo.setGroupRightInfo(groupRightInfo);
        groupInfoRepository.save(groupInfo);

        GroupUser groupUser = new GroupUser();
        groupUser.setId(new GroupUserId());
        groupUser.setGroupInfo(groupInfo);
        groupUser.setCredential(credential);
        groupUserRepository.save(groupUser);

        Node node = savableNode();
        node.setPortfolio(portfolio);
        repository.save(node);

        assertTrue(repository.isPublic(node.getId()));

        credential.setLogin("foo");
        credentialRepository.save(credential);

        assertFalse(repository.isPublic(node.getId()));

        credential.setLogin("sys_public");
        credentialRepository.save(credential);

        groupRightInfo.setLabel("");
        groupRightInfoRepository.save(groupRightInfo);

        assertFalse(repository.isPublic(node.getId()));
    }

    @Test
    public void getNodesWithResources() {
        Portfolio portfolio = portfolioRecord();

        Node node1 = savableNode();
        Node node3 = savableNode();

        node1.setPortfolio(portfolio);

        repository.save(node1);
        repository.save(node3);

        assertNull(node3.getPortfolio());

        List<Node> nodes = repository.getNodesWithResources(portfolio.getId());

        assertTrue(nodes.stream().anyMatch(n -> n.getId().equals(node1.getId())));
        assertFalse(nodes.stream().anyMatch(n -> n.getId().equals(node3.getId())));
    }

    @Test
    public void getNodes_FromPortfolioId() {
        Portfolio portfolio = portfolioRecord();

        Node node1 = savableNode();
        Node node2 = savableNode();

        node1.setPortfolio(portfolio);

        repository.saveAll(Arrays.asList(node1, node2));

        assertNull(node2.getPortfolio());

        List<Node> nodes = repository.getNodes(portfolio.getId());

        assertEquals(1, nodes.size());
        assertEquals(node1.getId(), nodes.get(0).getId());
    }

    @Test
    public void getNodes_FromIdsList() {
        Node node = savableNode();
        repository.save(node);

        List<UUID> ids = Arrays.asList(UUID.randomUUID(), node.getId());

        List<Node> nodes = repository.getNodes(ids);

        assertEquals(1, nodes.size());
        assertEquals(node.getId(), nodes.get(0).getId());
    }

    @Test
    public void getSharedNodes() {
        Portfolio portfolio = portfolioRecord();

        Node node1 = savableNode();
        Node node2 = savableNode();
        Node node3 = savableNode();

        node1.setSharedNode(true);
        node1.setPortfolio(portfolio);

        node2.setSharedNode(false);
        node2.setPortfolio(portfolio);

        node3.setSharedNode(true);

        repository.saveAll(Arrays.asList(node1, node2, node3));

        List<Node> nodes = repository.getSharedNodes(portfolio.getId());

        assertTrue(nodes.stream().anyMatch(n -> n.getId().equals(node1.getId())));

        assertFalse(nodes.stream().anyMatch(n -> n.getId().equals(node2.getId())));
        assertFalse(nodes.stream().anyMatch(n -> n.getId().equals(node3.getId())));
    }

    @Test
    public void getRootNodeByPortfolio() {
        Portfolio portfolio = portfolioRecord();

        Node node = savableNode();
        node.setAsmType("asmRoot");
        node.setPortfolio(portfolio);
        repository.save(node);

        Node found = repository.getRootNodeByPortfolio(portfolio.getId());

        assertEquals(node.getId(), found.getId());

        node.setAsmType("");
        repository.save(node);

        assertNull(repository.getRootNodeByPortfolio(portfolio.getId()));
    }

    @Test
    public void isCodeExist() {
        String code = "foo";

        Node node = savableNode();
        node.setAsmType("asmRoot");
        node.setCode(code);
        repository.save(node);

        assertTrue(repository.isCodeExist(code));

        node.setAsmType("");
        repository.save(node);

        assertFalse(repository.isCodeExist(code));
    }

    @Test
    public void isCodeExist_WithNodeId() {
        String code = "foo";
        Portfolio portfolio = portfolioRecord();

        Node node = savableNode();
        node.setAsmType("asmRoot");
        node.setCode(code);
        node.setPortfolio(portfolio);

        Node otherNode = savableNode();
        otherNode.setPortfolio(portfolio);

        repository.saveAll(Arrays.asList(node, otherNode));

        assertTrue(repository.isCodeExist(code, otherNode.getId()));
        assertFalse(repository.isCodeExist(code, node.getId()));
    }

    @Test
    public void getFirstLevelChildren() {
        Node parent = savableNode();
        repository.save(parent);

        Node child1 = savableNode();
        Node child2 = savableNode();

        child1.setNodeOrder(2);
        child2.setNodeOrder(1);

        child1.setParentNode(parent);
        child2.setParentNode(parent);

        repository.saveAll(Arrays.asList(child1, child2));

        List<Node> nodes = repository.getFirstLevelChildren(parent.getId());

        assertEquals(2, nodes.size());

        // The list order *does* matter.
        assertEquals(child2.getId(), nodes.get(0).getId());
        assertEquals(child1.getId(), nodes.get(1).getId());
    }

    @Test
    public void getNodesBySemanticTag() {
        Portfolio portfolio = portfolioRecord();

        Node node = savableNode();
        node.setPortfolio(portfolio);
        node.setSemantictag("foobarbaz");
        repository.save(node);

        List<Node> nodes = repository
                .getNodesBySemanticTag(portfolio.getId(), "bar");

        assertEquals(1, nodes.size());
        assertEquals(node.getId(), nodes.get(0).getId());
    }

    @Test
    public void getMetadataWad() {
        String rawData = "foo";

        Node node = savableNode();
        node.setMetadataWad(rawData);
        repository.save(node);

        String found = repository.getMetadataWad(node.getId());

        assertEquals(rawData, found);
    }

    @Test
    public void getDirectChildren() {
        Node parent = savableNode();
        repository.save(parent);

        Node child = savableNode();
        child.setParentNode(parent);
        repository.save(child);

        Node subchild = savableNode();
        subchild.setParentNode(child);
        repository.save(subchild);

        List<Node> nodes = repository
                .getDirectChildren(Collections.singletonList(parent.getId()));

        assertEquals(1, nodes.size());
        assertEquals(child.getId(), nodes.get(0).getId());
    }

    @Test
    public void getNodeNextOrderChildren() {
        Node parent = savableNode();
        repository.save(parent);

        Node child = savableNode();
        child.setParentNode(parent);
        repository.save(child);

        Node subchild = savableNode();
        subchild.setParentNode(child);
        repository.save(child);

        assertEquals(Integer.valueOf(1), repository.getNodeNextOrderChildren(parent.getId()));
    }

    @Test
    public void getNodesByOrder() {
        Node parent = savableNode();
        repository.save(parent);

        Node child1 = savableNode();
        Node child2 = savableNode();
        Node child3 = savableNode();

        child1.setNodeOrder(1);
        child2.setNodeOrder(2);
        child3.setNodeOrder(3);

        child1.setParentNode(parent);
        child2.setParentNode(parent);
        child2.setParentNode(parent);

        repository.saveAll(Arrays.asList(child1, child2, child3));

        List<Node> nodes = repository.getNodesByOrder(parent.getId(), 2);

        assertEquals(2, nodes.size());

        assertTrue(nodes.stream().anyMatch(n -> n.getId().equals(child1.getId())));
        assertTrue(nodes.stream().anyMatch(n -> n.getId().equals(child2.getId())));

        assertFalse(nodes.stream().anyMatch(n -> n.getId().equals(child3.getId())));
    }

    @Test
    public void getNodeUuidByPortfolioModelAndSemanticTag() {
        UUID modelId = UUID.randomUUID();
        String tag = "foo";

        Portfolio portfolio = portfolioRecord();
        portfolio.setModelId(modelId);
        portfolioRepository.save(portfolio);

        Node node = savableNode();
        node.setPortfolio(portfolio);
        node.setSemantictag(tag);
        repository.save(node);

        UUID found = repository.getNodeUuidByPortfolioModelAndSemanticTag(modelId, tag);

        assertEquals(node.getId(), found);
    }

    @Test
    public void getParentNodeUUIDs() {
        UUID sharedId = UUID.randomUUID();
        Node parent = savableNode();

        repository.save(parent);

        Node child1 = savableNode();
        Node child2 = savableNode();

        child1.setParentNode(parent);
        child2.setParentNode(parent);

        child1.setNodeOrder(2);
        child2.setNodeOrder(1);

        child1.setSharedNodeUuid(sharedId);

        repository.saveAll(Arrays.asList(child1, child2));

        List<UUID> ids = repository.getParentNodeUUIDs(parent.getId());

        assertEquals(2, ids.size());

        // Node order *does* matter
        assertEquals(child2.getId(), ids.get(0));
        assertEquals(sharedId, ids.get(1));
    }
}