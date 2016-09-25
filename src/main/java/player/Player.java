package player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import jdk.nashorn.internal.ir.annotations.Immutable;

public final class Player {

    public static void main(String args[]) {

        InputSupplier in = new InputReader(new Scanner(System.in));

        InputRepository repo = new InputRepository(in);
        AI ai = new StateMachineAI(repo);

        while (true) {
            ai.updateRepository();
            Action[] actions = ai.play();
            for (Action action : actions) {
                System.out.println(action.asString());
            }
        }
    }

    public static class StateMachineAI extends AI {

        private final InputRepository repo;

        private int[][] gridEvaluation;

        public StateMachineAI(InputRepository repo) {
            super(repo);
            this.repo = repo;
        }

        @Override
        public Action[] play() {
            gridEvaluation = new int[repo.height][repo.width];

            return new Action[] { Action.bomb(6, 5) };
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
        }

        @Override
        public void update() {

            remainingRounds--;

            boxes.clear();
            opponents.clear();
            ownerBombs.clear();
            bombs.clear();

            for (int i = 0; i < height; i++) {
                String row = in.nextLine();
                for (int j = 0; j < row.length(); j++) {
                    if (row.charAt(j) == '.') {
                        this.grid[i][j] = CellType.FLOOR;
                    } else {
                        this.grid[i][j] = CellType.BOX;
                        boxes.add(new Box(j, i));
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
                } else {
                    Bomb bomb = new Bomb(owner, x, y, param1, param2);
                    ownerBombs.putIfAbsent(owner, new ArrayList<>());
                    ownerBombs.get(owner).add(bomb);
                    bombs.add(bomb);
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

        public int getRemainingRounds() {
            return remainingRounds;
        }

    }

    @Immutable
    public static class Bomb {
        private final int owner;
        private final int x;
        private final int y;
        private final int roundsToExplode;
        private final int explosionRange;

        public Bomb(
                int owner,
                int x,
                int y,
                int roundsToExplode,
                int explosionRange) {

            this.owner = owner;
            this.x = x;
            this.y = y;
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

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Bomb bomb = (Bomb) o;
            return owner == bomb.owner &&
                    x == bomb.x &&
                    y == bomb.y &&
                    roundsToExplode == bomb.roundsToExplode &&
                    explosionRange == bomb.explosionRange;
        }

        @Override
        public int hashCode() {
            return Objects.hash(owner, roundsToExplode);
        }
    }

    @Immutable
    public static class Bomberman {
        private final int id;
        private final int x;
        private final int y;
        private final int bombsToPlace;
        private final int explosionRange;

        public Bomberman(
                int id,
                int x,
                int y,
                int bombsToPlace,
                int explosionRange) {

            this.id = id;
            this.x = x;
            this.y = y;
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

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

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
                    x == bomberman.x &&
                    y == bomberman.y &&
                    bombsToPlace == bomberman.bombsToPlace &&
                    explosionRange == bomberman.explosionRange;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return "Bomberman{" +
                    "id=" + id +
                    ", x=" + x +
                    ", y=" + y +
                    ", bombsToPlace=" + bombsToPlace +
                    ", explosionRange=" + explosionRange +
                    '}';
        }
    }

    @Immutable
    public static class Box {
        private final int x;
        private final int y;

        public Box(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
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
            return x == box.x &&
                    y == box.y;
        }

        @Override
        public String toString() {
            return "Box{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    public enum CellType {
        FLOOR, BOX
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
