package org.jeromerodrigo.lucidengine.test;

import static org.junit.Assert.assertTrue;

import org.jeromerodrigo.lucidengine.tiledmap.Tileset;
import org.junit.BeforeClass;
import org.junit.Test;

public class TilesetTest {

	static final int HEIGHT = 512;

	static final int TILE_SZ = 32;
	static Tileset tileset;
	static final int WIDTH = 512;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		tileset = new Tileset("", WIDTH, HEIGHT, TILE_SZ, TILE_SZ, 1);
	}

	@Test
	public void testgetTileIdByMapId() {

		assertTrue(tileset.getTileIdByMapId(1)[0] == 1);
		assertTrue(tileset.getTileIdByMapId(1)[1] == 1);

	}

}
