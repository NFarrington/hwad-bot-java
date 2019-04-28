package xyz.nowiknowmy.hogwarts.services;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import org.springframework.stereotype.Service;
import xyz.nowiknowmy.hogwarts.domain.Guild;
import xyz.nowiknowmy.hogwarts.domain.Points;
import xyz.nowiknowmy.hogwarts.helpers.Str;
import xyz.nowiknowmy.hogwarts.repositories.GuildRepository;
import xyz.nowiknowmy.hogwarts.repositories.PointsRepository;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessageService {
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
            throw new UnsupportedOperationException("Not implemented");
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
