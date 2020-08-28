package eportfolium.com.karuta.business.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('ADMIN') or @nodeManagerImpl.isDesigner(principal, #id)")
public @interface IsAdminOrDesignerOnNode {
}
