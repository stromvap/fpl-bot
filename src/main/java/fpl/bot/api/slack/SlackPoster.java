package fpl.bot.api.slack;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class SlackPoster {
    private static final Logger log = Logger.getLogger(SlackPoster.class);

    @Value("${postToSlack}")
    boolean postToSlack;

    @Value("${slackAuthToken}")
    String slackAuthToken;

    public void sendMessage(String message, String channel) {
        if (!postToSlack) {
            log.info("postToSlack was disabled, not posting to Slack");
            log.info("The following message would have been sent to #" + channel + ": " + message);
            return;
        }

        log.info("Sending the following message to #" + channel + ": " + message);

        WebClient webClient = WebClient.create("https://slack.com/api/chat.postMessage", Collections.singletonList(new JacksonJaxbJsonProvider()));
        webClient.type(MediaType.APPLICATION_JSON_VALUE);
        ClientConfiguration config = WebClient.getConfig(webClient);
        config.getInInterceptors().add(new LoggingInInterceptor());
        config.getOutInterceptors().add(new LoggingOutInterceptor());
        webClient.query("token", slackAuthToken);
        webClient.query("channel", channel);
        webClient.query("text", message);
        webClient.get();
    }
}
