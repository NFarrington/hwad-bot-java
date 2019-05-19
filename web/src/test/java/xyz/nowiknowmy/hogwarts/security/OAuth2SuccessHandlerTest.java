package xyz.nowiknowmy.hogwarts.security;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.nowiknowmy.hogwarts.repositories.UserRepository;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class OAuth2SuccessHandlerTest {

    @Autowired
    private OAuth2SuccessHandler successHandler;

    @Autowired
    private UserRepository userRepository;

    @After
    public void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    public void onAuthenticationSuccess() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        Map<String, Object> principalAttributes = Map.ofEntries(entry("id", "12345678901234567890"), entry("username", "Alice"));
        Set<GrantedAuthority> authorities = Set.of(new OAuth2UserAuthority("ROLE_USER", principalAttributes));
        Authentication authentication = new OAuth2AuthenticationToken(
            new DefaultOAuth2User(authorities, principalAttributes, "username"),
            authorities,
            "discord"
        );

        successHandler.onAuthenticationSuccess(request, response, authentication);

        assertEquals(MockHttpServletResponse.SC_FOUND, response.getStatus());
        assertNotNull(userRepository.findByUid("12345678901234567890"));
    }

}
