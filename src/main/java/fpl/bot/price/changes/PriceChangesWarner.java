package fpl.bot.price.changes;

import fpl.bot.api.fplstatistics.FplStatisticsService;
import fpl.bot.api.fplstatistics.Player;
import fpl.bot.api.discord.DiscordBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.rightPad;

@Component
public class PriceChangesWarner {
    private static final Logger log = LoggerFactory.getLogger(PriceChangesWarner.class);

    @Autowired
    private FplStatisticsService fplStatisticsService;

    @Autowired
    private DiscordBot discordBot;

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
                    append(" £ now: ").
                    append(playerLikelyToRise.getPrice()).
                    append("\n");
        }

        String message = "```" + risersMessage.toString() + "\n" + fallersMessage.toString() + "```";

        discordBot.sendMessageToPriceBotChannels(message);
    }
}
