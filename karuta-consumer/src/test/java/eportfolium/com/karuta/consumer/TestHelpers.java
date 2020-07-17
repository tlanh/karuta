package eportfolium.com.karuta.consumer;

import eportfolium.com.karuta.consumer.repositories.CredentialRepository;
import eportfolium.com.karuta.consumer.repositories.PortfolioRepository;
import eportfolium.com.karuta.consumer.repositories.ResourceRepository;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.bean.Resource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

public abstract class TestHelpers {
    @Autowired
    protected CredentialRepository credentialRepository;

    @Autowired
    protected PortfolioRepository portfolioRepository;

    @Autowired
    protected ResourceRepository resourceRepository;

    protected Credential savableCredential() {
        Credential credential = new Credential();

        credential.setLogin(UUID.randomUUID().toString());
        credential.setOther("");
        credential.setDisplayFirstname("");
        credential.setDisplayLastname("");
        credential.setPassword("");

        return credential;
    }

    protected Credential credentialRecord() {
        return credentialRepository.save(savableCredential());
    }

    protected Credential credentialRecord(String login) {
        Credential credential = savableCredential();
        credential.setLogin(login);
        credentialRepository.save(credential);

        return credential;
    }

    protected Portfolio savablePortfolio() {
        Credential credential = savableCredential();
        Portfolio portfolio = new Portfolio();

        portfolio.setCredential(credential);
        portfolio.setModifUserId(42L);

        return portfolio;
    }

    protected Portfolio portfolioRecord() {
        Portfolio portfolio = savablePortfolio();
        credentialRepository.save(portfolio.getCredential());
        portfolioRepository.save(portfolio);

        return portfolio;
    }

    protected Node savableNode() {
        Node node = new Node();

        node.setModifUserId(42L);
        node.setMetadata("");
        node.setMetadataEpm("");
        node.setMetadataWad("");
        node.setSharedNode(true);
        node.setSharedRes(true);
        node.setSharedNodeRes(true);

        return node;
    }

    protected Resource savableResource() {
        Resource resource = new Resource();
        resource.setModifUserId(42L);

        return resource;
    }

    protected Resource resourceRecord() {
        Credential credential = credentialRecord();
        Resource resource = savableResource();

        resource.setCredential(credential);
        resourceRepository.save(resource);

        return resource;
    }
}
