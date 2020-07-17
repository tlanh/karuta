package eportfolium.com.karuta.webapp.rest.controller;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import eportfolium.com.karuta.webapp.rest.ControllerTest;
import eportfolium.com.karuta.webapp.rest.MvcTest;
import org.junit.Test;

@MvcTest
public class SimpleControllerTest extends ControllerTest  {

    @Test
    public void simpleGet() throws Exception {
        get("/hello")
                .andExpect(status().isOk())
                .andExpect(content().string("Hello World!"))
                .andDo(document("hello"));

    }
}
