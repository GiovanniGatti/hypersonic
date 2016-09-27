package player;

import static player.Player.BoxContent.EXTRA_BOMB;
import static player.Player.BoxContent.EXTRA_RANGE;
import static player.Player.BoxContent.NO_ITEM;
import static player.Player.CellType.BLOCK;
import static player.Player.CellType.BOX;
import static player.Player.CellType.FLOOR;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import player.Player.Bomb;
import player.Player.Bomberman;
import player.Player.Box;
import player.Player.Cell;
import player.Player.InputRepository;
import player.Player.InputSupplier;
import player.Player.Item;
import player.Player.ItemType;

@DisplayName("An input repository")
class InputRepositoryTest implements WithAssertions {

    private InputSupplierState state;

    @BeforeEach
    void setUp() {
        state = new InputSupplierState();
    }

    @Test
    @DisplayName("parses grid correctly")
    void parsesGridCorrectly() {
        InputSupplier inputSupplier =
                state.withGrid(
                        ".0.",
                        "0X0",
                        ".0.")
                        .withBombermans(anyBombermanWith(0, 0, 0), anyBombermanWith(2, 2, 0))
                        .toInputSupplier();

        InputRepository repository = new InputRepository(inputSupplier);
        repository.update();

        assertThat(repository.getGrid()[0][0]).isEqualByComparingTo(FLOOR);
        assertThat(repository.getGrid()[0][1]).isEqualByComparingTo(BOX);
        assertThat(repository.getGrid()[0][2]).isEqualByComparingTo(FLOOR);
        assertThat(repository.getGrid()[1][0]).isEqualByComparingTo(BOX);
        assertThat(repository.getGrid()[1][1]).isEqualByComparingTo(BLOCK);
        assertThat(repository.getGrid()[1][2]).isEqualByComparingTo(BOX);
        assertThat(repository.getGrid()[2][0]).isEqualByComparingTo(FLOOR);
        assertThat(repository.getGrid()[2][1]).isEqualByComparingTo(BOX);
        assertThat(repository.getGrid()[2][2]).isEqualByComparingTo(FLOOR);
    }

    @Test
    @DisplayName("maps opponents and player")
    void returnOpponents() {
        InputSupplier inputSupplier =
                state.withMyId(1)
                        .withBombermans(
                                new Bomberman(0, 2, 0, 1, 3),
                                new Bomberman(1, 3, 0, 1, 3),// player
                                new Bomberman(2, 4, 0, 1, 3))
                        .toInputSupplier();

        InputRepository repository = new InputRepository(inputSupplier);
        repository.update();

        assertThat(repository.getOpponents())
                .containsOnly(new Bomberman(0, 2, 0, 1, 3), new Bomberman(2, 4, 0, 1, 3));

        assertThat(repository.getPlayer()).isEqualTo(new Bomberman(1, 3, 0, 1, 3));
    }

    @Test
    @DisplayName("returns all bombs")
    void returnBombs() {
        InputSupplier inputSupplier =
                state.withBombs(
                        new Bomb(0, 1, 2, 3, 3),
                        new Bomb(1, 1, 2, 3, 3))
                        .toInputSupplier();

        InputRepository repository = new InputRepository(inputSupplier);
        repository.update();

        assertThat(repository.getBombs())
                .containsOnly(new Bomb(0, 1, 2, 3, 3), new Bomb(1, 1, 2, 3, 3));
    }

    @Test
    @DisplayName("maps bombs to players")
    void bombPlayerMapping() {
        InputSupplier inputSupplier =
                state
                        .withBombermans(
                                new Bomberman(0, 2, 0, 1, 3),
                                new Bomberman(1, 3, 0, 1, 3),
                                new Bomberman(2, 4, 0, 1, 3))
                        .withBombs(
                                new Bomb(0, 1, 2, 3, 3),
                                new Bomb(0, 1, 3, 4, 3),
                                new Bomb(1, 1, 2, 3, 3))
                        .toInputSupplier();

        InputRepository repository = new InputRepository(inputSupplier);
        repository.update();

        assertThat(repository.getBombsFor(0))
                .containsOnly(new Bomb(0, 1, 2, 3, 3), new Bomb(0, 1, 3, 4, 3));

        assertThat(repository.getBombsFor(1))
                .containsOnly(new Bomb(1, 1, 2, 3, 3));

        assertThat(repository.getBombsFor(2)).isEmpty();
    }

    @Test
    @DisplayName("lists all boxes")
    void listsAllBoxes() {
        InputSupplier inputSupplier =
                state.withGrid(
                        ".0.",
                        "1.2",
                        ".0.")
                        .toInputSupplier();

        InputRepository repository = new InputRepository(inputSupplier);
        repository.update();

        assertThat(repository.getBoxes())
                .containsOnly(
                        new Box(NO_ITEM, 1, 0),
                        new Box(EXTRA_RANGE, 0, 1),
                        new Box(EXTRA_BOMB, 2, 1),
                        new Box(NO_ITEM, 1, 2));
    }

    @Test
    @DisplayName("lists all items")
    void listsAllItems() {
        InputSupplier inputSupplier =
                state.withItems(
                        new Item(ItemType.EXTRA_RANGE, 0, 1),
                        new Item(ItemType.EXTRA_BOMB, 2, 1))
                        .toInputSupplier();

        InputRepository repository = new InputRepository(inputSupplier);
        repository.update();

        assertThat(repository.getItems())
                .containsOnly(
                        new Item(ItemType.EXTRA_RANGE, 0, 1),
                        new Item(ItemType.EXTRA_BOMB, 2, 1));
    }

    @Test
    @DisplayName("compute the correct explosion range when blocked by box")
    void computeExplosionRange() {
        InputSupplier inputSupplier =
                state.withGrid(
                        "....",
                        ".1X.",
                        "....")
                        .toInputSupplier();

        InputRepository repository = new InputRepository(inputSupplier);
        repository.update();

        List<Cell> cells = new ArrayList<>();
        repository.acceptForExplosionRange(new Cell(1, 1), 3, (x, y) -> cells.add(new Cell(x, y)));

        assertThat(cells)
                .containsOnly(new Cell(1, 0), new Cell(0, 1), new Cell(1, 2));
    }

    @Test
    @DisplayName("compute the correct explosion range (generic case)")
    void computeExplosionRange2() {
        InputSupplier inputSupplier =
                state.withGrid(
                        "...0",
                        ".X1X",
                        ".2..",
                        ".X.X",
                        "0102")
                        .withMyId(0)
                        .withBombs(new Bomb(0, 2, 0, 8, 3))
                        .withBombermans(new Bomberman(0, 2, 0, 1, 3))
                        .toInputSupplier();

        InputRepository repo = new InputRepository(inputSupplier);
        repo.update();

        List<Cell> boxes =
                repo.getBoxes().stream()
                        .map(Box::getCell)
                        .collect(Collectors.toCollection(ArrayList::new));

        List<Cell> bombs = repo.getBombs().stream()
                .map(Bomb::getCell).collect(Collectors.toCollection(ArrayList::new));

        Set<Cell> toDiscard = new HashSet<>();

        for (Cell box : boxes) {
            repo.acceptForExplosionRange(box, 3, (x, y) -> {
                if (bombs.contains(new Cell(x, y))) {
                    toDiscard.add(box);
                }
            });
        }

        boxes.removeAll(toDiscard);

        int[][] gridEvaluation = new int[repo.getHeight()][repo.getWidth()];
        for (Cell box : boxes) {
            repo.acceptForExplosionRange(box, 3, (x, y) -> gridEvaluation[y][x]++);
        }

        List<Cell> cells = new ArrayList<>();
        repo.acceptForExplosionRange(new Cell(2, 0), 3, (x, y) -> cells.add(new Cell(x, y)));

        assertThat(cells)
                .containsOnly(new Cell(0, 0), new Cell(1, 0));
    }

    @Test
    @DisplayName("simulates an explosion without obstruction")
    void explosionGoesWithoutObstruction() {
        InputSupplier inputSupplier =
                state.withGrid(
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        ".........")
                        .withMyId(0)
                        .withBombermans(new Bomberman(0, 0, 0, 1, 3))
                        .toInputSupplier();

        InputRepository repository = new InputRepository(inputSupplier);
        repository.update();

        List<Cell> collected = new ArrayList<>();

        repository.acceptForExplosionRange(new Cell(3, 3), 3, (x, y) -> collected.add(new Cell(x, y)));

        assertThat(collected).containsOnly(
                new Cell(1, 3), new Cell(2, 3),
                new Cell(4, 3), new Cell(5, 3),
                new Cell(3, 2), new Cell(3, 1),
                new Cell(3, 4), new Cell(3, 5));
    }

    @Test
    @DisplayName("simulates an explosion with a box obstructing")
    void explosionGoesWithBoxObstruction() {
        InputSupplier inputSupplier =
                state.withGrid(
                        ".........",
                        ".........",
                        "...0.....",
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        ".........")
                        .withMyId(0)
                        .withBombermans(new Bomberman(0, 0, 0, 1, 3))
                        .toInputSupplier();

        InputRepository repository = new InputRepository(inputSupplier);
        repository.update();

        List<Cell> collected = new ArrayList<>();

        repository.acceptForExplosionRange(new Cell(3, 3), 3, (x, y) -> collected.add(new Cell(x, y)));

        assertThat(collected).containsOnly(
                new Cell(1, 3), new Cell(2, 3),
                new Cell(4, 3), new Cell(5, 3),
                new Cell(3, 4), new Cell(3, 5));
    }

    @Test
    @DisplayName("simulates an explosion with an wall obstructing")
    void explosionGoesWithWallObstruction() {
        InputSupplier inputSupplier =
                state.withGrid(
                        ".........",
                        ".........",
                        ".........",
                        "..X......",
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        ".........")
                        .withMyId(0)
                        .withBombermans(new Bomberman(0, 0, 0, 1, 3))
                        .toInputSupplier();

        InputRepository repository = new InputRepository(inputSupplier);
        repository.update();

        List<Cell> collected = new ArrayList<>();

        repository.acceptForExplosionRange(new Cell(3, 3), 3, (x, y) -> collected.add(new Cell(x, y)));

        assertThat(collected).containsOnly(
                new Cell(4, 3), new Cell(5, 3),
                new Cell(3, 2), new Cell(3, 1),
                new Cell(3, 4), new Cell(3, 5));
    }

    @Test
    @DisplayName("simulates an explosion with a player obstructing")
    void explosionGoesWithPlayerObstruction() {
        InputSupplier inputSupplier =
                state.withGrid(
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        ".........")
                        .withMyId(0)
                        .withBombermans(new Bomberman(0, 4, 3, 1, 3))
                        .toInputSupplier();

        InputRepository repository = new InputRepository(inputSupplier);
        repository.update();

        List<Cell> collected = new ArrayList<>();

        repository.acceptForExplosionRange(new Cell(3, 3), 3, (x, y) -> collected.add(new Cell(x, y)));

        assertThat(collected).containsOnly(
                new Cell(1, 3), new Cell(2, 3),
                new Cell(3, 2), new Cell(3, 1),
                new Cell(3, 4), new Cell(3, 5));
    }

    @Test
    @DisplayName("simulates an explosion with an item obstructing")
    void explosionGoesWithItemObstruction() {
        InputSupplier inputSupplier =
                state.withGrid(
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        ".........",
                        ".........")
                        .withMyId(0)
                        .withItems(new Item(ItemType.EXTRA_BOMB, 3, 4))
                        .toInputSupplier();

        InputRepository repository = new InputRepository(inputSupplier);
        repository.update();

        List<Cell> collected = new ArrayList<>();

        repository.acceptForExplosionRange(new Cell(3, 3), 3, (x, y) -> collected.add(new Cell(x, y)));

        assertThat(collected).containsOnly(
                new Cell(1, 3), new Cell(2, 3),
                new Cell(4, 3), new Cell(5, 3),
                new Cell(3, 2), new Cell(3, 1));
    }

    private static Bomberman anyBombermanWith(int id, int x, int y) {
        return new Bomberman(id, x, y, 1, 3);
    }

    private static Bomb anyBombAt(int x, int y) {
        return new Bomb(1, x, y, 8, 3);
    }
}