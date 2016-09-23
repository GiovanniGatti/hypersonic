package player.engine;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

import com.google.common.collect.ImmutableMap;

import player.Player.Action;

abstract class ConfigurableGE implements GameEngine {

    private Winner winner;
    private int rounds;

    private final Map<String, Object> conf;
    private final Queue<Integer> playerInput;
    private final Queue<Integer> opponentInput;

    ConfigurableGE(Map<String, Object> conf) {
        this.rounds = 0;
        this.winner = Winner.ON_GOING;
        this.conf = ImmutableMap.copyOf(conf);
        this.playerInput = new ArrayDeque<>();
        this.opponentInput = new ArrayDeque<>();
    }

    Map<String, Object> getConf() {
        return conf;
    }

    protected abstract Winner runRound(Action[] playerActions, Action[] opponentActions);

    void toPlayerInput(int... values) {
        for (int value : values) {
            playerInput.add(value);
        }
    }

    void toOpponentInput(int... values) {
        for (int value : values) {
            opponentInput.add(value);
        }
    }

    @Override
    public void run(Action[] playerActions, Action[] opponentActions) {
        this.winner = runRound(playerActions, opponentActions);
        rounds++;
    }

    @Override
    public int playerInput() {
        if (playerInput.isEmpty()) {
            throw new IllegalStateException("No inputs are available to player");
        }
        return playerInput.poll();
    }

    @Override
    public int opponentInput() {
        if (opponentInput.isEmpty()) {
            throw new IllegalStateException("No inputs are available to opponent");
        }
        return opponentInput.poll();
    }

    @Override
    public int getNumberOfRounds() {
        return rounds;
    }

    @Override
    public Winner getWinner() {
        return winner;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConfigurableGE that = (ConfigurableGE) o;
        return Objects.equals(conf, that.conf);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(conf);
    }
}
