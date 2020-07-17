package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.CredentialGroup;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class CredentialGroupDocumentTest extends DocumentTest {
    @Test
    public void basicSerializationWithLabel() throws JsonProcessingException {
        CredentialGroup group = new CredentialGroup();

        group.setId(12L);
        group.setLabel("designer");

        CredentialGroupDocument document = new CredentialGroupDocument(group);

        String output = mapper.writeValueAsString(document);

        assertContains("<group id=\"12\">", output);
        assertContains("<label>designer</label>", output);
    }

    @Test
    public void basicSerializationWithList() throws JsonProcessingException {
        Credential user = new Credential();

        user.setId(42L);
        user.setLogin("johndoe");

        List<CredentialDocument> users = Collections.singletonList(
                new CredentialDocument(user)
        );

        CredentialGroupDocument group = new CredentialGroupDocument(34L, users);

        String output = mapper.writeValueAsString(group);

        assertContains("<group id=\"34\">", output);

        assertContains("<user id=\"42\">", output);
        assertContains("<username>johndoe</username>", output);
    }
}
