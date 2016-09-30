package player.contest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

import player.Player.AI;
import player.Player.BlockAwareAI;
import player.Player.DefaultGeneticAI;
import player.Player.InputRepository;
import player.Player.InputSupplier;
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
        ExecutorService matchPool = Executors.newFixedThreadPool(4);

        Contest contest =
                new Contest(
                        generateAIs(),
                        getAllEngines(),
                        gamePool,
                        matchPool);

        ContestResult contestResult = contest.call();

        System.out.println(contestResult);

        gamePool.shutdown();
        matchPool.shutdown();
    }

    private static List<Function<InputSupplier, Supplier<AI>>> generateAIs() {
        // 500, 45, 30, 10, 60
        List<Function<InputSupplier, Supplier<AI>>> list = new ArrayList<>();

        // final int deadBombermanWeight = 10;
        // final int bombItemWeight = 10;
        final int explosionRangeWeight = 50;
        final int freedomWeight = 10;
        // final int destroyedBoxWeight = 10;

        int[] deadBombermanWeight = new int[] { 30, 60, 100 };
        int[] bombWeight = new int[] { 30, 50, 70 };
        int[] destroyedBoxWeight = new int[] { 30, 60, 90 };

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                // for (int k = 0; k < 10; k++) {
                // for (int l = 0; l < 10; l++) {
                for (int m = 0; m < 3; m++) {
                    list.add(
                            buildBlockAwareAi(
                                    deadBombermanWeight[i],
                                    bombWeight[j],
                                    // bombItemWeight,
                                    // explosionRangeWeight + k * 10,
                                    explosionRangeWeight,
                                    // freedomWeight + l * 10,
                                    freedomWeight,
                                    destroyedBoxWeight[m]));
                }
                // }
                // }
            }
        }

        list.add(aiSupplier -> () -> new DefaultGeneticAI(new InputRepository(aiSupplier)));

        return list;
    }

    private static Function<InputSupplier, Supplier<AI>> buildBlockAwareAi(
            int deadBombermanWeight,
            int bombItemWeight,
            int explosionRangeWeight,
            int freedomWeight,
            int destroyedBoxWeight) {

        return aiSupplier -> () -> new BlockAwareAI(
                deadBombermanWeight,
                bombItemWeight,
                explosionRangeWeight,
                freedomWeight,
                destroyedBoxWeight,
                new InputRepository(aiSupplier));
    }

    private static List<Supplier<GameEngine>> getAllEngines() {
        List<Supplier<GameEngine>> engines = new ArrayList<>();

        for (String[] grid : Grids.getAllGrids()) {
            engines.add(() -> new PvPGE(grid));
        }

        return engines;
    }
}
