package ch.plaintext.schuetu.service;

/**
 * Interface for components that can be connected to a Game
 */
public interface GameConnectable {

    Game getGame();

    void setGame(Game game);

}
