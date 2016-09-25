package player;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import player.Player.Bomb;
import player.Player.Bomberman;
import player.Player.InputSupplier;

public class InputSupplierState {

    private int width;
    private int height;
    private int myId;
    private String[] grid;

    private Set<Bomberman> bombermans;
    private Set<Bomb> bombs;

    public InputSupplierState() {
        width = 13;
        height = 11;
        myId = 0;
        grid = new String[11];
        grid[0] = ".....0.0.....";
        grid[1] = "...0.....0...";
        grid[2] = ".0...0.0...0.";
        grid[3] = ".............";
        grid[4] = "0.0.0...0.0.0";
        grid[5] = "......0......";
        grid[6] = "0.0.0...0.0.0";
        grid[7] = ".............";
        grid[8] = ".0...0.0...0.";
        grid[9] = "...0.....0...";
        grid[10] = ".....0.0.....";

        bombermans = new HashSet<>();
        bombermans.add(new Bomberman(0, 0, 0, 1, 3));
        bombermans.add(new Bomberman(1, 12, 10, 1, 3));

        bombs = new HashSet<>();
    }

    public InputSupplierState withGrid(String... grid) {
        height = grid.length;
        width = grid[0].length();

        for (String row : grid) {
            if (row.length() != width) {
                throw new IllegalStateException("Expected grid to have always the same width");
            }
        }

        this.grid = grid;

        return this;
    }

    public InputSupplierState withBombermans(Bomberman... bombermans) {
        this.bombermans.clear();
        Collections.addAll(this.bombermans, bombermans);
        return this;
    }

    public InputSupplierState withBombs(Bomb... bombs) {
        this.bombs.clear();
        Collections.addAll(this.bombs, bombs);
        return this;
    }

    public InputSupplierState withMyId(int myId) {
        this.myId = myId;
        return this;
    }

    public InputSupplier toInputSupplier() {
        return new ToInputStream(this);
    }

    private static class ToInputStream implements InputSupplier {

        private final Queue<String> stringQueue;
        private final Queue<Integer> intQueue;

        ToInputStream(InputSupplierState state) {
            intQueue = new ArrayDeque<>();
            stringQueue = new ArrayDeque<>();

            intQueue.add(state.width);
            intQueue.add(state.height);
            intQueue.add(state.myId);
            stringQueue.add("");

            Collections.addAll(stringQueue, state.grid);

            intQueue.add(state.bombermans.size() + state.bombs.size());

            for (Bomberman bomberman : state.bombermans) {
                intQueue.add(0);
                intQueue.add(bomberman.getId());
                intQueue.add(bomberman.getX());
                intQueue.add(bomberman.getY());
                intQueue.add(bomberman.getBombsToPlace());
                intQueue.add(bomberman.getExplosionRange());
            }

            for (Bomb bomb : state.bombs) {
                intQueue.add(1);
                intQueue.add(bomb.getOwner());
                intQueue.add(bomb.getX());
                intQueue.add(bomb.getY());
                intQueue.add(bomb.getRoundsToExplode());
                intQueue.add(bomb.getExplosionRange());
            }

            stringQueue.add("");
        }

        @Override
        public int nextInt() {
            return intQueue.remove();
        }

        @Override
        public String nextLine() {
            return stringQueue.remove();
        }
    }
}
