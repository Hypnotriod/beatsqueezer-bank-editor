package com.hypnotriod.beatsqueezereditor.controller;

import com.hypnotriod.beatsqueezereditor.base.BaseController;
import com.hypnotriod.beatsqueezereditor.constants.Config;
import com.hypnotriod.beatsqueezereditor.constants.FileExtensions;
import com.hypnotriod.beatsqueezereditor.constants.Strings;
import com.hypnotriod.beatsqueezereditor.facade.Facade;
import com.hypnotriod.beatsqueezereditor.model.entity.SampleLoop;
import com.hypnotriod.beatsqueezereditor.model.entity.Sample;
import com.hypnotriod.beatsqueezereditor.model.entity.SustainLoop;
import com.hypnotriod.beatsqueezereditor.model.entity.WaveHeader;
import com.hypnotriod.beatsqueezereditor.tools.NoteFrequencyUtil;
import com.hypnotriod.beatsqueezereditor.tools.StringUtils;
import com.hypnotriod.beatsqueezereditor.tools.WavePCMNormalizeTool;
import com.sun.media.sound.WaveFileReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.stage.FileChooser;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author Ilya Pikin
 */
public class LoadSamplesController extends BaseController {

    private boolean truncatedSampleMessageShown = false;

    private final FileChooser.ExtensionFilter filter
            = new FileChooser.ExtensionFilter(FileExtensions.WAVE_FILE_BROWSE_NAME, FileExtensions.WAVE_FILE_BROWSE_FILTER_WAV, FileExtensions.WAVE_FILE_BROWSE_FILTER_WAVE);

    public LoadSamplesController(Facade facade) {
        super(facade);
    }

    public List<File> chooseFiles() {
        List<File> result;
        FileChooser fileChooser = getMainModel().getFileChooser();

        fileChooser.setTitle(Strings.LOAD_SAMPLES);
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(filter);
        fileChooser.setInitialFileName(null);

        result = fileChooser.showOpenMultipleDialog(getFacade().getPrimaryStage());

        if (result != null && result.size() > 0) {
            File existDirectory = result.get(0).getParentFile();
            fileChooser.setInitialDirectory(existDirectory);
        }

        return result;
    }

    public void loadSamples(List<File> files, int pitchStep, int pitch) {
        if (files != null && files.size() > 0) {
            parseFiles(files, pitchStep, pitch);
        }
    }

    private void parseFiles(List<File> files, int pitchStep, int pitch) {
        int i;
        Sample sample;
        WaveHeader waveHeader;
        int sampleRate;
        File additionalFile;
        ArrayList<File> additionalFiles = new ArrayList<>();

        for (File file : files) {
            additionalFile = checkIsAdditionalSample(file);

            if (additionalFile != null) {
                additionalFiles.add(additionalFile);
            } else {
                for (i = 0; i < pitchStep; i++) {
                    try {
                        sampleRate = NoteFrequencyUtil.getPitchedSampleRate(Config.SAMPLE_RATE, i + pitch);
                        waveHeader = new WaveHeader(file);
                        sample = new Sample(getMainModel().getNoteIdOfNextSample());
                        sample.pitch = i;
                        sample.groupID = getMainModel().sampleOptions.groupID;
                        sample.dynamic = getMainModel().sampleOptions.isDynamic;
                        sample.panorama = getMainModel().sampleOptions.panorama;
                        sample.disableNoteOff = getMainModel().sampleOptions.playThrough;
                        sample.fileName = StringUtils.removeFileExtension(file.getName());
                        sample.fileRealName = sample.fileName;
                        sample.filePath = file.getPath();
                        sample.waveHeader = waveHeader;
                        sample.channels = (getMainModel().sampleOptions.stereo && sample.waveHeader.channels == 2) ? 2 : 1;
                        sample.samplesData = convertWAVEData(file, sampleRate, sample.channels);
                        parseLoopPoints(sample, sampleRate, Sample.EXT_DEFAULT, sample.waveHeader.channels);
                        if (sample.loop != null) {
                            sample.loopEnabled = getMainModel().sampleOptions.loopEnabled;
                        }
                        if (i > 0) {
                            sample.fileName += String.format(Strings.SAMPLE_PITCHED, i);
                        }

                        getMainModel().addSample(sample);
                    } catch (OutOfMemoryError e) {
                        showMessageBoxError(Strings.OUT_OF_MEMORY_ERROR);
                        return;
                    } catch (Error | Exception e) {
                        String message = String.format(Strings.WAV_FILE_ERROR, e.getMessage());
                        showMessageBoxError(message);
                        break;
                    }
                }
            }
        }

        for (File file : additionalFiles) {
            manageAdditionalSample(file, null, pitch);
        }
    }

    private File checkIsAdditionalSample(File file) {
        String extStr;
        String fileName;

        fileName = StringUtils.removeFileExtension(file.getName());
        extStr = fileName.substring(fileName.length() - 2, fileName.length());

        return (extStr.equals(Sample.EXT_P) || extStr.equals(Sample.EXT_F)) ? file : null;
    }

    public void manageAdditionalSample(File file, String fileName, int pitch) {
        String extStr;
        String fileNameNoExt;
        int sampleRate;
        WaveHeader waveHeader;
        Sample sample;
        boolean managed = false;

        if (fileName == null) {
            fileName = StringUtils.removeFileExtension(file.getName());
        }
        extStr = fileName.substring(fileName.length() - 2, fileName.length());

        fileNameNoExt = fileName.substring(0, fileName.length() - 2);
        for (String key : getMainModel().samples.keySet()) {
            sample = getMainModel().samples.get(key);
            if (sample.fileRealName.equals(fileNameNoExt)) {
                try {
                    sampleRate = NoteFrequencyUtil.getPitchedSampleRate(Config.SAMPLE_RATE, sample.pitch + pitch);
                    waveHeader = new WaveHeader(file);
                    switch (extStr) {
                        case Sample.EXT_DEFAULT:
                            sample.waveHeader = waveHeader;
                            sample.samplesData = convertWAVEData(file, sampleRate, sample.channels);
                            break;
                        case Sample.EXT_P:
                            sample.waveHeaderP = waveHeader;
                            sample.samplesDataP = convertWAVEData(file, sampleRate, sample.channels);
                            break;
                        default:
                            sample.waveHeaderF = waveHeader;
                            sample.samplesDataF = convertWAVEData(file, sampleRate, sample.channels);
                            break;
                    }
                    parseLoopPoints(sample, sampleRate, extStr, sample.channels);

                    managed = true;
                } catch (Exception e) {
                    String message = String.format(Strings.WAV_FILE_ERROR, e.getMessage());
                    showMessageBoxError(message);
                    break;
                }
            }
        }

        if (managed == false) {
            String message = String.format(Strings.DEFAULT_SAMPLE_NOT_FOUND, fileName);
            showMessageBoxInfo(message);
        }
    }

    private void parseLoopPoints(Sample sample, int sampleRate, String extStr, int channels) {
        byte[] samplesData;
        WaveHeader waveHeader;
        SustainLoop loop = null;
        long endLoopByte;
        long startLoopByte;

        switch (extStr) {
            case Sample.EXT_P:
                waveHeader = sample.waveHeaderP;
                break;
            case Sample.EXT_F:
                waveHeader = sample.waveHeaderF;
                break;
            default:
                waveHeader = sample.waveHeader;
                break;
        }

        samplesData = getSamplesDataByExtension(extStr, sample);

        if (waveHeader.cuePoints != null && waveHeader.cuePoints.length > 0) {
            loop = new SustainLoop();
            loop.start = (long) ((float) samplesData.length / (float) waveHeader.dataSize * (float) waveHeader.cuePoints[0].frameOffset * channels);
            loop.end = samplesData.length / Config.BYTES_PER_SAMPLE;
        }
        if (waveHeader.samplerChk != null && waveHeader.samplerChk.sampleLoops != null && waveHeader.samplerChk.sampleLoops.length > 0) {
            for (SampleLoop sampleLoop : waveHeader.samplerChk.sampleLoops) {
                if (sampleLoop.playCount == 0) {
                    loop = new SustainLoop();
                    loop.start = (long) ((float) samplesData.length / (float) waveHeader.dataSize * (float) sampleLoop.start * channels);
                    loop.end = (long) ((float) samplesData.length / (float) waveHeader.dataSize * (float) sampleLoop.end * channels) + (1 * sample.channels);
                    break;
                }
            }
        }

        switch (extStr) {
            case Sample.EXT_P:
                sample.loopP = loop;
                break;
            case Sample.EXT_F:
                sample.loopF = loop;
                break;
            default:
                sample.loop = loop;
                break;
        }

        if (loop != null) {
            // Truncate sample by end of loop 
            samplesData = getSamplesDataByExtension(extStr, sample);
            endLoopByte = loop.end * Config.BYTES_PER_SAMPLE;

            if (samplesData.length > endLoopByte) {
                byte[] tmp = samplesData;

                samplesData = new byte[(int) endLoopByte];
                System.arraycopy(tmp, 0, samplesData, 0, (int) endLoopByte);

                if (!truncatedSampleMessageShown && sampleRate == Config.SAMPLE_RATE) {
                    truncatedSampleMessageShown = true;
                    showMessageBoxInfo(Strings.TRUNCATED_SAMPLE_ERROR);
                }
            }

            // Increase loop length
            if (loop.end - loop.start < Config.MIN_LOOP_LENGTH_SAMPLES) {
                samplesData = getSamplesDataByExtension(extStr, sample);

                byte[] tmp = samplesData;
                byte[] tail;
                int i;
                endLoopByte = loop.end * Config.BYTES_PER_SAMPLE;
                startLoopByte = loop.start * Config.BYTES_PER_SAMPLE;

                tail = new byte[(int) (endLoopByte - startLoopByte)];
                System.arraycopy(tmp, (int) startLoopByte, tail, 0, tail.length);
                i = (Config.MIN_LOOP_LENGTH_BYTES / tail.length) + 1;
                samplesData = new byte[samplesData.length + (tail.length * i)];
                System.arraycopy(tmp, 0, samplesData, 0, (int) startLoopByte);

                for (; i >= 0; i--) {
                    System.arraycopy(tail, 0, samplesData, (int) startLoopByte, tail.length);
                    startLoopByte += tail.length;
                }

                loop.end = samplesData.length / Config.BYTES_PER_SAMPLE;
            }

            switch (extStr) {
                case Sample.EXT_P:
                    sample.samplesDataP = samplesData;
                    break;
                case Sample.EXT_F:
                    sample.samplesDataF = samplesData;
                    break;
                default:
                    sample.samplesData = samplesData;
                    break;
            }
        }
    }

    private byte[] getSamplesDataByExtension(String extStr, Sample sample) {
        switch (extStr) {
            case Sample.EXT_P:
                return sample.samplesDataP;
            case Sample.EXT_F:
                return sample.samplesDataF;
            default:
                return sample.samplesData;
        }
    }

    private byte[] convertWAVEData(File waveFile, int sampleRate, int channels) throws UnsupportedAudioFileException, IOException {
        byte[] result;
        List<Byte> arrayList = new ArrayList<>();
        int readBytesCount;
        byte[] buffer = new byte[Config.RESAMPLER_BUFFER_SIZE];

        WaveFileReader reader = new WaveFileReader();
        try ( AudioInputStream audioIn = reader.getAudioInputStream(waveFile)) {
            AudioFormat format = new AudioFormat(sampleRate, Config.BIT_RATE, channels, true, false);
            try ( AudioInputStream resampler = AudioSystem.getAudioInputStream(format, audioIn)) {
                while (true) {
                    readBytesCount = resampler.read(buffer, 0, Config.RESAMPLER_BUFFER_SIZE);
                    for (int i = 0; i < readBytesCount; i++) {
                        arrayList.add(buffer[i]);
                    }
                    if (readBytesCount != Config.RESAMPLER_BUFFER_SIZE) {
                        break;
                    }
                }

                result = toPrimitives(arrayList);

                resampler.close();
            }

            audioIn.close();
        }

        arrayList.clear();

        if (getMainModel().sampleOptions.normalizeIndex > 0) {
            WavePCMNormalizeTool.normalize16Bit(result, getMainModel().sampleOptions.normalizeIndex - 1);
        }

        return result;
    }

    private byte[] toPrimitives(List<Byte> arrayList) {
        byte[] bytes = new byte[arrayList.size()];
        Iterator<Byte> iterator = arrayList.iterator();

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = iterator.next();
        }

        return bytes;
    }
}
