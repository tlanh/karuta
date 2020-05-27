package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;

public class PortfolioListTest extends DocumentTest {
    @Test
    public void basicSerialization() throws JsonProcessingException {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();

        PortfolioDocument first = new PortfolioDocument(firstId);
        PortfolioDocument second = new PortfolioDocument(secondId);

        PortfolioList list = new PortfolioList(Arrays.asList(first, second));
        String output = mapper.writeValueAsString(list);

        assertContains("<portfolios count=\"2\">", output);
        assertContains("<portfolio id=\"" + firstId + "\" ", output);
        assertContains("<portfolio id=\"" + secondId + "\" ", output);
        assertContains("</portfolios>", output);
    }
}