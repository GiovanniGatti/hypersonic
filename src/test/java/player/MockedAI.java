package player;

import java.util.Collections;
import java.util.Map;

import org.mockito.Mockito;

import com.google.common.base.MoreObjects;

import player.Player.AI;
import player.Player.Action;

public final class MockedAI {

    private MockedAI() {
        // Utility class
    }

    public static AI any() {
        return newBuilder().build();
    }

    public static AI anyWithActions(Action... actions) {
        return newBuilder()
                .withActions(actions)
                .build();
    }

    public static AI anyConf(Map<String, Object> conf) {
        return newBuilder()
                .withConf(conf)
                .build();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private Map<String, Object> conf;
        private Action[] actions;

        private Builder() {
            this.conf = Collections.emptyMap();
            this.actions = new Action[] { Mockito.mock(Action.class) };
        }

        public Builder withConf(Map<String, Object> conf) {
            this.conf = conf;
            return this;
        }

        public Builder withActions(Action... actions) {
            this.actions = actions;
            return this;
        }

        AI build() {
            return new MockedArtificialIntelligence(conf, actions);
        }
    }

    private static class MockedArtificialIntelligence extends AI {

        private final Action[] actions;

        private MockedArtificialIntelligence(Map<String, Object> conf, Action[] actions) {
            super(conf, MockedArtificialIntelligence::noOp);
            this.actions = actions;
        }

        @Override
        public Action[] play() {
            return actions;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("conf", getConf())
                    .add("actions", actions)
                    .toString();
        }

        private static void noOp() {
            // ILB
        }
    }
}
