package player.engine;

import player.Player.Action;

public interface GameEngine {

    /**
     * Computes the very first state, which usually is a match set up (build maps, boards, place players into their
     * start position...)
     */
    void start();

    /**
     * Computes the new match state based on the players' actions
     */
    void run(Action[] playerActions, Action[] opponentActions);

    /**
     * 
     * @return the match winner, {@code Winner.ON_GOING} otherwise
     */
    Winner getWinner();

    /**
     * Player input stream.
     */
    int playerInput();

    /**
     * Opponent input stream.
     */
    int opponentInput();

    /**
     * Returns the current player score
     */
    int getPlayerScore();

    /**
     * Returns the current opponent score
     */
    int getOpponentScore();

    /**
     * 
     * Returns the number of executed rounds
     */
    int getNumberOfRounds();
}
