package com.hypnotriod.beatsqueezereditor.controller;

import com.hypnotriod.beatsqueezereditor.base.BaseController;
import com.hypnotriod.beatsqueezereditor.constants.FileExtensions;
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
        String[] notesNames = getMainModel().getNoteNamesDisplay();
        ArrayList<String> notesIdsMatches = new ArrayList<>();
        for (Map.Entry<String, Sample> entry : getMainModel().samples.entrySet()) {
            Sample sample = entry.getValue();
            if (notesIdsMatches.contains(notesNames[sample.noteId])) {
                message = String.format(Strings.NOTE_IS_DUBLICATED, notesNames[sample.noteId]);
                showMessageBoxInfo(message);
                return false;
            }
            notesIdsMatches.add(notesNames[sample.noteId]);
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
        String fileName = StringUtils.removeFileExtension(getMainModel().sampleOptions.fileName) + FileExtensions.WAV_FILE_EXTENSION;

        fileChooser.setTitle(Strings.EXPORT_SAMPLES);
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(extensionFilter);
        fileChooser.setInitialFileName(fileName);
        result = fileChooser.showSaveDialog(getFacade().getPrimaryStage());

        if (result != null) {
            getMainModel().setInitialDirectoryForFileChooser(result.getParentFile());
        }

        return result;
    }

    public void saveSamples(File file) {
        String name = StringUtils.removeFileExtension(file.getPath());
        String[] notesNames = getMainModel().getNoteNamesDisplay();

        for (Map.Entry<String, Sample> entry : getMainModel().samples.entrySet()) {
            Sample sample = entry.getValue();
            String sampleName;

            if (sample.samplesData != null) {
                sampleName = StringUtils.getSampleName(name, sample.noteId, notesNames) + FileExtensions.WAV_FILE_EXTENSION;
                writeSample(sampleName, sample.samplesData, sample.channels, sample.loop, sample.noteId);
            }
            if (sample.samplesDataP != null) {
                sampleName = StringUtils.getSampleNameP(name, sample.noteId, notesNames) + FileExtensions.WAV_FILE_EXTENSION;
                writeSample(sampleName, sample.samplesDataP, sample.channels, sample.loopP, sample.noteId);
            }
            if (sample.samplesDataF != null) {
                sampleName = StringUtils.getSampleNameF(name, sample.noteId, notesNames) + FileExtensions.WAV_FILE_EXTENSION;
                writeSample(sampleName, sample.samplesDataF, sample.channels, sample.loopF, sample.noteId);
            }
        }
    }

    private void writeSample(String fullPath, byte[] samplesData, int channelsCount, SustainLoop loop, int noteId) {
        try {
            if (loop != null) {
                long[] cuePoints = {loop.start};
                WavFileWriter.writeWavSampleFile_16_44100(samplesData, fullPath, channelsCount, cuePoints, noteId);
            } else {
                WavFileWriter.writeWavSampleFile_16_44100(samplesData, fullPath, channelsCount, null, noteId);
            }
        } catch (OutOfMemoryError e) {
            showMessageBoxError(Strings.OUT_OF_MEMORY_ERROR);
        } catch (Error | Exception e) {
            showMessageBoxError(e.getMessage());
        }
    }
}
