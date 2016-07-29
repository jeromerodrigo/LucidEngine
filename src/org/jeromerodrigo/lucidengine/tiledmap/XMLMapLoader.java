package org.jeromerodrigo.lucidengine.tiledmap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.dyn4j.dynamics.World;
import org.jeromerodrigo.lucidengine.tiledmap.TiledMap.Orientation;

/**
 * A MapLoader that loads the map from XML
 * @author Jerome
 *
 */

public final class XMLMapLoader implements MapLoader {

    private static final Logger LOG = LogManager.getLogger(XMLMapLoader.class);

    public static final XMLMapLoader INSTANCE = new XMLMapLoader();

    private static XMLInputFactory2 xmlif;

    static {
        xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
        xmlif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES,
                Boolean.FALSE);
        xmlif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,
                Boolean.FALSE);
        xmlif.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);

        xmlif.configureForSpeed();
    }

    private XMLMapLoader() {
        // Prevents instantiation
    }

    @Override
    public TiledMap loadMap(final String path) {

        int width = 0;
        int height = 0;
        int tileWidth = 0;
        int tileHeight = 0;

        TiledMap.Orientation orientation = null;

        // Tile Layer Variables
        final Map<String, List<Integer>> layerList = new HashMap<String, List<Integer>>();
        String layerName = "";

        // Collision Variables
        final List<CollisionObject> collObjs = new ArrayList<CollisionObject>();
        int objX = 0;
        int objY = 0;
        int objWidth = 0;
        int objHeight = 0;

        // Tileset Variables
        final List<Tileset> tilesets = new ArrayList<Tileset>();
        int tsWidth = 0;
        int tsHeight = 0;
        int tsTileW = 0;
        int tsTileH = 0;
        int firstGid = 0;

        String source = "";

        try {

            final XMLStreamReader2 xmlr = (XMLStreamReader2) xmlif
                    .createXMLStreamReader(new FileInputStream(path));

            while (xmlr.hasNext()) {

                final int eventType = xmlr.next();

                String curElem = "";

                if (xmlr.isStartElement() || xmlr.isEndElement()) {
                    curElem = xmlr.getName().toString();
                }

                switch (eventType) {
                case XMLEvent.START_ELEMENT:

                    for (int i = 0; i < xmlr.getAttributeCount(); i++) {

                        final String name = xmlr.getAttributeLocalName(i);

                        switch (curElem) {
                        case "map":

                            switch (name) {
                            case "orientation":

                                final String orientationStr = xmlr
                                .getAttributeValue(i);

                                if ("orthogonal".equals(orientationStr)) {
                                    orientation = Orientation.ORTHOGONAL;
                                } else if ("isometric".equals(orientationStr)) {
                                    orientation = Orientation.ISOMETRIC;
                                }

                                break;
                            case "width":
                                width = xmlr.getAttributeAsInt(i);
                                break;
                            case "height":
                                height = xmlr.getAttributeAsInt(i);
                                break;
                            case "tilewidth":
                                tileWidth = xmlr.getAttributeAsInt(i);
                                break;
                            case "tileheight":
                                tileHeight = xmlr.getAttributeAsInt(i);
                                break;
                            }

                            break;
                        case "tileset":

                            switch (name) {
                            case "firstgid":
                                firstGid = xmlr.getAttributeAsInt(i);
                                break;
                            case "tilewidth":
                                tsTileW = xmlr.getAttributeAsInt(i);
                                break;
                            case "tileheight":
                                tsTileH = xmlr.getAttributeAsInt(i);
                                break;

                            }

                            break;
                        case "image":

                            switch (name) {
                            case "source":
                                source = xmlr.getAttributeValue(i);
                                break;
                            case "width":
                                tsWidth = xmlr.getAttributeAsInt(i);
                                break;
                            case "height":
                                tsHeight = xmlr.getAttributeAsInt(i);
                                break;
                            }

                            break;
                        case "layer":

                            if ("name".equals(name)) {
                                layerName = xmlr.getAttributeValue(i);
                                layerList.put(layerName,
                                        new ArrayList<Integer>());
                            }

                            break;
                        case "tile":

                            if ("gid".equals(name)) {
                                layerList.get(layerName).add(
                                        xmlr.getAttributeAsInt(i));
                            }

                            break;
                        case "object":

                            switch (name) {
                            case "x":
                                objX = xmlr.getAttributeAsInt(i);
                                break;
                            case "y":
                                objY = xmlr.getAttributeAsInt(i);
                                break;
                            case "width":
                                objWidth = xmlr.getAttributeAsInt(i);
                                break;
                            case "height":
                                objHeight = xmlr.getAttributeAsInt(i);
                                break;
                            }

                            break;
                        }

                    }

                    break;
                case XMLEvent.END_ELEMENT:

                    if ("tileset".equals(curElem)) {
                        tilesets.add(new Tileset(source, tsWidth, tsHeight,
                                tsTileW, tsTileH, firstGid));
                    } else if ("object".equals(curElem)) {
                        collObjs.add(new CollisionObject(objX, objY, objWidth,
                                objHeight, tileWidth, tileHeight));
                    }

                    break;
                }

            }

        } catch (final FileNotFoundException e) {
            LOG.fatal("Map file not found!\n" + e.getMessage());
        } catch (final XMLStreamException e) {
            LOG.fatal("XML Stream Error.\n" + e.getMessage());
        }

        final TiledMap map = new TiledMap(orientation, width, height,
                tileWidth, tileHeight);

        for (final String key : layerList.keySet()) {

            final List<Integer> tileList = layerList.get(key);

            int idx = 0;

            final int[][] tileData = new int[width][height];

            for (int y = 0; y < height; y++) {

                for (int x = 0; x < width; x++) {

                    tileData[x][y] = tileList.get(idx);
                    idx++;

                }

            }

            // Add new tile layer
            map.addTileLayer(key, tileData);
        }

        // Set collision objects
        map.setCollisionObjects(collObjs);

        // Add the tilesets
        for (final Tileset tSet : tilesets) {
            map.addTileset(tSet);
        }

        return map;
    }

    @Override
    public void loadMapToPhysics(final TiledMap map, final World physics) {

        for (final CollisionObject obj : map.getCollisionObjects()) {
            physics.addBody(obj);
        }

    }

}
