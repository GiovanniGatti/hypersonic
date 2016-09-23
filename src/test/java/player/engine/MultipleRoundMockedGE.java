package player.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import player.Player;
import player.engine.MockedGE.Builder;

public class MultipleRoundMockedGE implements GameEngine {

    private final Iterator<GameEngine> rounds;
    private GameEngine currentState;
    private int startCount;
    private int runCount;

    public MultipleRoundMockedGE(Builder startState, Builder... rounds) {
        List<GameEngine> r = new ArrayList<>();

        for (Builder round : rounds) {
            r.add(round.build());
        }

        this.rounds = r.iterator();
        this.currentState = startState.build();
        this.startCount = 0;
        this.runCount = 0;
    }

    @Override
    public void start() {
        startCount++;
    }

    @Override
    public void run(Player.Action[] playerActions, Player.Action[] opponentActions) {
        runCount++;
        currentState = rounds.next();
    }

    @Override
    public Winner getWinner() {
        return currentState.getWinner();
    }

    @Override
    public int playerInput() {
        return currentState.playerInput();
    }

    @Override
    public int opponentInput() {
        return currentState.opponentInput();
    }

    @Override
    public int getPlayerScore() {
        return currentState.getPlayerScore();
    }

    @Override
    public int getOpponentScore() {
        return currentState.getOpponentScore();
    }

    @Override
    public int getNumberOfRounds() {
        return currentState.getNumberOfRounds();
    }

    public int getStartCount() {
        return startCount;
    }

    public int getRunCount() {
        return runCount;
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
