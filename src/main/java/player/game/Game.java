package player.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import player.Player.AI;
import player.engine.GameEngine;
import player.engine.Winner;
import player.match.Match;
import player.match.Match.MatchResult;

/**
 * Plays multiple matches between to AIs. It is useful when IAs or State supplier are not deterministic,
 * otherwise, a single match is enough
 */
public class Game implements Callable<Game.GameResult> {

    private static final int DEFAULT_NUMBER_OF_MATCHES = 5;

    private final Function<IntSupplier, Supplier<AI>> player;
    private final Function<IntSupplier, Supplier<AI>> opponent;
    private final Supplier<GameEngine> gameEngine;
    private final int numberOfMatches;
    private final ExecutorService executorService;

    public Game(
            Function<IntSupplier, Supplier<AI>> player,
            Function<IntSupplier, Supplier<AI>> opponent,
            Supplier<GameEngine> gameEngine,
            ExecutorService executorService) {

        this(player, opponent, gameEngine, executorService, DEFAULT_NUMBER_OF_MATCHES);
    }

    public Game(
            Function<IntSupplier, Supplier<AI>> player,
            Function<IntSupplier, Supplier<AI>> opponent,
            Supplier<GameEngine> gameEngine,
            ExecutorService executorService,
            int numberOfMatches) {

        this.player = player;
        this.opponent = opponent;
        this.gameEngine = gameEngine;
        this.numberOfMatches = numberOfMatches;
        this.executorService = executorService;
    }

    @Override
    public GameResult call() throws InterruptedException, ExecutionException {
        List<Callable<MatchResult>> matches = new ArrayList<>();
        for (int i = 0; i < numberOfMatches; i++) {
            matches.add(new Match(player, opponent, gameEngine));
        }

        List<Future<MatchResult>> futures = executorService.invokeAll(matches);

        AI lastPlayer = null;
        AI lastOpponent = null;
        GameEngine lastGameEngine = null;

        GameResult gameResult = new GameResult();
        for (Future<MatchResult> future : futures) {
            MatchResult matchResult = future.get();
            AI player = matchResult.getPlayer();
            AI opponent = matchResult.getOpponent();
            GameEngine gameEngine = matchResult.getGameEngine();

            Preconditions.checkArgument(
                    lastPlayer == null || lastPlayer.equals(player),
                    "Illegal usage, players should always be the same, but found lastPlayer=%s, player=%s",
                    lastPlayer, player);

            Preconditions.checkArgument(
                    lastOpponent == null || lastOpponent.equals(opponent),
                    "Illegal usage, opponents should always be the same, but found lastOpponent=%s, opponent=%s",
                    lastOpponent, opponent);

            Preconditions.checkArgument(
                    lastGameEngine == null || lastGameEngine.equals(gameEngine),
                    "Illegal usage, game engines should always be the same, " +
                            "but found lastGameEngine=%s, gameEngine=%s",
                    lastGameEngine, gameEngine);

            lastPlayer = player;
            lastOpponent = opponent;
            lastGameEngine = gameEngine;

            gameResult.addMatchResult(matchResult);
        }

        return gameResult;
    }

    public static final class GameResult {
        private List<MatchResult> matchResults;

        private GameResult() {
            this.matchResults = new ArrayList<>();
        }

        private void addMatchResult(MatchResult result) {
            matchResults.add(result);
        }

        public double getAveragePlayerScore() {
            double totalPlayerScore =
                    matchResults.stream()
                            .mapToDouble(MatchResult::getPlayerScore)
                            .sum();

            return totalPlayerScore / matchResults.size();
        }

        public double getAverageOpponentScore() {
            double totalOpponentScore =
                    matchResults.stream()
                            .mapToDouble(MatchResult::getOpponentScore)
                            .sum();

            return totalOpponentScore / matchResults.size();
        }

        public double getAverageNumberOfRounds() {
            double totalNumberOfRounds =
                    matchResults.stream()
                            .mapToDouble(MatchResult::getRounds)
                            .sum();

            return totalNumberOfRounds / matchResults.size();
        }

        public double getPlayerWinRate() {
            double numberOfPlayerVictories =
                    matchResults.stream()
                            .map(MatchResult::getWinner)
                            .filter(Winner.PLAYER::equals)
                            .count();

            return numberOfPlayerVictories / matchResults.size();
        }

        public long getNumberOfMatches() {
            return matchResults.size();
        }

        public Winner getWinner() {
            long playerVictoriesCount = matchResults.stream()
                    .map(MatchResult::getWinner)
                    .filter(Winner.PLAYER::equals)
                    .count();

            long opponentVictoriesCount = matchResults.stream()
                    .map(MatchResult::getWinner)
                    .filter(Winner.OPPONENT::equals)
                    .count();

            return playerVictoriesCount > opponentVictoriesCount ? Winner.PLAYER : Winner.OPPONENT;
        }

        public List<MatchResult> getMatchResults() {
            return Collections.unmodifiableList(matchResults);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("averagePlayerScore", getAveragePlayerScore())
                    .add("averageOpponentScore", getAverageOpponentScore())
                    .add("averageNumberOfRounds", getAverageNumberOfRounds())
                    .add("playerWinRate", getPlayerWinRate())
                    .add("numberOfMatches", getNumberOfMatches())
                    .add("winner", getWinner())
                    .add("matchResults", matchResults)
                    .toString();
        }
    }
}
