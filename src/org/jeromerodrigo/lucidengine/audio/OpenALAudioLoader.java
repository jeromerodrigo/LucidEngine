package org.jeromerodrigo.lucidengine.audio;

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OpenALAudioLoader implements AudioLoader<OpenALAudio> {

    private static final Logger LOG = LogManager
            .getLogger(OpenALAudioLoader.class);

    public static final OpenALAudioLoader INSTANCE = new OpenALAudioLoader();

    private OpenALAudioLoader() {

    }

    @Override
    public OpenALAudio loadAudio(final String name, final URL url) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading audio {}", name);
        }

        final OpenALAudio audio = new OpenALAudio(name, url);

        OpenALSoundStore.INSTANCE.uploadAudio(name, audio.getFormat(),
                audio.getData(), audio.getSampleRate(), audio);

        return audio;
    }

}
