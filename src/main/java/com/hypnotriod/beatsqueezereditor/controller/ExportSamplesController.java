package com.hypnotriod.beatsqueezereditor.controller;

import com.hypnotriod.beatsqueezereditor.base.BaseController;
import com.hypnotriod.beatsqueezereditor.constants.FileExtensions;
import com.hypnotriod.beatsqueezereditor.constants.Notes;
import com.hypnotriod.beatsqueezereditor.constants.Strings;
import com.hypnotriod.beatsqueezereditor.facade.Facade;
import com.hypnotriod.beatsqueezereditor.model.entity.Sample;
import com.hypnotriod.beatsqueezereditor.model.entity.SustainLoop;
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

    private final FileChooser.ExtensionFilter extensionFilter
            = new FileChooser.ExtensionFilter(FileExtensions.WAVE_FILE_BROWSE_NAME, FileExtensions.WAVE_FILE_BROWSE_FILTER_WAV);

    public ExportSamplesController(Facade facade) {
        super(facade);
    }

    public boolean checkCondition() {
        String message;
        ArrayList<String> notesIDsMatches = new ArrayList<>();
        for (Map.Entry<String, Sample> entry : getMainModel().samples.entrySet()) {
            Sample sample = entry.getValue();
            if (notesIDsMatches.contains(Notes.NOTES_NAMES[sample.noteID])) {
                message = String.format(Strings.NOTE_IS_DUBLICATED, Notes.NOTES_NAMES[sample.noteID]);
                showMessageBoxInfo(message);
                return false;
            }
            notesIDsMatches.add(Notes.NOTES_NAMES[sample.noteID]);
        }

        if (getMainModel().samples.isEmpty()) {
            showMessageBoxInfo(Strings.NO_SAMPLES);
            return false;
        }

        return true;
    }

    public File chooseFile() {
        File result;
        FileChooser fileChooser = getMainModel().getFileChooser();
        String fileName = StringUtils.removeFileExtension(getMainModel().sampleOptions.fileName) + ".wav";

        fileChooser.setTitle(Strings.EXPORT_SAMPLES);
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(extensionFilter);
        fileChooser.setInitialFileName(fileName);
        result = fileChooser.showSaveDialog(getFacade().getPrimaryStage());

        if (result != null) {
            File existDirectory = result.getParentFile();
            fileChooser.setInitialDirectory(existDirectory);
        }

        return result;
    }

    public void saveSamples(File file) {
        String name = StringUtils.removeFileExtension(file.getPath());

        for (Map.Entry<String, Sample> entry : getMainModel().samples.entrySet()) {
            Sample sample = entry.getValue();

            if (sample.samplesData != null) {
                writeSample(StringUtils.getSampleName(name, sample.noteID) + ".wav", sample.samplesData, sample.channels, sample.loop, sample.noteID);
            }
            if (sample.samplesDataP != null) {
                writeSample(StringUtils.getSampleNameP(name, sample.noteID) + ".wav", sample.samplesDataP, sample.channels, sample.loopP, sample.noteID);
            }
            if (sample.samplesDataF != null) {
                writeSample(StringUtils.getSampleNameF(name, sample.noteID) + ".wav", sample.samplesDataF, sample.channels, sample.loopF, sample.noteID);
            }
        }
    }

    private void writeSample(String fullPath, byte[] samplesData, int channelsCount, SustainLoop loop, int noteID) {
        try {
            if (loop != null) {
                long[] cuePoints = {loop.start};
                WavFileWriter.writeWavSampleFile_16_44100(samplesData, fullPath, channelsCount, cuePoints, noteID);
            } else {
                WavFileWriter.writeWavSampleFile_16_44100(samplesData, fullPath, channelsCount, null, noteID);
            }
        } catch (OutOfMemoryError e) {
            showMessageBoxError(Strings.OUT_OF_MEMORY_ERROR);
        } catch (Error | Exception e) {
            showMessageBoxError(e.getMessage());
        }
    }
}
