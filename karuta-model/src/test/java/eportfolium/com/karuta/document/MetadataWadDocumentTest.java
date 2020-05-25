package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class MetadataWadDocumentTest extends DocumentTest {
    @Test
    public void from() throws JsonProcessingException {
        String xml = "public=\"true\" sharedResource=\"true\" " +
                "sharedNode=\"true\" sharedNodeResource=\"true\" semantictag=\"foo\" " +
                "seenoderoles=\"bar\" delnoderoles=\"baz\" editnoderoles=\"quux\" " +
                "editresroles=\"foo\" submitroles=\"bar\" showtoroles=\"baz\" " +
                "showroles=\"quux\" notifyroles=\"foo\" menuroles=\"bar\" " +
                "submitted=\"true\" submitteddate=\"2020-02-02T09:00:00\"";

        MetadataWadDocument document = MetadataWadDocument.from(xml);
        Date date = new Calendar.Builder()
                            .setDate(2020, 1, 2)
                            .setTimeOfDay(9, 0, 0)
                            .setTimeZone(TimeZone.getTimeZone("UTC"))
                            .build()
                            .getTime();

        assertTrue(document.getPublic());
        assertTrue(document.getSharedResource());
        assertTrue(document.getSharedNode());
        assertTrue(document.getSharedNodeResource());

        assertEquals("foo", document.getSemantictag());
        assertEquals("bar", document.getSeenoderoles());
        assertEquals("baz", document.getDelnoderoles());
        assertEquals("quux", document.getEditnoderoles());
        assertEquals("foo", document.getEditresroles());
        assertEquals("bar", document.getSubmitroles());
        assertEquals("baz", document.getShowtoroles());
        assertEquals("quux", document.getShowroles());
        assertEquals("foo", document.getNotifyroles());
        assertEquals("bar", document.getMenuroles());

        assertTrue(document.getSubmitted());
        assertEquals(date, document.getSubmitteddate());
    }
}