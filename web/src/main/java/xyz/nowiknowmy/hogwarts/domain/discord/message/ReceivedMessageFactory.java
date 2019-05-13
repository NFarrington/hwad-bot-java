package xyz.nowiknowmy.hogwarts.domain.discord.message;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class ReceivedMessageFactory {

    private MessageCreateEvent event;

    private Message message;
    private MessageChannel channel;
    @Nullable
    private Guild guild;
    private List<Role> guildRoles = new ArrayList<>();
    @Nullable
    private Member member;
    private List<Role> memberRoles = new ArrayList<>();
    @Nullable
    private User user;

    private ReceivedMessageFactory(MessageCreateEvent event) {
        this.event = event;
    }

    public static Mono<ReceivedMessageFactory> load(MessageCreateEvent event) {
        return new ReceivedMessageFactory(event).load();
    }

    private Mono<ReceivedMessageFactory> load() {
        event.getMember().ifPresent(discordMember -> this.member = discordMember);

        this.message = event.getMessage();

        message.getAuthor().ifPresent(discordUser -> this.user = discordUser);

        return event.getGuild()
            .map(discordGuild -> this.guild = discordGuild)
            .flatMap(discordGuild -> discordGuild.getRoles().collectList())
            .map(discordGuildRoles -> this.guildRoles = discordGuildRoles)
            .then(Mono.justOrEmpty(member))
            .flatMap(discordMember -> discordMember.getRoles().collectList())
            .map(discordMemberRoles -> this.memberRoles = discordMemberRoles)
            .then(message.getChannel())
            .map(discordChannel -> this.channel = discordChannel)
            .thenReturn(this);
    }

    public ReceivedMessage create() {
        if (guild != null) {
            return member != null
                ? new GuildMemberMessage(message, channel, guild, guildRoles, member, memberRoles)
                : new GuildWebhookMessage(message, channel, guild, guildRoles);
        }

        if (user != null) {
            return new PrivateUserMessage(message, channel, user);
        }

        throw new IllegalStateException("No guild or user information present.");
    }
}
