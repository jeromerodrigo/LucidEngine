package org.jeromerodrigo.lucidengine.test;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jeromerodrigo.lucidengine.tiledmap.TiledMap;
import org.jeromerodrigo.lucidengine.tiledmap.XMLMapLoader;
import org.junit.Test;

public class XMLMapLoaderTest {

    private static final Logger LOG = LogManager
            .getLogger(XMLMapLoaderTest.class);

    @Test
    public void testLoadMap() {

        final TiledMap map = XMLMapLoader.INSTANCE
                .loadMap("res/world/level_1.tmx");

        assertTrue("Map height not more than zero!", map.getHeight() > 0);
        assertTrue("Map width not more than zero!", map.getWidth() > 0);

        assertNotSame("The tiles here should not be 0!", map.getTilesAt("background")[0][0], 0);

        assertTrue("No collision objects in the map!", map.getCollisionObjects().size() > 0);

        assertTrue("No tilesets found in the map!", map.getTilesets().size() > 0);

        LOG.info("Tileset SRC:" + map.getTilesets().get(0).SOURCE);
        LOG.info("Tileset W:" + map.getTilesets().get(0).WIDTH);
        LOG.info("Tileset H:" + map.getTilesets().get(0).HEIGHT);

    }

}
