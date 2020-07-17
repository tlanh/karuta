package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import static org.junit.Assert.*;

public class RoleListTest extends DocumentTest {
    @Test
    public void deserialize() throws JsonProcessingException {
        String xml = "<roles>" +
                        "<action>reset</action>" +
                        "<role>" +
                            "<label>designer</label>" +
                            "<right>" +
                                "<RD>true</RD>" +
                                "<WR>true</WR>" +
                                "<DL>true</DL>" +
                                "<SB>true</SB>" +
                            "</right>" +
                        "</role>" +
                "</roles>";

        RoleList list = mapper.readerFor(RoleList.class)
                            .readValue(xml);

        assertEquals("reset", list.getAction());
        assertEquals(1, list.getRoles().size());

        RoleDocument role = list.getRoles().get(0);

        assertEquals("designer", role.getLabel());
        assertEquals(1, role.getRights().size());

        RightDocument right = role.getRights().get(0);

        assertTrue(right.getRD());
        assertTrue(right.getWR());
        assertTrue(right.getDL());
        assertTrue(right.getSB());
    }
}