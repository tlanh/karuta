package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.model.bean.PortfolioGroup;
import org.junit.Test;

import java.util.Collections;

public class PortfolioGroupListTest extends DocumentTest {
    @Test
    public void basicSerialization() throws JsonProcessingException {
        PortfolioGroup group = new PortfolioGroup();

        group.setId(10L);
        group.setLabel("designer");
        group.setType("Foo");

        PortfolioGroupDocument document = new PortfolioGroupDocument(group);
        PortfolioGroupList list = new PortfolioGroupList(Collections.singletonList(document));

        String output = mapper.writeValueAsString(list);

        assertContains("<portfolio>", output);

        assertContains("<group id=\"10\" type=\"foo\">", output);
        assertContains("<label>designer</label>", output);

        assertContains("</group></portfolio>", output);
    }
}