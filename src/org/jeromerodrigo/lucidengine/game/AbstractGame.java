package org.jeromerodrigo.lucidengine.game;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractGame implements Game {

    private final Map<String, AbstractScene> scenes;

    private AbstractScene currentScene;

    public AbstractGame() {
        scenes = new HashMap<String, AbstractScene>();
    }

    public void putScene(final AbstractScene scene) {
        scenes.put(scene.getName(), scene);
    }

    public void setCurrentScene(final String sceneName) {
        if (scenes.containsKey(sceneName)) {
            currentScene = scenes.get(sceneName);
        }
    }

    public void removeScene(final String sceneName) {
        if (scenes.containsKey(sceneName)) {
            scenes.remove(sceneName);
        }
    }

    @Override
    public void dispose() {
        currentScene.dispose();
    }

    @Override
    public void processInput() {
        currentScene.processInput();
    }

    @Override
    public void render() {
        currentScene.render();
    }

    @Override
    public void update(final int delta) {
        currentScene.update(delta);
    }

}
