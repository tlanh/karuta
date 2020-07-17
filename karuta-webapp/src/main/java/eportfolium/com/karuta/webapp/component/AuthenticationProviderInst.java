package eportfolium.com.karuta.webapp.component;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

@EnableWebSecurity
@Configuration
public class AuthenticationProviderInst implements AuthenticationProvider {
	
		@Override
		public Authentication authenticate( Authentication auth ) throws AuthenticationException
		{
			/// Checked already
			List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
			UsernamePasswordAuthenticationToken authUser = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getPrincipal(), grantedAuthorities);
			authUser.setDetails(auth.getDetails());
					
			return authUser;
		}

		@Override
		public boolean supports( Class<?> arg0 )
		{
			return true;
		}
}