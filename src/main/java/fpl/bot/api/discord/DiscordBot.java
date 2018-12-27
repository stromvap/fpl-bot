package fpl.bot.api.discord;

import fpl.bot.api.fplstatistics.FplStatisticsService;
import fpl.bot.api.fplstatistics.Player;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Component
public class DiscordBot {
    private static final Logger log = LoggerFactory.getLogger(DiscordBot.class);

    private static List<String> friendlyReminderMessages = new ArrayList<>();

    static {
        friendlyReminderMessages.add("Psst %s, %s is likely to change in price tonight, he is at %s. You are welcome!");
        friendlyReminderMessages.add("I care about you %s, i just wanted to let you know that %s is at %s!");
        friendlyReminderMessages.add("%s, take good care of %s, he needs you! He is at %s");
        friendlyReminderMessages.add("You rang, %s? %s is at %s");
        friendlyReminderMessages.add("%s and %s (%s) sitting in a tree...?");
    }

    @Value("${discordToken}")
    private String discordToken;

    @Autowired
    private FplStatisticsService fplStatisticsService;

    private JDA jda;

    @PostConstruct
    public void startJda() throws Exception {
        log.info("Trying to connect to Discord WebSocket");

        if(discordToken.equalsIgnoreCase("changeit")) {
            return;
        }

        jda = new JDABuilder(discordToken).
                addEventListener(new FriendlyPriceWarnerReminder()).
                build();
        jda.awaitReady();
        log.info("Finished Building JDA!");
    }

    public void sendMessageToPriceBotChannels(String message) {
        jda.getGuilds().stream().
                flatMap(g -> g.getTextChannels().stream()).
                filter(tc -> tc.getName().contains("price")).
                filter(tc -> tc.getName().contains("bot")).
                filter(TextChannel::canTalk).
                forEach(c -> c.sendMessage(message).queue());
    }

    public class FriendlyPriceWarnerReminder extends ListenerAdapter {

        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            User author = event.getAuthor();

            Message message = event.getMessage();
            String msg = message.getContentDisplay();

            if (!author.isBot() && event.isFromType(ChannelType.TEXT)) {
                TextChannel textChannel = event.getTextChannel();
                Member member = event.getMember();

                String name;
                if (message.isWebhookMessage()) {
                    name = author.getName();
                } else {
                    name = member.getEffectiveName();
                }

                if (!textChannel.getName().contains("price")) {
                    return;
                }

                Optional<Player> mentionedPlayerAtRisk = fplStatisticsService.getPlayersAtRisk().stream().
                        filter(p -> StringUtils.containsIgnoreCase(msg, p.getName())).
                        findFirst();

                if (mentionedPlayerAtRisk.isPresent()) {
                    Player p = mentionedPlayerAtRisk.get();
                    String friendlyResponse = friendlyReminderMessages.get(new Random().nextInt(friendlyReminderMessages.size()));
                    textChannel.sendMessage(String.format(friendlyResponse, name, p.getName(), p.getPriceChangePercentage()+"")).queue();
                }
            }
        }
    }

    @PreDestroy
    public void destroy() {
        log.info("Shutting down Discord connection");
        jda.shutdownNow();
    }

}
