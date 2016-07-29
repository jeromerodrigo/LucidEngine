package org.jeromerodrigo.lucidengine.tiledmap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jeromerodrigo.lucidengine.Camera;
import org.jeromerodrigo.lucidengine.Drawable;
import org.jeromerodrigo.lucidengine.graphics.SpriteBatch;

public abstract class MapRenderer implements Drawable {

    private static final Logger LOG = LogManager.getLogger(MapRenderer.class);

    protected final TiledMap map;

    protected final Camera camera;

    public MapRenderer(final TiledMap map, final Camera cam) {

        if (!map.getOrientation().equals(getRenderOrientation())) {
            LOG.fatal("Map orientation not supported!");
        }

        this.map = map;
        camera = cam;

    }

    protected abstract TiledMap.Orientation getRenderOrientation();

    protected abstract void render(SpriteBatch batch, float xRender,
            float yRender, int mapX, int mapY, int tilesX, int tilesY);

}
