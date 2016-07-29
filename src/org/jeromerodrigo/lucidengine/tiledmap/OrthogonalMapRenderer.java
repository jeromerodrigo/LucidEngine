package org.jeromerodrigo.lucidengine.tiledmap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jeromerodrigo.lucidengine.Camera;
import org.jeromerodrigo.lucidengine.graphics.Sprite;
import org.jeromerodrigo.lucidengine.graphics.SpriteBatch;
import org.jeromerodrigo.lucidengine.graphics.texture.Texture;
import org.jeromerodrigo.lucidengine.graphics.texture.TextureRegion;
import org.lwjgl.util.vector.Vector2f;

public class OrthogonalMapRenderer extends MapRenderer {

    private static final Logger LOG = LogManager
            .getLogger(OrthogonalMapRenderer.class);

    private final Map<String, Sprite[][]> spriteArrayByLayerName;

    public OrthogonalMapRenderer(final TiledMap map, final Camera cam) {
        super(map, cam);

        final Map<Tileset, Texture> textureByTilesetMap = new HashMap<Tileset, Texture>();

        for (final Tileset ts : map.getTilesets()) {
            String fileSource = ts.SOURCE;

            if (ts.SOURCE.startsWith("../")) {
                fileSource = "res/" + fileSource.substring(3);
            }

            Texture tex = null;

            try {
                tex = new Texture(ts.SOURCE_URL, Texture.NEAREST);
            } catch (final IOException e) {
                LOG.fatal(e);
            }

            textureByTilesetMap.put(ts, tex);
        }

        spriteArrayByLayerName = new HashMap<String, Sprite[][]>();

        for (final String layerName : map.getTileLayerNames()) {

            final int[][] tiles = map.getTilesAt(layerName);

            final Sprite[][] spriteArray = new Sprite[tiles.length][tiles[0].length];

            for (int x = 0; x < tiles.length; x++) {
                for (int y = 0; y < tiles[x].length; y++) {

                    Tileset tileset = null;

                    for (final Tileset ts : map.getTilesets()) {

                        if (tiles[x][y] >= ts.FIRST_GID
                                && tiles[x][y] <= ts.LAST_GID) {
                            tileset = ts;
                            break;
                        }
                    }

                    if (tiles[x][y] != 0) {

                        final int xid = tileset.getTileIdByMapId(tiles[x][y])[0] - 1;
                        final int yid = tileset.getTileIdByMapId(tiles[x][y])[1] - 1;

                        spriteArray[x][y] = new Sprite(new TextureRegion(
                                textureByTilesetMap.get(tileset), xid
                                * map.getTileWidth(), yid
                                * map.getTileHeight(), tileset.TILE_W,
                                tileset.TILE_H), 0, 0, tileset.TILE_W,
                                tileset.TILE_H);
                    }
                }
            }

            spriteArrayByLayerName.put(layerName, spriteArray);
        }

    }

    @Override
    protected TiledMap.Orientation getRenderOrientation() {
        return TiledMap.Orientation.ORTHOGONAL;
    }

    @Override
    public void render(final SpriteBatch batch, final float xRender,
            final float yRender, final int mapX, final int mapY,
            final int tilesX, final int tilesY) {

        final int xStart = mapX < 0 ? 0 : mapX;
        final int yStart = mapY < 0 ? 0 : mapY;

        int xEnd = xStart + tilesX;
        int yEnd = yStart + tilesY;

        xEnd = xEnd > map.getWidth() ? map.getWidth() : xEnd;
        yEnd = yEnd > map.getHeight() ? map.getHeight() : yEnd;

        batch.getViewMatrix().translate(new Vector2f(xRender, yRender));

        for (final String layerName : spriteArrayByLayerName.keySet()) {

            final Sprite[][] spriteArray = spriteArrayByLayerName
                    .get(layerName);

            for (int x = xStart; x < xEnd; x++) {

                for (int y = yStart; y < yEnd; y++) {

                    if (spriteArray[x][y] != null) {
                        spriteArray[x][y].setLocation(x * map.getTileWidth(), y
                                * map.getTileHeight());
                        spriteArray[x][y].render(batch);
                    }

                }

            }

        }

    }

    @Override
    public void render(final SpriteBatch batch) {
        // Translate to map coordinates
        final int mapX = (int) Math.round(camera.getX()) / map.getTileWidth();
        final int mapY = (int) Math.round(camera.getY()) / map.getTileHeight();

        render(batch, 0, 0, mapX, mapY,
                camera.displayWidth / map.getTileWidth() + 1,
                camera.displayHeight / map.getTileHeight() + 2);
    }
}
