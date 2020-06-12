package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.consumer.RepositoryTest;
import eportfolium.com.karuta.consumer.TestHelpers;
import eportfolium.com.karuta.model.bean.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@RepositoryTest
public class CredentialRepositoryTest extends TestHelpers {
    @Autowired
    private CredentialRepository repository;

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private GroupUserRepository groupUserRepository;

    @Autowired
    private GroupInfoRepository groupInfoRepository;

    @Autowired
    private GroupRightInfoRepository groupRightInfoRepository;

    @Autowired
    private CredentialSubstitutionRepository credentialSubstitutionRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    public void findByLoginAndAdmin() {
        Credential credential = savableCredential();

        credential.setLogin("jdoe");
        credential.setIsAdmin(1);

        repository.save(credential);

        Credential found = repository.findByLoginAndAdmin("jdoe",1);

        assertEquals(credential.getId(), found.getId());
    }

    @Test
    public void findActiveById() {
        Credential credential = savableCredential();
        credential.setActive(1);

        repository.save(credential);

        Optional<Credential> found = repository.findActiveById(credential.getId());

        assertTrue(found.isPresent());
        assertEquals(credential.getId(), found.get().getId());

        credential.setActive(0);
        repository.save(credential);

        assertFalse(repository.findActiveById(credential.getId()).isPresent());
    }

    @Test
    public void isAdmin() {
        Credential credential = savableCredential();
        credential.setIsAdmin(1);

        repository.save(credential);

        assertTrue(repository.isAdmin(credential.getId()));

        credential.setIsAdmin(0);
        repository.save(credential);

        assertFalse(repository.isAdmin(credential.getId()));
    }

    @Test
    public void isCreator() {
        Credential credential = savableCredential();
        credential.setIsDesigner(1);

        repository.save(credential);

        assertTrue(repository.isCreator(credential.getId()));

        credential.setIsDesigner(0);
        repository.save(credential);

        assertFalse(repository.isCreator(credential.getId()));
    }

    @Test
    public void isDesigner() {
        Credential credential = savableCredential();
        repository.save(credential);

        Portfolio portfolio = new Portfolio();
        portfolio.setCredential(credential);
        portfolio.setModifUserId(42L);
        portfolioRepository.save(portfolio);

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setLabel("");
        groupInfoRepository.save(groupInfo);

        GroupUser groupUser = new GroupUser();
        groupUser.setId(new GroupUserId());
        groupUser.setCredential(credential);
        groupUser.setGroupInfo(groupInfo);
        groupUserRepository.save(groupUser);

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setLabel("designer");
        groupRightInfo.setPortfolio(portfolio);
        groupRightInfo.setGroupInfo(groupInfo);
        groupRightInfoRepository.save(groupRightInfo);

        Node node = new Node();
        node.setMetadata("");
        node.setMetadataEpm("");
        node.setMetadataWad("");
        node.setPortfolio(portfolio);
        node.setModifUserId(42L);
        node.setSharedNode(false);
        node.setSharedRes(false);
        node.setSharedNodeRes(false);
        nodeRepository.save(node);

        assertTrue(repository.isDesigner(credential.getId(), node.getId()));
    }

    @Test
    public void getPublicId() {
        Credential credential = savableCredential();
        credential.setLogin("sys_public");

        repository.save(credential);

        assertEquals(credential.getId(), repository.getPublicId());
    }

    @Test
    public void getLoginById() {
        Credential credential = savableCredential();
        credential.setLogin("johnny");

        repository.save(credential);

        assertEquals(credential.getLogin(), repository.getLoginById(credential.getId()));
    }

    @Test
    public void getEmailByLogin() {
        Credential credential = savableCredential();
        credential.setLogin("johnyy");
        credential.setEmail("john@doe.com");

        repository.save(credential);

        assertEquals(credential.getEmail(), repository.getEmailByLogin(credential.getLogin()));
    }

    @Test
    public void getIdByLogin() {
        Credential credential = savableCredential();

        credential.setLogin("johndoe");
        credential.setDisplayFirstname("John");
        credential.setDisplayLastname("Doe");

        repository.save(credential);

        Long id = repository.getIdByLogin(credential.getLogin());

        assertEquals(credential.getId(), id);
    }

    @Test
    public void getIdByLoginAndEmail() {
        Credential credential = savableCredential();

        credential.setLogin("johnyy");
        credential.setEmail("foo@bar.com");

        repository.save(credential);

        Long foundId = repository.getIdByLoginAndEmail(credential.getLogin(),
                credential.getEmail());

        assertEquals(credential.getId(), foundId);
    }

    @Test
    public void getUsers() {
        Credential matching = savableCredential();
        Credential other = savableCredential();

        matching.setLogin("johnd");
        matching.setDisplayFirstname("John");
        matching.setDisplayLastname("Doe");

        other.setLogin("marcd");
        other.setDisplayFirstname("Marc");
        other.setDisplayLastname("Doe");

        repository.save(matching);
        repository.save(other);

        List<Credential> users = repository.getUsers("jo", "Jo", "Doe");
        assertEquals(1, users.size());

        Credential found = users.get(0);

        assertEquals(matching.getId(), found.getId());
        assertEquals(matching.getLogin(), found.getLogin());
        assertEquals(matching.getDisplayFirstname(), found.getDisplayFirstname());
        assertEquals(matching.getDisplayLastname(), found.getDisplayLastname());
    }

    @Test
    public void getUsersByRole() {
        Credential credential = savableCredential();
        repository.save(credential);

        Portfolio portfolio = new Portfolio();
        portfolio.setCredential(credential);
        portfolio.setModifUserId(42L);
        portfolioRepository.save(portfolio);

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setPortfolio(portfolio);
        groupRightInfo.setLabel("designer");
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

        List<Credential> users = repository.getUsersByRole(portfolio.getId(), "designer");

        assertEquals(1, users.size());
        assertEquals(credential.getId(), users.get(0).getId());
    }

    @Test
    public void getUserInfos() {
        Credential credential = savableCredential();
        repository.save(credential);
        testEntityManager.flush();

        CredentialSubstitution cs = new CredentialSubstitution();
        cs.setId(new CredentialSubstitutionId());
        cs.setCredentialSubstitutionId(18L);
        cs.setCredential(credential);
        cs.setType("foo");
        credentialSubstitutionRepository.save(cs);
        testEntityManager.flush();

        credential.setCredentialSubstitution(cs);
        repository.save(credential);

        Credential found = repository.getUserInfos(credential.getId());

        assertEquals(found.getId(), credential.getId());
        assertNotNull(found.getCredentialSubstitution());
    }
}
