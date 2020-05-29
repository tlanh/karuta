package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.consumer.RepositoryTest;
import eportfolium.com.karuta.model.bean.ResourceTable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@RepositoryTest
public class ResourceTableRepositoryTest {
    @Autowired
    private ResourceTableRepository repository;

    @Test
    public void modifDateIsUpdatedOnSave() {
        ResourceTable resource = new ResourceTable();
        resource.setModifUserId(42L);

        assertNull(resource.getModifDate());

        repository.save(resource);

        assertNotNull(resource.getModifDate());
    }
}