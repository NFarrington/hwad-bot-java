package xyz.nowiknowmy.hogwarts.domain.discord.message;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.Role;
import org.springframework.util.Assert;

import java.util.List;

public class GuildMemberMessage extends GuildMessage {

    private Member member;
    private List<Role> memberRoles;

    GuildMemberMessage(Message message, MessageChannel channel,
                       Guild guild, List<Role> guildRoles,
                       Member member, List<Role> memberRoles) {
        super(message, channel, guild, guildRoles);

        Assert.notNull(member, "Member must not be null.");
        Assert.notNull(memberRoles, "Member roles must not be null.");
        this.member = member;
        this.memberRoles = memberRoles;
    }

    public Member getMember() {
        return member;
    }

    public List<Role> getMemberRoles() {
        return memberRoles;
    }
}
