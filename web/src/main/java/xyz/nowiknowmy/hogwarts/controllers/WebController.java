package xyz.nowiknowmy.hogwarts.controllers;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import xyz.nowiknowmy.hogwarts.domain.Guild;
import xyz.nowiknowmy.hogwarts.domain.Revision;
import xyz.nowiknowmy.hogwarts.repositories.GuildRepository;
import xyz.nowiknowmy.hogwarts.repositories.RevisionRepository;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Controller
public class WebController {

    @Value("${discord.base-url}")
    String discordBaseUrl;

    private final GuildRepository guildRepository;
    private final RevisionRepository revisionRepository;

    public WebController(GuildRepository guildRepository, RevisionRepository revisionRepository) {
        this.guildRepository = guildRepository;
        this.revisionRepository = revisionRepository;
    }

    @GetMapping("/")
    public String getRoot() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String getHome() {
        return "homepage";
    }

    @GetMapping("/name-changes")
    public ModelAndView getNameChanges(@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient oAuth2Client) throws IOException {
        String accessToken = oAuth2Client.getAccessToken().getTokenValue();

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(discordBaseUrl + "/users/@me/guilds");
        get.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        HttpResponse response = client.execute(get);

        String responseBody = EntityUtils.toString(response.getEntity());
        JSONArray data = new JSONArray(responseBody);

        List<String> guildIds = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            guildIds.add(data.getJSONObject(i).getString("id"));
        }

        List<Integer> guilds = guildRepository.findByGuildIdIn(guildIds)
            .stream()
            .map(Guild::getId)
            .collect(Collectors.toList());

        List<Revision> changes = revisionRepository.findByGuildIdWhereKeyIsUsernameOrNickname("App\\Models\\Member", guilds);
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
