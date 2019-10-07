package com.hypnotriod.beatsqueezereditor.model;

import com.hypnotriod.beatsqueezereditor.base.BaseModel;
import com.hypnotriod.beatsqueezereditor.constants.Notes;
import com.hypnotriod.beatsqueezereditor.model.entity.SampleOptions;
import com.hypnotriod.beatsqueezereditor.model.entity.Sample;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javafx.stage.FileChooser;

/**
 *
 * @author Ilya Pikin
 */
public class MainModel extends BaseModel {

    private final FileChooser fileChooser = new FileChooser();
    public final HashMap<String, Sample> samples = new HashMap<>();
    public final SampleOptions sampleOptions = new SampleOptions();

    private long itemIdCounter = 0;

    public MainModel() {
        for (int i = 0; i < sampleOptions.filtersValues.length; i++) {
            sampleOptions.filtersValues[i] = -1;
        }
    }

    public FileChooser getFileChooser() {
        File initialDirectory = fileChooser.getInitialDirectory();
        if (initialDirectory == null || initialDirectory.exists() == false) {
            fileChooser.setInitialDirectory(null);
        }

        return fileChooser;
    }

    public void addSample(Sample sample) {
        samples.put(getSampleItemId(), sample);
    }

    public int getNoteIdOfNextSample() {
        int result = sampleOptions.noteId++;
        if (sampleOptions.noteId >= Notes.NOTES_NAMES.length) {
            sampleOptions.noteId = Notes.NOTES_NAMES.length - 1;
        }
        return result;
    }

    public void clearAllSamples() {
        samples.entrySet().forEach((entry) -> {
            entry.getValue().dispose();
        });
        samples.clear();
    }

    public boolean stopAllSamples() {
        boolean wasPlayed = false;

        for (Map.Entry<String, Sample> entry : samples.entrySet()) {
            if (entry.getValue().isPlaying) {
                wasPlayed = true;
            }
            entry.getValue().isPlaying = false;
        }

        return wasPlayed;
    }

    public void deleteSample(String id) {
        samples.get(id).dispose();
        samples.remove(id);
    }

    public long getAllSamplesDataSize() {
        long result = 0;
        Sample sample;

        for (Map.Entry<String, Sample> entry : samples.entrySet()) {
            sample = entry.getValue();

            result += sample.samplesData.length;

            if (sample.samplesDataP != null) {
                result += sample.samplesDataP.length;
            }
            if (sample.samplesDataF != null) {
                result += sample.samplesDataF.length;
            }
        }

        return result;
    }

    private String getSampleItemId() {
        return String.valueOf(itemIdCounter++);
    }
}
