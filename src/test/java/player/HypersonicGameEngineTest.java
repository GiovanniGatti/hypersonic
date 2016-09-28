package player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import player.Player.Bomb;
import player.Player.Bomberman;
import player.Player.Cell;
import player.Player.CellType;
import player.Player.HypersonicGameEngine;
import player.Player.Item;
import player.Player.ItemType;
import player.Player.SimplifiedAction;

@DisplayName("A hypersonic game engine")
public class HypersonicGameEngineTest implements WithAssertions {

    @Test
    @DisplayName("creates an item it contains in the place of a destroyed box")
    void createsAnItemItContainsInThePlaceOfADestroyedBox() {
        CellType[][] grid =
                createGrid(
                        "...",
                        "1.2",
                        "...");

        List<Bomb> bombs = Collections.singletonList(new Bomb(0, 1, 1, 1, 2));
        List<Item> items = Collections.emptyList();
        Bomberman[] bombermen = new Bomberman[0];

        HypersonicGameEngine engine = new HypersonicGameEngine(grid, bombs, items, bombermen);

        engine.perform();

        assertThat(engine.getItems())
                .containsOnly(
                        new Item(ItemType.EXTRA_BOMB, 2, 1),
                        new Item(ItemType.EXTRA_RANGE, 0, 1));

        assertThat(engine.getBombs()).isEmpty();
    }

    @Test
    @DisplayName("destroys an item in explosion")
    void destroysAnItemInExplosion() {
        CellType[][] grid =
                createGrid(
                        "...",
                        "...",
                        "...");

        List<Bomb> bombs = Collections.singletonList(new Bomb(0, 1, 1, 1, 2));
        List<Item> items = Arrays.asList(new Item(ItemType.EXTRA_BOMB, 2, 1), new Item(ItemType.EXTRA_RANGE, 0, 1));
        Bomberman[] bombermen = new Bomberman[0];

        HypersonicGameEngine engine = new HypersonicGameEngine(grid, bombs, items, bombermen);

        engine.perform();

        assertThat(engine.getItems()).isEmpty();
        assertThat(engine.getBombs()).isEmpty();
    }

    @Test
    @DisplayName("triggers a second explosion")
    void triggersASecondExplosion() {
        CellType[][] grid =
                createGrid(
                        "...",
                        "...",
                        "...");

        List<Bomb> bombs = Arrays.asList(new Bomb(0, 1, 1, 1, 2), new Bomb(1, 0, 1, 8, 2));
        List<Item> items = Collections.emptyList();
        Bomberman[] bombermen = new Bomberman[0];

        HypersonicGameEngine engine = new HypersonicGameEngine(grid, bombs, items, bombermen);

        engine.perform();

        assertThat(engine.getBombs()).isEmpty();
    }

    @Test
    @DisplayName("does not let explosions pass through items, boxes or blocks")
    void doesNotLetExplosionsPassThroughItemsBoxesOrBlocks() {
        CellType[][] grid =
                createGrid(
                        "0...",
                        "0...",
                        "..X0",
                        "....",
                        "0...");

        List<Bomb> bombs = Collections.singletonList(new Bomb(0, 0, 2, 1, 5));
        List<Item> items = Collections.singletonList(new Item(ItemType.EXTRA_BOMB, 0, 3));
        Bomberman[] bombermen = new Bomberman[0];

        HypersonicGameEngine engine = new HypersonicGameEngine(grid, bombs, items, bombermen);

        engine.perform();

        assertThat(engine.getGrid()[0][0]).isEqualTo(CellType.BOX_WITH_NO_ITEM);
        assertThat(engine.getGrid()[1][0]).isEqualTo(CellType.FLOOR);
        assertThat(engine.getGrid()[4][0]).isEqualTo(CellType.BOX_WITH_NO_ITEM);

        assertThat(engine.getGrid()[2][2]).isEqualTo(CellType.BLOCK);
        assertThat(engine.getGrid()[2][3]).isEqualTo(CellType.BOX_WITH_NO_ITEM);

        assertThat(engine.getItems()).isEmpty();
        assertThat(engine.getBombs()).isEmpty();
    }

    @Test
    @DisplayName("kills players hit by explosions")
    void killsPlayersHitByExplosions() {
        CellType[][] grid =
                createGrid(
                        "...",
                        "...",
                        "...");

        List<Bomb> bombs = Collections.singletonList(new Bomb(0, 1, 1, 1, 5));
        List<Item> items = Collections.emptyList();
        Bomberman[] bombermen = new Bomberman[] { new Bomberman(0, 0, 1, 0, 0), new Bomberman(1, 0, 2, 0, 0) };

        HypersonicGameEngine engine = new HypersonicGameEngine(grid, bombs, items, bombermen);

        engine.perform(SimplifiedAction.MOVE_UP, SimplifiedAction.MOVE_UP);

        assertThat(engine.isBombermenDead(0)).isTrue();
        assertThat(engine.getBombermen()[0]).extracting(Bomberman::getCell).containsOnly(new Cell(0, 1));

        assertThat(engine.isBombermenDead(1)).isFalse();
        assertThat(engine.getBombermen()[1]).extracting(Bomberman::getCell).containsOnly(new Cell(0, 1));
    }

    @Test
    @DisplayName("lets the player place a bomb")
    void letThePlayerPlaceABomb() {
        CellType[][] grid =
                createGrid(
                        "...",
                        "...",
                        "...");

        List<Bomb> bombs = Collections.emptyList();
        List<Item> items = Collections.emptyList();
        Bomberman[] bombermen = new Bomberman[] { new Bomberman(0, 0, 1, 1, 0) };

        HypersonicGameEngine engine = new HypersonicGameEngine(grid, bombs, items, bombermen);

        engine.perform(SimplifiedAction.BOMB_AND_MOVE_UP);

        assertThat(engine.getBombermen()[0]).extracting(Bomberman::getCell).containsOnly(new Cell(0, 0));
        assertThat(engine.getBombs()).containsOnly(new Bomb(0, 0, 1, 8, 0));
    }

    @Test
    @DisplayName("does not let the player place a bomb that he doesn't have")
    void doesNotLetThePlayerPlaceABombThatHeDoesntHave() {
        CellType[][] grid =
                createGrid(
                        "...",
                        "...",
                        "...");

        List<Bomb> bombs = Collections.emptyList();
        List<Item> items = Collections.emptyList();
        Bomberman[] bombermen = new Bomberman[] { new Bomberman(0, 0, 1, 0, 0) };

        HypersonicGameEngine engine = new HypersonicGameEngine(grid, bombs, items, bombermen);

        engine.perform(SimplifiedAction.BOMB_AND_STAY);

        assertThat(engine.getBombs()).isEmpty();
    }

    @Test
    @DisplayName("does not let the player move over a box")
    void doesNotLetThePlayerMoveOverABox() {
        CellType[][] grid =
                createGrid(
                        "...",
                        ".1.",
                        "...");

        List<Bomb> bombs = Collections.emptyList();
        List<Item> items = Collections.emptyList();
        Bomberman[] bombermen = new Bomberman[] { new Bomberman(0, 0, 1, 0, 0) };

        HypersonicGameEngine engine = new HypersonicGameEngine(grid, bombs, items, bombermen);

        engine.perform(SimplifiedAction.MOVE_RIGHT);

        assertThat(engine.getBombermen()[0]).extracting(Bomberman::getCell).containsOnly(new Cell(0, 1));
    }

    private CellType[][] createGrid(String... grid) {
        CellType[][] cellTypes = new CellType[grid.length][grid[0].length()];

        for (int i = 0; i < grid.length; i++) {
            String row = grid[i];
            for (int j = 0; j < row.length(); j++) {
                char cell = row.charAt(j);
                if (cell == '.') {
                    cellTypes[i][j] = CellType.FLOOR;
                } else if (cell == 'X') {
                    cellTypes[i][j] = CellType.BLOCK;
                } else {
                    if (cell == '0') {
                        cellTypes[i][j] = CellType.BOX_WITH_NO_ITEM;
                    } else if (cell == '1') {
                        cellTypes[i][j] = CellType.BOX_WITH_EXTRA_RANGE;
                    } else {
                        cellTypes[i][j] = CellType.BOX_WITH_EXTRA_BOMB;
                    }
                }
            }
        }

        return cellTypes;
    }
}