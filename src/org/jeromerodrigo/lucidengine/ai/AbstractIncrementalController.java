package org.jeromerodrigo.lucidengine.ai;

import java.util.LinkedList;
import java.util.List;

import org.dyn4j.geometry.Vector2;
import org.jeromerodrigo.lucidengine.tiledmap.TiledMap;

/**
 * An abstract implementation of a controller that is aware of the collision
 * objects in the map and is capable of generating a list of steps the AI can
 * take to reach its destination.
 *
 * @author Jerome
 * @url http://www.cokeandcode.com/main/tutorials/path-finding/
 *
 */

public abstract class AbstractIncrementalController implements Controller {

    private final List<Vector2> steps;

    public AbstractIncrementalController(final TiledMap map) {
        steps = new LinkedList<Vector2>();
    }

    @Override
    public void control(final Controllable controllable) {

    }

}
