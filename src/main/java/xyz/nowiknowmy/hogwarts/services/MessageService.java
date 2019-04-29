package xyz.nowiknowmy.hogwarts.services;

import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.function.Tuples;
import xyz.nowiknowmy.hogwarts.authorization.MemberAuthorization;
import xyz.nowiknowmy.hogwarts.domain.Guild;
import xyz.nowiknowmy.hogwarts.domain.Member;
import xyz.nowiknowmy.hogwarts.domain.Points;
import xyz.nowiknowmy.hogwarts.helpers.Str;
import xyz.nowiknowmy.hogwarts.repositories.GuildRepository;
import xyz.nowiknowmy.hogwarts.repositories.MemberRepository;
import xyz.nowiknowmy.hogwarts.repositories.PointsRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
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
    private final MemberRepository memberRepository;

    private static final ZoneId zone = ZoneId.of("America/New_York");

    public MessageService(GuildRepository guildRepository, PointsRepository pointsRepository, MemberRepository memberRepository) {
        this.guildRepository = guildRepository;
        this.pointsRepository = pointsRepository;
        this.memberRepository = memberRepository;
    }

    public Mono<Void> handle(Message message) {
        Mono<Guild> myGuild = message.getGuild()
            .map(guild -> guild.getId().asString())
            .map(guildRepository::findByGuildId);
        String content = message.getContent().get();

        if (Str.regex("^!(time|servertime) ?.*$").matches(content)) {
            return sendServerTime(message);
        } else if (Str.regex("^!points ?.*$").matches(content)) {
            return myGuild.flatMap(guild -> sendPointsSummary(message, guild));
        } else if (Str.regex("^!(gryffindor|hufflepuff|ravenclaw|slytherin) ?.*$").matches(content)) {
            List<String> regexMatches = Str.regex("^!(gryffindor|hufflepuff|ravenclaw|slytherin) ?.*$").groups(content);

            return message.getAuthorAsMember()
                .flatMap(member -> member.getRoles().collectList())
                .flatMapMany(Flux::fromIterable)
                .filter(role -> Str.regex("^(Gryffindor|Hufflepuff|Ravenclaw|Slytherin)$", Pattern.CASE_INSENSITIVE).matches(role.getName()))
                .count()
                .flatMap(existingHouses -> {
                    if (existingHouses == 0) {
                        return message.getGuild()
                            .flatMap(guild -> guild.getRoles().collectList())
                            .flatMapMany(Flux::fromIterable)
                            .filter(role -> role.getName().compareToIgnoreCase(regexMatches.get(1)) == 0)
                            .singleOrEmpty()
                            .map(Optional::of)
                            .defaultIfEmpty(Optional.empty())
                            .flatMap(role -> role.isPresent()
                                ? message.getAuthorAsMember()
                                .flatMap(member -> member.addRole(role.get().getId()))
                                .then(message.getChannel())
                                .flatMap(channel -> channel.createMessage(String.format("You are now a member of %s!", regexMatches.get(1))))
                                .then()
                                : sendError(message, "Sorry, we could not find that role!")
                                .then());
                    } else {
                        return sendError(message, "Sorry, you already have a house!").then();
                    }
                });
        } else if (Str.regex("^!([ghrs]) (add|sub|subtract|set) (\\d+)$").matches(content)) {
            logger.info(content);
            List<String> regexMatches = Str.regex("^!([ghrs]) (add|sub|subtract|set) (\\d+)$").groups(content);

            return message.getAuthorAsMember()
                .flatMap(member -> new MemberAuthorization(member).canModifyPoints()
                    .flatMap(authorized -> {
                        if (authorized) {
                            return myGuild.map(guild -> updatePoints(guild, regexMatches.get(1), regexMatches.get(2), regexMatches.get(3)))
                                .map(points -> Tuples.of(message, points))
                                .flatMap(TupleUtils.function(this::sendPointsUpdate));
                        } else {
                            return sendError(message, "Sorry, you are not permitted to modify house points!");
                        }
                    })).then();
        } else if (Str.regex("^!inactive (\\d+[d|m|y])$").matches(content)) {
            List<String> regexMatches = Str.regex("^!inactive (\\d+[d|m|y])$").groups(content);

            return message.getAuthorAsMember()
                .filterWhen(member -> new MemberAuthorization(member).canListInactive())
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty())
                .flatMap(member -> {
                    if (member.isPresent()) {
                        Duration interval = Duration.parse("P" + regexMatches.get(1).toUpperCase());
                        return myGuild
                            .flatMap(guild -> sendInactiveList(message, guild, interval));
                    } else {
                        return sendError(message, "Sorry, you are not permitted to list inactive members!");
                    }
                }).then();
        } else if (Str.regex("^!bumpyears$").matches(content)) {
            throw new UnsupportedOperationException("Not implemented");
        } else if (Str.regex("^!removetags$").matches(content)) {
            throw new UnsupportedOperationException("Not implemented");
        }

        return Mono.empty();
    }

    private Mono<Message> sendInactiveList(Message message, Guild guild, Duration interval) {
        LocalDateTime inactiveSince = LocalDateTime.now().minus(interval);
        List<Member> inactiveMembers = memberRepository.findInactiveMembers(guild.getId(), inactiveSince);
        String inactiveMembersString = inactiveMembers.stream().map(member -> {
            String name = member.getNickname() != null ? member.getNickname() : member.getUsername();
            String lastMessage = member.getLastMessageAt() != null
                ? ZonedDateTime.ofInstant(member.getLastMessageAt().toInstant(), zone).format(DateTimeFormatter.ofPattern("y-M-d H:mm z").withLocale(new Locale("en", "US")))
                : "[unknown]";
            return String.format("%s since %s", name, lastMessage);
        }).collect(Collectors.joining("\n"));

        if (inactiveMembersString == null || inactiveMembersString.isEmpty()) {
            return message.getChannel()
                .flatMap(channel -> channel.createMessage("No inactive members were found."));
        }

        if (inactiveMembersString.getBytes().length > 2042) {
            inactiveMembersString = inactiveMembersString.substring(0, 2039) + "...";
        }

        final String description = String.format("```%s```", inactiveMembersString);

        return message.getChannel()
            .flatMap(channel -> channel.createMessage(
                messageSpec -> messageSpec
                    .setContent("The following members are inactive:")
                    .setEmbed(embedSpec -> embedSpec
                        .setTitle("Inactive Members")
                        .setDescription(description))
            ));
    }

    private Mono<Message> sendPointsUpdate(Message message, Points points) {
        return message.getChannel()
            .flatMap(channel -> {
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

                return channel.createMessage(house + " now has " + points.getPoints() + " points.");
            });
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

    private Mono<Message> sendError(Message message, String errorMessage) {
        return message.getChannel()
            .flatMap(channel -> channel.createMessage(errorMessage));
    }

    private Mono<Void> sendServerTime(Message message) {
        return message.getChannel()
            .flatMap(channel -> {
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
