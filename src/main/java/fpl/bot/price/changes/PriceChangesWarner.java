package fpl.bot.price.changes;

import fpl.bot.api.discord.DiscordPoster;
import fpl.bot.api.fplstatistics.FplStatisticsService;
import fpl.bot.api.fplstatistics.Player;
import fpl.bot.api.slack.SlackPoster;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.rightPad;

@Component
public class PriceChangesWarner {
    private static final Logger log = Logger.getLogger(PriceChangesWarner.class);

    @Value("${slackPriceChangeChannel}")
    String slackPriceChangeChannel;

    @Value("${discordPriceChangeWebhook}")
    String discordPriceChangeWebhook;

    @Autowired
    private FplStatisticsService fplStatisticsService;

    @Autowired
    private SlackPoster slackPoster;

    @Autowired
    private DiscordPoster discordPoster;

    @Scheduled(cron = "0 0 21 * * ?")
    public void checkPotentialPriceChanges() {
        StringBuilder risersMessage = new StringBuilder();
        risersMessage.append("LIKELY RISERS:\n");

        StringBuilder fallersMessage = new StringBuilder();
        fallersMessage.append("LIKELY FALLERS:\n");

        for (Player playerLikelyToRise : fplStatisticsService.getPlayersAtRisk()) {
            (playerLikelyToRise.isAboutToRise() ? risersMessage : fallersMessage).
                    append(rightPad(playerLikelyToRise.getName(), 20, " ")).
                    append("at ").
                    append(rightPad(playerLikelyToRise.getPriceChangePercentage() + "%.", 8, " ")).
                    append(" Current price: ").
                    append(playerLikelyToRise.getPrice()).
                    append("\n");
        }

        String message = "```" + risersMessage.toString() + "\n" + fallersMessage.toString() + "```";

        slackPoster.sendMessage(message, slackPriceChangeChannel);
        discordPoster.sendMessage(message, discordPriceChangeWebhook);
    }
}
