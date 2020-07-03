package eportfolium.com.karuta.webapp.component;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf()
                .disable()
                .cors()
                .and()
                    .authorizeRequests()
                    .antMatchers("/credential/login", "/hello", "/docs/**")
                    .permitAll()
                .and()
                    .authorizeRequests()
                    .anyRequest()
                    .authenticated()
                .and()
                    .logout()
                    .logoutUrl("/credential/logout");
    }
}