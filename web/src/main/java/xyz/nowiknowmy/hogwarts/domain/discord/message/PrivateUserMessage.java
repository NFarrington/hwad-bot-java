package xyz.nowiknowmy.hogwarts.domain.discord.message;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import org.springframework.util.Assert;

public class PrivateUserMessage extends ReceivedMessage {

    private User user;

    PrivateUserMessage(Message message, MessageChannel channel, User user) {
        super(message, channel);

        Assert.notNull(user, "User must not be null.");
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
