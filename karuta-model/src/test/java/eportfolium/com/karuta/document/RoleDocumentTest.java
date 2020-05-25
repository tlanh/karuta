package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.bean.Portfolio;
import org.junit.Test;


import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;

public class RoleDocumentTest extends DocumentTest {
    @Test
    public void basicSerialization() throws JsonProcessingException {
        GroupRightInfo groupRightInfo = new GroupRightInfo();

        UUID portfolioId = UUID.randomUUID();

        groupRightInfo.setId(12L);
        groupRightInfo.setLabel("designer");
        groupRightInfo.setOwner(1);
        groupRightInfo.setPortfolio(new Portfolio(portfolioId));

        RoleDocument document = new RoleDocument(groupRightInfo);
        String output = mapper.writeValueAsString(document);

        assertContains("<role id=\"12\" owner=\"1\">", output);
        assertContains("<label>designer</label>", output);
        assertContains("<portfolio_id>" + portfolioId + "</portfolio_id>", output);
    }

    @Test
    public void serizaliationWithRights() throws JsonProcessingException {
        GroupRightInfo groupRightInfo = new GroupRightInfo();

        groupRightInfo.setId(12L);
        groupRightInfo.setLabel("designer");
        groupRightInfo.setOwner(1);
        groupRightInfo.setPortfolio(new Portfolio());

        RoleDocument document = new RoleDocument(groupRightInfo);

        RightDocument rightDocument = new RightDocument(true, true, true, true);
        document.setRights(Collections.singletonList(rightDocument));

        String output = mapper.writeValueAsString(document);

        assertContains("<role id=\"12\" owner=\"1\">", output);
        assertContains("<label>designer</label>", output);
        assertContains("<right>", output);
        assertContains("<RD>true</RD>", output);

        refuteContains("<rights>", output);
    }

    @Test
    public void deserizaliation() throws JsonProcessingException {
        UUID portfolioId = UUID.randomUUID();

        String xml = "<role>" +
                        "<label>designer</label>" +
                        "<portfolio_id>" + portfolioId + "</portfolio_id>" +
                "</role>";

        RoleDocument document = mapper.readerFor(RoleDocument.class)
                                    .readValue(xml);

        assertEquals("designer", document.getLabel());
        assertEquals(portfolioId, document.getPortfolioId());
    }
}