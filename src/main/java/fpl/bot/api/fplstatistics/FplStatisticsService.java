package fpl.bot.api.fplstatistics;

import org.apache.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
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

        FplStatistics fplStatistics = getFplStatistics(0, NUMBER_OF_PLAYERS_TO_FETCH);
        playersAtRisk.addAll(extractPlayers(fplStatistics));

        fplStatistics = getFplStatistics(fplStatistics.getTotalNumberOfPlayers() - NUMBER_OF_PLAYERS_TO_FETCH, fplStatistics.getTotalNumberOfPlayers());
        playersAtRisk.addAll(extractPlayers(fplStatistics));

        return playersAtRisk;
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

    private FplStatistics getFplStatistics(int offset, int length) {
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromHttpUrl("http://www.fplstatistics.co.uk/Home/AjaxPricesHandler").
                // Not really sure what this number is but it changes sometimes
                queryParam("iselRow", 299).
                queryParam("iDisplayStart", offset).
                queryParam("iDisplayLength", length);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<FplStatistics> responseEntity = restTemplate.exchange(urlBuilder.toUriString(), HttpMethod.GET, null, FplStatistics.class);
        return responseEntity.getBody();
    }
}
