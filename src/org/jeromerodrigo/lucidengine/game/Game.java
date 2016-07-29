package org.jeromerodrigo.lucidengine.game;

import org.jeromerodrigo.lucidengine.Updateable;

public interface Game extends Updateable {
	
	String getName();

	void dispose();

	void processInput();
	
	void render();

}
