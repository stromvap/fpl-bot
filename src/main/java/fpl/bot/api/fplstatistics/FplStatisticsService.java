package fpl.bot.api.fplstatistics;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class FplStatisticsService {
    private static final Logger log = Logger.getLogger(FplStatisticsService.class);

    static final double THRESHOLD = 99.0;

    private static final int INDEX_PLAYER_NAME = 1;
    private static final int INDEX_PRICE = 7;
    private static final int INDEX_PRICE_CHANGE_PERCENTAGE = 11;

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

        int iselRowIndex = html.indexOf("iselRow");
        String iselRowString = html.substring(iselRowIndex, iselRowIndex + 30);
        return Integer.parseInt(iselRowString.replaceAll("\\D", ""));
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

        return players;
    }

    private FplStatistics getFplStatistics(int iselRow) {
        WebClient webClient = WebClient.create("http://www.fplstatistics.co.uk/Home/AjaxPricesCHandler", Collections.singletonList(new JacksonJaxbJsonProvider()));
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
}
