package org.jeromerodrigo.lucidengine.entity;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Mass;
import org.jeromerodrigo.lucidengine.animation.Animation;
import org.jeromerodrigo.lucidengine.graphics.AnimatedSprite;
import org.jeromerodrigo.lucidengine.graphics.SpriteBatch;
import org.jeromerodrigo.lucidengine.graphics.texture.TextureRegion;

public abstract class AbstractEntity extends Body implements Entity {

    private final transient AnimatedSprite spr;

    public AbstractEntity(final TextureRegion initialRegion,
            final float xLocation, final float yLocation, final float width,
            final float height) {
        super();

        spr = new AnimatedSprite(initialRegion, xLocation, yLocation, width,
                height);

        final BodyFixture bodyFixture = new BodyFixture(
                Geometry.createCircle(width / 2));

        bodyFixture.setFriction(10.0);
        bodyFixture.setDensity(1.0);

        addFixture(bodyFixture);
        setMass(getMassType());

        translate(xLocation, yLocation);
    }

    protected abstract Mass.Type getMassType();

    protected final void putAnimation(final Animation anim) {
        spr.putAnimation(anim);
    }

    protected final void setSelectedAnimation(final Animation animation) {
        spr.setSelectedAnimation(animation);
    }

    protected final Animation getSelectedAnimation() {
        return spr.getSelectedAnimation();
    }

    protected final AnimatedSprite getSprite() {
        return spr;
    }

    @Override
    public double getX() {
        return transform.getTranslationX();
    }

    @Override
    public double getY() {
        return transform.getTranslationY();
    }

    @Override
    public void render(final SpriteBatch batch) {
        spr.setLocation((float) getX(), (float) getY());
        spr.render(batch);
    }
}
