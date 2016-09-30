package player.contest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import player.Player.DefaultGeneticAI;
import player.Player.InputRepository;
import player.contest.Contest.ContestResult;
import player.engine.GameEngine;
import player.engine.PvPGE;
import player.grid.Grids;

public final class ContestRunner {

    private ContestRunner() {
        // Main class
    }

    public static void main(String args[]) throws ExecutionException, InterruptedException {
        ExecutorService gamePool = Executors.newFixedThreadPool(2);
        ExecutorService matchPool = Executors.newFixedThreadPool(3);

        Contest contest = new Contest(
                Arrays.asList(
                        aiSupplier -> () -> new DefaultGeneticAI(new InputRepository(aiSupplier)),
                        aiSupplier -> () -> new DefaultGeneticAI(32, 20, 5, .7, .001, new InputRepository(aiSupplier)),
                        aiSupplier -> () -> new DefaultGeneticAI(8, 80, 5, .7, .001, new InputRepository(aiSupplier)),
                        aiSupplier -> () -> new DefaultGeneticAI(16, 40, 10, .7, .001, new InputRepository(aiSupplier)),
                        aiSupplier -> () -> new DefaultGeneticAI(16, 40, 5, .8, .001, new InputRepository(aiSupplier)),
                        aiSupplier -> () -> new DefaultGeneticAI(16, 40, 5, .7, .003, new InputRepository(aiSupplier))),
                getAllEngines(),
                gamePool,
                matchPool);

        ContestResult contestResult = contest.call();

        System.out.println(contestResult);

        gamePool.shutdown();
        matchPool.shutdown();
    }

    private static List<Supplier<GameEngine>> getAllEngines() {
        List<Supplier<GameEngine>> engines = new ArrayList<>();

        for (String[] grid : Grids.getAllGrids()) {
            engines.add(() -> new PvPGE(grid));
        }

        return engines;
    }
}
