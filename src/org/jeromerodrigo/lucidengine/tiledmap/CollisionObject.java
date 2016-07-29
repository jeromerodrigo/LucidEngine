package org.jeromerodrigo.lucidengine.tiledmap;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Mass.Type;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;

public class CollisionObject extends Body {

    /**
     * Creates a rectangular collision object. Used to be added to the physics
     * engine/world.
     * 
     * @param x
     *            the x component of location in the physics world
     * @param y
     *            the y component of location in the physics world
     * @param width
     *            the width of the rectangle
     * @param height
     *            the height of the rectangle
     * @param tileWidth
     *            the width of a map tile
     * @param tileHeight
     *            the height of a map tile
     */

    public CollisionObject(final float x, final float y, final float width,
            final float height, final int tileWidth, final int tileHeight) {
        super();

        final Polygon poly = Geometry.createPolygon(new Vector2(0, 0),
                new Vector2(width, 0), new Vector2(width, height), new Vector2(
                        0, height));

        addFixture(poly);

        // Needed to subtract by half of TILE_SZ, not sure why
        translate(x - tileWidth / 2, y - tileHeight / 2);

        setMass(Type.INFINITE);
    }

}
