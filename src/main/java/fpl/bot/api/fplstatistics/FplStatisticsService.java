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

    private static final int NUMBER_OF_PLAYERS_TO_FETCH = 50;

    private static final int INDEX_PLAYER_NAME = 1;
    private static final int INDEX_PRICE = 6;
    private static final int INDEX_PRICE_CHANGE_PERCENTAGE = 10;

    public List<Player> getPlayersAtRisk() {
        List<Player> playersAtRisk = new ArrayList<>();

        int iselRow = getIselRow();

        FplStatistics fplStatistics = getFplStatistics(0, NUMBER_OF_PLAYERS_TO_FETCH, iselRow);
        playersAtRisk.addAll(extractPlayers(fplStatistics));

        fplStatistics = getFplStatistics(fplStatistics.getTotalNumberOfPlayers() - NUMBER_OF_PLAYERS_TO_FETCH, fplStatistics.getTotalNumberOfPlayers(), iselRow);
        playersAtRisk.addAll(extractPlayers(fplStatistics));

        return playersAtRisk;
    }

    private int getIselRow() {
        WebClient webClient = WebClient.create("http://www.fplstatistics.co.uk/");
        ClientConfiguration config = WebClient.getConfig(webClient);
        config.getInInterceptors().add(new LoggingInInterceptor());
        config.getOutInterceptors().add(new LoggingOutInterceptor());

        String html = webClient.get(String.class);

        int iselRowIndex = html.indexOf("iselRow");
        String iselRowString = html.substring(iselRowIndex, iselRowIndex+30);
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

    private FplStatistics getFplStatistics(int offset, int length, int iselRow) {
        WebClient webClient = WebClient.create("http://www.fplstatistics.co.uk/Home/AjaxPricesCHandler", Collections.singletonList(new JacksonJaxbJsonProvider()));
        ClientConfiguration config = WebClient.getConfig(webClient);
        config.getInInterceptors().add(new LoggingInInterceptor());
        config.getOutInterceptors().add(new LoggingOutInterceptor());
        webClient.query("iselRow", iselRow);
        webClient.query("iDisplayStart", offset);
        webClient.query("iDisplayLength", length);
        return webClient.get(FplStatistics.class);
    }
}
