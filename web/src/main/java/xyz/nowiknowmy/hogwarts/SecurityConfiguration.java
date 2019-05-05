package xyz.nowiknowmy.hogwarts;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/h2-console/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/home", "/name-changes").hasRole("USER")
            .antMatchers("/**").permitAll()
            .and().formLogin().loginPage("/oauth")
            .and().csrf()
//            .and().exceptionHandling().accessDeniedPage("/Access_Denied")
            .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
    }
}
