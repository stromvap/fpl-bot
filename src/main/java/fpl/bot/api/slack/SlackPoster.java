package fpl.bot.api.slack;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class SlackPoster {
    private static final Logger log = Logger.getLogger(SlackPoster.class);

    @Value("${postToSlack}")
    boolean postToSlack;

    @Value("${slackAuthToken}")
    String slackAuthToken;

    public void sendMessage(String message, String channel) {
        if(!postToSlack) {
            log.info("postToSlack was disabled, not posting to Slack");
            log.info("The following message would have been sent to #" + channel + ": " + message);
            return;
        }

        log.info("Sending the following message to #" + channel + ": " + message);

        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromHttpUrl("https://slack.com/api/chat.postMessage").
                queryParam("token", slackAuthToken).
                queryParam("channel", channel).
                queryParam("text", message);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getForEntity(urlBuilder.build().toUriString(), String.class);
    }
}
