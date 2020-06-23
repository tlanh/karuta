package eportfolium.com.karuta.business.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('admin') or hasRole('designer')")
public @interface IsAdminOrDesigner {
}
