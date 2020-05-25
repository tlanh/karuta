package eportfolium.com.karuta.model.bean;

import org.junit.Test;

import static org.junit.Assert.*;

public class CredentialTest {
    @Test
    public void addAndRemovePortfolio() {
        Portfolio portfolio = new Portfolio();
        Credential credential = new Credential();

        assertEquals(0, credential.getPortfolios().size());

        credential.internalAddPortfolio(portfolio);
        assertEquals(1, credential.getPortfolios().size());

        credential.internalRemovePortfolio(portfolio);
        assertEquals(0, credential.getPortfolios().size());
    }
}
