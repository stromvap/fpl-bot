package fpl.bot.api.discord;

import fpl.bot.api.fplstatistics.FplStatisticsService;
import fpl.bot.api.fplstatistics.Player;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class DiscordBotTest {

    @Mock
    private FplStatisticsService fplStatisticsServiceMock;

    @InjectMocks
    private DiscordBot unitUnderTest;

    @Test
    public void verify_That_A_Mentioned_Player_Is_Found() {
        Player son = new Player();
        son.setName("Son");

        Mockito.when(fplStatisticsServiceMock.getPlayersAtRisk()).thenReturn(Collections.singletonList(son));

        Optional<Player> mentionedPlayer = unitUnderTest.getMentionedPlayer("Is Son about to rise?");

        Assertions.assertThat(mentionedPlayer.isPresent()).isTrue();
    }

    @Test
    public void verify_That_A_Mentioned_Player_Is_Case_Insesitive() {
        Player son = new Player();
        son.setName("Son");

        Mockito.when(fplStatisticsServiceMock.getPlayersAtRisk()).thenReturn(Collections.singletonList(son));

        Optional<Player> mentionedPlayer = unitUnderTest.getMentionedPlayer("Is son about to rise?");

        Assertions.assertThat(mentionedPlayer.isPresent()).isTrue();
    }

    @Test
    public void verify_That_A_Mentioned_Player_Is_Not_Found() {
        Player someoneElse = new Player();
        someoneElse.setName("Else");

        Mockito.when(fplStatisticsServiceMock.getPlayersAtRisk()).thenReturn(Collections.singletonList(someoneElse));

        Optional<Player> mentionedPlayer = unitUnderTest.getMentionedPlayer("Is Son about to rise?");

        Assertions.assertThat(mentionedPlayer.isPresent()).isFalse();
    }

    @Test
    public void verify_That_A_Mentioned_Player_Is_Not_Found_Inside_A_Player_Name() {
        Player son = new Player();
        son.setName("son");

        Mockito.when(fplStatisticsServiceMock.getPlayersAtRisk()).thenReturn(Collections.singletonList(son));

        Optional<Player> mentionedPlayer = unitUnderTest.getMentionedPlayer("Is richarlison about to rise?");

        Assertions.assertThat(mentionedPlayer.isPresent()).isFalse();
    }
}