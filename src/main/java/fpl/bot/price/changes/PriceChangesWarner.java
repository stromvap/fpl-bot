package fpl.bot.price.changes;

import fpl.bot.api.fplstatistics.FplStatisticsService;
import fpl.bot.api.fplstatistics.Player;
import fpl.bot.api.slack.SlackPoster;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PriceChangesWarner {
    private static final Logger log = Logger.getLogger(PriceChangesWarner.class);

    @Value("${priceChangeChannel}")
    String priceChangeChannel;

    @Autowired
    private FplStatisticsService fplStatisticsService;

    @Autowired
    private SlackPoster slackPoster;

    @Scheduled(cron = "0 0 21 * * ?")
    public void checkPotentialPriceChanges() {
        StringBuilder slackMessage = new StringBuilder();

        for (Player playerLikelyToRise : fplStatisticsService.getPlayersAtRisk()) {
            slackMessage.
                    append(playerLikelyToRise.isAboutToRise() ? ":green_heart:" : ":red_circle:").
                    append(":grey_question: *").
                    append(playerLikelyToRise.getName()).
                    append("* is likely to *").
                    append(playerLikelyToRise.isAboutToRise() ? "rise" : "fall").
                    append("* tomorrow! He is at *").
                    append(playerLikelyToRise.getPriceChangePercentage()).
                    append("%* now. Current price: ").
                    append(playerLikelyToRise.getPrice()).
                    append("\n");
        }

        slackPoster.sendMessage(slackMessage.toString(), priceChangeChannel);
    }
}
