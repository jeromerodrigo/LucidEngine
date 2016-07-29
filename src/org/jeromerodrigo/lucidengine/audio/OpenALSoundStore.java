package org.jeromerodrigo.lucidengine.audio;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.Util;

public final class OpenALSoundStore {

    private static final Logger LOG = LogManager
            .getLogger(OpenALSoundStore.class);

    public static final OpenALSoundStore INSTANCE = new OpenALSoundStore();

    public static final int BUFFER_SIZE = 20;

    public static final int MAX_SOURCES = 20;

    private final IntBuffer buffer;

    private final IntBuffer source;

    private final Map<String, Integer> soundNameIndex;

    private int runningIndex;

    private OpenALSoundStore() {
        buffer = BufferUtils.createIntBuffer(BUFFER_SIZE);
        AL10.alGenBuffers(buffer);
        Util.checkALError();

        source = BufferUtils.createIntBuffer(MAX_SOURCES);
        AL10.alGenSources(source);
        Util.checkALError();

        soundNameIndex = new HashMap<String, Integer>();

        runningIndex = 0;
    }

    public void uploadAudio(final String name, final int format,
            final ByteBuffer data, final int freq, final OpenALAudio audio) {

        AL10.alBufferData(buffer.get(runningIndex), format, data, freq);

        AL10.alSourcei(source.get(runningIndex), AL10.AL_BUFFER,
                buffer.get(runningIndex));
        AL10.alSourcef(source.get(runningIndex), AL10.AL_PITCH, 1.0f);
        AL10.alSourcef(source.get(runningIndex), AL10.AL_GAIN, 1.0f);
        AL10.alSource(source.get(runningIndex), AL10.AL_POSITION,
                audio.getSourcePos());
        AL10.alSource(source.get(runningIndex), AL10.AL_VELOCITY,
                audio.getSourceVel());
        AL10.alSourcei(source.get(runningIndex), AL10.AL_LOOPING, AL10.AL_FALSE);

        soundNameIndex.put(name, runningIndex);

        Util.checkALError();

        runningIndex++;

        if (LOG.isDebugEnabled()) {
            LOG.debug("{} successfully loaded to audio buffer.", name);
        }
    }

    public int getAudio(final String name) {
        return source.get(soundNameIndex.get(name));
    }

    public boolean isPlaying(final String name) {
        final int playing = AL10.alGetSourcei(getAudio(name),
                AL10.AL_SOURCE_STATE);

        return playing == AL10.AL_PLAYING;
    }

    public void destroy() {

        if (LOG.isInfoEnabled()) {
            LOG.info("Destroying audio buffers...");
        }

        AL10.alDeleteBuffers(buffer);
        AL10.alDeleteSources(source);
    }

    public IntBuffer getBuffer() {
        return buffer;
    }

    public IntBuffer getSource() {
        return source;
    }

}
