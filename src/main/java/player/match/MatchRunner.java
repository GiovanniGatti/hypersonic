package player.match;

import player.Player.GeneticAI;
import player.Player.InputRepository;
import player.engine.PvPGE;
import player.grid.Grids;
import player.match.Match.MatchResult;

public final class MatchRunner {

    private MatchRunner() {
        // Main class
    }

    public static void main(String args[]) {

        Match match = new Match(
                playerSupplier -> () -> new GeneticAI(new InputRepository(playerSupplier)),
                opponentSupplier -> () -> new GeneticAI(new InputRepository(opponentSupplier)),
                () -> new PvPGE(Grids.GRID_1));

        MatchResult matchResult = match.call();

        System.out.println(matchResult);
    }
}
