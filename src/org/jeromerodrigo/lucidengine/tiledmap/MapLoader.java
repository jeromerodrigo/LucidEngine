package org.jeromerodrigo.lucidengine.tiledmap;

import org.dyn4j.dynamics.World;

public interface MapLoader {

    /**
     * Loads a tiled map from the source provided
     * 
     * @param path
     *            The url source of the tiled map
     * @return A TiledMap
     */

    TiledMap loadMap(String path);

    /**
     * Loads the collision objects from the Map into the physics engine
     * 
     * @param map
     *            The TiledMap that holds the collision objects
     * @param physics
     *            The physics engine
     */

    void loadMapToPhysics(TiledMap map, World physics);

}
