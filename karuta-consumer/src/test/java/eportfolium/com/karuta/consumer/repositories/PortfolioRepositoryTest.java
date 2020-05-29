package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.consumer.RepositoryTest;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.Portfolio;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@RepositoryTest
public class PortfolioRepositoryTest {
    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private PortfolioRepository repository;

    @Test
    public void modifDateIsUpdatedOnSave() {
        Credential credential = new Credential();
        credential.setLogin("jdoe");
        credential.setDisplayFirstname("John");
        credential.setDisplayLastname("Doe");
        credential.setOther("");
        credential.setPassword("s3cr3t");

        credentialRepository.save(credential);

        Portfolio portfolio = new Portfolio();
        portfolio.setCredential(credential);
        portfolio.setModifUserId(42L);

        assertNull(portfolio.getModifDate());

        repository.save(portfolio);

        assertNotNull(portfolio.getModifDate());
    }
}