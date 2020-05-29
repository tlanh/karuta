package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.consumer.RepositoryTest;
import eportfolium.com.karuta.model.bean.Node;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@RepositoryTest
public class NodeRepositoryTest {
    @Autowired
    private NodeRepository repository;

    @Test
    public void modifDateIsUpdatedOnSave() {
        Node node = new Node();
        node.setModifUserId(42L);
        node.setMetadata("foo");
        node.setMetadataEpm("bar");
        node.setMetadataWad("baz");
        node.setSharedNode(true);
        node.setSharedNodeRes(true);
        node.setSharedRes(true);

        assertNull(node.getModifDate());

        repository.save(node);

        assertNotNull(node.getModifDate());
    }
}