package player.game;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import player.Player.BlockAwareAI;
import player.Player.DefaultGeneticAI;
import player.Player.InputRepository;
import player.engine.PvPGE;
import player.game.Game.GameResult;
import player.grid.Grids;

public final class GameRunner {

    private GameRunner() {
        // Main class
    }

    public static void main(String args[]) throws ExecutionException, InterruptedException {

        ExecutorService pool = Executors.newFixedThreadPool(5);

        Game game = new Game(
                playerSupplier -> () -> new BlockAwareAI(new InputRepository(playerSupplier)),
                opponentSupplier -> () -> new DefaultGeneticAI(new InputRepository(opponentSupplier)),
                () -> new PvPGE(Grids.GRID_1),
                pool,
                10);

        GameResult gameResult = game.call();

        System.out.println(gameResult);

        pool.shutdown();
    }
}
