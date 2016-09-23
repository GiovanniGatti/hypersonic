package player.match;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import player.MockedAI;
import player.Player;
import player.Player.Action;
import player.engine.GameEngine;
import player.engine.MockedGE;
import player.engine.MultipleRoundMockedGE;
import player.engine.Winner;
import player.match.Match.MatchResult;

@DisplayName("A match")
class MatchTest implements WithAssertions {

    @Test
    @DisplayName("starts up only once the provided game engine")
    void startUpGameEngine() {
        MockedGE.Builder start = MockedGE.newBuilder().withWinner(Winner.ON_GOING);
        MockedGE.Builder round1 = MockedGE.newBuilder().withWinner(Winner.PLAYER);

        MultipleRoundMockedGE gameEngine = new MultipleRoundMockedGE(start, round1);

        Supplier<GameEngine> gameEngineSupplier = () -> gameEngine;

        Match match = new Match(anyAIInput(), anyAIInput(), gameEngineSupplier);

        match.call();

        assertThat(gameEngine.getStartCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("is played until someone wins")
    void playUntilWinner() {
        MockedGE.Builder start = MockedGE.newBuilder().withWinner(Winner.ON_GOING);
        MockedGE.Builder round1 = MockedGE.newBuilder().withWinner(Winner.ON_GOING);
        MockedGE.Builder round2 = MockedGE.newBuilder().withWinner(Winner.ON_GOING);
        MockedGE.Builder round3 = MockedGE.newBuilder().withWinner(Winner.PLAYER);

        MultipleRoundMockedGE gameEngine = new MultipleRoundMockedGE(start, round1, round2, round3);

        Supplier<GameEngine> gameEngineSupplier = () -> gameEngine;

        Match match = new Match(anyAIInput(), anyAIInput(), gameEngineSupplier);

        match.call();

        assertThat(gameEngine.getRunCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("consists of providing AI input and them playing their output actions")
    void playAIActions() {

        Action playerAction = Mockito.mock(Action.class);
        Function<IntSupplier, Supplier<Player.AI>> playerAIInput =
                (input) -> () -> MockedAI.anyWithActions(playerAction);

        Action opponentAction = Mockito.mock(Action.class);
        Function<IntSupplier, Supplier<Player.AI>> opponentAIInput =
                (input) -> () -> MockedAI.anyWithActions(opponentAction);

        GameEngine gameEngine = Mockito.mock(GameEngine.class);
        when(gameEngine.getWinner()).thenReturn(Winner.PLAYER);
        Supplier<GameEngine> gameEngineBuild = () -> gameEngine;

        Match match = new Match(playerAIInput, opponentAIInput, gameEngineBuild);

        match.call();

        ArgumentCaptor<Action[]> playerActions = ArgumentCaptor.forClass(Action[].class);
        ArgumentCaptor<Action[]> opponentActions = ArgumentCaptor.forClass(Action[].class);
        verify(gameEngine).run(playerActions.capture(), opponentActions.capture());

        assertThat(playerAction).isEqualTo(playerActions.getValue()[0]);
        assertThat(opponentAction).isEqualTo(opponentActions.getValue()[0]);
    }

    @Nested
    @DisplayName("returns")
    class MatchResults {

        @Test
        @DisplayName("the right winner")
        void rightWinner() {
            Supplier<GameEngine> gameEngineBuild = () -> MockedGE.anyWithWinner(Winner.OPPONENT);

            Match match = new Match(anyAIInput(), anyAIInput(), gameEngineBuild);

            MatchResult matchResult = match.call();

            assertThat(matchResult.getWinner()).isEqualTo(Winner.OPPONENT);
        }

        @Test
        @DisplayName("the right player score")
        void playerScore() {
            Supplier<GameEngine> gameEngineBuild = () -> MockedGE.anyWithPlayerScore(17);

            Match match = new Match(anyAIInput(), anyAIInput(), gameEngineBuild);

            MatchResult matchResult = match.call();

            assertThat(matchResult.getPlayerScore()).isEqualTo(17);
        }

        @Test
        @DisplayName("the right opponent score")
        void opponentScore() {
            Supplier<GameEngine> gameEngineBuild = () -> MockedGE.anyWithOpponentScore(17);

            Match match = new Match(anyAIInput(), anyAIInput(), gameEngineBuild);

            MatchResult matchResult = match.call();

            assertThat(matchResult.getOpponentScore()).isEqualTo(17);
        }

        @Test
        @DisplayName("the right number of rounds")
        void numberOfRounds() {
            Supplier<GameEngine> gameEngineBuild = () -> MockedGE.anyWithNumberOfRounds(3);

            Match match = new Match(anyAIInput(), anyAIInput(), gameEngineBuild);

            MatchResult matchResult = match.call();

            assertThat(matchResult.getRounds()).isEqualTo(3);
        }
    }

    private static Function<IntSupplier, Supplier<Player.AI>> anyAIInput() {
        return (input) -> MockedAI::any;
    }
}