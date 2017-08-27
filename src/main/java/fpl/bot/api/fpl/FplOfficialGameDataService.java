package fpl.bot.api.fpl;

import org.apache.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class FplOfficialGameDataService {
    private static final Logger log = Logger.getLogger(FplOfficialGameDataService.class);

    private FplOfficialGameData fplOfficialGameData;

    @PostConstruct
    public void updateGameData() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<FplOfficialGameData> responseEntity = restTemplate.exchange("https://fantasy.premierleague.com/drf/bootstrap-static", HttpMethod.GET, null, FplOfficialGameData.class);
        fplOfficialGameData = responseEntity.getBody();
    }

    public List<FplOfficialPlayer> getPlayers() {
        updateGameData();
        return fplOfficialGameData.getFplOfficialPlayers();
    }

    public FplOfficialPlayer getPlayer(int id) {
        return fplOfficialGameData.getFplOfficialPlayers().stream().filter(p -> p.getId() == id).findFirst().get();
    }

    public FplOfficialTeam getTeam(int teamCode) {
        return fplOfficialGameData.getFplOfficialTeams().stream().filter(p -> p.getCode() == teamCode).findFirst().get();
    }

    public FplOfficialTeam getTeamById(int id) {
        return fplOfficialGameData.getFplOfficialTeams().stream().filter(p -> p.getId() == id).findFirst().get();
    }
}
