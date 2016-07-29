package org.jeromerodrigo.lucidengine.animation;

import org.jeromerodrigo.lucidengine.graphics.texture.TextureRegion;

public class Frame {

    private final int duration;

    private int timesRendered;

    private final TextureRegion texRegion;

    /**
     * Constructs a Frame object used to render a particular animation from an
     * existing Sprite object
     *
     * @param spr
     *            The existing Sprite object
     * @param duration
     *            The time until this Frame is done rendering
     * @param texRegion
     *            The new texture region to switch to during animation
     */

    public Frame(final int duration, final TextureRegion texRegion) {
        this.duration = duration;
        this.texRegion = texRegion;
        timesRendered = 0;
    }

    /**
     * Gets the status of the Frame
     *
     * @return True, if done rendering. False, if otherwise.
     */

    protected boolean isDone() {
        return timesRendered >= duration;
    }

    public TextureRegion getTexRegion() {
        return texRegion;
    }

    public void updateRendered() {
        timesRendered = isDone() ? 0 : timesRendered + 1;
    }

}
