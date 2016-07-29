package org.jeromerodrigo.lucidengine.game;

import java.util.ArrayList;
import java.util.List;

import org.jeromerodrigo.lucidengine.Drawable;
import org.jeromerodrigo.lucidengine.Updateable;
import org.jeromerodrigo.lucidengine.ai.Controllable;
import org.jeromerodrigo.lucidengine.ai.Controller;
import org.jeromerodrigo.lucidengine.graphics.SpriteBatch;

public abstract class AbstractScene implements Game {

    protected final SpriteBatch batch;

    private Controller controller;

    private final String sceneName;
    private final List<Drawable> drawables;
    private final List<Updateable> updateables;
    private final List<Controllable> controllables;

    public AbstractScene(final String name, final SpriteBatch spriteBatch) {

        drawables = new ArrayList<Drawable>();
        updateables = new ArrayList<Updateable>();
        controllables = new ArrayList<Controllable>();

        sceneName = name;
        batch = spriteBatch;
    }

    @Override
    public void update(final int delta) {
        for (final Updateable updateable : updateables) {
            updateable.update(delta);
        }
    }

    @Override
    public void dispose() {
        batch.flush();
    }

    @Override
    public final String getName() {
        return sceneName;
    }

    @Override
    public void processInput() {

        if (controller != null) {
            for (final Controllable controllable : controllables) {
                controllable.accept(controller);
            }
        }
    }

    @Override
    public void render() {
        batch.begin();

        for (final Drawable drawable : drawables) {
            drawable.render(batch);
        }

        batch.end();
    }

    public final void addDrawable(final Drawable drawable) {
        drawables.add(drawable);
    }

    public final void addUpdateable(final Updateable updateable) {
        updateables.add(updateable);
    }

    public final void addControllable(final Controllable controllable) {
        controllables.add(controllable);
    }

    public final void setController(final Controller controllerToSet) {
        controller = controllerToSet;
    }
}
