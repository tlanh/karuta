package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.consumer.RepositoryTest;
import eportfolium.com.karuta.consumer.TestHelpers;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.CredentialSubstitution;
import eportfolium.com.karuta.model.bean.CredentialSubstitutionId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@RepositoryTest
public class CredentialSubstitutionRepositoryTest extends TestHelpers {
    @Autowired
    private CredentialSubstitutionRepository repository;

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    public void getFor() {
        Credential credential = credentialRecord();
        testEntityManager.flush();

        CredentialSubstitution credentialSubstitution = new CredentialSubstitution();

        String type = "USER";

        credentialSubstitution.setId(new CredentialSubstitutionId());
        credentialSubstitution.setCredentialSubstitutionId(12L);
        credentialSubstitution.setCredential(credential);
        credentialSubstitution.setType(type);

        repository.save(credentialSubstitution);

        CredentialSubstitution found = repository.getFor(credential.getId(), type);

        assertEquals(credentialSubstitution.getId(), found.getId());
    }
}