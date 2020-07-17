package eportfolium.com.karuta.model.bean;

import org.junit.Test;

import static org.junit.Assert.*;

public class PortfolioTest {
    @Test
    public void setCredential() {
        Portfolio portfolio = new Portfolio();
        Credential first = new Credential();
        Credential second = new Credential();

        assertEquals(0, first.getPortfolios().size());
        assertEquals(0, second.getPortfolios().size());
        assertNull(portfolio.getCredential());

        portfolio.setCredential(first);

        assertEquals(first, portfolio.getCredential());
        assertEquals(1, first.getPortfolios().size());

        portfolio.setCredential(second);

        assertEquals(second, portfolio.getCredential());
        assertEquals(1, second.getPortfolios().size());
        assertEquals(0, first.getPortfolios().size());
    }
}
