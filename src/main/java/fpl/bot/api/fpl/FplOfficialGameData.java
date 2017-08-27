package fpl.bot.api.fpl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FplOfficialGameData {
    @JsonProperty("elements")
    private List<FplOfficialPlayer> fplOfficialPlayers;

    @JsonProperty("teams")
    private List<FplOfficialTeam> fplOfficialTeams;

    public List<FplOfficialPlayer> getFplOfficialPlayers() {
        return fplOfficialPlayers;
    }

    public void setFplOfficialPlayers(List<FplOfficialPlayer> fplOfficialPlayers) {
        this.fplOfficialPlayers = fplOfficialPlayers;
    }

    public List<FplOfficialTeam> getFplOfficialTeams() {
        return fplOfficialTeams;
    }

    public void setFplOfficialTeams(List<FplOfficialTeam> fplOfficialTeams) {
        this.fplOfficialTeams = fplOfficialTeams;
    }
}
