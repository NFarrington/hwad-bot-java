package xyz.nowiknowmy.hogwarts.services;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import org.springframework.stereotype.Service;
import xyz.nowiknowmy.hogwarts.domain.Guild;
import xyz.nowiknowmy.hogwarts.repositories.GuildRepository;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MessageService {
    private final GuildRepository guildRepository;

    public MessageService(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    public Void handle(Message message) {
        Guild guild = guildRepository.findByGuildId(message.getGuild().block().getId().asString());
        String content = message.getContent().get();

        Pattern r;
        Matcher m;
        r = Pattern.compile("^!(time|servertime) ?.*$");
        m = r.matcher(content);
        if (m.find()) {
            return sendServerTime(message.getChannel().block());
        }

        return null;
    }

    private Void sendServerTime(MessageChannel channel) {
        ZoneId zone = ZoneId.of("America/New_York");
        ZonedDateTime zdt = ZonedDateTime.now(zone);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("h:mma z")
                .withLocale(new Locale("en", "US"));
        String time = dtf.format(zdt);

        String timeMessage = String.format("It is currently %s.", time);
        channel.createMessage(timeMessage).block();

        return null;
    }
}
