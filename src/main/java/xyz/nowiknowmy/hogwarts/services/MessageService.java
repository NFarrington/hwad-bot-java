package xyz.nowiknowmy.hogwarts.services;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import xyz.nowiknowmy.hogwarts.BotEventListener;
import xyz.nowiknowmy.hogwarts.authorization.MemberAuthorization;
import xyz.nowiknowmy.hogwarts.domain.Guild;
import xyz.nowiknowmy.hogwarts.domain.Points;
import xyz.nowiknowmy.hogwarts.exceptions.AuthorizationException;
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

    public Mono<Void> handle(Message message) {
        Guild guild = guildRepository.findByGuildId(message.getGuild().block().getId().asString());
        String content = message.getContent().get();

        if (Str.regex("^!(time|servertime) ?.*$").matches(content)) {
            return sendServerTime(message);
        } else if (Str.regex("^!points ?.*$").matches(content)) {
            return sendPointsSummary(message, guild);
        } else if (Str.regex("^!(gryffindor|hufflepuff|ravenclaw|slytherin) ?.*$").matches(content)) {
            List<String> regexMatches = Str.regex("^!(gryffindor|hufflepuff|ravenclaw|slytherin) ?.*$").groups(content);
            List<Role> existingHouse = message.getAuthorAsMember().block().getRoles().collectList().block()
                    .stream().filter(role -> Str.regex("^(Gryffindor|Hufflepuff|Ravenclaw|Slytherin)$", Pattern.CASE_INSENSITIVE).matches(role.getName()))
                    .collect(Collectors.toList());

            if (existingHouse.size() > 0) {
                sendError(message.getChannel().block(), "Sorry, you already have a house!");
                return Mono.empty();
            }

            Optional<Role> role = message.getGuild().block().getRoles().collectList().block()
                    .stream().filter(role1 -> role1.getName().compareToIgnoreCase(regexMatches.get(1)) == 0)
                    .findFirst();

            if (role.isEmpty()) {
                sendError(message.getChannel().block(), "Sorry, we could not find that role!");
                return Mono.empty();
            }

            message.getAuthorAsMember().block().addRole(role.get().getId()).block();
            message.getChannel().block().createMessage(String.format("You are now a member of %s!", regexMatches.get(1))).block();
        } else if (Str.regex("^!([ghrs]) (add|sub|subtract|set) (\\d+)$").matches(content)) {
            logger.info(content);
            List<String> regexMatches = Str.regex("^!([ghrs]) (add|sub|subtract|set) (\\d+)$").groups(content);

            message.getAuthorAsMember()
                    .filterWhen(member -> (new MemberAuthorization(member)).canModifyPoints())
                    .doOnError(error -> {
                        if (error instanceof AuthorizationException) {
                            message.getChannel().subscribe(channel -> sendError(channel, "Sorry, you are not permitted to modify house points!"));
                        } else {
                            throw new RuntimeException(error);
                        }
                    })
                    .map(member -> updatePoints(guild, regexMatches.get(1), regexMatches.get(2), regexMatches.get(3)))
                    .map(points -> sendPointsUpdate(message.getChannel().block(), points))
                    .subscribe();
        } else if (Str.regex("^!inactive (\\d+[d|m|y])$").matches(content)) {
            throw new UnsupportedOperationException("Not implemented");
        } else if (Str.regex("^!bumpyears$").matches(content)) {
            throw new UnsupportedOperationException("Not implemented");
        } else if (Str.regex("^!removetags$").matches(content)) {
            throw new UnsupportedOperationException("Not implemented");
        }

        return Mono.empty();
    }

    private MessageChannel sendPointsUpdate(MessageChannel channel, Points points) {
        logger.info("Updating channel");
        final String house;
        switch (points.getHouse()) {
            case "g":
                house = "Gryffindor";
                break;
            case "h":
                house = "Hufflepuff";
                break;
            case "r":
                house = "Ravenclaw";
                break;
            case "s":
                house = "Slytherin";
                break;
            default:
                throw new IllegalArgumentException(String.format("'%s' is not a house.", points.getHouse()));
        }

        channel.createMessage(house + " now has " + points.getPoints() + " points.").subscribe();

        return channel;
    }

    private Points updatePoints(Guild guild, String house, String operation, String points) {
        logger.info(String.format("Updating points for %s, %s %s %s", guild.getId(), house, operation, points));

        Points currentPoints = pointsRepository.findByGuildIdAndHouse(guild.getId(), house);
        if (currentPoints == null) {
            currentPoints = new Points(guild.getId(), house, 0L);
        }

        switch (operation) {
            case "add":
                currentPoints.setPoints(currentPoints.getPoints() + Long.valueOf(points));
                break;
            case "sub":
            case "subtract":
                currentPoints.setPoints(currentPoints.getPoints() - Long.valueOf(points));
                break;
            case "set":
                currentPoints.setPoints(Long.valueOf(points));
                break;
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }

        pointsRepository.save(currentPoints);

        return currentPoints;
    }

    private void sendError(MessageChannel channel, String errorMessage) {
        channel.createMessage(errorMessage).subscribe();
    }

    private Mono<Void> sendServerTime(Message message) {
        return message.getChannel()
            .flatMap(channel -> {
                ZoneId zone = ZoneId.of("America/New_York");
                ZonedDateTime zdt = ZonedDateTime.now(zone);
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("h:mma z")
                    .withLocale(new Locale("en", "US"));
                String time = dtf.format(zdt);

                String timeMessage = String.format("It is currently %s.", time);
                return channel.createMessage(timeMessage);
            }).flatMap(ignored -> Mono.empty());
    }

    private Mono<Void> sendPointsSummary(Message message, Guild guild) {
        return message.getChannel()
            .flatMap(channel -> {
                Map<String, Long> points = pointsRepository.findByGuildId(guild.getId())
                    .stream().collect(Collectors.toMap(Points::getHouse, Points::getPoints));
                String pointsMessage = String.format("Gryffindor: %s\nHufflepuff: %s\nRavenclaw: %s\nSlytherin: %s\n",
                    points.getOrDefault("g", 0L),
                    points.getOrDefault("h", 0L),
                    points.getOrDefault("r", 0L),
                    points.getOrDefault("s", 0L));

                return channel.createMessage(pointsMessage);
            }).flatMap(ignored -> Mono.empty());
    }
}
