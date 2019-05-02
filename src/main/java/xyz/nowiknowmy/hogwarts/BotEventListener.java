package xyz.nowiknowmy.hogwarts;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.UserUpdateEvent;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.event.domain.lifecycle.DisconnectEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.lifecycle.ReconnectEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import xyz.nowiknowmy.hogwarts.domain.Member;
import xyz.nowiknowmy.hogwarts.repositories.GuildRepository;
import xyz.nowiknowmy.hogwarts.repositories.MemberRepository;
import xyz.nowiknowmy.hogwarts.services.MessageService;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BotEventListener {

    private final MessageService messageService;
    private final GuildRepository guildRepository;
    private final MemberRepository memberRepository;

    private static final Logger logger = LoggerFactory.getLogger(BotEventListener.class);

    @Value("${discord.bot.token}")
    private String discordBotToken;

    public BotEventListener(MessageService messageService, GuildRepository guildRepository, MemberRepository memberRepository) {
        this.messageService = messageService;
        this.guildRepository = guildRepository;
        this.memberRepository = memberRepository;
    }

    @Scheduled(fixedDelay = 10000)
    public void scheduleFixedDelayTask() {
        logger.info("Starting Discord bot @ " + System.currentTimeMillis() / 1000);

        final DiscordClient client = new DiscordClientBuilder(discordBotToken).build();

        client.getEventDispatcher().on(ReadyEvent.class)
            .subscribe(ready -> logger.info("Logged in as " + ready.getSelf().getUsername() + " @ " + System.currentTimeMillis() / 1000));
        client.getEventDispatcher().on(DisconnectEvent.class)
            .subscribe(disconnect -> logger.error("Disconnected @ " + System.currentTimeMillis() / 1000));
        client.getEventDispatcher().on(ReconnectEvent.class)
            .subscribe(reconnect -> logger.error("Reconnected @ " + System.currentTimeMillis() / 1000));

        client.getEventDispatcher().on(GuildCreateEvent.class)
            .map(GuildCreateEvent::getGuild)
            .flatMap(this::syncGuild)
            .map(Guild::getName)
            .subscribe(guildName -> logger.info("Guild create event " + guildName + " @ " + System.currentTimeMillis() / 1000));

        client.getEventDispatcher().on(UserUpdateEvent.class)
            .map(UserUpdateEvent::getCurrent)
            .flatMap(this::syncUser)
            .subscribe(user -> logger.info("User update event " + user.getUsername() + " @ " + System.currentTimeMillis() / 1000));

        client.getEventDispatcher().on(MemberJoinEvent.class)
            .map(MemberJoinEvent::getMember)
            .flatMap(this::syncGuildMember)
            .subscribe(member -> logger.info(String.format("Member join event %s @ %s", member.getUsername(), System.currentTimeMillis() / 1000)));

        client.getEventDispatcher().on(MemberUpdateEvent.class)
            .flatMap(MemberUpdateEvent::getMember)
            .flatMap(this::syncGuildMember)
            .subscribe(member -> logger.info(String.format("Member update event %s @ %s", member.getUsername(), System.currentTimeMillis() / 1000)));

        client.getEventDispatcher().on(MemberLeaveEvent.class)
            .filter(event -> event.getMember().isPresent())
            .map(event -> event.getMember().get())
            .flatMap(this::deleteGuildMember)
            .subscribe(member -> logger.info(String.format("Member leave event %s @ %s", member.getUsername(), System.currentTimeMillis() / 1000)));

        client.getEventDispatcher().on(MessageCreateEvent.class)
            .map(MessageCreateEvent::getMessage)
            .flatMap(messageService::handle)
            .doOnError(error -> logger.error(error.getMessage(), error))
            .retry()
            .subscribe();
//                .subscribe(messageService::handle, error -> logger.error(error.getMessage(), error));

        client.login().block();
    }

    private Mono<discord4j.core.object.entity.Member> deleteGuildMember(discord4j.core.object.entity.Member member) {
        xyz.nowiknowmy.hogwarts.domain.Guild myGuild = guildRepository.findByGuildId(member.getGuildId().asString());
        memberRepository.softDelete(memberRepository.findByUidAndGuildId(member.getId().asString(), myGuild.getId()).getId());

        return Mono.just(member);
    }

    private Mono<User> syncUser(User user) {
        List<Member> members = memberRepository.findByUid(user.getId().asString());
        members.forEach(member -> {
            member.setUsername(user.getUsername());
            memberRepository.save(member);
        });

        return Mono.just(user);
    }

    private Mono<Guild> syncGuild(Guild guild) {
        xyz.nowiknowmy.hogwarts.domain.Guild myGuild = guildRepository.findByGuildIdWithTrashed(guild.getId().asString());
        if (myGuild == null) {
            myGuild = new xyz.nowiknowmy.hogwarts.domain.Guild();
        }
        myGuild.setDeletedAt(null);
        myGuild.setGuildId(guild.getId().asString());
        myGuild.setName(guild.getName());
        guildRepository.save(myGuild);

        return syncGuildMembers(guild)
            .map(ignored -> guild);
    }

    private Mono<Guild> syncGuildMembers(Guild guild) {
        logger.info("Syncing guild members for guild " + guild.getName());
        xyz.nowiknowmy.hogwarts.domain.Guild myGuild = guildRepository.findByGuildId(guild.getId().asString());
        List<Member> knownMembers = memberRepository.findByGuildId(myGuild.getId());

        return guild.getMembers()
            .map(member -> {
                logger.info("syncGuildMembers, about to sync member: " + member.getUsername());
                return member;
            })
            .flatMap(this::syncGuildMember)
            .map(member -> knownMembers.removeIf(knownMember -> knownMember.getUid().equals(member.getId().asString())))
            .then()
            .flatMap(ignored -> {
                knownMembers.forEach(member -> memberRepository.softDelete(member.getId()));
                return Mono.just(guild);
            });
    }

    private Mono<discord4j.core.object.entity.Member> syncGuildMember(discord4j.core.object.entity.Member member) {
        logger.info("Syncing guild member " + member.getUsername());
        return syncUser(member).flatMap(ignored -> {
            xyz.nowiknowmy.hogwarts.domain.Guild guild = guildRepository.findByGuildId(member.getGuildId().asString());
            Member myMember = memberRepository.findByUidAndGuildIdWithTrashed(member.getId().asString(), guild.getId());
            if (myMember == null) {
                logger.info(String.format("myMember is null for user %s %s %s ", member.getUsername(), member.getId().asString(), guild.getId()));
                myMember = new Member();
                myMember.setUid(member.getId().asString());
                myMember.setGuildId(guild.getId());
                myMember.setLastMessageAt(LocalDateTime.now());
            }
            myMember.setDeletedAt(null);
            myMember.setUsername(member.getUsername());
            if (member.getNickname().isPresent()) {
                myMember.setNickname(member.getNickname().get());
            }
            myMember.setBot(member.isBot());

            memberRepository.save(myMember);

            return Mono.just(member);
        });
    }

//    private void syncGuilds(Set<Guild> guilds) {
//        for (Guild guild : guilds) {
//            syncGuild(guild);
//        }
//    }
}
