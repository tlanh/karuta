package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.consumer.RepositoryTest;
import eportfolium.com.karuta.model.bean.CredentialGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@RepositoryTest
public class CredentialGroupRepositoryTest {
    @Autowired
    private CredentialGroupRepository repository;

    @Test
    public void findByLabel() {
        CredentialGroup credentialGroup = new CredentialGroup();
        credentialGroup.setLabel("foo");
        repository.save(credentialGroup);

        CredentialGroup found = repository.findByLabel("foo");

        assertEquals(credentialGroup.getId(), found.getId());
    }
}