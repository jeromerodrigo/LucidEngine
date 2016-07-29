package org.jeromerodrigo.lucidengine;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jeromerodrigo.lucidengine.audio.OpenALSoundStore;
import org.jeromerodrigo.lucidengine.game.Game;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.Util;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

public abstract class AbstractGameEngine implements GameEngine {

    private static final Logger LOG = LogManager
            .getLogger(AbstractGameEngine.class);

    private long currentTime;

    private long lastFrame;

    private long lastFPS;

    private int fps;

    private final int TARGET_FPS;

    private int displayWidth, displayHeight;

    public AbstractGameEngine(final Properties prop)
            throws NumberFormatException, LWJGLException {

        final boolean needDisplay = prop.getProperty("needDisplay").equals(
                "true");

        if (needDisplay) {

            displayWidth = Integer.parseInt(prop.getProperty("displayWidth"));

            displayHeight = Integer.parseInt(prop.getProperty("displayHeight"));

            Display.setDisplayMode(new DisplayMode(displayWidth, displayHeight));
            Display.setVSyncEnabled(prop.getProperty("vSync").equals("true"));
            Display.create();

            if (LOG.isDebugEnabled()) {
                LOG.debug("OpenGL Version {}",
                        GL11.glGetString(GL11.GL_VERSION));
                LOG.debug("Display Size - W: {} | H: {}", displayWidth,
                        displayHeight);
                LOG.debug("Fullscreen: {}", Display.isFullscreen());
            }
        }

        final boolean needKeyboard = prop.getProperty("needKeyboard").equals(
                "true");

        if (needKeyboard) {
            Keyboard.create();
        }

        final boolean needAudio = prop.getProperty("needAudio").equals("true");

        if (needAudio) {
            AL.create();
            Util.checkALError();
        }

        TARGET_FPS = Integer.parseInt(prop.getProperty("targetFps"));

        // Setup projection matrix
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, Display.getWidth(), Display.getHeight(), 0, 1, -1);

        // Enable 2D Textures
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Enable blending
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Setup model view
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        // Setup the background color
        GL11.glClearColor(0, 0, 0, 0);
    }

    @Override
    public int getDisplayWidth() {
        return displayWidth;
    }

    @Override
    public int getDisplayHeight() {
        return displayHeight;
    }

    @Override
    public void start(final Game game) {

        if (LOG.isInfoEnabled()) {
            LOG.info("Starting game {}...", game.getName());
        }

        updateTime();

        lastFrame = currentTime;
        lastFPS = currentTime;

        while (!Display.isCloseRequested()) {
            game.processInput();

            game.update(getDelta());

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
            GL11.glLoadIdentity();

            game.render();

            Display.update();
            Display.sync(TARGET_FPS);

            if (currentTime - lastFPS > 1000) {
                Display.setTitle(String.valueOf(fps));
                fps = 0;
                lastFPS += 1000;
            }

            fps++;

            updateTime();
        }

        game.dispose();

        if (Display.isCreated()) {
            Display.destroy();
        }

        if (Keyboard.isCreated()) {
            Keyboard.destroy();
        }

        if (AL.isCreated()) {
            OpenALSoundStore.INSTANCE.destroy();
            AL.destroy();
        }

    }

    public final int getDelta() {
        return (int) (currentTime - lastFrame);
    }

    public final void updateTime() {
        currentTime = Sys.getTime() * 1000 / Sys.getTimerResolution();
    }
}
