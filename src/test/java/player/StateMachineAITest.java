package player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import player.Player.InputRepository;
import player.Player.StateMachineAI;

class StateMachineAITest {

    private InputSupplierState state;

    @BeforeEach
    void setUp() {
        state = new InputSupplierState();
    }

    @Test
    void tmp() {
//        state
//                .withGrid(
//                        ".......",
//                        ".......",
//                        ".......",
//                        "...0...",
//                        "...0...",
//                        ".......",
//                        "......."
//                );

        InputRepository repository = new InputRepository(state.toInputSupplier());

        StateMachineAI ai = new StateMachineAI(repository);

        ai.updateRepository();

        ai.play();
    }
}