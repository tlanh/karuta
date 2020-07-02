package eportfolium.com.karuta.business.security.test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithMockUser;

@Retention(RetentionPolicy.RUNTIME)
@WithMockUser(roles={"ADMIN"})
public @interface AsAdmin {
}
