package fpl.bot.api.fplstatistics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FplStatistics {

    @JsonProperty("iTotalRecords")
    private int totalNumberOfPlayers;

    @JsonProperty("aaData")
    private List<List<String>> players;

    public int getTotalNumberOfPlayers() {
        return totalNumberOfPlayers;
    }

    public void setTotalNumberOfPlayers(int totalNumberOfPlayers) {
        this.totalNumberOfPlayers = totalNumberOfPlayers;
    }

    public List<List<String>> getPlayers() {
        return players;
    }

    public void setPlayers(List<List<String>> players) {
        this.players = players;
    }
}
