package player.game;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import player.Player.GeneticAI;
import player.Player.InputRepository;
import player.engine.PvPGE;
import player.game.Game.GameResult;
import player.grid.Grids;

public final class GameRunner {

    private GameRunner() {
        // Main class
    }

    public static void main(String args[]) throws ExecutionException, InterruptedException {

        ExecutorService service = Executors.newFixedThreadPool(3);

        Game game = new Game(
                playerSupplier -> () -> new GeneticAI(new InputRepository(playerSupplier)),
                opponentSupplier -> () -> new GeneticAI(new InputRepository(opponentSupplier)),
                () -> new PvPGE(Grids.GRID_1),
                service);

        GameResult gameResult = game.call();

        System.out.println(gameResult);
    }
}
