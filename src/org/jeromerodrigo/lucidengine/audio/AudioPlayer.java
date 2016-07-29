package org.jeromerodrigo.lucidengine.audio;

public interface AudioPlayer<T extends Audio> {

    void play(T audio);

    void play(T audio, boolean isLooping);

    void pause(T audio);

    void stop(T audio);

    void setVolume(T audio, float volume);

}
