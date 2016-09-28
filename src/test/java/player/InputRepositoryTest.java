package player;

import static player.Player.CellType.BLOCK;
import static player.Player.CellType.BOX_WITH_EXTRA_BOMB;
import static player.Player.CellType.BOX_WITH_EXTRA_RANGE;
import static player.Player.CellType.BOX_WITH_NO_ITEM;
import static player.Player.CellType.FLOOR;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import player.Player.Bomb;
import player.Player.Bomberman;
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
                        ".1.",
                        "2X0",
                        ".0.")
                        .withBombermans(anyBombermanWith(0, 0, 0), anyBombermanWith(2, 2, 0))
                        .toInputSupplier();

        InputRepository repository = new InputRepository(inputSupplier);
        repository.update();

        assertThat(repository.getGrid()[0][0]).isEqualByComparingTo(FLOOR);
        assertThat(repository.getGrid()[0][1]).isEqualByComparingTo(BOX_WITH_EXTRA_RANGE);
        assertThat(repository.getGrid()[0][2]).isEqualByComparingTo(FLOOR);
        assertThat(repository.getGrid()[1][0]).isEqualByComparingTo(BOX_WITH_EXTRA_BOMB);
        assertThat(repository.getGrid()[1][1]).isEqualByComparingTo(BLOCK);
        assertThat(repository.getGrid()[1][2]).isEqualByComparingTo(BOX_WITH_NO_ITEM);
        assertThat(repository.getGrid()[2][0]).isEqualByComparingTo(FLOOR);
        assertThat(repository.getGrid()[2][1]).isEqualByComparingTo(BOX_WITH_NO_ITEM);
        assertThat(repository.getGrid()[2][2]).isEqualByComparingTo(FLOOR);
    }

    @Test
    @DisplayName("maps player")
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

    private static Bomberman anyBombermanWith(int id, int x, int y) {
        return new Bomberman(id, x, y, 1, 3);
    }

    private static Bomb anyBombAt(int x, int y) {
        return new Bomb(1, x, y, 8, 3);
    }
}