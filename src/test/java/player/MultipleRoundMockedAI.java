package player;

import static player.Player.AI;
import static player.Player.Action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import player.MockedAI.Builder;

public class MultipleRoundMockedAI extends AI {

    private final Iterator<AI> rounds;

    public MultipleRoundMockedAI(Builder... rounds) {
        super(Collections.emptyMap(), MultipleRoundMockedAI::noOp);

        List<AI> r = new ArrayList<>();
        for (Builder round : rounds) {
            r.add(round.build());
        }
        this.rounds = r.iterator();
    }

    @Override
    public Action[] play() {
        return rounds.next().play();
    }

    private static void noOp() {
        // ILB
    }
}
