package eportfolium.com.karuta.consumer;

import eportfolium.com.karuta.consumer.repositories.CredentialRepository;
import eportfolium.com.karuta.consumer.repositories.PortfolioRepository;
import eportfolium.com.karuta.consumer.repositories.ResourceTableRepository;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.bean.ResourceTable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

public abstract class TestHelpers {
    @Autowired
    protected CredentialRepository credentialRepository;

    @Autowired
    protected PortfolioRepository portfolioRepository;

    @Autowired
    protected ResourceTableRepository resourceTableRepository;

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

    protected ResourceTable savableResource() {
        ResourceTable resource = new ResourceTable();
        resource.setModifUserId(42L);

        return resource;
    }

    protected ResourceTable resourceRecord() {
        Credential credential = credentialRecord();
        ResourceTable resource = savableResource();

        resource.setCredential(credential);
        resourceTableRepository.save(resource);

        return resource;
    }
}
