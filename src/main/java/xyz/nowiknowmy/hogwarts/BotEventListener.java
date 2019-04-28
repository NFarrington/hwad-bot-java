package xyz.nowiknowmy.hogwarts;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.lifecycle.DisconnectEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.lifecycle.ReconnectEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xyz.nowiknowmy.hogwarts.services.MessageService;

@Component
public class BotEventListener {

    private final MessageService messageService;

    private static final Logger logger = LoggerFactory.getLogger(BotEventListener.class);

    @Value("${discord.bot.token}")
    private String discordBotToken;

    public BotEventListener(MessageService messageService) {
        this.messageService = messageService;
    }

    @Scheduled(fixedDelay = 10000)
    public void scheduleFixedDelayTask() {
        logger.info("Starting Discord bot @ " + System.currentTimeMillis() / 1000);

        final DiscordClient client = new DiscordClientBuilder(discordBotToken).build();

        client.getEventDispatcher().on(ReadyEvent.class)
                .subscribe(ready -> logger.info("Logged in as " + ready.getSelf().getUsername() + " @ " + System.currentTimeMillis() / 1000));
        client.getEventDispatcher().on(DisconnectEvent.class)
                .subscribe(disconnect -> logger.info("Disconnected @ " + System.currentTimeMillis() / 1000));
        client.getEventDispatcher().on(ReconnectEvent.class)
                .subscribe(reconnect -> logger.info("Reconnected @ " + System.currentTimeMillis() / 1000));

        client.getEventDispatcher().on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .subscribe(messageService::handle);

        client.login().block();
    }

}
