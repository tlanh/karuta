package eportfolium.com.karuta.business;

import eportfolium.com.karuta.business.security.RightsExpressionsHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@SpringBootApplication
@EntityScan("eportfolium.com.karuta.model.bean")
@EnableJpaRepositories("eportfolium.com.karuta.consumer.repositories")
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

    @Configuration
    @EnableGlobalMethodSecurity(jsr250Enabled = true, prePostEnabled = true, securedEnabled = true)
    public static class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
        @Autowired
        private RightsExpressionsHandler rightsExpressionsHandler;

        @Override
        protected MethodSecurityExpressionHandler createExpressionHandler() {
            return rightsExpressionsHandler;
        }
    }
}
