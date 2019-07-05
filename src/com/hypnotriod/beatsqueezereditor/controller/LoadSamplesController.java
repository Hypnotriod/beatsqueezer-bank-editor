package com.hypnotriod.beatsqueezereditor.controller;

import com.hypnotriod.beatsqueezereditor.base.BaseController;
import com.hypnotriod.beatsqueezereditor.constants.CConfig;
import com.hypnotriod.beatsqueezereditor.constants.CStrings;
import com.hypnotriod.beatsqueezereditor.facade.Facade;
import com.hypnotriod.beatsqueezereditor.model.vo.SampleLoopVO;
import com.hypnotriod.beatsqueezereditor.model.vo.SampleVO;
import com.hypnotriod.beatsqueezereditor.model.vo.SustainLoopVO;
import com.hypnotriod.beatsqueezereditor.model.vo.WaveHeaderVO;
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

    private boolean _truncatedSampleMessageShown = false;

    private final FileChooser.ExtensionFilter filter
            = new FileChooser.ExtensionFilter(CConfig.WAVE_FILE_BROWSE_NAME, CConfig.WAVE_FILE_BROWSE_FILTER_WAV, CConfig.WAVE_FILE_BROWSE_FILTER_WAVE);

    public LoadSamplesController(Facade facade) {
        super(facade);
    }

    public List<File> chooseFiles() {
        List<File> result;
        FileChooser fileChooser = getMainModel().getFileChooser();

        fileChooser.setTitle(CStrings.LOAD_SAMPLES);
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(filter);
        fileChooser.setInitialFileName(null);

        result = fileChooser.showOpenMultipleDialog(getFacade().primaryStage);

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
        SampleVO sampleVO;
        WaveHeaderVO waveHeaderVO;
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
                        sampleRate = NoteFrequencyUtil.getPitchedSampleRate(CConfig.SAMPLE_RATE, i + pitch);
                        waveHeaderVO = new WaveHeaderVO(file);
                        sampleVO = new SampleVO(getMainModel().getNoteIdOfNextSample());
                        sampleVO.pitch = i;
                        sampleVO.groupID = getMainModel().optionsVO.groupID;
                        sampleVO.dynamic = getMainModel().optionsVO.isDynamic;
                        sampleVO.panorama = getMainModel().optionsVO.panorama;
                        sampleVO.disableNoteOff = getMainModel().optionsVO.playThrough;
                        sampleVO.fileName = StringUtils.removeFileExtention(file.getName());
                        sampleVO.fileRealName = sampleVO.fileName;
                        sampleVO.filePath = file.getPath();
                        sampleVO.waveHeader = waveHeaderVO;
                        sampleVO.channels = (getMainModel().optionsVO.stereo && sampleVO.waveHeader.channels == 2) ? 2 : 1;
                        sampleVO.samplesData = convertWAVEData(file, sampleRate, sampleVO.channels);
                        parseLoopPoints(sampleVO, sampleRate, CConfig.EXT_DEFAULT, sampleVO.waveHeader.channels);
                        if (sampleVO.loop != null) {
                            sampleVO.loopEnabled = getMainModel().optionsVO.loopEnabled;
                        }
                        if (i > 0) {
                            sampleVO.fileName += String.format(CStrings.SAMPLE_PITCHED, i);
                        }

                        getMainModel().addSampleVO(sampleVO);
                    } catch (OutOfMemoryError e) {
                        getFacade().mainView.showMessageBoxError(CStrings.OUT_OF_MEMORY_ERROR);
                        return;
                    } catch (Error | Exception e) {
                        String message = String.format(CStrings.WAV_FILE_ERROR, e.getMessage());
                        getFacade().mainView.showMessageBoxError(message);
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

        fileName = StringUtils.removeFileExtention(file.getName());
        extStr = fileName.substring(fileName.length() - 2, fileName.length());

        return (extStr.equals(CConfig.EXT_P) || extStr.equals(CConfig.EXT_F)) ? file : null;
    }

    public void manageAdditionalSample(File file, String fileName, int pitch) {
        String extStr;
        String fileNameNoExt;
        int sampleRate;
        WaveHeaderVO waveHeaderVO;
        SampleVO sampleVO;
        boolean managed = false;

        if (fileName == null) {
            fileName = StringUtils.removeFileExtention(file.getName());
        }
        extStr = fileName.substring(fileName.length() - 2, fileName.length());

        fileNameNoExt = fileName.substring(0, fileName.length() - 2);
        for (String key : getMainModel().sampleVOs.keySet()) {
            sampleVO = getMainModel().sampleVOs.get(key);
            if (sampleVO.fileRealName.equals(fileNameNoExt)) {
                try {
                    sampleRate = NoteFrequencyUtil.getPitchedSampleRate(CConfig.SAMPLE_RATE, sampleVO.pitch + pitch);
                    waveHeaderVO = new WaveHeaderVO(file);
                    switch (extStr) {
                        case CConfig.EXT_DEFAULT:
                            sampleVO.waveHeader = waveHeaderVO;
                            sampleVO.samplesData = convertWAVEData(file, sampleRate, sampleVO.channels);
                            break;
                        case CConfig.EXT_P:
                            sampleVO.waveHeaderP = waveHeaderVO;
                            sampleVO.samplesDataP = convertWAVEData(file, sampleRate, sampleVO.channels);
                            break;
                        default:
                            sampleVO.waveHeaderF = waveHeaderVO;
                            sampleVO.samplesDataF = convertWAVEData(file, sampleRate, sampleVO.channels);
                            break;
                    }
                    parseLoopPoints(sampleVO, sampleRate, extStr, sampleVO.channels);

                    managed = true;
                } catch (Exception e) {
                    String message = String.format(CStrings.WAV_FILE_ERROR, e.getMessage());
                    getFacade().mainView.showMessageBoxError(message);
                    break;
                }
            }
        }

        if (managed == false) {
            String message = String.format(CStrings.DEFAULT_SAMPLE_NOT_FOUND, fileName);
            getFacade().mainView.showMessageBoxInfo(message);
        }
    }

    private void parseLoopPoints(SampleVO sampleVO, int sampleRate, String extStr, int channels) {
        byte[] samplesData;
        WaveHeaderVO waveHeader;
        SustainLoopVO loop = null;
        long endLoopByte;
        long startLoopByte;

        if (extStr.equals(CConfig.EXT_P)) {
            waveHeader = sampleVO.waveHeaderP;
        } else if (extStr.equals(CConfig.EXT_F)) {
            waveHeader = sampleVO.waveHeaderF;
        } else {
            waveHeader = sampleVO.waveHeader;
        }

        samplesData = getSamplesDataByExtention(extStr, sampleVO);

        if (waveHeader.cuePoints != null && waveHeader.cuePoints.length > 0) {
            loop = new SustainLoopVO();
            loop.start = (long) ((float) samplesData.length / (float) waveHeader.dataSize * (float) waveHeader.cuePoints[0].frameOffset * channels);
            loop.end = samplesData.length / CConfig.BYTES_PER_SAMPLE;
        }
        if (waveHeader.samplerChk != null && waveHeader.samplerChk.sampleLoops != null && waveHeader.samplerChk.sampleLoops.length > 0) {
            for (SampleLoopVO sampleLoop : waveHeader.samplerChk.sampleLoops) {
                if (sampleLoop.playCount == 0) {
                    loop = new SustainLoopVO();
                    loop.start = (long) ((float) samplesData.length / (float) waveHeader.dataSize * (float) sampleLoop.start * channels);
                    loop.end = (long) ((float) samplesData.length / (float) waveHeader.dataSize * (float) sampleLoop.end * channels) + (1 * sampleVO.channels);
                    break;
                }
            }
        }

        if (extStr.equals(CConfig.EXT_P)) {
            sampleVO.loopP = loop;
        } else if (extStr.equals(CConfig.EXT_F)) {
            sampleVO.loopF = loop;
        } else {
            sampleVO.loop = loop;
        }

        if (loop != null) {
            // Truncate sample by end of loop 
            samplesData = getSamplesDataByExtention(extStr, sampleVO);
            endLoopByte = loop.end * CConfig.BYTES_PER_SAMPLE;

            if (samplesData.length > endLoopByte) {
                byte[] tmp = samplesData;

                samplesData = new byte[(int) endLoopByte];
                System.arraycopy(tmp, 0, samplesData, 0, (int) endLoopByte);

                if (!_truncatedSampleMessageShown && sampleRate == CConfig.SAMPLE_RATE) {
                    _truncatedSampleMessageShown = true;
                    getFacade().mainView.showMessageBoxInfo(CStrings.TRUNCATED_SAMPLE_ERROR);
                }
            }

            // Increase loop length
            if (loop.end - loop.start < CConfig.MIN_LOOP_LENGTH_SAMPLES) {
                samplesData = getSamplesDataByExtention(extStr, sampleVO);

                byte[] tmp = samplesData;
                byte[] tail;
                int i;
                endLoopByte = loop.end * CConfig.BYTES_PER_SAMPLE;
                startLoopByte = loop.start * CConfig.BYTES_PER_SAMPLE;

                tail = new byte[(int) (endLoopByte - startLoopByte)];
                System.arraycopy(tmp, (int) startLoopByte, tail, 0, tail.length);
                i = (CConfig.MIN_LOOP_LENGTH_BYTES / tail.length) + 1;
                samplesData = new byte[samplesData.length + (tail.length * i)];
                System.arraycopy(tmp, 0, samplesData, 0, (int) startLoopByte);

                for (; i >= 0; i--) {
                    System.arraycopy(tail, 0, samplesData, (int) startLoopByte, tail.length);
                    startLoopByte += tail.length;
                }

                loop.end = samplesData.length / CConfig.BYTES_PER_SAMPLE;
            }

            if (extStr.equals(CConfig.EXT_P)) {
                sampleVO.samplesDataP = samplesData;
            } else if (extStr.equals(CConfig.EXT_F)) {
                sampleVO.samplesDataF = samplesData;
            } else {
                sampleVO.samplesData = samplesData;
            }
        }
    }

    private byte[] getSamplesDataByExtention(String extStr, SampleVO sampleVO) {
        if (extStr.equals(CConfig.EXT_P)) {
            return sampleVO.samplesDataP;
        } else if (extStr.equals(CConfig.EXT_F)) {
            return sampleVO.samplesDataF;
        } else {
            return sampleVO.samplesData;
        }
    }

    private byte[] convertWAVEData(File waveFile, int sampleRate, int channels) throws UnsupportedAudioFileException, IOException {
        byte[] result;
        List<Byte> arrayList = new ArrayList<>();
        int readBytesCount;
        byte[] buffer = new byte[CConfig.RESAMPLER_BUFFER_SIZE];

        WaveFileReader reader = new WaveFileReader();
        try ( AudioInputStream audioIn = reader.getAudioInputStream(waveFile)) {
            AudioFormat format = new AudioFormat(sampleRate, CConfig.BIT_RATE, channels, true, false);
            try ( AudioInputStream resampler = AudioSystem.getAudioInputStream(format, audioIn)) {
                while (true) {
                    readBytesCount = resampler.read(buffer, 0, CConfig.RESAMPLER_BUFFER_SIZE);
                    for (int i = 0; i < readBytesCount; i++) {
                        arrayList.add(buffer[i]);
                    }
                    if (readBytesCount != CConfig.RESAMPLER_BUFFER_SIZE) {
                        break;
                    }
                }

                result = toPrimitives(arrayList);

                resampler.close();
            }

            audioIn.close();
        }

        arrayList.clear();

        if (getMainModel().optionsVO.normalizeIndex > 0) {
            WavePCMNormalizeTool.normalize16Bit(result, getMainModel().optionsVO.normalizeIndex - 1);
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
