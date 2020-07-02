package eportfolium.com.karuta.business.security.test;

import org.springframework.security.test.context.support.WithMockUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithMockUser(roles = {"DESIGNER"})
public @interface AsDesigner {
}
