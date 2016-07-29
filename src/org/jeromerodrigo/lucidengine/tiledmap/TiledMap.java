package org.jeromerodrigo.lucidengine.tiledmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a Tiled Map. This class is used to store metadata related to the
 * map.
 * 
 * @author Jerome
 *
 */

public class TiledMap {

    public static enum Orientation {
        ISOMETRIC, ORTHOGONAL
    }

    private List<CollisionObject> collisionObjects;

    private final int height;

    private final Orientation orientation;

    private final int tileHeight;
    private final Map<String, int[][]> tileLayers;
    private final List<Tileset> tilesets;

    private final int tileWidth;

    private final int width;

    public TiledMap(final Orientation orientation, final int width,
            final int height, final int tileWidth, final int tileHeight) {

        this.orientation = orientation;

        collisionObjects = new ArrayList<CollisionObject>();

        tilesets = new ArrayList<Tileset>();

        tileLayers = new HashMap<String, int[][]>();

        this.width = width;
        this.height = height;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;

    }

    protected void addTileLayer(final String layerName, final int[][] tileData) {
        tileLayers.put(layerName, tileData);
    }

    protected void addTileset(final Tileset tileset) {
        tilesets.add(tileset);
    }

    public List<CollisionObject> getCollisionObjects() {
        return collisionObjects;
    }

    public int getHeight() {
        return height;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public int[][] getTilesAt(final String layerName) {
        return tileLayers.get(layerName);
    }

    public Set<String> getTileLayerNames() {
        return tileLayers.keySet();
    }

    public List<Tileset> getTilesets() {
        return tilesets;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getWidth() {
        return width;
    }

    protected void setCollisionObjects(final List<CollisionObject> objs) {
        collisionObjects = objs;
    }

}
