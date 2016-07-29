package org.jeromerodrigo.lucidengine.ai;

import org.dyn4j.geometry.Vector2;

public interface Actor {

	Vector2 DOWN = new Vector2(0, -1);
	Vector2 LEFT = new Vector2(-1, 0);
	Vector2 RIGHT = new Vector2(1, 0);
	Vector2 UP = new Vector2(0, 1);

	void move(Vector2 movement);

	void say(String message);

}
