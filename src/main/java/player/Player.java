package player;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
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

        private static final SimplifiedAction[] POSSIBLE_ACTIONS = SimplifiedAction.values();

        private final Random random;
        private final InputRepository repo;

        public GeneticAI(InputRepository repo) {
            super(repo);
            this.random = new Random();
            this.repo = repo;
        }

        @Override
        public Action[] play() {
            long currentTimeMillis = System.currentTimeMillis();
            Chromosome chromosome = find(15, 16, 5);
            System.err.println(System.currentTimeMillis() - currentTimeMillis);
            System.err.println(chromosome);

            SimplifiedAction nextAction = chromosome.genes[0];

            int height = repo.getHeight();
            int width = repo.getWidth();

            int x = repo.getPlayer().getCell().getX();
            int y = repo.getPlayer().getCell().getY();

            switch (nextAction) {
            case MOVE_UP:
                if (y - 1 < 0) {
                    return new Action[] { Action.move(x, 0) };
                }
                return new Action[] { Action.move(x, y - 1) };
            case BOMB_AND_MOVE_UP:
                if (y - 1 < 0) {
                    return new Action[] { Action.bomb(x, 0) };
                }
                return new Action[] { Action.bomb(x, y - 1) };
            case MOVE_DOWN:
                if (y + 1 >= height) {
                    return new Action[] { Action.move(x, height - 1) };
                }
                return new Action[] { Action.move(x, y + 1) };
            case BOMB_AND_MOVE_DOWN:
                if (y + 1 >= height) {
                    return new Action[] { Action.bomb(x, height - 1) };
                }
                return new Action[] { Action.bomb(x, y + 1) };
            case MOVE_LEFT:
                if (x - 1 < 0) {
                    return new Action[] { Action.move(0, y) };
                }
                return new Action[] { Action.move(x - 1, y) };
            case BOMB_AND_MOVE_LEFT:
                if (x - 1 < 0) {
                    return new Action[] { Action.bomb(0, y) };
                }
                return new Action[] { Action.bomb(x - 1, y) };
            case MOVE_RIGHT:
                if (x + 1 >= width) {
                    return new Action[] { Action.move(width - 1, y) };
                }
                return new Action[] { Action.move(x + 1, y) };
            case BOMB_AND_MOVE_RIGHT:
                if (x + 1 >= width) {
                    return new Action[] { Action.bomb(width - 1, y) };
                }
                return new Action[] { Action.bomb(x + 1, y) };
            case STAY:
                return new Action[] { Action.move(x, y) };
            case BOMB_AND_STAY:
                return new Action[] { Action.bomb(x, y) };
            default:
                throw new IllegalStateException("Unknown action=" + nextAction);
            }
        }

        private Chromosome find(int movements, int popSize, int generations) {

            // Create the pool
            List<Chromosome> pool = new ArrayList<>(popSize);
            List<Chromosome> newPool = new ArrayList<>(popSize);

            // Generate unique chromosomes in the pool
            for (int i = 0; i < popSize; i++) {
                Chromosome chromosome = new Chromosome(generateRandomMovements(movements));
                chromosome.evaluate(
                        new HypersonicGameEngine(
                                repo.getGrid(),
                                repo.getBombs(),
                                repo.getItems(),
                                new Bomberman[] { repo.getPlayer() }));
                pool.add(chromosome);
            }

            // Loop until solution is found
            for (int generation = 0; generation < generations; generation++) {
                // Clear the new pool
                newPool.clear();

                // Loop until the pool has been processed
                for (int x = pool.size() - 1; x >= 0; x -= 2) {
                    // Select two members
                    Chromosome n1 = selectMember(pool);
                    Chromosome n2 = selectMember(pool);

                    // Cross over and mutate
                    n1.crossOver(n2);
                    n1.mutate();
                    n2.mutate();

                    // evaluate new nodes
                    n1.evaluate(
                            new HypersonicGameEngine(
                                    repo.getGrid(),
                                    repo.getBombs(),
                                    repo.getItems(),
                                    new Bomberman[] { repo.getPlayer() }));
                    n2.evaluate(
                            new HypersonicGameEngine(
                                    repo.getGrid(),
                                    repo.getBombs(),
                                    repo.getItems(),
                                    new Bomberman[] { repo.getPlayer() }));

                    // Add to the new pool
                    newPool.add(n1);
                    newPool.add(n2);
                }

                // Add the newPool back to the old pool
                pool.addAll(newPool);

                // double tot = 0.0;
                // for (int x = newPool.size() - 1; x >= 0; x--) {
                // double evaluate = (newPool.get(x)).evaluate;
                // tot += evaluate;
                // }
                // System.out.println("generation " + generation + ": " + tot);
            }

            double maxScore = Double.NEGATIVE_INFINITY;
            Chromosome best = null;
            for (Chromosome chromosome : newPool) {
                if (chromosome.score > maxScore) {
                    maxScore = chromosome.score;
                    best = chromosome;
                }
            }

            return best;
        }

        private Chromosome selectMember(List<Chromosome> l) {

            // Get the total fitness
            double tot = 0.0;
            for (int x = l.size() - 1; x >= 0; x--) {
                double score = (l.get(x)).score;
                tot += score;
            }
            double slice = tot * random.nextDouble();

            // Loop to find the node
            double ttot = 0.0;
            for (int x = l.size() - 1; x >= 0; x--) {
                Chromosome node = l.get(x);
                ttot += node.score;
                if (ttot >= slice) {
                    l.remove(x);
                    return node;
                }
            }

            return l.remove(l.size() - 1);
        }

        private SimplifiedAction[] generateRandomMovements(int movements) {

            SimplifiedAction[] actions = new SimplifiedAction[movements];

            for (int i = 0; i < movements; i++) {
                actions[i] = POSSIBLE_ACTIONS[random.nextInt(POSSIBLE_ACTIONS.length)];
            }

            return actions;
        }
    }

    private static class Chromosome {

        private static final SimplifiedAction[] POSSIBLE_ACTIONS = SimplifiedAction.values();

        private final double crossoverRate;
        private final double mutationRate;
        private final Random random;

        private SimplifiedAction[] genes;
        private double score;

        public Chromosome(
                double crossoverRate,
                double mutationRate,
                SimplifiedAction[] genes,
                Random random) {

            this.crossoverRate = crossoverRate;
            this.mutationRate = mutationRate;
            this.genes = genes;
            this.random = random;
            this.score = 0.0;
        }

        public Chromosome(SimplifiedAction[] genes) {
            this(.7, .001, genes, new Random());
        }

        public void evaluate(HypersonicGameEngine gameEngine) {
            // TODO: single bomberman is being evaluated at a time -
            // need to update model: add opponents' movements to constr
            Bomberman bomberman = gameEngine.getBombermen()[0];
            boolean wasHeDead = false;

            for (SimplifiedAction action : genes) {

                int pastBombsToPlace = bomberman.getTotalAvailableBombs();
                int pastExplosionRange = bomberman.getExplosionRange();
                int pastAvailablePlaces = gameEngine.accessiblePlacesFor(bomberman.getId());

                gameEngine.perform(true, action);

                if (gameEngine.isBombermenDead(bomberman.getId()) && !wasHeDead) {
                    this.score -= 100;
                    wasHeDead = true;
                } else {
                    this.score++;
                }

                if (bomberman.getTotalAvailableBombs() > pastBombsToPlace) {
                    this.score += 3;
                }

                if (bomberman.getExplosionRange() > pastExplosionRange) {
                    this.score += 3;
                }

                int availablePlaces = gameEngine.accessiblePlacesFor(bomberman.getId());
                if (availablePlaces > pastAvailablePlaces) {
                    this.score += 3;
                } else if (availablePlaces == 0) {
                    this.score--;
                } else {
                    this.score++;
                }
            }
        }

        public void crossOver(Chromosome another) {
            if (random.nextDouble() < crossoverRate) {
                int randomGene = random.nextInt(genes.length);

                SimplifiedAction[] child1 = new SimplifiedAction[genes.length];
                SimplifiedAction[] child2 = new SimplifiedAction[genes.length];

                for (int j = 0; j < randomGene; j++) {
                    child1[j] = genes[j];
                    child2[j] = another.genes[j];
                }

                for (int j = randomGene; j < genes.length; j++) {
                    child1[j] = another.genes[j];
                    child2[j] = genes[j];
                }

                this.genes = child1;
                another.genes = child2;
            }
        }

        public void mutate() {
            for (int i = 0; i < genes.length; i++) {
                if (random.nextDouble() <= mutationRate) {
                    genes[i] = POSSIBLE_ACTIONS[random.nextInt(POSSIBLE_ACTIONS.length)];
                }
            }
        }

        @Override
        public String toString() {
            return "Chromosome{" +
                    "genes=" + Arrays.toString(genes) +
                    ", score=" + score +
                    '}';
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

            this.grid = new CellType[height][width];
            for (int i = 0; i < height; i++) {
                System.arraycopy(grid[i], 0, this.grid[i], 0, grid.length);
            }

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
                            bomberman.incrementTotalAvailableBombs();
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

        public int accessiblePlacesFor(int id) {
            for (Bomberman bomberman : bombermen) {
                if (bomberman.getId() == id) {
                    int x = bomberman.getCell().getX();
                    int y = bomberman.getCell().getY();

                    return runFloodFillAvaliablePlacesCalculator(y, x);
                }
            }

            throw new IllegalStateException("Unknown bomberman with id=" + id);
        }

        private int runFloodFillAvaliablePlacesCalculator(int i, int j) {
            return runFloodFillAvaliablePlacesCalculator(new boolean[height][width], i, j);
        }

        private int runFloodFillAvaliablePlacesCalculator(boolean[][] mark, int i, int j) {
            int accessiblePlaces = 1;
            mark[i][j] = true;

            if (i - 1 >= 0 &&
                    !mark[i - 1][j] &&
                    grid[i - 1][j] == CellType.FLOOR &&
                    bombsGrid[i - 1][j] == null) {

                accessiblePlaces += runFloodFillAvaliablePlacesCalculator(mark, i - 1, j);
            }

            if (i + 1 < height &&
                    !mark[i + 1][j] &&
                    grid[i + 1][j] == CellType.FLOOR &&
                    bombsGrid[i + 1][j] == null) {

                accessiblePlaces += runFloodFillAvaliablePlacesCalculator(mark, i + 1, j);
            }

            if (j - 1 >= 0 &&
                    !mark[i][j - 1] &&
                    grid[i][j - 1] == CellType.FLOOR &&
                    bombsGrid[i][j - 1] == null) {

                accessiblePlaces += runFloodFillAvaliablePlacesCalculator(mark, i, j - 1);
            }

            if (j + 1 < width &&
                    !mark[i][j + 1] &&
                    grid[i][j + 1] == CellType.FLOOR &&
                    bombsGrid[i][j + 1] == null) {

                accessiblePlaces += runFloodFillAvaliablePlacesCalculator(mark, i, j + 1);
            }

            return accessiblePlaces;
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
                    for (Bomberman bomberman : bombermen) {
                        if (bomberman.getId() == owner) {
                            bomberman.incrementTotalAvailableBombs();
                            break;
                        }
                    }
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

        public Bomberman[] getBombermen() {
            return bombermen.toArray(new Bomberman[bombermen.size()]);
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

        private int totalAvailableBombs;

        public Bomberman(Bomberman other) {

            this.id = other.getId();
            this.cell = new Cell(other.getCell().getX(), other.getCell().getY());
            this.bombsToPlace = other.getBombsToPlace();
            this.explosionRange = other.getExplosionRange();
            this.totalAvailableBombs = other.getTotalAvailableBombs();
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
            this.totalAvailableBombs = 1;
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

        public Cell getCell() {
            return cell;
        }

        public void setCell(Cell cell) {
            this.cell = cell;
        }

        public void incrementTotalAvailableBombs() {
            this.totalAvailableBombs++;
        }

        public int getTotalAvailableBombs() {
            return totalAvailableBombs;
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
            return "StaticEntity{" +
                    "cell=" + cell +
                    ", entityType=" + entityType +
                    '}';
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
