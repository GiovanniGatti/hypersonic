package player.contest;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import player.Player.GeneticAI;
import player.Player.InputRepository;
import player.contest.Contest.ContestResult;
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
                        aiSupplier -> () -> new GeneticAI(new InputRepository(aiSupplier)),
                        aiSupplier -> () -> new GeneticAI(new InputRepository(aiSupplier))),
                Arrays.asList(() -> new PvPGE(Grids.GRID_1), () -> new PvPGE(Grids.GRID_2)),
                gamePool,
                matchPool);

        ContestResult contestResult = contest.call();

        System.out.println(contestResult);
    }
}
