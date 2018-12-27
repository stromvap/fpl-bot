package fpl.bot.live.score;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class LiveScoreChecker {
    private static final long SCHEDULE_RATE_IN_SECONDS = 30;

    private static final Logger log = LoggerFactory.getLogger(LiveScoreChecker.class);

    private LocalDateTime lastTimeChecked = LocalDateTime.MIN;

    @Value("${ignoreEventsBeforeApplicationStart}")
    boolean ignoreEventsBeforeApplicationStart;

    @Value("${liveScoreEnabled}")
    boolean liveScoreEnabled;

    @Autowired
    private FplOfficialLiveScoreFetcher fplOfficialLiveScoreFetcher;

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

        // TODO: Implement sending here, perhaps via DiscordBot
    }
}
