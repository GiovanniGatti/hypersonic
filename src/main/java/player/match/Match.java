package player.match;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import player.Player.AI;
import player.Player.Action;
import player.engine.GameEngine;
import player.engine.Winner;

/**
 *
 * Represents a single match between any two IAs
 *
 */
public final class Match implements Callable<Match.MatchResult> {

    private final AI player;
    private final AI opponent;
    private final GameEngine gameEngine;

    public Match(
            Function<IntSupplier, Supplier<AI>> player,
            Function<IntSupplier, Supplier<AI>> opponent,
            Supplier<GameEngine> gameEngine) {

        this.gameEngine = gameEngine.get();
        this.player = player.apply(this.gameEngine::playerInput).get();
        this.opponent = opponent.apply(this.gameEngine::opponentInput).get();
    }

    @Override
    public MatchResult call() {
        gameEngine.start();

        do {
            Action[] playerActions = player.play();
            Action[] opponentActions = opponent.play();

            gameEngine.run(playerActions, opponentActions);

        } while (gameEngine.getWinner() == Winner.ON_GOING);

        return new MatchResult(
                player,
                opponent,
                gameEngine,
                gameEngine.getPlayerScore(),
                gameEngine.getOpponentScore(),
                gameEngine.getNumberOfRounds(),
                gameEngine.getWinner());
    }

    public static final class MatchResult {

        private final AI player;
        private final AI opponent;
        private final GameEngine gameEngine;
        private final int playerScore;
        private final int opponentScore;
        private final int rounds;
        private final Winner winner;

        private MatchResult(
                AI player,
                AI opponent,
                GameEngine gameEngine,
                int playerScore,
                int opponentScore,
                int rounds,
                Winner winner) {

            this.player = player;
            this.opponent = opponent;
            this.gameEngine = gameEngine;

            this.playerScore = playerScore;
            this.opponentScore = opponentScore;
            this.rounds = rounds;
            this.winner = winner;
        }

        public AI getPlayer() {
            return player;
        }

        public AI getOpponent() {
            return opponent;
        }

        public GameEngine getGameEngine() {
            return gameEngine;
        }

        public int getPlayerScore() {
            return playerScore;
        }

        public int getOpponentScore() {
            return opponentScore;
        }

        public int getRounds() {
            return rounds;
        }

        public Winner getWinner() {
            return winner;
        }

        @Override
        public String toString() {
            return com.google.common.base.MoreObjects.toStringHelper(this)
                    .add("player", player)
                    .add("opponent", opponent)
                    .add("gameEngine", gameEngine)
                    .add("playerScore", playerScore)
                    .add("opponentScore", opponentScore)
                    .add("rounds", rounds)
                    .add("winner", winner)
                    .toString();
        }
    }
}
