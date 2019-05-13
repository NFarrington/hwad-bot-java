package xyz.nowiknowmy.hogwarts.domain.discord.message;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.Role;

import java.util.List;

public class GuildWebhookMessage extends GuildMessage {

    GuildWebhookMessage(Message message, MessageChannel channel,
                        Guild guild, List<Role> guildRoles) {
        super(message, channel, guild, guildRoles);
    }

}
