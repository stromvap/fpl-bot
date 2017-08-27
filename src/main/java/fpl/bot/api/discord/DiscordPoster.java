package fpl.bot.api.discord;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;

@Component
public class DiscordPoster {
    private static final Logger log = Logger.getLogger(DiscordPoster.class);

    @Value("${postToDiscord}")
    boolean postToDiscord;

    public void sendMessage(String message, String webhook) {
        if (!postToDiscord) {
            log.info("postToDiscord was disabled, not posting to Discord");
            log.info("The following message would have been sent to webhook " + webhook + ": " + message);
            return;
        }

        log.info("Sending the following message to webhook " + webhook + ": " + message);

        DiscordMessage discordMessage = new DiscordMessage();
        discordMessage.content = message;

        // For some reason i could not get RestTemplate to work. WebClient works so i'll just use that for now.
        WebClient webClient = WebClient.create("https://discordapp.com/api/webhooks/", Collections.singletonList(new JacksonJaxbJsonProvider()));
        webClient.type(MediaType.APPLICATION_JSON_VALUE);
        ClientConfiguration config = WebClient.getConfig(webClient);
        config.getInInterceptors().add(new LoggingInInterceptor());
        config.getOutInterceptors().add(new LoggingOutInterceptor());
        webClient.path(webhook).post(discordMessage);
    }

    @XmlRootElement
    private static class DiscordMessage {
        public String content;
    }
}
