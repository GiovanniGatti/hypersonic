package player.game;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import player.MockedAI;
import player.Player.AI;
import player.Player.Action;
import player.engine.GameEngine;
import player.engine.MockedGE;
import player.engine.Winner;
import player.game.Game.GameResult;

@DisplayName("A game")
class GameTest implements WithAssertions {

    private ExecutorService service;

    @BeforeEach
    void init() {
        service = Executors.newFixedThreadPool(3);
    }

    @Test
    @DisplayName("is a run of multiple matches")
    void playMatches() throws Exception {

        Game game = new Game(anyAIInput(), anyAIInput(), MockedGE::any, service, 4);

        GameResult result = game.call();

        assertThat(result.getMatchResults()).hasSize(4);
    }

    @Test
    @DisplayName("winner is the player that won the most number of matches")
    void playerWinRate() throws Exception {
        GameEngine match1 = MockedGE.anyWithWinner(Winner.PLAYER);
        GameEngine match2 = MockedGE.anyWithWinner(Winner.PLAYER);
        GameEngine match3 = MockedGE.anyWithWinner(Winner.OPPONENT);
        GameEngine match4 = MockedGE.anyWithWinner(Winner.OPPONENT);
        GameEngine match5 = MockedGE.anyWithWinner(Winner.OPPONENT);

        List<GameEngine> matches = Arrays.asList(match1, match2, match3, match4, match5);
        Iterator<GameEngine> it = matches.iterator();

        Game game = new Game(anyAIInput(), anyAIInput(), it::next, service, matches.size());

        GameResult result = game.call();

        assertThat(result.getWinner()).isEqualTo(Winner.OPPONENT);
    }

    @Test
    @DisplayName("requires that all supplied player AIs are the same")
    void throwIllegalArgumentExceptionIfOneOfSuppliedPlayerAIsIsDifferentFromTheOthers() {
        AI playerRound1 = MockedAI.any();
        AI playerRound2 = new AnotherAI();

        List<AI> player = Arrays.asList(playerRound1, playerRound2);
        Iterator<AI> it = player.iterator();

        Game game = new Game((inputStream) -> it::next, anyAIInput(), MockedGE::any, service, player.size());

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(game::call)
                .withMessageContaining("Illegal usage, players should always be the same");
    }

    @Test
    @DisplayName("requires that all supplied opponent AIs are the same")
    void throwIllegalArgumentExceptionIfOneOfSuppliedOpponentAIsIsDifferentFromTheOthers() {
        AI opponentRound1 = MockedAI.any();
        AI opponentRound2 = new AnotherAI();

        List<AI> opponent = Arrays.asList(opponentRound1, opponentRound2);
        Iterator<AI> it = opponent.iterator();

        Game game = new Game(anyAIInput(), (inputStream) -> it::next, MockedGE::any, service, opponent.size());

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(game::call)
                .withMessageContaining("Illegal usage, opponents should always be the same");
    }

    @Test
    @DisplayName("requires that all supplied game engines are the same")
    void throwIllegalArgumentExceptionIfOneOfSuppliedGameEnginesIsDifferentFromTheOthers() {
        GameEngine match1 = MockedGE.anyWithPlayerScore(15);
        GameEngine match2 = new AnotherGE();

        List<GameEngine> matches = Arrays.asList(match1, match2);
        Iterator<GameEngine> it = matches.iterator();

        Game game = new Game(anyAIInput(), anyAIInput(), it::next, service, matches.size());

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(game::call)
                .withMessageContaining("Illegal usage, game engines should always be the same");
    }

    @Nested
    @DisplayName("that finished, returns a result with")
    class Statisticts {

        @Test
        @DisplayName("the right average player score")
        void averagePlayerScore() throws Exception {
            GameEngine match1 = MockedGE.anyWithPlayerScore(15);
            GameEngine match2 = MockedGE.anyWithPlayerScore(16);
            GameEngine match3 = MockedGE.anyWithPlayerScore(17);

            List<GameEngine> matches = Arrays.asList(match1, match2, match3);
            Iterator<GameEngine> it = matches.iterator();

            Game game = new Game(anyAIInput(), anyAIInput(), it::next, service, matches.size());

            GameResult result = game.call();

            assertThat(result.getAveragePlayerScore()).isEqualTo(16);
        }

        @Test
        @DisplayName("the right average opponent score")
        void averageOpponentScore() throws Exception {
            GameEngine match1 = MockedGE.anyWithOpponentScore(15);
            GameEngine match2 = MockedGE.anyWithOpponentScore(16);
            GameEngine match3 = MockedGE.anyWithOpponentScore(17);

            List<GameEngine> matches = Arrays.asList(match1, match2, match3);
            Iterator<GameEngine> it = matches.iterator();

            Game game = new Game(anyAIInput(), anyAIInput(), it::next, service, matches.size());

            GameResult result = game.call();

            assertThat(result.getAverageOpponentScore()).isEqualTo(16);
        }

        @Test
        @DisplayName("the right average number of rounds")
        void averageNumberOfRounds() throws Exception {
            GameEngine match1 = MockedGE.anyWithNumberOfRounds(15);
            GameEngine match2 = MockedGE.anyWithNumberOfRounds(16);
            GameEngine match3 = MockedGE.anyWithNumberOfRounds(17);

            List<GameEngine> matches = Arrays.asList(match1, match2, match3);
            Iterator<GameEngine> it = matches.iterator();

            Game game = new Game(anyAIInput(), anyAIInput(), it::next, service, matches.size());

            GameResult result = game.call();

            assertThat(result.getAverageNumberOfRounds()).isEqualTo(16);
        }

        @Test
        @DisplayName("the right player win rate")
        void playerWinRate() throws Exception {
            GameEngine match1 = MockedGE.anyWithWinner(Winner.PLAYER);
            GameEngine match2 = MockedGE.anyWithWinner(Winner.PLAYER);
            GameEngine match3 = MockedGE.anyWithWinner(Winner.OPPONENT);

            List<GameEngine> matches = Arrays.asList(match1, match2, match3);
            Iterator<GameEngine> it = matches.iterator();

            Game game = new Game(anyAIInput(), anyAIInput(), it::next, service, matches.size());

            GameResult result = game.call();

            assertThat(result.getPlayerWinRate()).isEqualTo(2.0 / 3.0);
        }

        @Test
        @DisplayName("the right number of matches")
        void numberOfMatches() throws Exception {
            Game game = new Game(anyAIInput(), anyAIInput(), MockedGE::any, service, 5);

            GameResult result = game.call();

            assertThat(result.getNumberOfMatches()).isEqualTo(5L);
        }

        @Test
        @DisplayName("a readable output")
        void readableOutput() throws Exception {
            MockedGE.Builder match = MockedGE.newBuilder()
                    .withOpponentScore(3)
                    .withPlayerScore(5)
                    .withNumberOfRounds(7)
                    .withWinner(Winner.PLAYER);

            Game game = new Game(anyAIInput(), anyAIInput(), match::build, service, 1);

            GameResult result = game.call();

            assertThat(result.toString()).contains(
                    "GameResult{averagePlayerScore=5.0, " +
                            "averageOpponentScore=3.0, " +
                            "averageNumberOfRounds=7.0, " +
                            "playerWinRate=1.0, " +
                            "numberOfMatches=1, " +
                            "winner=PLAYER, " +
                            "matchResults=[");

        }
    }

    private static Function<IntSupplier, Supplier<AI>> anyAIInput() {
        return (input) -> MockedAI::any;
    }

    private static class AnotherAI extends AI {

        public AnotherAI() {
            super(AnotherAI::noOp);
        }

        @Override
        public Action[] play() {
            return new Action[0];
        }

        private static void noOp() {
            // ILB
        }
    }

    private static class AnotherGE implements GameEngine {

        @Override
        public void start() {

        }

        @Override
        public void run(Action[] playerActions, Action[] opponentActions) {

        }

        @Override
        public Winner getWinner() {
            return null;
        }

        @Override
        public int playerInput() {
            return 0;
        }

        @Override
        public int opponentInput() {
            return 0;
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
        public int getNumberOfRounds() {
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            return this == o || !(o == null || getClass() != o.getClass());

        }

        @Override
        public int hashCode() {
            return Objects.hash(getClass());
        }
    }
}