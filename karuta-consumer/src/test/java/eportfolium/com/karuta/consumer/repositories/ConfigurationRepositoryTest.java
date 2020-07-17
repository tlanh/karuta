package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.consumer.RepositoryTest;
import eportfolium.com.karuta.model.bean.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@RepositoryTest
public class ConfigurationRepositoryTest {
    @Autowired
    private ConfigurationRepository repository;

    @Test
    public void modifDateIsUpdatedOnSave() {
        Configuration configuration = new Configuration();
        configuration.setName("foo");

        assertNull(configuration.getModifDate());

        repository.save(configuration);

        assertNotNull(configuration.getModifDate());
    }
}