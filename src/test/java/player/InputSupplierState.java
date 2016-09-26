package player;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import player.Player.Bomb;
import player.Player.Bomberman;
import player.Player.InputSupplier;
import player.Player.Item;
import player.Player.ItemType;

public class InputSupplierState {

    private int width;
    private int height;
    private int myId;
    private String[] grid;

    private Set<Bomberman> bombermen;
    private Set<Bomb> bombs;
    private Set<Item> items;

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

        bombermen = new HashSet<>();
        bombermen.add(new Bomberman(0, 0, 0, 1, 3));
        bombermen.add(new Bomberman(1, 12, 10, 1, 3));

        bombs = new HashSet<>();
        items = new HashSet<>();
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
        this.bombermen.clear();
        Collections.addAll(this.bombermen, bombermans);
        return this;
    }

    public InputSupplierState withBombs(Bomb... bombs) {
        this.bombs.clear();
        Collections.addAll(this.bombs, bombs);
        return this;
    }

    public InputSupplierState withItems(Item... items) {
        this.items.clear();
        Collections.addAll(this.items, items);
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

            intQueue.add(state.bombermen.size() + state.bombs.size() + state.items.size());

            for (Bomberman bomberman : state.bombermen) {
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

            for (Item item : state.items) {
                intQueue.add(2);
                intQueue.add(0);
                intQueue.add(item.getX());
                intQueue.add(item.getY());
                intQueue.add((item.getItemType() == ItemType.EXTRA_RANGE) ? 1 : 2);
                intQueue.add(0);
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
