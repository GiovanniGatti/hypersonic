package player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.BiConsumer;

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

    public static class InputRepository implements RepositoryUpdater {

        private final InputSupplier in;

        private final int width;
        private final int height;
        private final int myId;

        private int remainingRounds;

        private final CellType[][] grid;
        private final List<Box> boxes;

        private Bomberman player;
        private final List<Bomberman> opponents;
        private final Map<Integer, List<Bomb>> ownerBombs;

        private final List<Bomb> bombs;

        private final List<Item> items;

        private final int[][] distTo;

        private final Map<Cell, Bomb> bombsAt;
        private final Map<Cell, Item> itemsAt;
        private final Map<Cell, Bomberman> bombermenAt;

        private final List<Cell> targetableBoxes;

        InputRepository(InputSupplier in) {
            this.in = in;
            this.width = in.nextInt();
            this.height = in.nextInt();
            this.myId = in.nextInt();
            in.nextLine();

            this.remainingRounds = 200;

            this.grid = new CellType[height][width];
            this.boxes = new ArrayList<>();

            this.ownerBombs = new HashMap<>();
            this.opponents = new ArrayList<>();
            this.bombs = new ArrayList<>();
            this.items = new ArrayList<>();

            this.distTo = new int[height][width];
            for (int i = 0; i < height; i++) {
                Arrays.fill(distTo[i], Integer.MAX_VALUE);
            }

            this.bombsAt = new HashMap<>();
            this.targetableBoxes = new ArrayList<>();
            this.itemsAt = new HashMap<>();
            this.bombermenAt = new HashMap<>();
        }

        @Override
        public void update() {

            remainingRounds--;

            boxes.clear();
            opponents.clear();
            ownerBombs.clear();
            bombs.clear();
            items.clear();
            bombsAt.clear();
            targetableBoxes.clear();
            itemsAt.clear();
            bombermenAt.clear();

            for (int i = 0; i < height; i++) {
                String row = in.nextLine();
                for (int j = 0; j < row.length(); j++) {
                    char cell = row.charAt(j);
                    if (cell == '.') {
                        this.grid[i][j] = CellType.FLOOR;
                    } else if (cell == 'X') {
                        this.grid[i][j] = CellType.BLOCK;
                    } else {
                        this.grid[i][j] = CellType.BOX;
                        if (cell == '0') {
                            boxes.add(new Box(BoxContent.NO_ITEM, j, i));
                        } else if (cell == '1') {
                            boxes.add(new Box(BoxContent.EXTRA_RANGE, j, i));
                        } else {
                            boxes.add(new Box(BoxContent.EXTRA_BOMB, j, i));
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
                    Bomberman bomberman = new Bomberman(owner, x, y, param1, param2);
                    if (owner == myId) {
                        player = bomberman;
                    } else {
                        opponents.add(bomberman);
                    }

                    bombermenAt.put(new Cell(x, y), bomberman);
                } else if (entityType == 1) {
                    Bomb bomb = new Bomb(owner, x, y, param1, param2);
                    ownerBombs.putIfAbsent(owner, new ArrayList<>());
                    ownerBombs.get(owner).add(bomb);
                    bombs.add(bomb);
                    bombsAt.put(new Cell(x, y), bomb);
                } else {
                    Item item = new Item((param1 == 1) ? ItemType.EXTRA_RANGE : ItemType.EXTRA_BOMB, x, y);
                    items.add(item);
                    itemsAt.put(new Cell(x, y), item);
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

        public List<Bomberman> getOpponents() {
            return opponents;
        }

        public List<Bomb> getBombs() {
            return bombs;
        }

        public List<Bomb> getBombsFor(int owner) {
            return ownerBombs.getOrDefault(owner, new ArrayList<>());
        }

        public List<Box> getBoxes() {
            return boxes;
        }

        public List<Item> getItems() {
            return items;
        }

        public int getRemainingRounds() {
            return remainingRounds;
        }

        public List<Cell> getTargetableBoxes() {
            return targetableBoxes;
        }

        void acceptForExplosionRange(Bomb bomb, BiConsumer<Integer, Integer> consumer) {
            acceptForExplosionRange(bomb.getCell(), bomb.getExplosionRange(), consumer);
        }

        void acceptForExplosionRange(Cell bomb, int range, BiConsumer<Integer, Integer> consumer) {
            final int x = bomb.getX();
            final int y = bomb.getY();

            // evaluating left X axis
            for (int j = x - 1; j > x - range; j--) {
                if (j < 0 || hasObstructionAt(j, y)) {
                    break;
                }
                consumer.accept(j, y);
            }

            // evaluating right X axis
            for (int j = x + 1; j < x + range; j++) {
                if (j > (width - 1) || hasObstructionAt(j, y)) {
                    break;
                }
                consumer.accept(j, y);
            }

            // evaluating upper Y axis
            for (int i = y - 1; i > y - range; i--) {
                if (i < 0 || hasObstructionAt(x, i)) {
                    break;
                }
                consumer.accept(x, i);
            }

            // evaluating down Y axis
            for (int i = y + 1; i < y + range; i++) {
                if (i > (height - 1) || hasObstructionAt(x, i)) {
                    break;
                }
                consumer.accept(x, i);
            }
        }

        private boolean hasBombAt(int x, int y) {
            return bombsAt.containsKey(new Cell(x, y));
        }

        private boolean hasItemAt(int x, int y) {
            return itemsAt.containsKey(new Cell(x, y));
        }

        private boolean hasBombermanAt(int x, int y) {
            return bombermenAt.containsKey(new Cell(x, y));
        }

        private boolean hasObstructionAt(int x, int y) {
            return grid[y][x] != CellType.FLOOR || hasBombAt(x, y) || hasItemAt(x, y) || hasBombermanAt(x, y);
        }
    }

    @Immutable
    public static final class Bomb extends Entity {
        private final int owner;
        private final int roundsToExplode;
        private final int explosionRange;

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

        public int getOwner() {
            return owner;
        }

        public int getRoundsToExplode() {
            return roundsToExplode;
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

    @Immutable
    public static final class Bomberman extends Entity {
        private final int id;
        private final int bombsToPlace;
        private final int explosionRange;

        public Bomberman(
                int id,
                int x,
                int y,
                int bombsToPlace,
                int explosionRange) {

            super(EntityType.PLAYER, x, y);

            this.id = id;
            this.bombsToPlace = bombsToPlace;
            this.explosionRange = explosionRange;
        }

        public int getId() {
            return id;
        }

        public int getBombsToPlace() {
            return bombsToPlace;
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

            Bomberman bomberman = (Bomberman) o;
            return id == bomberman.id &&
                    bombsToPlace == bomberman.bombsToPlace &&
                    explosionRange == bomberman.explosionRange;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, bombsToPlace, explosionRange);
        }
    }

    @Immutable
    public static final class Item extends Entity {

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
    public static class Entity {

        private final Cell cell;
        private final EntityType entityType;

        public Entity(EntityType entityType, int x, int y) {
            this.cell = new Cell(x, y);
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

            Entity entity = (Entity) o;
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
    public static final class Box {

        private final BoxContent boxContent;
        private final Cell cell;

        public Box(BoxContent boxContent, int x, int y) {
            this.cell = new Cell(x, y);
            this.boxContent = boxContent;
        }

        public BoxContent getBoxContent() {
            return boxContent;
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

            Box box = (Box) o;
            return boxContent == box.boxContent &&
                    Objects.equals(cell, box.cell);
        }

        @Override
        public int hashCode() {
            return Objects.hash(boxContent, cell);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Box{");
            sb.append("boxContent=").append(boxContent);
            sb.append(", cell=").append(cell);
            sb.append('}');
            return sb.toString();
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

    public enum BoxContent {
        NO_ITEM, EXTRA_RANGE, EXTRA_BOMB
    }

    public enum EntityType {
        PLAYER, BOMB, ITEM
    }

    public enum CellType {
        FLOOR, BOX, BLOCK
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
