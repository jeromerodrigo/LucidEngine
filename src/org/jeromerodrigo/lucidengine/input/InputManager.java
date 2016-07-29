package org.jeromerodrigo.lucidengine.input;

import org.dyn4j.geometry.Vector2;
import org.jeromerodrigo.lucidengine.ai.Actor;
import org.lwjgl.input.Keyboard;

public final class InputManager {

	private static Vector2 movement = new Vector2(0, 0);

	public static void processInput(final Actor actor) {

		if (Keyboard.getEventKeyState())
			return;

		movement.x = 0;
		movement.y = 0;

		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			movement.y--;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			movement.x++;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			movement.y++;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			movement.x--;
		}

		actor.move(movement);

	}
}
