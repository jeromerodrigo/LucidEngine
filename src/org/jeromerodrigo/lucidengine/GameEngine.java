package org.jeromerodrigo.lucidengine;

import org.jeromerodrigo.lucidengine.game.Game;

public interface GameEngine {

    void start(Game game);

    int getDisplayHeight();

    int getDisplayWidth();
}
