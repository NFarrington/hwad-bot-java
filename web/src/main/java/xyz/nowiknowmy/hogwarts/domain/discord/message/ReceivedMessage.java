package xyz.nowiknowmy.hogwarts.domain.discord.message;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import org.springframework.util.Assert;

public abstract class ReceivedMessage {

    private Message message;
    private MessageChannel channel;
    private String messageContent;

    ReceivedMessage(Message message, MessageChannel channel) {
        Assert.notNull(message, "Message must not be null.");
        Assert.notNull(channel, "Channel must not be null.");
        this.message = message;
        this.channel = channel;

        messageContent = message.getContent().orElse("");
    }

    public Message getMessage() {
        return message;
    }

    public MessageChannel getChannel() {
        return channel;
    }

    public String getMessageContent() {
        return messageContent;
    }
}
