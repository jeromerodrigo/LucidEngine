package org.jeromerodrigo.lucidengine.animation;

import java.util.List;

import org.jeromerodrigo.lucidengine.Updateable;
import org.jeromerodrigo.lucidengine.graphics.Sprite;

/**
 * Wrapper class that holds multiple Frame objects. Abstracts an animation that
 * occurs.
 *
 * @author Jerome Edward
 */

public class Animation implements Updateable {

    private int curFrame;

    private final List<Frame> frameList;

    private final Sprite sprite;

    /**
     * Constructs an Animation object consisting of multiple Frames
     *
     * @param frames
     *            The frames to be included in the animation
     */

    public Animation(final Sprite sprite, final List<Frame> frames) {
        this.sprite = sprite;
        frameList = frames;
        curFrame = 0;
    }

    @Override
    public final void update(final int delta) {

        final Frame frame = frameList.get(curFrame);

        sprite.setTexRegion(frame.getTexRegion());

        frame.updateRendered();

        if (frame.isDone()) {
            curFrame++;

            if (isDone()) {
                curFrame = 0;
            }
        }
    }

    public boolean isDone() {
        return curFrame >= frameList.size();
    }

}
