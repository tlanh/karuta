package eportfolium.com.karuta.document;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class DocumentTest {
    protected static final XmlMapper mapper = new XmlMapper();

    public static void assertContains(String other, String string) {
        assertTrue(
                String.format("%s is not present in %s", other, string),
                string.contains(other));
    }

    public static void refuteContains(String other, String string) {
        assertFalse(String.format("%s must not be present in %s but is", other, string),
                string.contains(other));
    }
}
