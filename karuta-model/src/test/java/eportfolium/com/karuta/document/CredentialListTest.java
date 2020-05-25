package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.model.bean.Credential;
import org.junit.Test;

import java.util.Collections;

public class CredentialListTest extends DocumentTest {
    @Test
    public void basicSerialization() throws JsonProcessingException {
        Credential user = new Credential();

        user.setId(42L);
        user.setLogin("johndoe");

        CredentialDocument document = new CredentialDocument(user);
        CredentialList list = new CredentialList(Collections.singletonList(document));

        String output = mapper.writeValueAsString(list);

        assertContains("<users>", output);

        assertContains("<user id=\"42\">", output);
        assertContains("<username>johndoe</username>", output);
    }
}