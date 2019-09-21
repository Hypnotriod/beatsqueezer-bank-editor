package com.hypnotriod.beatsqueezereditor.model.vo;

import com.hypnotriod.beatsqueezereditor.constants.CConfig;
import com.hypnotriod.beatsqueezereditor.tools.ByteArrayInputStream16BitMonoToStereo;
import com.hypnotriod.beatsqueezereditor.tools.RawPCMDataPlayer;
import java.io.ByteArrayInputStream;
import javax.sound.sampled.AudioInputStream;

/**
 *
 * @author Ilya Pikin
 */
public class SampleVO {

    public int channels = 1;
    public byte[] samplesData = null;
    public WaveHeaderVO waveHeader = null;
    public byte[] samplesDataP = null;
    public WaveHeaderVO waveHeaderP = null;
    public byte[] samplesDataF = null;
    public WaveHeaderVO waveHeaderF = null;
    public String filePath = null;
    public String fileRealName = null;
    public String fileName = null;
    public int pitch = 0;
    public int noteID = 0;
    public int groupID = 0;
    public boolean dynamic = false;
    public boolean disableNoteOff = false;
    public long panorama = 0;
    public boolean loopEnabled = false;
    public SustainLoopVO loop = null;
    public SustainLoopVO loopP = null;
    public SustainLoopVO loopF = null;

    public boolean isPlaying = false;
    public String selectedSampleExt = CConfig.EXT_DEFAULT;
    public String playingSampleExt = CConfig.EXT_DEFAULT;

    public SampleVO(int noteID) {
        this.noteID = noteID;
    }

    public AudioInputStream getAudioStream() {
        byte[] data;

        switch (selectedSampleExt) {
            case CConfig.EXT_P:
                data = samplesDataP;
                break;
            case CConfig.EXT_F:
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
