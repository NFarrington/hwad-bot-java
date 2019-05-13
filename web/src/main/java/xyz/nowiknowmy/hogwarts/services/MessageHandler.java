package xyz.nowiknowmy.hogwarts.services;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import xyz.nowiknowmy.hogwarts.domain.Guild;
import xyz.nowiknowmy.hogwarts.domain.Member;
import xyz.nowiknowmy.hogwarts.domain.Points;
import xyz.nowiknowmy.hogwarts.domain.discord.message.GuildMemberMessage;
import xyz.nowiknowmy.hogwarts.domain.discord.message.GuildWebhookMessage;
import xyz.nowiknowmy.hogwarts.domain.discord.message.ReceivedMessage;
import xyz.nowiknowmy.hogwarts.domain.discord.message.PrivateUserMessage;
import xyz.nowiknowmy.hogwarts.exceptions.UnknownMessageException;
import xyz.nowiknowmy.hogwarts.repositories.GuildRepository;
import xyz.nowiknowmy.hogwarts.repositories.MemberRepository;
import xyz.nowiknowmy.hogwarts.repositories.PointsRepository;
import xyz.nowiknowmy.hogwarts.utils.MemberAuthorization;
import xyz.nowiknowmy.hogwarts.utils.Str;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    private final GuildRepository guildRepository;
    private final PointsRepository pointsRepository;
    private final MemberRepository memberRepository;

    private static final ZoneId zone = ZoneId.of("America/New_York");

    public MessageHandler(GuildRepository guildRepository, PointsRepository pointsRepository, MemberRepository memberRepository) {
        this.guildRepository = guildRepository;
        this.pointsRepository = pointsRepository;
        this.memberRepository = memberRepository;
    }

    public Mono<Message> handle(ReceivedMessage messageDetails) {
        if (messageDetails instanceof GuildMemberMessage) {
            return handleGuildMemberMessage((GuildMemberMessage) messageDetails);
        }

        if (messageDetails instanceof GuildWebhookMessage) {
            return handleGuildWebhookMessage((GuildWebhookMessage) messageDetails);
        }

        if (messageDetails instanceof PrivateUserMessage) {
            return handlePrivateUserMessage((PrivateUserMessage) messageDetails);
        }

        throw new UnknownMessageException("Unknown message type: " + messageDetails.getClass().getName());
    }

    private Mono<Message> handlePrivateUserMessage(PrivateUserMessage messageDetails) {
        logger.info("Received a private message from \"{}\". Message: \"{}\")",
            messageDetails.getUser(),
            messageDetails.getMessage().getContent().orElse(""));

        return Mono.empty();
    }

    private Mono<Message> handleGuildMemberMessage(GuildMemberMessage messageDetails) {
        // TODO: check for null
        Guild myGuild = guildRepository.findByGuildId(messageDetails.getGuild().getId().asString());

        return processMessageContents(messageDetails, myGuild);
    }

    private Mono<Message> handleGuildWebhookMessage(GuildWebhookMessage messageDetails) {
        return Mono.empty();
    }

    private Mono<Message> processMessageContents(GuildMemberMessage messageDetails, Guild myGuild) {
        String messageContent = messageDetails.getMessageContent();

        if (messageDetails.getMessageContent().isEmpty()) {
            return Mono.empty();
        } else if (Str.regex("^!(time|servertime) ?.*$").matches(messageContent)) {
            return sendServerTime(messageDetails.getChannel());
        } else if (Str.regex("^!points ?.*$").matches(messageContent)) {
            return sendPointsSummary(messageDetails.getChannel(), myGuild);
        } else if (Str.regex("^!(gryffindor|hufflepuff|ravenclaw|slytherin) ?.*$").matches(messageContent)) {
            List<String> regexMatches = Str.regex("^!(gryffindor|hufflepuff|ravenclaw|slytherin) ?.*$").groups(messageContent);
            regexMatches.set(1, StringUtils.capitalize(regexMatches.get(1)));

            if (memberHasAHouse(messageDetails.getMemberRoles())) {
                return sendError(messageDetails.getChannel(), "Sorry, you already have a house!");
            }

            return findRoleByName(regexMatches.get(1), messageDetails.getGuildRoles())
                .map(value -> messageDetails.getMember().addRole(value.getId())
                    .then(messageDetails.getChannel().createMessage(String.format("You are now a member of %s!", regexMatches.get(1)))))
                .orElseGet(() -> sendError(messageDetails.getChannel(), String.format("Sorry, we could not find a role called %s!", regexMatches.get(1))));
        } else if (Str.regex("^!([ghrs]) (add|sub|subtract|set) (\\d+)$").matches(messageContent)) {
            if (!MemberAuthorization.canModifyPoints(messageDetails.getMemberRoles())) {
                return sendError(messageDetails.getChannel(), "Sorry, you are not permitted to modify house points!");
            }

            List<String> regexMatches = Str.regex("^!([ghrs]) (add|sub|subtract|set) (\\d+)$").groups(messageContent);
            Points points = updatePoints(myGuild, regexMatches.get(1), regexMatches.get(2), regexMatches.get(3));
            return sendPointsUpdate(messageDetails.getChannel(), points);
        } else if (Str.regex("^!inactive (\\d+[d|m|y])$").matches(messageContent)) {
            if (!MemberAuthorization.canListInactive(messageDetails.getMemberRoles())) {
                return sendError(messageDetails.getChannel(), "Sorry, you are not permitted to list inactive members!");
            }

            List<String> regexMatches = Str.regex("^!inactive (\\d+[d|m|y])$").groups(messageContent);
            Duration interval = Duration.parse("P" + regexMatches.get(1).toUpperCase());
            return sendInactiveList(messageDetails.getChannel(), myGuild, interval);
        } else if (Str.regex("^!bumpyears$").matches(messageContent)) {
            if (!MemberAuthorization.canBumpYears(messageDetails.getMemberRoles())) {
                return sendError(messageDetails.getChannel(), "Sorry, you are not permitted to bump year groups!");
            }

            return messageDetails.getChannel().createMessage("Updating years... this may take a minute or two!")
                .flatMap(sentMessage -> moveYearsForward(messageDetails.getGuild(), messageDetails.getGuildRoles())
                    .then(sentMessage.edit(editSpec -> editSpec.setContent("Updating years... done!"))));
        }

        return Mono.empty();
    }

    private Optional<Role> findRoleByName(String roleName, List<Role> roles) {
        return roles.stream()
            .filter(role -> role.getName().compareToIgnoreCase(roleName) == 0)
            .findFirst();
    }

    private static boolean memberHasAHouse(List<Role> memberRoles) {
        return memberRoles.stream().anyMatch(role ->
            Str.regex("^(Gryffindor|Hufflepuff|Ravenclaw|Slytherin)$", Pattern.CASE_INSENSITIVE).matches(role.getName()));
    }

    private Mono<Message> moveYearsForward(discord4j.core.object.entity.Guild guild, List<Role> guildRoles) {
        Map<String, String> transitions = new HashMap<>();
        transitions.put("First Year", "Second Year");
        transitions.put("Second Year", "Third Year");
        transitions.put("Third Year", "Fourth Year");
        transitions.put("Fourth Year", "Fifth Year");
        transitions.put("Fifth Year", "Six Year");
        transitions.put("Sixth Year", "Seventh Year");
        transitions.put("Seventh Year", "Graduated Year");

        return guild.getMembers()
            .flatMap(member -> member.getRoles()
                .filter(role -> transitions.containsKey(role.getName()))
                .flatMap(oldRole -> Flux.fromIterable(guildRoles)
                    .filter(guildRole -> guildRole.getName().equals(transitions.get(oldRole.getName())))
                    .flatMap(newRole -> Flux.concat(member.addRole(newRole.getId()), member.removeRole(oldRole.getId())))
                    .then(Mono.empty())
                ).then(Mono.empty())
            ).then(Mono.empty());
    }

    private Mono<Message> sendInactiveList(MessageChannel channel, Guild guild, Duration interval) {
        LocalDateTime inactiveSince = LocalDateTime.now().minus(interval);
        List<Member> inactiveMembers = memberRepository.findInactiveMembers(guild.getId(), inactiveSince);
        String inactiveMembersString = inactiveMembers.stream().map(member -> {
            String name = member.getNickname() != null ? member.getNickname() : member.getUsername();
            String lastMessage = member.getLastMessageAt() != null
                ? ZonedDateTime.of(member.getLastMessageAt(), TimeZone.getDefault().toZoneId()).withZoneSameInstant(zone).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z").withLocale(new Locale("en", "US")))
                : "[unknown]";
            return String.format("%s since %s", name, lastMessage);
        }).collect(Collectors.joining("\n"));

        if (inactiveMembersString == null || inactiveMembersString.isEmpty()) {
            return channel.createMessage("No inactive members were found.");
        }

        if (inactiveMembersString.length() > 2042) {
            inactiveMembersString = inactiveMembersString.substring(0, 2039) + "...";
        }

        final String description = String.format("```%s```", inactiveMembersString);

        return channel.createMessage(messageSpec ->
            messageSpec.setContent("The following members are inactive:").setEmbed(embedSpec ->
                embedSpec.setTitle("Inactive Members").setDescription(description)));
    }

    private Mono<Message> sendPointsUpdate(MessageChannel channel, Points points) {
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

        return channel.createMessage(String.format("%s now has %s points.", house, points.getPoints()));
    }

    private Points updatePoints(Guild guild, String house, String operation, String points) {
        logger.info("Updating points for {}, {} {} {}", guild.getId(), house, operation, points);

        Points currentPoints = pointsRepository.findByGuildIdAndHouse(guild.getId(), house);
        if (currentPoints == null) {
            currentPoints = new Points(guild.getId(), house, 0L);
        }

        switch (operation) {
            case "add":
                currentPoints.setPoints(currentPoints.getPoints() + Long.parseLong(points));
                break;
            case "sub":
            case "subtract":
                currentPoints.setPoints(currentPoints.getPoints() - Long.parseLong(points));
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

    private Mono<Message> sendError(MessageChannel channel, String errorMessage) {
        return channel.createMessage(errorMessage);
    }

    private Mono<Message> sendServerTime(MessageChannel channel) {
        ZonedDateTime zdt = ZonedDateTime.now(zone);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("h:mma z")
            .withLocale(new Locale("en", "US"));
        String time = dtf.format(zdt);

        String timeMessage = String.format("It is currently %s.", time);

        return channel.createMessage(timeMessage);
    }

    private Mono<Message> sendPointsSummary(MessageChannel channel, Guild guild) {
        Map<String, Long> points = pointsRepository.findByGuildId(guild.getId())
            .stream().collect(Collectors.toMap(Points::getHouse, Points::getPoints));
        String pointsMessage = String.format("Gryffindor: %s%nHufflepuff: %s%nRavenclaw: %s%nSlytherin: %s%n",
            points.getOrDefault("g", 0L),
            points.getOrDefault("h", 0L),
            points.getOrDefault("r", 0L),
            points.getOrDefault("s", 0L));

        return channel.createMessage(pointsMessage);
    }
}
