package org.jeromerodrigo.lucidengine.audio;

import java.net.URL;

public interface AudioLoader<T extends Audio> {

    T loadAudio(String name, URL url);

}
