package fpl.bot.api.fplstatistics;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=FplStatisticsService.class)
public class FplStatisticsServiceTest {

    private static final Logger log = LoggerFactory.getLogger(FplStatisticsServiceTest.class);

    @Autowired
    private FplStatisticsService unitUnderTest;

    @Test
    public void verify_That_FplStatistics_Can_Be_Called() {
        List<Player> playersAtRisk = unitUnderTest.getPlayersAtRisk();

        log.info(playersAtRisk.toString());

        assertThat(playersAtRisk).isNotEmpty();
        assertThat(playersAtRisk).doesNotHaveDuplicates();

        Player aPlayer = playersAtRisk.get(0);
        assertThat(aPlayer.getName()).isNotBlank();
        assertThat(aPlayer.getPrice()).isBetween(3d, 15d);
        assertThat(aPlayer.getPriceChangePercentage()).isBetween(-110d, 110d);
    }
}