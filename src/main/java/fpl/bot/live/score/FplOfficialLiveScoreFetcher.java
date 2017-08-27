package fpl.bot.live.score;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fpl.bot.api.fpl.FplOfficialGameDataService;
import fpl.bot.api.fpl.FplOfficialPlayer;
import fpl.bot.api.fpl.FplOfficialEvent;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FplOfficialLiveScoreFetcher {
    private static final Logger log = Logger.getLogger(FplOfficialLiveScoreFetcher.class);

    private static final TypeReference<List<FplOfficialEvent>> listOfEventsTypeReference = new TypeReference<List<FplOfficialEvent>>() {
    };

    private static final int INDEX_GOALS = 0;
    private static final int INDEX_ASSISTS = 1;
    private static final String FIELD_NAME_HOME = "h";
    private static final String FIELD_NAME_AWAY = "a";

    @Value("${gameweek}")
    int gameweek;

    @Autowired
    private FplOfficialGameDataService fplOfficialGameDataService;

    private Set<Event> liveMatchEvents = new HashSet<>();

    public List<Event> getNewEventsSince(LocalDateTime localDateTime) {
        fetchNewEvents();
        return liveMatchEvents.stream().filter(e -> e.getTimeOfEvent().isAfter(localDateTime)).collect(Collectors.toList());
    }

    private void fetchNewEvents() {
        updateEvents(getGameWeekJson());
    }

    private JsonNode getGameWeekJson() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange("https://fantasy.premierleague.com/drf/fixtures/?event=" + gameweek, HttpMethod.GET, null, String.class);

        try {
            return new ObjectMapper().readTree(responseEntity.getBody());
        } catch (IOException e) {
            log.error(e);
            throw new IllegalStateException(e);
        }
    }

    private void updateEvents(JsonNode eventNodes) {
        for (JsonNode eventNode : eventNodes) {
            if (eventNode.get("stats").size() == 0) {
                log.warn("Game information not complete! Ignoring game " + fplOfficialGameDataService.getTeamById(eventNode.get("team_h").asInt()).getName() + " vs " + fplOfficialGameDataService.getTeamById(eventNode.get("team_a").asInt()).getName());
                continue;
            }

            addEvents(eventNode.get("stats").get(INDEX_GOALS).get("goals_scored"), EventType.GOAL);
            addEvents(eventNode.get("stats").get(INDEX_ASSISTS).get("assists"), EventType.ASSIST);
        }
    }

    @SuppressWarnings("unchecked")
    private void addEvents(JsonNode jsonNode, EventType eventType) {
        try {
            ((List<FplOfficialEvent>) new ObjectMapper().readValue(jsonNode.get(FIELD_NAME_HOME).toString(), listOfEventsTypeReference)).forEach(e -> mapAndAdd(e, eventType));
            ((List<FplOfficialEvent>) new ObjectMapper().readValue(jsonNode.get(FIELD_NAME_AWAY).toString(), listOfEventsTypeReference)).forEach(e -> mapAndAdd(e, eventType));
        } catch (IOException e) {
            log.error(e);
            throw new IllegalStateException(e);
        }
    }

    private void mapAndAdd(FplOfficialEvent fplOfficialEvent, EventType eventType) {
        for (int i = 0; i < fplOfficialEvent.getNumberOfOccurences(); i++) {
            Event event = new Event();
            event.setId(i);
            event.setType(eventType);
            event.setPlayerName(getPlayerName(fplOfficialEvent.getPlayerId()));
            event.setTeamSlackIcon(getTeamSlackIcon(fplOfficialEvent.getPlayerId()));
            event.setTimeOfEvent(LocalDateTime.now());
            liveMatchEvents.add(event);
        }
    }

    private String getPlayerName(int playerId) {
        FplOfficialPlayer player = fplOfficialGameDataService.getPlayer(playerId);
        return player.getFirstName() + " " + player.getSecondName();
    }

    private String getTeamSlackIcon(int playerId) {
        return teamSlackIcons.getOrDefault(fplOfficialGameDataService.getPlayer(playerId).getTeamCode(), ":grey_question:");
    }

    private static final Map<Integer, String> teamSlackIcons = new HashMap<>();

    static {
        teamSlackIcons.put(3, ":arsenal:");
        teamSlackIcons.put(91, ":bournemouth:");
        teamSlackIcons.put(36, ":brighton:");
        teamSlackIcons.put(90, ":burnley:");
        teamSlackIcons.put(8, ":chelsea:");
        teamSlackIcons.put(31, ":crystal_palace:");
        teamSlackIcons.put(11, ":everton:");
        teamSlackIcons.put(38, ":huddersfield:");
        teamSlackIcons.put(13, ":leicestercity:");
        teamSlackIcons.put(14, ":liverpool:");
        teamSlackIcons.put(43, ":manchestercity:");
        teamSlackIcons.put(1, ":manchesterunited:");
        teamSlackIcons.put(4, ":newcastleunited:");
        teamSlackIcons.put(20, ":southampton:");
        teamSlackIcons.put(110, ":stoke_city:");
        teamSlackIcons.put(80, ":swansea_city:");
        teamSlackIcons.put(6, ":tottenham_hotspur:");
        teamSlackIcons.put(57, ":watford:");
        teamSlackIcons.put(35, ":west_bromwich:");
        teamSlackIcons.put(21, ":west_ham_united:");
    }
}
