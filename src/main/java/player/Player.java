package player;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Scanner;

import jdk.nashorn.internal.ir.annotations.Immutable;

public final class Player {

    public static void main(String args[]) {

        InputSupplier in = new InputReader(new Scanner(System.in));

        InputRepository repo = new InputRepository(in);
        AI ai = new GeneticAI(repo);

        while (true) {
            ai.updateRepository();
            Action[] actions = ai.play();
            for (Action action : actions) {
                System.out.println(action.asString());
            }
        }
    }

    public static class GeneticAI extends AI {

        private final InputRepository repo;

        public GeneticAI(InputRepository repo) {
            super(repo);
            this.repo = repo;
        }

        @Override
        public Action[] play() {

            return new Action[] { Action.move(0, 0) };
        }
    }

    public static final class HypersonicGameEngine {

        private final int height;
        private final int width;

        private final CellType[][] grid;
        private final List<Bomb> bombs;
        private final List<Item> items;
        private final Bomberman[] bombermen;
        private final boolean[] deadBombermen;

        private final Item[][] itemsGrid;
        private final Bomb[][] bombsGrid;

        /**
         * Builder with initial state
         */
        public HypersonicGameEngine(
                CellType[][] grid,
                List<Bomb> bombs,
                List<Item> items,
                Bomberman[] bombermen) {

            this.height = grid.length;
            this.width = grid[0].length;
            this.itemsGrid = new Item[height][width];
            this.bombsGrid = new Bomb[height][width];

            this.grid = grid;
            this.bombs = new ArrayList<>(bombs.size());

            for (Bomb bomb : bombs) {
                Bomb copy = new Bomb(bomb);
                this.bombs.add(copy);

                int x = bomb.getCell().getX();
                int y = bomb.getCell().getY();
                this.bombsGrid[y][x] = copy;
            }

            this.items = new ArrayList<>(items.size());
            for (Item item : items) {
                this.items.add(item);

                int x = item.getCell().getX();
                int y = item.getCell().getY();
                this.itemsGrid[y][x] = item;
            }

            this.bombermen = bombermen;
            for (int i = 0; i < bombermen.length; i++) {
                this.bombermen[i] = new Bomberman(bombermen[i]);
            }

            this.deadBombermen = new boolean[bombermen.length];
        }

        public void perform(SimplifiedAction... actions) {
            perform(false, actions);
        }

        public void perform(boolean relaxed, SimplifiedAction... actions) {
            if (actions.length != bombermen.length) {
                throw new IllegalStateException("Expected " + bombermen.length + " actions, but found " + actions);
            }

            List<Cell> boxesToDestroy = new ArrayList<>();
            List<Cell> itemsToDestroy = new ArrayList<>();

            Queue<Bomb> toEvaluate = new ArrayDeque<>(bombs);

            for (Bomb bomb = toEvaluate.poll(); bomb != null; bomb = toEvaluate.poll()) {

                if (bomb.decrementRoundsToExplode() <= 0) {
                    int x = bomb.getCell().getX();
                    int y = bomb.getCell().getY();
                    int range = bomb.getExplosionRange();

                    int owner = bomb.getOwner();
                    // if hits any bomberman, he dies
                    for (Bomberman bomberman : bombermen) {
                        if (owner == bomberman.getId()) {
                            bomberman.incrementBombsToPlace();
                        }

                        if (bomberman.getCell().getX() == x) {
                            if (Math.abs(bomberman.getCell().getY() - y) <= range) {
                                deadBombermen[bomberman.getId()] = true;
                            }
                        } else if (bomberman.getCell().getY() == y) {
                            if (Math.abs(bomberman.getCell().getX() - x) <= range) {
                                deadBombermen[bomberman.getId()] = true;
                            }
                        }
                    }

                    // evaluating east
                    for (int j = x - 1; j > x - range; j--) {
                        if (j < 0 || grid[y][j] == CellType.BLOCK) {
                            break;
                        }

                        if (grid[y][j] == CellType.BOX_WITH_NO_ITEM
                                || grid[y][j] == CellType.BOX_WITH_EXTRA_BOMB
                                || grid[y][j] == CellType.BOX_WITH_EXTRA_RANGE) {
                            boxesToDestroy.add(new Cell(j, y));
                            break;
                        }

                        if (itemsGrid[y][j] != null) {
                            itemsToDestroy.add(new Cell(j, y));
                            break;
                        }

                        // bombs are detonated in chain
                        if (bombsGrid[y][j] != null) {
                            bombsGrid[y][j].setRoundsToExplode(1);
                            toEvaluate.add(bombsGrid[y][j]);
                        }
                    }

                    // evaluating west
                    for (int j = x + 1; j < x + range; j++) {
                        if (j > (width - 1) || grid[y][j] == CellType.BLOCK) {
                            break;
                        }

                        if (grid[y][j] == CellType.BOX_WITH_NO_ITEM
                                || grid[y][j] == CellType.BOX_WITH_EXTRA_BOMB
                                || grid[y][j] == CellType.BOX_WITH_EXTRA_RANGE) {
                            boxesToDestroy.add(new Cell(j, y));
                            break;
                        }

                        if (itemsGrid[y][j] != null) {
                            itemsToDestroy.add(new Cell(j, y));
                            break;
                        }

                        // bombs are detonated in chain
                        if (bombsGrid[y][j] != null) {
                            bombsGrid[y][j].setRoundsToExplode(1);
                            toEvaluate.add(bombsGrid[y][j]);
                        }
                    }

                    // evaluating north
                    for (int i = y - 1; i > y - range; i--) {
                        if (i < 0 || grid[i][x] == CellType.BLOCK) {
                            break;
                        }

                        if (grid[i][x] == CellType.BOX_WITH_NO_ITEM
                                || grid[i][x] == CellType.BOX_WITH_EXTRA_BOMB
                                || grid[i][x] == CellType.BOX_WITH_EXTRA_RANGE) {
                            boxesToDestroy.add(new Cell(x, i));
                            break;
                        }

                        if (itemsGrid[i][x] != null) {
                            itemsToDestroy.add(new Cell(x, i));
                            break;
                        }

                        // bombs are detonated in chain
                        if (bombsGrid[i][x] != null) {
                            bombsGrid[i][x].setRoundsToExplode(1);
                            toEvaluate.add(bombsGrid[i][x]);
                        }
                    }

                    // evaluating down Y axis
                    for (int i = y + 1; i < y + range; i++) {
                        if (i > (height - 1) || grid[i][x] == CellType.BLOCK) {
                            break;
                        }
                        if (grid[i][x] == CellType.BOX_WITH_NO_ITEM
                                || grid[i][x] == CellType.BOX_WITH_EXTRA_BOMB
                                || grid[i][x] == CellType.BOX_WITH_EXTRA_RANGE) {
                            boxesToDestroy.add(new Cell(x, i));
                            break;
                        }

                        if (itemsGrid[i][x] != null) {
                            itemsToDestroy.add(new Cell(x, i));
                            break;
                        }

                        // bombs are detonated in chain
                        if (bombsGrid[i][x] != null) {
                            bombsGrid[i][x].setRoundsToExplode(1);
                            toEvaluate.add(bombsGrid[i][x]);
                        }
                    }

                    bombs.remove(bomb);
                    bombsGrid[y][x] = null;
                }

                for (Cell item : itemsToDestroy) {
                    int x = item.getX();
                    int y = item.getY();
                    items.remove(itemsGrid[y][x]);
                    itemsGrid[y][x] = null;
                }

                for (Cell box : boxesToDestroy) {
                    int x = box.getX();
                    int y = box.getY();

                    if (grid[y][x] == CellType.BOX_WITH_EXTRA_BOMB) {
                        Item item = new Item(ItemType.EXTRA_BOMB, x, y);
                        items.add(item);
                        itemsGrid[y][x] = item;
                    } else if (grid[y][x] == CellType.BOX_WITH_EXTRA_RANGE) {
                        Item item = new Item(ItemType.EXTRA_RANGE, x, y);
                        items.add(item);
                        itemsGrid[y][x] = item;
                    }

                    grid[y][x] = CellType.FLOOR;
                }
            }

            for (Bomberman bomberman : bombermen) {
                int id = bomberman.getId();
                if (!deadBombermen[id] || relaxed) {

                    int x = bomberman.getCell().getX();
                    int y = bomberman.getCell().getY();

                    SimplifiedAction action = actions[id];

                    switch (action) {
                    case BOMB_AND_MOVE_UP:
                        placeBomb(bomberman);
                    case MOVE_UP:
                        moveTo(bomberman, x, y - 1);
                        break;
                    case BOMB_AND_MOVE_DOWN:
                        placeBomb(bomberman);
                    case MOVE_DOWN:
                        moveTo(bomberman, x, y + 1);
                        break;
                    case BOMB_AND_MOVE_LEFT:
                        placeBomb(bomberman);
                    case MOVE_LEFT:
                        moveTo(bomberman, x - 1, y);
                        break;
                    case BOMB_AND_MOVE_RIGHT:
                        placeBomb(bomberman);
                    case MOVE_RIGHT:
                        moveTo(bomberman, x + 1, y);
                        break;
                    case BOMB_AND_STAY:
                        placeBomb(bomberman);
                    case STAY:
                        break;
                    }
                }
            }
        }

        private void placeBomb(Bomberman bomberman) {
            int id = bomberman.getId();
            int x = bomberman.getCell().getX();
            int y = bomberman.getCell().getY();

            if (bomberman.getBombsToPlace() > 0) {
                Bomb bomb = new Bomb(id, x, y, 8, bomberman.getExplosionRange());
                bombs.add(bomb);
                bombsGrid[y][x] = bomb;
                bomberman.decrementBombsToPlace();
            }
        }

        private void moveTo(Bomberman bomberman, int nextX, int nextY) {
            if (nextX >= 0 && nextX < width && nextY >= 0 && nextY < height) {
                if (grid[nextY][nextX] == CellType.FLOOR && bombsGrid[nextY][nextX] == null) {
                    bomberman.setCell(new Cell(nextX, nextY));
                    Item item = itemsGrid[nextY][nextX];
                    if (item != null) {
                        switch (item.getItemType()) {
                        case EXTRA_RANGE:
                            bomberman.incrementExplosionRange();
                            break;
                        case EXTRA_BOMB:
                            bomberman.incrementBombsToPlace();
                            break;
                        }
                        items.remove(item);
                        itemsGrid[nextY][nextX] = null;
                    }
                }
            }
        }

        public CellType[][] getGrid() {
            return grid;
        }

        public List<Item> getItems() {
            return items;
        }

        public List<Bomb> getBombs() {
            return bombs;
        }

        public Bomberman[] getBombermen() {
            return bombermen;
        }

        public boolean isBombermenDead(int id) {
            return deadBombermen[id];
        }
    }

    public static class InputRepository implements RepositoryUpdater {

        private final InputSupplier in;

        private final int width;
        private final int height;
        private final int myId;

        private final CellType[][] grid;
        private final List<Bomb> bombs;
        private final List<Item> items;

        private final List<Bomberman> bombermen;

        private Bomberman player;

        private int remainingRounds;

        InputRepository(InputSupplier in) {
            this.in = in;
            this.width = in.nextInt();
            this.height = in.nextInt();
            this.myId = in.nextInt();
            in.nextLine();

            this.remainingRounds = 200;

            this.grid = new CellType[height][width];
            this.bombs = new ArrayList<>();
            this.items = new ArrayList<>();
            this.bombermen = new ArrayList<>();
        }

        @Override
        public void update() {

            remainingRounds--;

            bombs.clear();
            items.clear();
            bombermen.clear();

            for (int i = 0; i < height; i++) {
                String row = in.nextLine();
                for (int j = 0; j < row.length(); j++) {
                    char cell = row.charAt(j);
                    if (cell == '.') {
                        this.grid[i][j] = CellType.FLOOR;
                    } else if (cell == 'X') {
                        this.grid[i][j] = CellType.BLOCK;
                    } else {
                        if (cell == '0') {
                            this.grid[i][j] = CellType.BOX_WITH_NO_ITEM;
                        } else if (cell == '1') {
                            this.grid[i][j] = CellType.BOX_WITH_EXTRA_RANGE;
                        } else {
                            this.grid[i][j] = CellType.BOX_WITH_EXTRA_BOMB;
                        }
                    }
                }
            }

            int entities = in.nextInt();

            for (int i = 0; i < entities; i++) {
                int entityType = in.nextInt();
                int owner = in.nextInt();
                int x = in.nextInt();
                int y = in.nextInt();
                int param1 = in.nextInt();
                int param2 = in.nextInt();

                if (entityType == 0) {
                    Bomberman player = new Bomberman(owner, x, y, param1, param2);
                    if (myId == owner) {
                        this.player = player;
                    }
                    bombermen.add(player);
                } else if (entityType == 1) {
                    bombs.add(new Bomb(owner, x, y, param1, param2));
                } else {
                    Item item = new Item((param1 == 1) ? ItemType.EXTRA_RANGE : ItemType.EXTRA_BOMB, x, y);
                    items.add(item);
                }
            }

            in.nextLine();
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getMyId() {
            return myId;
        }

        public CellType[][] getGrid() {
            return grid;
        }

        Bomberman getPlayer() {
            return player;
        }

        public List<Bomb> getBombs() {
            return bombs;
        }

        public List<Item> getItems() {
            return items;
        }

        public int getRemainingRounds() {
            return remainingRounds;
        }
    }

    public enum SimplifiedAction {
        MOVE_UP,
        MOVE_DOWN,
        MOVE_LEFT,
        MOVE_RIGHT,
        STAY,
        BOMB_AND_MOVE_UP,
        BOMB_AND_MOVE_DOWN,
        BOMB_AND_MOVE_LEFT,
        BOMB_AND_MOVE_RIGHT,
        BOMB_AND_STAY
    }

    public static final class Bomb extends StaticEntity {
        private final int owner;
        private final int explosionRange;
        private int roundsToExplode;

        public Bomb(
                int owner,
                int x,
                int y,
                int roundsToExplode,
                int explosionRange) {

            super(EntityType.BOMB, x, y);

            this.owner = owner;
            this.roundsToExplode = roundsToExplode;
            this.explosionRange = explosionRange;
        }

        public Bomb(Bomb bomb) {
            super(EntityType.BOMB, bomb.getCell());
            this.owner = bomb.getOwner();
            this.explosionRange = bomb.getExplosionRange();
            this.roundsToExplode = bomb.getRoundsToExplode();
        }

        public int getOwner() {
            return owner;
        }

        public int getRoundsToExplode() {
            return roundsToExplode;
        }

        public int incrementRoundsToExplode() {
            return ++roundsToExplode;
        }

        public int decrementRoundsToExplode() {
            return --roundsToExplode;
        }

        public void setRoundsToExplode(int value) {
            roundsToExplode = value;
        }

        public int getExplosionRange() {
            return explosionRange;
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

            Bomb bomb = (Bomb) o;
            return owner == bomb.owner &&
                    roundsToExplode == bomb.roundsToExplode &&
                    explosionRange == bomb.explosionRange;
        }
    }

    public static final class Bomberman {

        private final int id;

        private Cell cell;
        private int bombsToPlace;
        private int explosionRange;

        public Bomberman(Bomberman other) {

            this.id = other.getId();
            this.cell = new Cell(other.getCell().getX(), other.getCell().getY());
            this.bombsToPlace = other.getBombsToPlace();
            this.explosionRange = other.getExplosionRange();
        }

        public Bomberman(
                int id,
                int x,
                int y,
                int bombsToPlace,
                int explosionRange) {

            this.id = id;
            this.cell = new Cell(x, y);
            this.bombsToPlace = bombsToPlace;
            this.explosionRange = explosionRange;
        }

        public int getId() {
            return id;
        }

        public int getBombsToPlace() {
            return bombsToPlace;
        }

        public void incrementBombsToPlace() {
            this.bombsToPlace++;
        }

        public void decrementBombsToPlace() {
            this.bombsToPlace--;
        }

        public int getExplosionRange() {
            return explosionRange;
        }

        public void incrementExplosionRange() {
            this.explosionRange++;
        }

        public void decrementExplosionRange() {
            this.explosionRange--;
        }

        public Cell getCell() {
            return cell;
        }

        public void setCell(Cell cell) {
            this.cell = cell;
        }

        // public Action perform(SimplifiedAction action) {
        // int x = getCell().getX();
        // int y = getCell().getY();
        //
        // switch (action) {
        // case MOVE_UP:
        // if (x - 1 >= 0) {
        // return Action.move(x - 1, y);
        // }
        // return Action.move(0, y);
        //
        // case MOVE_DOWN:
        // if (x + 1 < ) {
        // return Action.move(x - 1, y);
        // }
        // return Action.move(0, y);
        //
        // case MOVE_LEFT:
        // break;
        // case MOVE_RIGHT:
        // break;
        // case STAY:
        // break;
        // case BOMB_AND_MOVE_UP:
        // break;
        // case BOMB_AND_MOVE_DOWN:
        // break;
        // case BOMB_AND_MOVE_LEFT:
        // break;
        // case BOMB_AND_MOVE_RIGHT:
        // break;
        // case BOMB_AND_STAY:
        // break;
        // }
        // }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Bomberman bomberman = (Bomberman) o;
            return id == bomberman.id &&
                    bombsToPlace == bomberman.bombsToPlace &&
                    explosionRange == bomberman.explosionRange &&
                    Objects.equals(cell, bomberman.cell);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, cell, bombsToPlace, explosionRange);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Bomberman{");
            sb.append("id=").append(id);
            sb.append(", cell=").append(cell);
            sb.append(", bombsToPlace=").append(bombsToPlace);
            sb.append(", explosionRange=").append(explosionRange);
            sb.append('}');
            return sb.toString();
        }
    }

    @Immutable
    public static final class Item extends StaticEntity {

        private final ItemType itemType;

        public Item(ItemType itemType, int x, int y) {
            super(EntityType.ITEM, x, y);
            this.itemType = itemType;
        }

        public ItemType getItemType() {
            return itemType;
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

            Item item = (Item) o;
            return itemType == item.itemType;
        }

        @Override
        public String toString() {
            return "Item{" +
                    "itemType=" + itemType +
                    "} " + super.toString();
        }
    }

    @Immutable
    public static class StaticEntity {

        private final Cell cell;
        private final EntityType entityType;

        public StaticEntity(EntityType entityType, int x, int y) {
            this(entityType, new Cell(x, y));
        }

        public StaticEntity(EntityType entityType, Cell cell) {
            this.cell = cell;
            this.entityType = entityType;
        }

        public EntityType getEntityType() {
            return entityType;
        }

        public Cell getCell() {
            return cell;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            StaticEntity entity = (StaticEntity) o;
            return Objects.equals(cell, entity.cell) &&
                    entityType == entity.entityType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(cell, entityType);
        }

        @Override
        public String toString() {
            return com.google.common.base.Objects.toStringHelper(this)
                    .add("cell", cell)
                    .add("entityType", entityType)
                    .toString();
        }
    }

    @Immutable
    public static class Cell {
        private final int x;
        private final int y;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        double squareDistTo(Cell cell) {
            return (cell.x - x) * (cell.x - x) + (cell.y - y) * (cell.y - y);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Cell cell = (Cell) o;
            return x == cell.x &&
                    y == cell.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            return "Cell{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    public enum ItemType {
        EXTRA_RANGE, EXTRA_BOMB
    }

    public enum EntityType {
        BOMB, ITEM
    }

    public enum CellType {
        FLOOR, BLOCK, BOX_WITH_NO_ITEM, BOX_WITH_EXTRA_RANGE, BOX_WITH_EXTRA_BOMB
    }

    public enum ActionType {
        MOVE, BOMB
    }

    /**
     * Represents an action that can be taken
     */
    @Immutable
    public static class Action {

        private final ActionType type;
        private final int x;
        private final int y;
        private final String message;

        public static Action move(int x, int y) {
            return new Action(ActionType.MOVE, x, y);
        }

        public static Action bomb(int x, int y) {
            return new Action(ActionType.BOMB, x, y);
        }

        public Action(ActionType type, int x, int y) {
            this(type, x, y, "");
        }

        public Action(ActionType type, int x, int y, String message) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.message = message;
        }

        public String asString() {
            return type.name() + " " + x + " " + y + " " + message;
        }
    }

    public static abstract class AI {

        private final Map<String, Object> conf;
        private final RepositoryUpdater updater;

        /**
         * Builds an AI with specified configuration.<br>
         * It is recommended to create a default configuration.
         */
        public AI(Map<String, Object> conf, RepositoryUpdater updater) {
            this.conf = Collections.unmodifiableMap(conf);
            this.updater = updater;
        }

        /**
         * Builds an AI with an empty configuration.
         */
        public AI(RepositoryUpdater updater) {
            this(Collections.emptyMap(), updater);
        }

        /**
         * Implements the IA algorithm
         *
         * @return the best ordered set of actions found
         */
        public abstract Action[] play();

        public Map<String, Object> getConf() {
            return conf;
        }

        public final void updateRepository() {
            updater.update();
        }

        @Override
        public final boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            AI ai = (AI) o;
            return Objects.equals(conf, ai.conf);
        }

        @Override
        public final int hashCode() {
            return Objects.hash(conf, getClass());
        }
    }

    public interface RepositoryUpdater {
        void update();
    }

    public static class InputReader implements InputSupplier {
        private final Scanner in;

        public InputReader(Scanner in) {
            this.in = in;
        }

        @Override
        public int nextInt() {
            return in.nextInt();
        }

        @Override
        public String nextLine() {
            return in.nextLine();
        }
    }

    public interface InputSupplier {
        int nextInt();

        String nextLine();
    }
}
