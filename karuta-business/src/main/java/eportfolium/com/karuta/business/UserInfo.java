package eportfolium.com.karuta.business;

import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.CredentialSubstitution;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserInfo implements UserDetails {
    private final Long id;
    private final Long substituteId;
    private final String username;
    private final List<GrantedAuthority> authorities;

    public UserInfo(Credential credential) {
        this.id = credential.getId();
        this.username = credential.getLogin();

        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>() {{
            add(new SimpleGrantedAuthority("ROLE_USER"));

            if (credential.getIsAdmin() == 1) {
                add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }

            if (credential.getIsDesigner() == 1) {
                add(new SimpleGrantedAuthority("ROLE_DESIGNER"));
            }
        }};

        this.authorities = Collections.unmodifiableList(authorities);

        CredentialSubstitution cs = credential.getCredentialSubstitution();

        if (cs != null && cs.getId() != null) {
            this.substituteId = cs.getId().getId();
        } else {
            this.substituteId = 0L;
        }
    }

    public Long getId() {
        return id;
    }

    public Long getSubstituteId() {
        return substituteId;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    public boolean isAdmin() {
        return authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    public boolean isDesigner() {
        return authorities.contains(new SimpleGrantedAuthority("ROLE_DESIGNER"));
    }
}