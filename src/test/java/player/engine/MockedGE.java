package player.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import com.google.common.base.MoreObjects;

import player.Player;

public final class MockedGE {

    private MockedGE() {
        // Utility class
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static GameEngine any() {
        return newBuilder().build();
    }

    public static GameEngine anyWithWinner(Winner winner) {
        return newBuilder()
                .withWinner(winner)
                .build();
    }

    public static GameEngine anyWithPlayerScore(int playerScore) {
        return newBuilder()
                .withPlayerScore(playerScore)
                .build();
    }

    public static GameEngine anyWithOpponentScore(int opponentScore) {
        return newBuilder()
                .withOpponentScore(opponentScore)
                .build();
    }

    public static GameEngine anyWithNumberOfRounds(int rounds) {
        return newBuilder()
                .withNumberOfRounds(rounds)
                .build();
    }

    public static GameEngine anyWithPlayerInput(int... playerInput) {
        return newBuilder()
                .withPlayerInput(playerInput)
                .build();
    }

    public static GameEngine anyWithOpponentInput(int... opponentInput) {
        return newBuilder()
                .withOpponentInput(opponentInput)
                .build();
    }

    public static final class Builder {

        private Random random;
        private Winner winner;
        private int playerScore;
        private int opponentScore;
        private int numberOfRounds;

        private List<Integer> playerInput;
        private List<Integer> opponentInput;

        private Builder() {
            this.random = new Random();

            this.winner = random.nextBoolean() ? Winner.PLAYER : Winner.OPPONENT;
            this.playerScore = random.nextInt(100);
            this.opponentScore = random.nextInt(100);
            this.numberOfRounds = random.nextInt(100);

            int inputSize = random.nextInt(4) + 1;
            List<Integer> playerInput = new ArrayList<>();
            List<Integer> opponentInput = new ArrayList<>();
            for (int i = 0; i < inputSize; i++) {
                playerInput.add(random.nextInt(100));
                opponentInput.add(random.nextInt(100));
            }

            this.playerInput = playerInput;
            this.opponentInput = opponentInput;
        }

        public Builder withWinner(Winner winner) {
            this.winner = winner;
            return this;
        }

        public Builder withPlayerScore(int playerScore) {
            this.playerScore = playerScore;
            return this;
        }

        public Builder withOpponentScore(int opponentScore) {
            this.opponentScore = opponentScore;
            return this;
        }

        public Builder withNumberOfRounds(int numberOfRounds) {
            this.numberOfRounds = numberOfRounds;
            return this;
        }

        public Builder withPlayerInput(int[] playerInput) {
            List<Integer> input = new ArrayList<>();
            for (int i : playerInput) {
                input.add(i);
            }
            this.playerInput = input;
            return this;
        }

        public Builder withOpponentInput(int... opponentInput) {
            List<Integer> input = new ArrayList<>();
            for (int i : opponentInput) {
                input.add(i);
            }
            this.opponentInput = input;
            return this;
        }

        public GameEngine build() {
            return new MockedGameEngine(winner, playerScore, opponentScore, numberOfRounds, playerInput, opponentInput);
        }
    }

    private static class MockedGameEngine implements GameEngine {

        private final Winner winner;
        private final int playerScore;
        private final int opponentScore;
        private final int numberOfRounds;

        private final List<Integer> playerInput;
        private final List<Integer> opponentInput;

        private final Iterator<Integer> playerInputIt;
        private final Iterator<Integer> opponentInputIt;

        private MockedGameEngine(
                Winner winner,
                int playerScore,
                int opponentScore,
                int numberOfRounds,
                List<Integer> playerInput,
                List<Integer> opponentInput) {

            this.winner = winner;
            this.playerScore = playerScore;
            this.opponentScore = opponentScore;
            this.numberOfRounds = numberOfRounds;
            this.playerInput = playerInput;
            this.opponentInput = opponentInput;

            this.playerInputIt = playerInput.iterator();
            this.opponentInputIt = opponentInput.iterator();
        }

        @Override
        public void start() {
            // ILB
        }

        @Override
        public void run(Player.Action[] playerActions, Player.Action[] opponentActions) {
            // ILB
        }

        @Override
        public Winner getWinner() {
            return winner;
        }

        @Override
        public int playerInput() {
            return playerInputIt.next();
        }

        @Override
        public int opponentInput() {
            return opponentInputIt.next();
        }

        @Override
        public int getPlayerScore() {
            return playerScore;
        }

        @Override
        public int getOpponentScore() {
            return opponentScore;
        }

        @Override
        public int getNumberOfRounds() {
            return numberOfRounds;
        }

        @Override
        public boolean equals(Object o) {
            return this == o || !(o == null || getClass() != o.getClass());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getClass());
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("winner", winner)
                    .add("playerScore", playerScore)
                    .add("opponentScore", opponentScore)
                    .add("numberOfRounds", numberOfRounds)
                    .add("playerInput", playerInput)
                    .add("opponentInput", opponentInput)
                    .add("playerInputIt", playerInputIt)
                    .add("opponentInputIt", opponentInputIt)
                    .toString();
        }
    }
}
