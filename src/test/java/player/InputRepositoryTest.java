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
    @DisplayName("compute the correct distances from player")
    void computeCorrectDistances() {
        InputSupplier inputSupplier =
                state.withGrid(
                        "....",
                        ".2X.",
                        "1..1")
                        .withMyId(0)
                        .withBombermans(anyBombermanWith(0, 0, 0), anyBombermanWith(1, 2, 2))
                        .withBombs(anyBombAt(3, 0))
                        .toInputSupplier();

        InputRepository repository = new InputRepository(inputSupplier);
        repository.update();

        assertThat(repository.distanceTo(0, 0)).isEqualTo(0);
        assertThat(repository.distanceTo(0, 1)).isEqualTo(1);
        assertThat(repository.distanceTo(0, 2)).isEqualTo(Integer.MAX_VALUE);

        assertThat(repository.distanceTo(1, 0)).isEqualTo(1);
        assertThat(repository.distanceTo(1, 1)).isEqualTo(Integer.MAX_VALUE);
        assertThat(repository.distanceTo(1, 2)).isEqualTo(Integer.MAX_VALUE);

        assertThat(repository.distanceTo(2, 0)).isEqualTo(2);
        assertThat(repository.distanceTo(2, 1)).isEqualTo(Integer.MAX_VALUE);
        assertThat(repository.distanceTo(2, 2)).isEqualTo(Integer.MAX_VALUE);

        assertThat(repository.distanceTo(3, 0)).isEqualTo(Integer.MAX_VALUE);
        assertThat(repository.distanceTo(3, 1)).isEqualTo(Integer.MAX_VALUE);
        assertThat(repository.distanceTo(3, 2)).isEqualTo(Integer.MAX_VALUE);
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
        repository.acceptForExplosionRange(new Cell(1, 1), (x, y) -> cells.add(new Cell(x, y)));

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
                        .map(Box::asCell)
                        .collect(Collectors.toCollection(ArrayList::new));

        List<Cell> bombs = repo.getBombs().stream()
                .map(Bomb::asCell).collect(Collectors.toCollection(ArrayList::new));

        Set<Cell> toDiscard = new HashSet<>();

        for (Cell box : boxes) {
            repo.acceptForExplosionRange(box, (x, y) -> {
                if (bombs.contains(new Cell(x, y))) {
                    toDiscard.add(box);
                }
            });
        }

        boxes.removeAll(toDiscard);

        int[][] gridEvaluation = new int[repo.getHeight()][repo.getWidth()];
        for (Cell box : boxes) {
            repo.acceptForExplosionRange(box, (x, y) -> gridEvaluation[y][x]++);
        }

        List<Cell> cells = new ArrayList<>();
        repo.acceptForExplosionRange(new Cell(2, 0), (x, y) -> cells.add(new Cell(x, y)));

        assertThat(cells)
                .containsOnly(new Cell(0, 0), new Cell(1, 0));
    }

    @Test
    @DisplayName("compute flood fill successfully")
    void computeFloodFill() {
        InputSupplier inputSupplier =
                state.withGrid(
                        "....2...2....",
                        ".X1X0X.X0X1X.",
                        "1.0.2...2.0.1",
                        ".X.X.X.X.X.X.",
                        ".1.0.2.2.0.1.",
                        ".X.X1X.X1X.X.",
                        ".1.0.2.2.0.1.",
                        ".X.X.X.X.X.X.",
                        "1.0.2...2.0.1",
                        ".X1X0X.X0X1X.",
                        "....2...2....")
                        .withBombermans(new Bomberman(0, 12, 10, 1, 3))
                        .toInputSupplier();

        InputRepository repository = new InputRepository(inputSupplier);
        repository.update();

        assertThat(repository.distanceTo(12, 10)).isEqualTo(0);
        assertThat(repository.distanceTo(12, 9)).isEqualTo(1);
        assertThat(repository.distanceTo(12, 8)).isEqualTo(Integer.MAX_VALUE);
        assertThat(repository.distanceTo(12, 7)).isEqualTo(Integer.MAX_VALUE);

        assertThat(repository.distanceTo(9, 10)).isEqualTo(3);
        assertThat(repository.distanceTo(8, 10)).isEqualTo(Integer.MAX_VALUE);
        assertThat(repository.distanceTo(7, 10)).isEqualTo(Integer.MAX_VALUE);

        assertThat(repository.getTargetableBoxes())
                .containsOnly(new Cell(8, 10), new Cell(10, 9), new Cell(12, 8));
    }

    private static Bomberman anyBombermanWith(int id, int x, int y) {
        return new Bomberman(id, x, y, 1, 3);
    }

    private static Bomb anyBombAt(int x, int y) {
        return new Bomb(1, x, y, 8, 3);
    }
}