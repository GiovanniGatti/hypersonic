package player.engine;

import java.util.Collections;
import java.util.Map;
import java.util.Random;

import player.Player;
import player.Player.Action;
import player.Player.Bomberman;
import player.Player.CellType;
import player.Player.HypersonicGameEngine;
import player.Player.InputSupplier;
import player.Player.SimplifiedAction;

public class PvPGE extends ConfigurableGE {

    private Random random;
    private HypersonicGameEngine gameEngine;

    private Bomberman player;
    private Bomberman opponent;

    PvPGE(Map<String, Object> conf) {
        super(conf);
        this.random = new Random();
    }

    @Override
    public void start() {
        if (random.nextBoolean()) {
            player = new Bomberman(0, 0, 0, 1, 3);
            opponent = new Bomberman(0, 12, 10, 1, 3);
        } else {
            opponent = new Bomberman(0, 0, 0, 1, 3);
            player = new Bomberman(0, 12, 10, 1, 3);
        }

        gameEngine =
                new HypersonicGameEngine(
                        getGrid(),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        player, opponent);
    }

    @Override
    public InputSupplier playerInput() {
        return null;
    }

    @Override
    public InputSupplier opponentInput() {
        return null;
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
    protected Winner runRound(Action[] playerActions, Action[] opponentActions) {

        SimplifiedAction player = playerActions[0].getSimplifiedAction();
        SimplifiedAction opponent = opponentActions[0].getSimplifiedAction();

        gameEngine.perform(player, opponent);

        boolean playerDead = gameEngine.isBombermenDead(this.player.getId());
        boolean opponentDead = gameEngine.isBombermenDead(this.opponent.getId());

        if (opponentDead && !playerDead) {
            return Winner.PLAYER;
        } else if (!opponentDead && playerDead) {
            return Winner.OPPONENT;
        } else if (!opponentDead && getNumberOfRounds() < 200) {
            return Winner.ON_GOING;
        }

        // TODO: when both dead or when both are alive but timeout occurred, winner is who has destroyed more boxes

        return Winner.PLAYER;
    }

    private CellType[][] getGrid() {
        return (CellType[][]) getConf().get("grid");
    }


}
