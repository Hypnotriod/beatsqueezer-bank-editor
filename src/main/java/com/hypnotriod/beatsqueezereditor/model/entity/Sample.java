package com.hypnotriod.beatsqueezereditor.model.entity;

import com.hypnotriod.beatsqueezereditor.tools.ByteArrayInputStream16BitMonoToStereo;
import com.hypnotriod.beatsqueezereditor.tools.RawPCMDataPlayer;
import java.io.ByteArrayInputStream;
import javax.sound.sampled.AudioInputStream;

/**
 *
 * @author Ilya Pikin
 */
public class Sample {
    
    public static final String EXT_DEFAULT = "_d";
    public static final String EXT_P       = "_p";
    public static final String EXT_F       = "_f";

    public int channels = 1;
    public byte[] samplesData = null;
    public WaveHeader waveHeader = null;
    public byte[] samplesDataP = null;
    public WaveHeader waveHeaderP = null;
    public byte[] samplesDataF = null;
    public WaveHeader waveHeaderF = null;
    public String filePath = null;
    public String fileRealName = null;
    public String fileName = null;
    public int pitch = 0;
    public int noteId = 0;
    public int groupId = 0;
    public boolean dynamic = false;
    public boolean disableNoteOff = false;
    public long panorama = 0;
    public boolean loopEnabled = false;
    public SustainLoop loop = null;
    public SustainLoop loopP = null;
    public SustainLoop loopF = null;

    public boolean isPlaying = false;
    public String selectedSampleExt = EXT_DEFAULT;
    public String playingSampleExt = EXT_DEFAULT;

    public AudioInputStream getAudioStream() {
        byte[] data;

        switch (selectedSampleExt) {
            case EXT_P:
                data = samplesDataP;
                break;
            case EXT_F:
                data = samplesDataF;
                break;
            default:
                data = samplesData;
                break;
        }

        if (data != null) {
            if (channels == 2) {
                return new AudioInputStream(
                        new ByteArrayInputStream(data),
                        RawPCMDataPlayer.AUDIO_FORMAT_44_16_STEREO,
                        data.length
                );
            } else {
                return new AudioInputStream(
                        new ByteArrayInputStream16BitMonoToStereo(data),
                        RawPCMDataPlayer.AUDIO_FORMAT_44_16_STEREO,
                        data.length
                );
            }
        }

        return null;
    }

    public void dispose() {
        if (waveHeader != null) {
            waveHeader.dispose();
        }

        loop = null;
        samplesData = null;
        waveHeader = null;
        filePath = null;
        fileName = null;
    }
}
