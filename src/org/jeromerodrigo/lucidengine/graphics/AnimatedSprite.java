package org.jeromerodrigo.lucidengine.graphics;

import java.util.HashSet;
import java.util.Set;

import org.jeromerodrigo.lucidengine.Updateable;
import org.jeromerodrigo.lucidengine.animation.Animation;
import org.jeromerodrigo.lucidengine.graphics.texture.TextureRegion;

public class AnimatedSprite extends Sprite implements Updateable {

    private transient final Set<Animation> animations;
    private transient Animation selectedAnimation;

    public AnimatedSprite(final TextureRegion texRegion, final float xLoc,
            final float yLoc, final float width, final float height) {
        super(texRegion, xLoc, yLoc, width, height);

        animations = new HashSet<Animation>();
        selectedAnimation = null;
    }

    public final void putAnimation(final Animation anim) {
        animations.add(anim);
    }

    public final void setSelectedAnimation(final Animation animation) {
        selectedAnimation = animation;
    }

    public final Animation getSelectedAnimation() {
        return selectedAnimation;
    }

    @Override
    public void update(final int delta) {
        if (selectedAnimation != null && !animations.isEmpty()
                && animations.contains(selectedAnimation)) {
            selectedAnimation.update(delta);
        }
    }
}
