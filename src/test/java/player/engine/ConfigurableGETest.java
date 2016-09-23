package player.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;

import player.Player;

@DisplayName("A configure game engine")
class ConfigurableGETest implements WithAssertions {

    @Test
    @DisplayName("starts an on going match")
    void startsWithMatchOnGoing() {
        ConfigurableGE ge = new ConfigurableGEImpl(Winner.PLAYER);
        assertThat(ge.getWinner()).isEqualTo(Winner.ON_GOING);
    }

    @Test
    @DisplayName("returns the winner after first round")
    void returnsExpectedWinner() {
        ConfigurableGE ge = new ConfigurableGEImpl(Winner.PLAYER);

        ge.run(null, null);

        assertThat(ge.getWinner()).isEqualTo(Winner.PLAYER);
        assertThat(ge.getNumberOfRounds()).isEqualTo(1);
    }

    @Test
    @DisplayName("has an immutable configuration")
    void immutableConfiguration() {
        Map<String, Object> conf = new HashMap<>();
        conf.put("key", "value");

        ConfigurableGE ge = new ConfigurableGEImpl(conf, Winner.PLAYER);

        conf.put("key", "anotherValue");
        assertThat(ge.getConf()).isNotEqualTo(conf);

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> ge.getConf().put("key", "anotherValue"));
    }

    @Nested
    @DisplayName("when loading player AI input")
    class PlayerInput {

        @Test
        @DisplayName("send the expected values")
        void playerAIInput() {
            ConfigurableGE ge = new ConfigurableGEImpl(Winner.PLAYER);
            ge.toPlayerInput(0, 2, 4);

            assertThat(ge.playerInput()).isEqualTo(0);
            assertThat(ge.playerInput()).isEqualTo(2);
            assertThat(ge.playerInput()).isEqualTo(4);
        }

        @Test
        @DisplayName("throws ISE when no input is available")
        void throwICEWhenNoInputIsAvailable() {
            ConfigurableGE ge = new ConfigurableGEImpl(Winner.PLAYER);

            assertThatExceptionOfType(IllegalStateException.class)
                    .isThrownBy(ge::playerInput)
                    .withMessageContaining("No inputs are available to player");
        }
    }

    @Nested
    @DisplayName("when loading opponent AI input")
    class OpponentInput {

        @Test
        @DisplayName("send the expected values")
        void opponentAIInput() {
            ConfigurableGE ge = new ConfigurableGEImpl(Winner.PLAYER);
            ge.toOpponentInput(0, 2, 4);

            assertThat(ge.opponentInput()).isEqualTo(0);
            assertThat(ge.opponentInput()).isEqualTo(2);
            assertThat(ge.opponentInput()).isEqualTo(4);
        }

        @Test
        @DisplayName("throws ISE when no input is available")
        void throwICEWhenNoInputIsAvailable() {
            ConfigurableGE ge = new ConfigurableGEImpl(Winner.PLAYER);

            assertThatExceptionOfType(IllegalStateException.class)
                    .isThrownBy(ge::opponentInput)
                    .withMessageContaining("No inputs are available to opponent");
        }
    }

    @Test
    @DisplayName("is not equals to another if both have the same configuration")
    void isEqualTo() {
        ConfigurableGE ge1 = new ConfigurableGEImpl(ImmutableMap.of("key1", "value1"), Winner.PLAYER);
        ConfigurableGE ge2 = new ConfigurableGEImpl(ImmutableMap.of("key1", "value1"), Winner.PLAYER);

        assertThat(ge1).isEqualTo(ge2);
    }

    @Test
    @DisplayName("is equals to another if both have different configurations")
    void isNotEqualTo() {
        ConfigurableGE ge1 = new ConfigurableGEImpl(ImmutableMap.of("key1", "value1"), Winner.PLAYER);
        ConfigurableGE ge2 = new ConfigurableGEImpl(ImmutableMap.of("key1", "value2"), Winner.PLAYER);

        assertThat(ge1).isNotEqualTo(ge2);
    }

    private static class ConfigurableGEImpl extends ConfigurableGE {

        private final Winner winner;

        ConfigurableGEImpl(Map<String, Object> conf, Winner winner) {
            super(conf);
            this.winner = winner;
        }

        ConfigurableGEImpl(Winner winner) {
            super(Collections.emptyMap());
            this.winner = winner;
        }

        @Override
        public void start() {
            // ILB
        }

        @Override
        public int getPlayerScore() {
            return 0;
        }

        @Override
        public int getOpponentScore() {
            return 0;
        }

        @Override
        protected Winner runRound(Player.Action[] playerActions, Player.Action[] opponentActions) {
            return winner;
        }
    }
}