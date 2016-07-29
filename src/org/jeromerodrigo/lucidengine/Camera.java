package org.jeromerodrigo.lucidengine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jeromerodrigo.lucidengine.entity.Entity;
import org.jeromerodrigo.lucidengine.tiledmap.TiledMap;

/**
 * Represents an in-game Camera object that hold the view matrices
 *
 * @author Jerome Edward
 */

public class Camera implements Updateable {

    private static final Logger LOG = LogManager.getLogger(Camera.class);

    /**
     * Holds the minimum camera offset value of x and y plane
     */

    private static final int OFFSET_MIN_X = 0, OFFSET_MIN_Y = 0;

    /**
     * The entity to track with the camera
     */

    private final Entity entity;

    /**
     * The maximum offset value of x and y plane
     */

    private final double offsetMaxX, offsetMaxY;

    /**
     * The display width and height
     */

    public final int displayWidth, displayHeight;

    /**
     * The origin location of the camera in x and y plane
     */

    private double camX, camY;

    /**
     * Creates a wrapper object to hold the coordinates tracking the
     * player'smovement on a TiledMap
     *
     * @param e
     *            The entity to track
     * @param map
     *            The TiledMap map object
     * @param screenWidth
     *            The width of the screen in pixels
     * @param screenHeight
     *            The height of the screen in pixels
     */

    public Camera(final Entity e, final TiledMap map, final int screenWidth,
            final int screenHeight) {

        displayWidth = screenWidth;
        displayHeight = screenHeight;

        offsetMaxX = map.getTileWidth() * map.getWidth() - screenWidth;
        offsetMaxY = map.getTileHeight() * map.getHeight() - screenHeight;
        entity = e;

        if (LOG.isTraceEnabled()) {
            LOG.trace("offsetMaxX : {}, offsetMaxY : {}", offsetMaxX,
                    offsetMaxY);
        }
    }

    @Override
    public final void update(final int delta) {
        camX = limitCameraLocation(
                getCameraLocationRelativeToDisplay(entity.getX(), displayWidth),
                offsetMaxX, OFFSET_MIN_X);
        camY = limitCameraLocation(
                getCameraLocationRelativeToDisplay(entity.getY(), displayHeight),
                offsetMaxY, OFFSET_MIN_Y);
    }

    /**
     * Limits the movement of the camera within maximum and minimum values.
     *
     * @param component
     *            the x or y component of camera coordinate
     * @param maxOffset
     *            the maximum offset value
     * @param minOffset
     *            the minimum offset value
     * @return the camera location limited within maximum and minimum offset
     *         range
     */

    private static final double limitCameraLocation(double component,
            final double maxOffset, final double minOffset) {
        if (component > maxOffset) {
            component = maxOffset;
        } else if (component < minOffset) {
            component = minOffset;
        }

        return component;
    }

    /**
     * Calculates the correct camera position for a single component x or y
     * relative to the size of the display.
     * 
     * @param component
     *            the x or y component of the entity being tracked
     * @param displayComponent
     *            the width or height of the display
     * @return the correct camera position for either x or y component
     */

    private static final double getCameraLocationRelativeToDisplay(
            final double component, final double displayComponent) {
        return component - displayComponent / 2;
    }

    /**
     * Gets the x-component of the camera's location
     *
     * @return x-component location of camera
     */

    public final double getX() {
        return camX;
    }

    /**
     * Gets the y-component of the camera's location
     *
     * @return y-component location of camera
     */

    public final double getY() {
        return camY;
    }

}
