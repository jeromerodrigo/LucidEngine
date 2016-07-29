package org.jeromerodrigo.lucidengine.audio;

import java.nio.FloatBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

public final class OpenALAudioPlayer implements AudioPlayer<OpenALAudio> {

    private static final Logger LOG = LogManager
            .getLogger(OpenALAudioPlayer.class);

    public static final OpenALAudioPlayer INSTANCE = new OpenALAudioPlayer();

    private final FloatBuffer listenerPos;
    private final FloatBuffer listenerVel;
    private final FloatBuffer listenerOri;

    private OpenALAudioPlayer() {
        listenerPos = BufferUtils.createFloatBuffer(3);
        listenerPos.put(new float[] { 0.0f, 0.0f, 0.0f }).flip();

        listenerVel = BufferUtils.createFloatBuffer(3);
        listenerVel.put(new float[] { 0.0f, 0.0f, 0.0f }).flip();

        listenerOri = BufferUtils.createFloatBuffer(3);
        listenerOri.put(new float[] { 0.0f, 0.0f, 0.0f }).flip();

        AL10.alListener(AL10.AL_POSITION, listenerPos);
        AL10.alListener(AL10.AL_VELOCITY, listenerVel);
        AL10.alListener(AL10.AL_ORIENTATION, listenerOri);

        if (LOG.isInfoEnabled()) {
            LOG.info("{} initialized.", OpenALAudioPlayer.class.getSimpleName());
        }
    }

    @Override
    public void play(final OpenALAudio audio) {
        AL10.alSourcePlay(OpenALSoundStore.INSTANCE.getAudio(audio.getName()));
    }

    @Override
    public void play(final OpenALAudio audio, final boolean isLooping) {
        AL10.alSourcei(OpenALSoundStore.INSTANCE.getAudio(audio.getName()),
                AL10.AL_LOOPING, isLooping ? AL10.AL_TRUE : AL10.AL_FALSE);
        play(audio);
    }

    @Override
    public void pause(final OpenALAudio audio) {
        AL10.alSourcePause(OpenALSoundStore.INSTANCE.getAudio(audio.getName()));
    }

    @Override
    public void stop(final OpenALAudio audio) {
        AL10.alSourceStop(OpenALSoundStore.INSTANCE.getAudio(audio.getName()));
    }

    @Override
    public void setVolume(final OpenALAudio audio, float volume) {

        if (volume > 1.0f) {
            volume = 1.0f;
        }

        AL10.alSourcef(OpenALSoundStore.INSTANCE.getAudio(audio.getName()),
                AL10.AL_GAIN, volume);
    }

}
