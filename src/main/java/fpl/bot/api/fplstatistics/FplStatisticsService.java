package fpl.bot.api.fplstatistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FplStatisticsService {
    private static final Logger log = Logger.getLogger(FplStatisticsService.class);

    static final double THRESHOLD = 99.0;

    private static final int INDEX_PLAYER_NAME = 1;
    private static final int INDEX_PRICE = 7;
    private static final int INDEX_PRICE_CHANGE_PERCENTAGE = 11;

    private static final Pattern jsonPattern = Pattern.compile("\\{.*}");

    public List<Player> getPlayersAtRisk() {
        int iselRow = getIselRow();

        FplStatistics fplStatistics = getFplStatistics(iselRow);
        return extractPlayers(fplStatistics);
    }

    private int getIselRow() {
        WebClient webClient = WebClient.create("http://www.fplstatistics.co.uk/");
        ClientConfiguration config = WebClient.getConfig(webClient);
        config.getInInterceptors().add(new LoggingInInterceptor());
        config.getOutInterceptors().add(new LoggingOutInterceptor());

        String html = webClient.get(String.class);
        return parseIselRow(html);
    }

    int parseIselRow(String html) {
        String iselRowHtml = new BufferedReader(new StringReader(html)).
                lines().
                filter(s -> s.contains("iselRow")).
                findFirst().
                orElseThrow(() -> new IllegalStateException("Failed to find iselRow"));

        Matcher iselRowMatcher = jsonPattern.matcher(iselRowHtml);
        if (iselRowMatcher.find()) {
            String iselRowJson = iselRowMatcher.group();
            try {
                return Integer.parseInt(new ObjectMapper().readValue(iselRowJson, IselRow.class).value);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to parse iselRow from json: " + iselRowJson);
            }
        }

        throw new IllegalStateException("Failed to find iselRow");
    }

    private List<Player> extractPlayers(FplStatistics fplStatistics) {
        List<Player> players = new ArrayList<>();

        for (List<String> playerInformation : fplStatistics.getPlayers()) {
            Player player = new Player();
            player.setName(playerInformation.get(INDEX_PLAYER_NAME));

            String priceString = playerInformation.get(INDEX_PRICE);
            player.setPrice(Double.parseDouble(priceString.substring(1, priceString.length() - 1)));
            player.setPriceChangePercentage(Double.parseDouble(playerInformation.get(INDEX_PRICE_CHANGE_PERCENTAGE)));

            if (player.getPriceChangePercentage() >= THRESHOLD || player.getPriceChangePercentage() <= -THRESHOLD) {
                players.add(player);
            }
        }

        players.sort(Comparator.comparingDouble(Player::getPriceChangePercentage).reversed());

        return players;
    }

    private FplStatistics getFplStatistics(int iselRow) {
        WebClient webClient = WebClient.create("http://www.fplstatistics.co.uk/Home/AjaxPricesDHandler", Collections.singletonList(new JacksonJaxbJsonProvider()));
        ClientConfiguration config = WebClient.getConfig(webClient);
        config.getInInterceptors().add(new LoggingInInterceptor());
        config.getOutInterceptors().add(new LoggingOutInterceptor());
        webClient.query("iselRow", iselRow);

        webClient.header("Host", "www.fplstatistics.co.uk");
        webClient.header("Connection", "keep-alive");
        webClient.header("Accept", "application/json, text/javascript, */*; q=0.01");
        webClient.header("X-Requested-With", "XMLHttpRequest");
        webClient.header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36");
        webClient.header("Referer", "http://www.fplstatistics.co.uk/");
        webClient.header("Accept-Encoding", "gzip, deflate");
        webClient.header("Accept-Language", "en-US,en;q=0.9,sv;q=0.8");

        return webClient.get(FplStatistics.class);
    }

    private static class IselRow {
        private String name;
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
