package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.model.bean.Credential;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

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

    @Test
    public void basicDeserialization() throws JsonProcessingException {
        String xml = "<users><user><firstname>John</firstname></user></users>";

        CredentialList list = mapper.readerFor(CredentialList.class)
                .readValue(xml);

        assertEquals(1, list.getUsers().size());
        assertEquals("John", list.getUsers().get(0).getFirstname());
    }
}