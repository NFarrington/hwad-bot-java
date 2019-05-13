package xyz.nowiknowmy.hogwarts.tasks;

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
import xyz.nowiknowmy.hogwarts.events.MemberPreSavePublisher;
import xyz.nowiknowmy.hogwarts.domain.discord.message.ReceivedMessageFactory;
import xyz.nowiknowmy.hogwarts.repositories.GuildRepository;
import xyz.nowiknowmy.hogwarts.repositories.MemberRepository;
import xyz.nowiknowmy.hogwarts.services.MessageHandler;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DiscordBot {

    private final MessageHandler messageService;
    private final GuildRepository guildRepository;
    private final MemberRepository memberRepository;
    private final MemberPreSavePublisher memberPreSavePublisher;

    private static final Logger logger = LoggerFactory.getLogger(DiscordBot.class);

    @Value("${discord.bot-token}")
    private String discordBotToken;

    public DiscordBot(MessageHandler messageService, GuildRepository guildRepository, MemberRepository memberRepository, MemberPreSavePublisher memberPreSavePublisher) {
        this.messageService = messageService;
        this.guildRepository = guildRepository;
        this.memberRepository = memberRepository;
        this.memberPreSavePublisher = memberPreSavePublisher;
    }

    @Scheduled(fixedDelay = 10000)
    public void scheduleFixedDelayTask() {
        logger.info("Starting Discord bot @ {}", System.currentTimeMillis() / 1000);

        final DiscordClient client = new DiscordClientBuilder(discordBotToken).build();

        client.getEventDispatcher().on(ReadyEvent.class)
            .subscribe(ready -> logger.info("Logged in as {} @ {}", ready.getSelf().getUsername(), System.currentTimeMillis() / 1000));
        client.getEventDispatcher().on(DisconnectEvent.class)
            .subscribe(disconnect -> logger.error("Disconnected @ {}", System.currentTimeMillis() / 1000));
        client.getEventDispatcher().on(ReconnectEvent.class)
            .subscribe(reconnect -> logger.error("Reconnected @ {}", System.currentTimeMillis() / 1000));

        client.getEventDispatcher().on(GuildCreateEvent.class)
            .map(GuildCreateEvent::getGuild)
            .flatMap(this::syncGuild)
            .map(Guild::getName)
            .doOnError(error -> logger.error(error.getMessage(), error))
            .retry()
            .subscribe(guildName -> logger.info("Guild create event {} @ {}", guildName, System.currentTimeMillis() / 1000));

        client.getEventDispatcher().on(UserUpdateEvent.class)
            .map(UserUpdateEvent::getCurrent)
            .flatMap(this::syncUser)
            .doOnError(error -> logger.error(error.getMessage(), error))
            .retry()
            .subscribe(user -> logger.info("User update event {} @ {}", user.getUsername(), System.currentTimeMillis() / 1000));

        client.getEventDispatcher().on(MemberJoinEvent.class)
            .map(MemberJoinEvent::getMember)
            .flatMap(this::syncGuildMember)
            .doOnError(error -> logger.error(error.getMessage(), error))
            .retry()
            .subscribe(member -> logger.info("Member join event {} @ {}", member.getUsername(), System.currentTimeMillis() / 1000));

        client.getEventDispatcher().on(MemberUpdateEvent.class)
            .flatMap(MemberUpdateEvent::getMember)
            .flatMap(this::syncGuildMember)
            .doOnError(error -> logger.error(error.getMessage(), error))
            .retry()
            .subscribe(member -> logger.info("Member update event {} @ {}", member.getUsername(), System.currentTimeMillis() / 1000));

        client.getEventDispatcher().on(MemberLeaveEvent.class)
            .filter(event -> event.getMember().isPresent())
            .map(event -> event.getMember().get())
            .flatMap(this::deleteGuildMember)
            .doOnError(error -> logger.error(error.getMessage(), error))
            .retry()
            .subscribe(member -> logger.info("Member leave event {} @ {}", member.getUsername(), System.currentTimeMillis() / 1000));

        client.getEventDispatcher().on(MessageCreateEvent.class)
            .flatMap(ReceivedMessageFactory::load)
            .map(ReceivedMessageFactory::create)
            .flatMap(messageService::handle)
            .doOnError(error -> logger.error(error.getMessage(), error))
            .retry()
            .subscribe();

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
            memberPreSavePublisher.publish(member);
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
        logger.info("Syncing guild members for guild {}", guild.getName());
        xyz.nowiknowmy.hogwarts.domain.Guild myGuild = guildRepository.findByGuildId(guild.getId().asString());
        List<Member> knownMembers = memberRepository.findByGuildId(myGuild.getId());

        return guild.getMembers()
            .map(member -> {
                logger.info("syncGuildMembers, about to sync member: {}", member.getUsername());
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
        logger.info("Syncing guild member {}", member.getUsername());
        return syncUser(member).flatMap(ignored -> {
            xyz.nowiknowmy.hogwarts.domain.Guild guild = guildRepository.findByGuildId(member.getGuildId().asString());
            Member myMember = memberRepository.findByUidAndGuildIdWithTrashed(member.getId().asString(), guild.getId());
            if (myMember == null) {
                myMember = new Member();
                myMember.setUid(member.getId().asString());
                myMember.setGuildId(guild.getId());
                myMember.setLastMessageAt(LocalDateTime.now());
            }
            myMember.setDeletedAt(null);
            myMember.setUsername(member.getUsername());
            if (member.getNickname().isPresent()) {
                myMember.setNickname(member.getNickname().get());
            } else {
                myMember.setNickname(null);
            }
            myMember.setBot(member.isBot());

            memberPreSavePublisher.publish(myMember);
            memberRepository.save(myMember);

            return Mono.just(member);
        });
    }
}
