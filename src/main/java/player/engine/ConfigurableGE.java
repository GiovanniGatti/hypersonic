package player.engine;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableMap;

import player.Player.Action;

abstract class ConfigurableGE implements GameEngine {

    private Winner winner;
    private int rounds;

    private final Map<String, Object> conf;

    ConfigurableGE(Map<String, Object> conf) {
        this.rounds = 0;
        this.winner = Winner.ON_GOING;
        this.conf = ImmutableMap.copyOf(conf);
    }

    Map<String, Object> getConf() {
        return conf;
    }

    protected abstract Winner runRound(Action[] playerActions, Action[] opponentActions);

    @Override
    public void run(Action[] playerActions, Action[] opponentActions) {
        this.winner = runRound(playerActions, opponentActions);
        rounds++;
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
