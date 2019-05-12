package xyz.nowiknowmy.hogwarts.events.discord;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.Role;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;

import java.util.List;

public class MessageEvent {

    private discord4j.core.event.domain.message.MessageCreateEvent event;

    @Nullable
    private Guild guild;
    private MessageChannel channel;
    @Nullable
    private Member member;
    @Nullable
    private List<Role> memberRoles;
    @Nullable
    private List<Role> guildRoles;
    private Message message;

    private MessageEvent(discord4j.core.event.domain.message.MessageCreateEvent event) {
        this.event = event;
    }

    private Mono<MessageEvent> parse() {
        event.getMember().ifPresent(discordMember -> this.member = discordMember);

        this.message = event.getMessage();

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

    public static Mono<MessageEvent> parse(discord4j.core.event.domain.message.MessageCreateEvent event) {
        return new MessageEvent(event).parse();
    }

    public MessageChannel getChannel() {
        return channel;
    }

    @Nullable
    public Guild getGuild() {
        return guild;
    }

    @Nullable
    public List<Role> getGuildRoles() {
        return guildRoles;
    }

    @Nullable
    public Member getMember() {
        return member;
    }

    @Nullable
    public List<Role> getMemberRoles() {
        return memberRoles;
    }

    public Message getMessage() {
        return message;
    }
}
