package xyz.nowiknowmy.hogwarts.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import xyz.nowiknowmy.hogwarts.security.OAuth2SuccessHandler;
import xyz.nowiknowmy.hogwarts.exceptions.UnexpectedAccessException;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    OAuth2SuccessHandler successHandler;

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/h2-console/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/").permitAll()
            .anyRequest().authenticated()
            .and().oauth2Login().successHandler(successHandler);
    }

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService() {
        return new OAuth2AuthorizedClientService() {
            private static final String EXCEPTION_MESSAGE = "OAuth2AuthorizedClientService is unused and should not be called.";

            @Override
            public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId, String principalName) {
                throw new UnexpectedAccessException(EXCEPTION_MESSAGE);
            }

            @Override
            public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal) {
                throw new UnexpectedAccessException(EXCEPTION_MESSAGE);
            }

            @Override
            public void removeAuthorizedClient(String clientRegistrationId, String principalName) {
                throw new UnexpectedAccessException(EXCEPTION_MESSAGE);
            }
        };
    }

    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new HttpSessionOAuth2AuthorizedClientRepository();
    }
}
