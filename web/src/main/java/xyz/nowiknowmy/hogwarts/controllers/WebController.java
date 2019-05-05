package xyz.nowiknowmy.hogwarts.controllers;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import reactor.util.function.Tuple2;
import xyz.nowiknowmy.hogwarts.domain.Guild;
import xyz.nowiknowmy.hogwarts.domain.Revision;
import xyz.nowiknowmy.hogwarts.domain.User;
import xyz.nowiknowmy.hogwarts.repositories.GuildRepository;
import xyz.nowiknowmy.hogwarts.repositories.RevisionRepository;
import xyz.nowiknowmy.hogwarts.repositories.UserRepository;

import javax.persistence.Tuple;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Controller
public class WebController {

    @Value("${discord.uri}")
    String discordUri;
    @Value("${discord.client.id}")
    String discordClientId;
    @Value("${discord.client.secret}")
    String discordClientSecret;

    private final UserRepository userRepository;
    private final GuildRepository guildRepository;
    private final RevisionRepository revisionRepository;

    public WebController(UserRepository userRepository, GuildRepository guildRepository, RevisionRepository revisionRepository) {
        this.userRepository = userRepository;
        this.guildRepository = guildRepository;
        this.revisionRepository = revisionRepository;
    }

    @GetMapping("/")
    public String getRoot() {
        return "redirect:/oauth";
    }

    @GetMapping("/oauth")
    public String getOAuth(HttpSession session, HttpServletRequest request, @RequestParam(required = false) String code, @RequestParam(required = false) String error) throws IOException {
        if (error != null) {
            // TODO: redirect with flashed error message
            throw new RuntimeException("An error occurred while logging you in: " + error);
        }

        if (code != null) {
//            session.setAttribute("discord.oauth.code", code);

            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(discordUri + "/oauth2/token");

            List<NameValuePair> arguments = new ArrayList<>(3);
            arguments.add(new BasicNameValuePair("client_id", discordClientId));
            arguments.add(new BasicNameValuePair("client_secret", discordClientSecret));
            arguments.add(new BasicNameValuePair("grant_type", "authorization_code"));
            arguments.add(new BasicNameValuePair("code", code));
//            arguments.add(new BasicNameValuePair("redirect_uri", appUri + "/oauth"));
            arguments.add(new BasicNameValuePair("redirect_uri", request.getRequestURL().toString()));

            post.setEntity(new UrlEncodedFormEntity(arguments));
            HttpResponse response = client.execute(post);

            // Print out the response message
            String responseBody = EntityUtils.toString(response.getEntity());
            System.out.println(responseBody);

            JSONObject obj = new JSONObject(responseBody);
            String accessToken = obj.getString("access_token");

            HttpGet get = new HttpGet(discordUri + "/users/@me");
            get.addHeader("Authorization", "Bearer " + accessToken);
            response = client.execute(get);

            // Print out the response message
            responseBody = EntityUtils.toString(response.getEntity());
            System.out.println(responseBody);

            obj = new JSONObject(responseBody);

            try {
                obj.getString("id");
            } catch (JSONException e) {
                throw new RuntimeException("Unable to retrieve user ID.");
            }

            User user = userRepository.findByUid(obj.getString("id"));
            if (user == null) {
                user = new User();
                user.setUid(obj.getString("id"));
            }
            user.setUsername(obj.getString("username"));
            userRepository.save(user);

            Authentication authentication = new UsernamePasswordAuthenticationToken(user, null,
                AuthorityUtils.createAuthorityList("ROLE_USER"));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            session.setAttribute("user.access-token", accessToken);

            return "redirect:/home";
        }
        URL requestURL = new URL(request.getRequestURL().toString());
        String port = requestURL.getPort() == -1 ? "" : ":" + requestURL.getPort();
        String appUri = requestURL.getProtocol() + "://" + requestURL.getHost() + port;
        return String.format("redirect:%s/oauth2/authorize?response_type=code&client_id=%s&scope=identify guilds&redirect_uri=%s/oauth", discordUri, discordClientId, appUri);
    }

    @GetMapping("/home")
    public String getHome() {
        return "homepage";
    }

    @GetMapping("/name-changes")
    public ModelAndView getLogin(HttpSession session) throws IOException {
        String accessToken = (String) session.getAttribute("user.access-token");
        if (accessToken == null) {
            throw new RuntimeException("No access token found.");
        }

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(discordUri + "/users/@me/guilds");
        get.addHeader("Authorization", "Bearer " + accessToken);
        HttpResponse response = client.execute(get);

        // Print out the response message
        String responseBody = EntityUtils.toString(response.getEntity());
        System.out.println(responseBody);

        JSONArray data = new JSONArray(responseBody);
        List<String> guildIds = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            guildIds.add(data.getJSONObject(i).getString("id"));
        }

        List<Integer> guilds = guildRepository.findByGuildIdIn(guildIds)
            .stream()
            .map(Guild::getId)
            .collect(Collectors.toList());

        List<Revision> changes = revisionRepository.findUsernameAndNickname("App\\Models\\Member", guilds);
        String[][] changesView = changes.stream()
            .map(revision -> {
                String[] change = new String[4];
                change[0] = revision.getKey();
                change[1] = revision.getOldValue();
                change[2] = revision.getNewValue();
                change[3] = ZonedDateTime.of(revision.getCreatedAt(), TimeZone.getDefault().toZoneId()).withZoneSameInstant(ZoneId.of("America/New_York"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z").withLocale(new Locale("en", "US")));
                return change;
            }).toArray(String[][]::new);

        ModelAndView page = new ModelAndView("changes");
        page.addObject("changes", changesView);
        return page;
    }
}
