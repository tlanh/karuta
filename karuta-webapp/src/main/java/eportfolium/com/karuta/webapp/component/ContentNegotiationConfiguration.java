package eportfolium.com.karuta.webapp.component;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebMvc
@Configuration
public class ContentNegotiationConfiguration implements WebMvcConfigurer {
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer contentNegotiationConfigurer) {
        contentNegotiationConfigurer.defaultContentType(MediaType.APPLICATION_XML);
    }
}
