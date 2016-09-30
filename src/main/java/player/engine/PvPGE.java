package player.engine;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;

import player.Player;
import player.Player.Action;
import player.Player.Bomb;
import player.Player.Bomberman;
import player.Player.CellType;
import player.Player.HypersonicGameEngine;
import player.Player.InputSupplier;
import player.Player.Item;
import player.Player.SimplifiedAction;

public class PvPGE extends ConfigurableGE {

    private final String[] startUpGrid;

    private HypersonicGameEngine gameEngine;

    private Bomberman player;
    private Bomberman opponent;

    private ToInputSupplier toPlayerInputSupplier;
    private ToInputSupplier toOpponentInputSupplier;

    public PvPGE(String... startUpGrid) {
        super(Collections.emptyMap());
        this.startUpGrid = startUpGrid;

        if (new Random().nextBoolean()) {
            player = new Bomberman(0, 0, 0, 1, 3);
            opponent = new Bomberman(1, 12, 10, 1, 3);
        } else {
            opponent = new Bomberman(0, 0, 0, 1, 3);
            player = new Bomberman(1, 12, 10, 1, 3);
        }

        gameEngine =
                new HypersonicGameEngine(
                        getStartUpGrid(),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        player, opponent);

        toPlayerInputSupplier = new ToInputSupplier(player.getId(), getStartUpGrid(), player, opponent);
        toOpponentInputSupplier = new ToInputSupplier(opponent.getId(), getStartUpGrid(), player, opponent);
    }

    @Override
    public void start() {
        // ILB
    }

    @Override
    public InputSupplier playerInput() {
        return toPlayerInputSupplier;
    }

    @Override
    public InputSupplier opponentInput() {
        return toOpponentInputSupplier;
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

        toPlayerInputSupplier.queueState(
                gameEngine.getGrid(),
                gameEngine.getBombermen(),
                gameEngine.getItems(),
                gameEngine.getBombs());

        toOpponentInputSupplier.queueState(
                gameEngine.getGrid(),
                gameEngine.getBombermen(),
                gameEngine.getItems(),
                gameEngine.getBombs());

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        PvPGE pvPGE = (PvPGE) o;
        return Arrays.equals(startUpGrid, pvPGE.startUpGrid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), startUpGrid);
    }

    private CellType[][] getStartUpGrid() {
        return toCellGrid(startUpGrid);
    }

    private static CellType[][] toCellGrid(String[] rows) {
        CellType[][] grid = new CellType[rows.length][rows[0].length()];

        for (int i = 0; i < rows.length; i++) {
            String row = rows[i];
            for (int j = 0; j < row.length(); j++) {
                char cell = row.charAt(j);
                if (cell == '.') {
                    grid[i][j] = CellType.FLOOR;
                } else if (cell == 'X') {
                    grid[i][j] = CellType.BLOCK;
                } else {
                    if (cell == '0') {
                        grid[i][j] = CellType.BOX_WITH_NO_ITEM;
                    } else if (cell == '1') {
                        grid[i][j] = CellType.BOX_WITH_EXTRA_RANGE;
                    } else {
                        grid[i][j] = CellType.BOX_WITH_EXTRA_BOMB;
                    }
                }
            }
        }

        return grid;
    }

    private static class ToInputSupplier implements InputSupplier {

        private final Queue<String> stringsQueue;
        private final Queue<Integer> intsQueue;

        ToInputSupplier(int playerId, CellType[][] grid, Bomberman... bombermen) {
            this.stringsQueue = new ArrayDeque<>();
            this.intsQueue = new ArrayDeque<>();

            toStringQueue("");// empty buffer line at the end - compatibility
            toIntQueue(grid[0].length, grid.length, playerId); // width, height, myId

            queueState(grid, bombermen, Collections.emptyList(), Collections.emptyList());
        }

        void toIntQueue(int... ints) {
            for (int v : ints) {
                intsQueue.add(v);
            }
        }

        void toStringQueue(String... strings) {
            Collections.addAll(stringsQueue, strings);
        }

        void queueState(CellType[][] grid, Bomberman[] bombermen, List<Item> items, List<Bomb> bombs) {
            toStringQueue(toString(grid));
            toStringQueue(""); // empty buffer line at the end - compatibility

            toIntQueue(bombermen.length + items.size() + bombs.size()); // number of entities

            for (Bomberman bomberman : bombermen) {
                toIntQueue(bomberman);
            }

            items.forEach(this::toIntQueue);

            bombs.forEach(this::toIntQueue);
        }

        private void toIntQueue(Bomberman bomberman) {
            toIntQueue(0); // entity type
            toIntQueue(bomberman.getId());
            toIntQueue(bomberman.getCell().getX());
            toIntQueue(bomberman.getCell().getY());
            toIntQueue(bomberman.getBombsToPlace());
            toIntQueue(bomberman.getExplosionRange());
        }

        private void toIntQueue(Bomb bomb) {
            toIntQueue(1); // entity type
            toIntQueue(bomb.getOwner());
            toIntQueue(bomb.getCell().getX());
            toIntQueue(bomb.getCell().getY());
            toIntQueue(bomb.getRoundsToExplode());
            toIntQueue(bomb.getExplosionRange());
        }

        private void toIntQueue(Item item) {
            toIntQueue(2); // entity type
            toIntQueue(0); // unused
            toIntQueue(item.getCell().getX());
            toIntQueue(item.getCell().getY());
            toIntQueue(item.getItemType() == Player.ItemType.EXTRA_RANGE ? 1 : 2);
            toIntQueue(0); // unused
        }

        private static String[] toString(CellType[][] grid) {
            String[] rows = new String[grid.length];

            for (int i = 0; i < grid.length; i++) {
                StringBuilder row = new StringBuilder();
                for (int j = 0; j < grid[0].length; j++) {
                    switch (grid[i][j]) {
                    case FLOOR:
                        row.append('.');
                        break;
                    case BLOCK:
                        row.append('X');
                        break;
                    case BOX_WITH_NO_ITEM:
                        row.append('0');
                        break;
                    case BOX_WITH_EXTRA_RANGE:
                        row.append('1');
                        break;
                    case BOX_WITH_EXTRA_BOMB:
                        row.append('2');
                        break;
                    }
                }
                rows[i] = row.toString();
            }

            return rows;
        }

        @Override
        public int nextInt() {
            return intsQueue.remove();
        }

        @Override
        public String nextLine() {
            return stringsQueue.remove();
        }
    }
}
