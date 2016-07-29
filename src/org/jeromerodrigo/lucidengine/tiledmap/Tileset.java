package org.jeromerodrigo.lucidengine.tiledmap;

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jeromerodrigo.lucidengine.util.ResourceLoader;

public final class Tileset {

    private static final Logger LOG = LogManager.getLogger(Tileset.class);

    public final int FIRST_GID, LAST_GID, TILE_W, TILE_H, WIDTH, HEIGHT;

    public final String SOURCE;

    public final URL SOURCE_URL;

    public final float STEP_X, STEP_Y;

    public Tileset(final String src, final int width, final int height,
            final int tileWidth, final int tileHeight, final int firstGid) {
        SOURCE = src;
        TILE_W = tileWidth;
        TILE_H = tileHeight;
        FIRST_GID = firstGid;

        WIDTH = width;
        HEIGHT = height;

        STEP_X = (float) TILE_W / WIDTH;
        STEP_Y = (float) TILE_H / HEIGHT;

        LAST_GID = WIDTH / TILE_W * (HEIGHT / TILE_H);

        String urlStr = SOURCE.startsWith("../") ? urlStr = "res/"
                + SOURCE.substring(3) : SOURCE;

        final URL url = ResourceLoader.getResource(urlStr);

        if (url == null) {
            LOG.fatal("URL not init!");
        }

        SOURCE_URL = url;

    }

    public int[] getTileIdByMapId(final int id) {

        final int idWithOffSet = id - (FIRST_GID - 1); // Apply first gid offset

        int idX = 0;
        int idY = 0;

        // Iterate over texture coordinate to get x, y component of the id
        int y;
        int count;

        for (y = 1, count = 1; y <= HEIGHT / TILE_H; y++) {

            for (int x = 1; x <= WIDTH / TILE_W; x++, count++) {

                if (count == idWithOffSet) {

                    idY = y;
                    idX = x;

                }

            }

        }

        return new int[] { idX, idY };

    }

}
