package org.jeromerodrigo.lucidengine.graphics.texture;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class TextureRegionStore {

    private static final Logger LOG = LogManager
            .getLogger(TextureRegionStore.class);

    private static final Map<String, TextureRegion> TEXTURE_REGIONS;

    static {
        TEXTURE_REGIONS = new HashMap<String, TextureRegion>();
    }

    private TextureRegionStore() {
    }

    /**
     * Adds a TextureRegion using pixel coordinates.
     *
     * @param regionName
     *            the name given to the TextureRegion
     * @param texture
     *            the main Texture
     * @param x
     *            the pixel coordinate on x-axis
     * @param y
     *            the pixel coordinate on y-axis
     * @param width
     *            the width of the region
     * @param height
     *            the height of the region
     */

    public static void put(final String regionName,
            final Texture texture, final int x, final int y, final int width,
            final int height) {
        TEXTURE_REGIONS.put(regionName, new TextureRegion(texture, x, y, width,
                height));
    }

    /**
     * Adds a TextureRegion using Id coordinates instead of pixels.
     *
     * @param regionName
     *            the name given to the TextureRegion
     * @param texture
     *            the main Texture
     * @param xId
     *            Id coordinate on x-axis
     * @param yId
     *            Id coordinate on y-axis
     * @param width
     *            the width of the region
     * @param height
     *            the height of the region
     */

    public static void putById(final String regionName,
            final Texture texture, final int xId, final int yId,
            final int width, final int height) {
        TEXTURE_REGIONS.put(regionName, new TextureRegion(texture, xId * width,
                yId * height, width, height));
    }

    /**
     * Get a TextureRegion by its name.
     *
     * @param key
     *            the name of the TextureRegion
     * @return TextureRegion
     */

    public static TextureRegion get(final String key) {

        TextureRegion tReg = null;

        if (TEXTURE_REGIONS.containsKey(key)) {
            tReg = TEXTURE_REGIONS.get(key);
        } else {
            LOG.warn("TextureRegion {} not found!", key);
        }

        return tReg;
    }
}
