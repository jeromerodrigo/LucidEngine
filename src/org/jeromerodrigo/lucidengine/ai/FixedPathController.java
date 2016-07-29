package org.jeromerodrigo.lucidengine.ai;

import java.util.ArrayList;
import java.util.List;

import org.dyn4j.geometry.Vector2;

public final class FixedPathController implements Controller {

	private final List<Vector2> path;

	public FixedPathController() {

		path = new ArrayList<Vector2>();

		path.add(Controllable.UP);
		path.add(Controllable.DOWN);

	}

	@Override
	public void control(final Controllable actor) {

	}

}
