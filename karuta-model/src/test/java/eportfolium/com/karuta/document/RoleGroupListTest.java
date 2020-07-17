package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class RoleGroupListTest extends DocumentTest {
    @Test
    public void basicSerialization() throws JsonProcessingException {
        List<Long> groups = Arrays.asList(1L, 2L, 3L, 4L);
        RoleGroupList list = new RoleGroupList(groups);

        String output = mapper.writeValueAsString(list);

        assertContains("<groups>", output);

        groups.forEach(group -> {
            assertContains(String.format("<group>%d</group>", group), output);
        });
    }
}