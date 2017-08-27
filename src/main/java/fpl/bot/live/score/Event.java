package fpl.bot.live.score;

import java.time.LocalDateTime;

public class Event {
    private int id;
    private LocalDateTime timeOfEvent;
    private EventType type;
    private int minuteOfMatch;
    private String playerName;
    private String teamSlackIcon;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getTimeOfEvent() {
        return timeOfEvent;
    }

    public void setTimeOfEvent(LocalDateTime timeOfEvent) {
        this.timeOfEvent = timeOfEvent;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public int getMinuteOfMatch() {
        return minuteOfMatch;
    }

    public void setMinuteOfMatch(int minuteOfMatch) {
        this.minuteOfMatch = minuteOfMatch;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getTeamSlackIcon() {
        return teamSlackIcon;
    }

    public void setTeamSlackIcon(String teamSlackIcon) {
        this.teamSlackIcon = teamSlackIcon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (id != event.id) return false;
        if (type != event.type) return false;
        return playerName != null ? playerName.equals(event.playerName) : event.playerName == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (playerName != null ? playerName.hashCode() : 0);
        return result;
    }

    public String getSlackMessage() {
        if (type == EventType.GOAL) {
            return ":soccer: Goal by *" + playerName + "* " + teamSlackIcon + "\n";
        } else if (type == EventType.ASSIST) {
            return ":soccer: Assist by *" + playerName + "* " + teamSlackIcon + "\n";
        } else {
            return "Something went wrong";
        }
    }
}
