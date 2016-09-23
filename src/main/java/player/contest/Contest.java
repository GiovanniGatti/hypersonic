package player.contest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import player.game.Game;
import player.game.Game.GameResult;
import player.match.Match;

/**
 * Play any number of AIs against each other and then check its performances
 */
public final class Contest implements Callable<Contest.ContestResult> {

    private static final int DEFAULT_NUMBER_OF_MATCHES = 5;

    private final List<Function<IntSupplier, Supplier<AI>>> ais;
    private final List<Supplier<GameEngine>> gameEngines;
    private final ExecutorService gameExecutorService;
    private final ExecutorService matchExecutorService;
    private final int numberOfMatches;

    public Contest(
            List<Function<IntSupplier, Supplier<AI>>> ais,
            List<Supplier<GameEngine>> gameEngines,
            ExecutorService gameExecutorService,
            ExecutorService matchExecutorService) {

        this(ais, gameEngines, gameExecutorService, matchExecutorService, DEFAULT_NUMBER_OF_MATCHES);
    }

    public Contest(
            List<Function<IntSupplier, Supplier<AI>>> ais,
            List<Supplier<GameEngine>> gameEngines,
            ExecutorService gameExecutorService,
            ExecutorService matchExecutorService,
            int numberOfMatches) {

        this.ais = ais;
        this.gameEngines = gameEngines;
        this.gameExecutorService = gameExecutorService;
        this.matchExecutorService = matchExecutorService;
        this.numberOfMatches = numberOfMatches;
    }

    @Override
    public ContestResult call() throws InterruptedException, ExecutionException {

        if (ais.size() < 2) {
            throw new IllegalStateException("Unable to play a contest with a single provided AI");
        }

        List<Callable<GameResult>> games = new ArrayList<>();
        for (Supplier<GameEngine> gameEngine : gameEngines) {

            for (int i = 0; i < ais.size() - 1; i++) {
                Function<IntSupplier, Supplier<AI>> player = ais.get(i);
                for (int j = i + 1; j < ais.size(); j++) {
                    Function<IntSupplier, Supplier<AI>> opponent = ais.get(j);
                    games.add(
                            new Game(
                                    player,
                                    opponent,
                                    gameEngine,
                                    matchExecutorService,
                                    numberOfMatches));
                }
            }
        }

        List<Future<GameResult>> futures = gameExecutorService.invokeAll(games);

        Score[] scores = new Score[ais.size()];

        int offset = 0;

        for (int g = 0; g < gameEngines.size(); g++) {

            for (int i = 0; i < ais.size() - 1; i++) {
                int k = 0;

                for (int j = i + 1; j < ais.size(); j++) {

                    Future<GameResult> future = futures.get(offset + k);
                    GameResult result = future.get();

                    AI player = result
                            .getMatchResults()
                            .stream()
                            .map(Match.MatchResult::getPlayer)
                            .findAny()
                            .orElseThrow(
                                    () -> new IllegalStateException("Expected at least one player, but none found"));

                    if (scores[i] == null) {
                        scores[i] = new Score(player);
                    }

                    Score playerScore = scores[i];

                    playerScore.updateAverageNumberOfRoundsMean(result.getAverageNumberOfRounds());
                    playerScore.updateAverageScoreMean(result.getAveragePlayerScore());
                    playerScore.updateAverageWinRateMean(result.getPlayerWinRate());

                    AI opponent = result
                            .getMatchResults()
                            .stream()
                            .map(Match.MatchResult::getOpponent)
                            .findAny()
                            .orElseThrow(
                                    () -> new IllegalStateException("Expected at least one opponent, but none found"));

                    if (scores[j] == null) {
                        scores[j] = new Score(opponent);
                    }

                    Score opponentScore = scores[j];

                    opponentScore.updateAverageNumberOfRoundsMean(result.getAverageNumberOfRounds());
                    opponentScore.updateAverageScoreMean(result.getAverageOpponentScore());
                    opponentScore.updateAverageWinRateMean(1.0 - result.getPlayerWinRate());

                    if (Winner.PLAYER == result.getWinner()) {
                        playerScore.incrementVictoryCount();
                    } else {
                        opponentScore.incrementVictoryCount();
                    }

                    k++;
                }

                offset += ais.size() - (i + 1);
            }
        }

        return new ContestResult(scores);
    }

    public static class ContestResult {

        private final List<Score> classification;

        private static final Comparator<Score> SCORE_COMPARATOR =
                Comparator.comparing(Score::getVictoryCount)
                        .thenComparing(Score::getAverageScore)
                        .thenComparing(Score::getAverageNumberOfRounds)
                        .reversed();

        ContestResult(Score[] scores) {

            for (int i = 0; i < scores.length; i++) {
                Preconditions.checkNotNull(scores[i], "Unexpected null value at %s", i);
            }

            this.classification = Arrays.asList(scores);
            Collections.sort(classification, SCORE_COMPARATOR);
        }

        List<Score> getClassification() {
            return Collections.unmodifiableList(classification);
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < classification.size(); i++) {
                stringBuilder.append(i + 1).append("- ").append(classification.get(i).toString()).append('\n');
            }
            return stringBuilder.toString();
        }
    }

    public static class Score {

        private final AI ai;

        private int victoryCount;

        private double averageScore;
        private int averageScoreCount;

        private double averageNumberOfRounds;
        private int averageNumberOfRoundsCount;

        private double averageWinRate;
        private int averageWinRateCount;

        Score(AI ai) {
            this.ai = ai;

            this.victoryCount = 0;

            this.averageScore = 0L;
            this.averageScoreCount = 0;

            this.averageNumberOfRounds = 0L;
            this.averageNumberOfRoundsCount = 0;

            this.averageWinRate = 0L;
            this.averageWinRateCount = 0;
        }

        void incrementVictoryCount() {
            victoryCount++;
        }

        void updateAverageScoreMean(double averageScore) {
            this.averageScore += (1.0 / (averageScoreCount + 1)) * (averageScore - this.averageScore);
            averageScoreCount++;
        }

        void updateAverageNumberOfRoundsMean(double averageScore) {
            this.averageNumberOfRounds += (1.0 / (averageNumberOfRoundsCount + 1))
                    * (averageScore - this.averageNumberOfRounds);
            averageNumberOfRoundsCount++;
        }

        void updateAverageWinRateMean(double averageWinRate) {
            this.averageWinRate += (1.0 / (averageWinRateCount + 1)) * (averageWinRate - this.averageWinRate);
            averageWinRateCount++;
        }

        AI getAi() {
            return ai;
        }

        int getVictoryCount() {
            return victoryCount;
        }

        public double getAverageScore() {
            return averageScore;
        }

        public double getAverageNumberOfRounds() {
            return averageNumberOfRounds;
        }

        public double getAverageWinRate() {
            return averageWinRate;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("ai", ai)
                    .add("victoryCount", victoryCount)
                    .add("averageScore", averageScore)
                    .add("averageNumberOfRounds", averageNumberOfRounds)
                    .add("averageWinRate", averageWinRate)
                    .toString();
        }
    }
}
