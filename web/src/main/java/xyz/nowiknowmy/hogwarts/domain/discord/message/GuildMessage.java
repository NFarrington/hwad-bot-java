package xyz.nowiknowmy.hogwarts.domain.discord.message;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.Role;
import org.springframework.util.Assert;

import java.util.List;

public abstract class GuildMessage extends ReceivedMessage {

    private Guild guild;
    private List<Role> guildRoles;

    GuildMessage(Message message, MessageChannel channel,
                 Guild guild, List<Role> guildRoles) {
        super(message, channel);

        Assert.notNull(guild, "Guild must not be null.");
        Assert.notNull(guildRoles, "Guild roles must not be null.");
        this.guild = guild;
        this.guildRoles = guildRoles;
    }

    public Guild getGuild() {
        return guild;
    }

    public List<Role> getGuildRoles() {
        return guildRoles;
    }
}
