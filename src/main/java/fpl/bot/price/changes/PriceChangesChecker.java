package fpl.bot.price.changes;

import fpl.bot.api.discord.DiscordPoster;
import fpl.bot.api.fpl.FplOfficialGameDataService;
import fpl.bot.api.fpl.FplOfficialPlayer;
import fpl.bot.api.slack.SlackPoster;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.rightPad;

@Component
public class PriceChangesChecker {
    private static final long SCHEDULE_RATE_IN_SECONDS = 3600;

    private static final Logger log = Logger.getLogger(PriceChangesChecker.class);

    @Value("${slackPriceChangeChannel}")
    String slackPriceChangeChannel;

    @Value("${discordPriceChangeWebhook}")
    String discordPriceChangeWebhook;

    @Autowired
    private FplOfficialGameDataService fplOfficialGameDataService;

    @Autowired
    private SlackPoster slackPoster;

    @Autowired
    private DiscordPoster discordPoster;

    private List<FplOfficialPlayer> players;

    @Scheduled(fixedRate = SCHEDULE_RATE_IN_SECONDS * 1000)
    public void checkPriceChanges() {
        List<FplOfficialPlayer> updatedPlayers = fplOfficialGameDataService.getPlayers();

        if (players == null) {
            log.info("First time fetching player information, can't compare price changes...");
            players = updatedPlayers;
            return;
        }

        List<FplOfficialPlayer> playersThatChangedPrice = updatedPlayers.stream().filter(this::hasPlayerChangedPrice).collect(Collectors.toList());

        if (playersThatChangedPrice.isEmpty()) {
            log.info("No new price changes, not posting anything to Slack");
            return;
        }

        StringBuilder risersMessage = new StringBuilder();
        risersMessage.append("RISERS:\n");

        StringBuilder fallersMessage = new StringBuilder();
        fallersMessage.append("FALLERS:\n");

        for (FplOfficialPlayer player : playersThatChangedPrice) {
            Optional<FplOfficialPlayer> oldPlayerInformation = findPlayer(player.getId());

            if (oldPlayerInformation.isPresent()) {
                boolean rised = player.getCost() > oldPlayerInformation.get().getCost();

                (rised ? risersMessage : fallersMessage).
                        append(rightPad(player.getFirstName() + " " + player.getSecondName(), 20, " ")).
                        append("New price: ").
                        append(new DecimalFormat("#.0").format((double) player.getCost() / 10d)).
                        append("\n");
            } else {
                log.error("This should be impossible");
            }
        }

        players = updatedPlayers;

        String message = "```" + risersMessage.toString() + "\n" + fallersMessage.toString() + "```";

        slackPoster.sendMessage(message, slackPriceChangeChannel);
        discordPoster.sendMessage(message, discordPriceChangeWebhook);
    }

    private boolean hasPlayerChangedPrice(FplOfficialPlayer p) {
        Optional<FplOfficialPlayer> player = findPlayer(p.getId());
        return player.isPresent() && p.getCost() != player.get().getCost();
    }

    private Optional<FplOfficialPlayer> findPlayer(int id) {
        return players.stream().filter(p -> p.getId() == id).findFirst();
    }
}
