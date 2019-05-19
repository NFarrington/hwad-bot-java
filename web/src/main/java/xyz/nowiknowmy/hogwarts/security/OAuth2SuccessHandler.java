package xyz.nowiknowmy.hogwarts.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import xyz.nowiknowmy.hogwarts.domain.User;
import xyz.nowiknowmy.hogwarts.repositories.UserRepository;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2SuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private UserRepository userRepository;

    public OAuth2SuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
        throws ServletException, IOException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        Map<String, Object> principalAttributes = token.getPrincipal().getAttributes();
        String uid = (String) principalAttributes.get("id");
        String username = (String) principalAttributes.get("username");

        User user = userRepository.findByUid(uid);
        if (user == null) {
            user = new User(uid, username);
        } else {
            user.setUsername(username);
        }
        userRepository.save(user);

        super.onAuthenticationSuccess(request, response, authentication);
    }

}
