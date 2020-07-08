package eportfolium.com.karuta.webapp.rest;

import org.springframework.security.test.context.support.WithUserDetails;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithUserDetails(userDetailsServiceBeanName = "customUserDetailsService", value = "admin")
public @interface AsAdmin {
}
