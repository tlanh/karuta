package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.model.bean.PortfolioGroup;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PortfolioGroupDocumentTest extends DocumentTest {
    @Test
    public void basicSerialization() throws JsonProcessingException {
        UUID firstPortfolioId = UUID.randomUUID();
        UUID secondPortfolioId = UUID.randomUUID();

        PortfolioDocument firstPortfolio = new PortfolioDocument(firstPortfolioId);
        PortfolioDocument secondPortfolio = new PortfolioDocument(secondPortfolioId);

        List<PortfolioDocument> portfolios = Arrays.asList(firstPortfolio, secondPortfolio);

        PortfolioGroupDocument document = new PortfolioGroupDocument(10L, portfolios);
        String output = mapper.writeValueAsString(document);

        assertContains("<group id=\"10\">", output);

        assertContains("<portfolio id=\"" + firstPortfolioId + "\"", output);
        assertContains("<portfolio id=\"" + secondPortfolioId + "\"", output);
    }

    @Test
    public void serializationWithEntity() throws JsonProcessingException {
        PortfolioGroup portfolioGroup = new PortfolioGroup();

        portfolioGroup.setId(10L);
        portfolioGroup.setLabel("designer");
        portfolioGroup.setType("Foo");

        PortfolioGroupDocument document = new PortfolioGroupDocument(portfolioGroup);
        String output = mapper.writeValueAsString(document);

        assertContains("<group id=\"10\" type=\"foo\">", output);
        assertContains("<label>designer</label>", output);
    }
}