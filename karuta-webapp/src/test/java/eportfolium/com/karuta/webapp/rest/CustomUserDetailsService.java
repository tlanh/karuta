package eportfolium.com.karuta.webapp.rest;

import eportfolium.com.karuta.business.UserInfo;
import eportfolium.com.karuta.model.bean.Credential;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Credential credential = new Credential();
        credential.setId(42L);

        if (username.equals("admin"))
            credential.setIsAdmin(1);
        else if (username.equals("designer"))
            credential.setIsDesigner(1);

        return new UserInfo(credential);
    }
}
