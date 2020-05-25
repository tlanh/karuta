package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.CredentialSubstitution;
import eportfolium.com.karuta.model.bean.CredentialSubstitutionId;
import org.junit.Test;

import static org.junit.Assert.*;

public class CredentialDocumentTest extends DocumentTest {

    @Test
    public void basicSerialization() throws JsonProcessingException {
        Credential user = new Credential();

        user.setId(42L);
        user.setLogin("johndoe");
        user.setEmail("foo@bar.com");
        user.setDisplayFirstname("John");
        user.setDisplayLastname("Doe");

        String output = mapper.writeValueAsString(new CredentialDocument(user));

        assertContains("<user id=\"42\">", output);

        assertContains("<username>johndoe</username>", output);
        assertContains("<email>foo@bar.com</email>", output);
        assertContains("<firstname>John</firstname>", output);
        assertContains("<lastname>Doe</lastname>", output);
    }

    @Test
    public void serializationWithExtra() throws JsonProcessingException {
        Credential user = new Credential();

        user.setId(42L);
        user.setLogin("johndoe");
        user.setEmail("foo@bar.com");
        user.setDisplayFirstname("John");
        user.setDisplayLastname("Doe");
        user.setIsDesigner(1);
        user.setIsAdmin(0);
        user.setOther("foo");
        user.setActive(1);

        String output = mapper.writeValueAsString(new CredentialDocument(user, true));

        assertContains("<user id=\"42\">", output);

        assertContains("<designer>1</designer>", output);
        assertContains("<admin>0</admin>", output);
        assertContains("<other>foo</other>", output);
        assertContains("<active>1</active>", output);
    }

    @Test
    public void passwordIsNeverOutput() throws JsonProcessingException {
        CredentialDocument document = new CredentialDocument(new Credential());

        document.setPassword("s3cr3t");
        document.setPrevpass("supers3cr3t");

        String output = mapper.writeValueAsString(document);

        assertEquals("<user/>", output);
    }

    @Test
    public void passwordIsProperlySetOnDeserialization() throws JsonProcessingException {
        String xml = "<user>" +
                "<password>s3cr3t</password>" +
                "<prevpass>supers3cr3t</prevpass>" +
                "</user>";
        CredentialDocument document = mapper.readerFor(CredentialDocument.class)
                                        .readValue(xml);

        assertEquals("s3cr3t", document.getPassword());
        assertEquals("supers3cr3t", document.getPrevpass());
    }

    @Test
    public void substitudeIsPresentWhenGiven() throws JsonProcessingException {
        Credential user = new Credential();
        CredentialSubstitution cs = new CredentialSubstitution();
        CredentialSubstitutionId csid = new CredentialSubstitutionId();

        csid.setId(12L);
        cs.setId(csid);
        user.setCredentialSubstitution(cs);

        String output = mapper.writeValueAsString(new CredentialDocument(user, true));

        assertContains("<substitute>1</substitute>", output);
        assertContains("<substituteId>12</substituteId>", output);
    }
}
