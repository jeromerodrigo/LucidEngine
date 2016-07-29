package org.jeromerodrigo.lucidengine.audio;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.WaveData;

public final class OpenALAudio implements Audio {

    private final FloatBuffer sourcePos;
    private final FloatBuffer sourceVel;
    private final String audioName;
    private final WaveData waveFile;

    public OpenALAudio(final String name, final URL url) {

        waveFile = WaveData.create(url);

        audioName = name;
        sourcePos = BufferUtils.createFloatBuffer(3);
        sourcePos.put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
        sourceVel = BufferUtils.createFloatBuffer(3);
        sourceVel.put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
    }

    public FloatBuffer getSourcePos() {
        return sourcePos;
    }

    public FloatBuffer getSourceVel() {
        return sourceVel;
    }

    @Override
    public String getName() {
        return audioName;
    }

    public int getFormat() {
        return waveFile.format;
    }

    public ByteBuffer getData() {
        return waveFile.data;
    }

    public int getSampleRate() {
        return waveFile.samplerate;
    }

}
