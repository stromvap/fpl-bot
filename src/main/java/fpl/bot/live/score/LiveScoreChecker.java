package fpl.bot.live.score;

import fpl.bot.api.discord.DiscordPoster;
import fpl.bot.api.slack.SlackPoster;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class LiveScoreChecker {
    private static final long SCHEDULE_RATE_IN_SECONDS = 30;

    private static final Logger log = Logger.getLogger(LiveScoreChecker.class);

    private LocalDateTime lastTimeChecked = LocalDateTime.MIN;

    @Value("${ignoreEventsBeforeApplicationStart}")
    boolean ignoreEventsBeforeApplicationStart;

    @Value("${liveScoreEnabled}")
    boolean liveScoreEnabled;

    @Value("${slackLiveScoreChannel}")
    String slackLiveScoreChannel;

    @Value("${discordLiveScoreWebhook}")
    String discordLiveScoreWebhook;

    @Autowired
    private FplOfficialLiveScoreFetcher fplOfficialLiveScoreFetcher;

    @Autowired
    private SlackPoster slackPoster;

    @Autowired
    private DiscordPoster discordPoster;

    @Scheduled(fixedRate = SCHEDULE_RATE_IN_SECONDS * 1000)
    public void checkLiveScores() {
        if (!liveScoreEnabled) {
            log.info("Live Score is not enabled, skipping check...");
            return;
        }

        List<Event> newEvents = fplOfficialLiveScoreFetcher.getNewEventsSince(lastTimeChecked);
        if (ignoreEventsBeforeApplicationStart && lastTimeChecked == LocalDateTime.MIN) {
            log.info("ignore.events.before.application.start is enabled, ignoring first events!");
            lastTimeChecked = LocalDateTime.now();
            return;
        }

        lastTimeChecked = LocalDateTime.now();

        if (newEvents.isEmpty()) {
            log.info("No new events, not posting anything to Slack");
            return;
        }

        StringBuilder message = new StringBuilder();

        for (Event event : newEvents) {
            message.append(event.getSlackMessage());
        }

        slackPoster.sendMessage(message.toString(), slackLiveScoreChannel);
        discordPoster.sendMessage(message.toString(), discordLiveScoreWebhook);
    }
}
