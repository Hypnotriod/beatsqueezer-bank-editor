package com.hypnotriod.beatsqueezereditor.controller;

import com.hypnotriod.beatsqueezereditor.base.BaseController;
import com.hypnotriod.beatsqueezereditor.constants.CConfig;
import com.hypnotriod.beatsqueezereditor.constants.CNotes;
import com.hypnotriod.beatsqueezereditor.constants.CStrings;
import com.hypnotriod.beatsqueezereditor.facade.Facade;
import com.hypnotriod.beatsqueezereditor.model.vo.SampleVO;
import com.hypnotriod.beatsqueezereditor.model.vo.SustainLoopVO;
import com.hypnotriod.beatsqueezereditor.tools.StringUtils;
import com.hypnotriod.beatsqueezereditor.tools.WavFileWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import javafx.stage.FileChooser;

/**
 *
 * @author Ilya Pikin
 */
public class ExportSamplesController extends BaseController {

    private final FileChooser.ExtensionFilter filter
            = new FileChooser.ExtensionFilter(CConfig.WAVE_FILE_BROWSE_NAME, CConfig.WAVE_FILE_BROWSE_FILTER_WAV);

    public ExportSamplesController(Facade facade) {
        super(facade);
    }

    public boolean checkCondition() {
        String message;
        ArrayList<String> notesIDsMatches = new ArrayList<>();
        for (Map.Entry<String, SampleVO> entry : getMainModel().sampleVOs.entrySet()) {
            SampleVO sampleVO = entry.getValue();
            if (notesIDsMatches.contains(CNotes.NOTES_NAMES[sampleVO.noteID])) {
                message = String.format(CStrings.NOTE_IS_DUBLICATED, CNotes.NOTES_NAMES[sampleVO.noteID]);
                showMessageBoxInfo(message);
                return false;
            }
            notesIDsMatches.add(CNotes.NOTES_NAMES[sampleVO.noteID]);
        }

        if (getMainModel().sampleVOs.isEmpty()) {
            showMessageBoxInfo(CStrings.NO_SAMPLES);
            return false;
        }

        return true;
    }

    public File chooseFile() {
        File result;
        FileChooser fileChooser = getMainModel().getFileChooser();
        String fileName = StringUtils.removeFileExtention(getMainModel().optionsVO.fileName) + ".wav";

        fileChooser.setTitle(CStrings.EXPORT_SAMPLES);
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(filter);
        fileChooser.setInitialFileName(fileName);
        result = fileChooser.showSaveDialog(getFacade().getPrimaryStage());

        if (result != null) {
            File existDirectory = result.getParentFile();
            fileChooser.setInitialDirectory(existDirectory);
        }

        return result;
    }

    public void saveSamples(File file) {
        String name = StringUtils.removeFileExtention(file.getPath());

        for (Map.Entry<String, SampleVO> entry : getMainModel().sampleVOs.entrySet()) {
            SampleVO sampleVO = entry.getValue();

            if (sampleVO.samplesData != null) {
                writeSample(StringUtils.getSampleName(name, sampleVO.noteID) + ".wav", sampleVO.samplesData, sampleVO.channels, sampleVO.loop, sampleVO.noteID);
            }
            if (sampleVO.samplesDataP != null) {
                writeSample(StringUtils.getSampleNameP(name, sampleVO.noteID) + ".wav", sampleVO.samplesDataP, sampleVO.channels, sampleVO.loopP, sampleVO.noteID);
            }
            if (sampleVO.samplesDataF != null) {
                writeSample(StringUtils.getSampleNameF(name, sampleVO.noteID) + ".wav", sampleVO.samplesDataF, sampleVO.channels, sampleVO.loopF, sampleVO.noteID);
            }
        }
    }

    private void writeSample(String fullPath, byte[] samplesData, int channelsCount, SustainLoopVO loop, int noteID) {
        try {
            if (loop != null) {
                long[] cuePoints = {loop.start};
                WavFileWriter.writeWavSampleFile_16_44100(samplesData, fullPath, channelsCount, cuePoints, noteID);
            } else {
                WavFileWriter.writeWavSampleFile_16_44100(samplesData, fullPath, channelsCount, null, noteID);
            }
        } catch (OutOfMemoryError e) {
            showMessageBoxError(CStrings.OUT_OF_MEMORY_ERROR);
        } catch (Error | Exception e) {
            showMessageBoxError(e.getMessage());
        }
    }
}
