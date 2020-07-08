package eportfolium.com.karuta.webapp.rest;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WebMvcTest
public @interface MvcTest {
}
