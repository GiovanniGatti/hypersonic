package player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import player.Player.GeneticAI;
import player.Player.InputRepository;
import player.Player.InputSupplier;

public class GeneticAITest {

    private InputSupplierState state;

    @BeforeEach
    void setUp() {
        state = new InputSupplierState();
    }

    @Test
    void tmp() {
        InputSupplier inputSupplier =
                state.withGrid(
                        "..0",
                        ".X.",
                        "1..")
                        .withMyId(0)
                        .withBombermans(new Player.Bomberman(0, 0, 0, 1, 3))
                        .toInputSupplier();

        InputRepository repository = new InputRepository(inputSupplier);
        repository.update();
        GeneticAI ai = new GeneticAI(repository);

        long currentTimeMillis = System.currentTimeMillis();
        ai.play();

        System.out.println(System.currentTimeMillis() - currentTimeMillis);
    }

    @Test
    void completle_blocked_should_never_place_a_bomb() {
        InputSupplier inputSupplier =
                state.withGrid(
                        ".X0",
                        "XX.",
                        "1..")
                        .withMyId(0)
                        .withBombermans(new Player.Bomberman(0, 0, 0, 1, 3))
                        .toInputSupplier();

        InputRepository repository = new InputRepository(inputSupplier);
        repository.update();
        GeneticAI ai = new GeneticAI(repository);

        long currentTimeMillis = System.currentTimeMillis();
        ai.play();

        System.out.println(System.currentTimeMillis() - currentTimeMillis);
    }
}