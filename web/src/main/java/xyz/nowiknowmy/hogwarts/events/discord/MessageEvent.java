package xyz.nowiknowmy.hogwarts.events.discord;

import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.Role;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

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
        if (event.getMember().isPresent()) {
            this.member = event.getMember().get();
        }

        this.message = event.getMessage();

        return event.getGuild()
            .map(guild -> this.guild = guild)
            .flatMap(guild -> guild.getRoles().collectList())
            .map(guildRoles -> this.guildRoles = guildRoles)
            .then(Mono.justOrEmpty(member))
            .flatMap(member -> member.getRoles().collectList())
            .map(memberRoles -> this.memberRoles = memberRoles)
            .then(message.getChannel())
            .map(channel -> this.channel = channel)
            .thenReturn(this);
    }

    public static Mono<MessageEvent> parse(discord4j.core.event.domain.message.MessageCreateEvent event) {
        MessageEvent messageEvent = new MessageEvent(event);
        return messageEvent.parse();
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
