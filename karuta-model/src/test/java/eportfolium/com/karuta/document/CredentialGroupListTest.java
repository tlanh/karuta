package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.model.bean.CredentialGroup;
import org.junit.Test;

import java.util.Collections;

public class CredentialGroupListTest extends DocumentTest {
    @Test
    public void basicSerialization() throws JsonProcessingException {
        CredentialGroup group = new CredentialGroup();

        group.setId(12L);
        group.setLabel("designer");

        CredentialGroupDocument document = new CredentialGroupDocument(group);

        CredentialGroupList list = new CredentialGroupList(Collections.singletonList(document));
        String output = mapper.writeValueAsString(list);

        assertContains("<groups>", output);

        assertContains("<group id=\"12\">", output);
        assertContains("<label>designer</label>", output);
    }

}