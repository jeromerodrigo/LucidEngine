package org.jeromerodrigo.lucidengine.graphics;

import org.jeromerodrigo.lucidengine.Drawable;
import org.jeromerodrigo.lucidengine.graphics.texture.TextureRegion;

public class Sprite implements Drawable {

    private TextureRegion texRegion;

    private float xLoc, yLoc;

    private final float width;

    private final float height;

    public Sprite(final TextureRegion texRegion, final float xLoc,
            final float yLoc, final float width, final float height) {
        this.texRegion = texRegion;
        this.width = width;
        this.height = height;
        this.xLoc = xLoc;
        this.yLoc = yLoc;
    }

    public final void setTexRegion(final TextureRegion texRegion) {
        this.texRegion = texRegion;
    }

    public final void setLocation(final float x, final float y) {
        xLoc = x;
        yLoc = y;
    }

    @Override
    public void render(final SpriteBatch batch) {
        batch.draw(texRegion, xLoc, yLoc, width, height);
    }
}
