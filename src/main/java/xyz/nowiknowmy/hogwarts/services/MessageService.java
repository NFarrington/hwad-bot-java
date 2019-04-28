package xyz.nowiknowmy.hogwarts.services;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xyz.nowiknowmy.hogwarts.BotEventListener;
import xyz.nowiknowmy.hogwarts.domain.Guild;
import xyz.nowiknowmy.hogwarts.domain.Points;
import xyz.nowiknowmy.hogwarts.helpers.Str;
import xyz.nowiknowmy.hogwarts.repositories.GuildRepository;
import xyz.nowiknowmy.hogwarts.repositories.PointsRepository;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private final GuildRepository guildRepository;
    private final PointsRepository pointsRepository;

    public MessageService(GuildRepository guildRepository, PointsRepository pointsRepository) {
        this.guildRepository = guildRepository;
        this.pointsRepository = pointsRepository;
    }

    public Void handle(Message message) {
        Guild guild = guildRepository.findByGuildId(message.getGuild().block().getId().asString());
        String content = message.getContent().get();

        if (Str.regex("^!(time|servertime) ?.*$").matches(content)) {
            return sendServerTime(message.getChannel().block());
        } else if (Str.regex("^!points ?.*$").matches(content)) {
            return sendPointsSummary(message.getChannel().block(), guild);
        } else if (Str.regex("^!(gryffindor|hufflepuff|ravenclaw|slytherin) ?.*$").matches(content)) {
            List<String> regexMatches = Str.regex("^!(gryffindor|hufflepuff|ravenclaw|slytherin) ?.*$").groups(content);
            List<Role> existingHouse = message.getAuthorAsMember().block().getRoles().collectList().block()
                    .stream().filter(role -> Str.regex("^(Gryffindor|Hufflepuff|Ravenclaw|Slytherin)$", Pattern.CASE_INSENSITIVE).matches(role.getName()))
                    .collect(Collectors.toList());

            if (existingHouse.size() > 0) {
                sendError(message.getChannel().block(), "Sorry, you already have a house!");
                return null;
            }

            Optional<Role> role = message.getGuild().block().getRoles().collectList().block()
                    .stream().filter(role1 -> role1.getName().compareToIgnoreCase(regexMatches.get(1)) == 0)
                    .findFirst();

            if (role.isEmpty()) {
                sendError(message.getChannel().block(), "Sorry, we could not find that role!");
                return null;
            }

            message.getAuthorAsMember().block().addRole(role.get().getId()).block();
            message.getChannel().block().createMessage(String.format("You are now a member of %s!", regexMatches.get(1))).block();
        } else if (Str.regex("^!([ghrs]) (add|sub|subtract|set) (\\d+)$").matches(content)) {
            throw new UnsupportedOperationException("Not implemented");
        } else if (Str.regex("^!inactive (\\d+[d|m|y])$").matches(content)) {
            throw new UnsupportedOperationException("Not implemented");
        } else if (Str.regex("^!bumpyears$").matches(content)) {
            throw new UnsupportedOperationException("Not implemented");
        } else if (Str.regex("^!removetags$").matches(content)) {
            throw new UnsupportedOperationException("Not implemented");
        }

        return null;
    }

    private void sendError(MessageChannel channel, String errorMessage) {
        channel.createMessage(errorMessage).block();
    }

    private Void sendServerTime(MessageChannel channel) {
        ZoneId zone = ZoneId.of("America/New_York");
        ZonedDateTime zdt = ZonedDateTime.now(zone);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("h:mma z")
                .withLocale(new Locale("en", "US"));
        String time = dtf.format(zdt);

        String timeMessage = String.format("It is currently %s.", time);
        channel.createMessage(timeMessage).block();

        return null;
    }

    private Void sendPointsSummary(MessageChannel channel, Guild guild) {
        Map<String, Long> points = pointsRepository.findByGuildId(guild.getId())
                .stream().collect(Collectors.toMap(Points::getHouse, Points::getPoints));
        String pointsMessage = String.format("Gryffindor: %s\nHufflepuff: %s\nRavenclaw: %s\nSlytherin: %s\n",
                points.getOrDefault("g", 0L),
                points.getOrDefault("h", 0L),
                points.getOrDefault("r", 0L),
                points.getOrDefault("s", 0L));

        channel.createMessage(pointsMessage).block();

        return null;
    }
}
