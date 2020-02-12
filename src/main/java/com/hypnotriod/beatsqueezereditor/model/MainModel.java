package com.hypnotriod.beatsqueezereditor.model;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.hypnotriod.beatsqueezereditor.base.BaseModel;
import com.hypnotriod.beatsqueezereditor.constants.Notes;
import com.hypnotriod.beatsqueezereditor.constants.Resources;
import com.hypnotriod.beatsqueezereditor.model.entity.SampleOptions;
import com.hypnotriod.beatsqueezereditor.model.entity.Sample;
import com.hypnotriod.beatsqueezereditor.model.entity.Settings;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Ilya Pikin
 */
public class MainModel extends BaseModel {

    private final FileChooser fileChooser = new FileChooser();
    public final HashMap<String, Sample> samples = new HashMap<>();
    public final SampleOptions sampleOptions = new SampleOptions();

    private final Gson gson = new Gson();
    private Settings settings = new Settings();
    private long itemIdCounter = 0;

    public MainModel() {
        loadSettings();
        sampleOptions.noteNamesDisplay = getNoteNamesDisplay();
        for (int i = 0; i < sampleOptions.filtersValues.length; i++) {
            sampleOptions.filtersValues[i] = -1;
        }
    }

    public String[] getNoteNamesDisplay() {
        switch (settings.noteNamesDisplay) {
            case C4:
                return Notes.NOTES_NAMES_C4;
            case C3:
                return Notes.NOTES_NAMES_C3;
            case NUMBERS:
                return Notes.NOTES_NAMES_NUMBERS;
            case PERCUSSION:
                return Notes.NOTES_NAMES_PERCUSSION;
            case C5:
            default:
                return Notes.NOTES_NAMES_C5;
        }
    }

    public int getNoteNamesDisplaySelectionIndex() {
        switch (settings.noteNamesDisplay) {
            case C4:
                return 1;
            case C3:
                return 2;
            case NUMBERS:
                return 3;
            case PERCUSSION:
                return 4;
            case C5:
            default:
                return 0;
        }
    }

    public void updateNoteNamesDisplayBySelectionIndex(int index) {
        switch (index) {
            case 1:
                settings.noteNamesDisplay = Notes.NoteNamesDisplay.C4;
                break;
            case 2:
                settings.noteNamesDisplay = Notes.NoteNamesDisplay.C3;
                break;
            case 3:
                settings.noteNamesDisplay = Notes.NoteNamesDisplay.NUMBERS;
                break;
            case 4:
                settings.noteNamesDisplay = Notes.NoteNamesDisplay.PERCUSSION;
                break;
            case 0:
            default:
                settings.noteNamesDisplay = Notes.NoteNamesDisplay.C5;
                break;
        }
        sampleOptions.noteNamesDisplay = getNoteNamesDisplay();
        saveSettings();
    }

    public FileChooser getFileChooser() {
        File initialDirectory = new File(settings.lastBrowsingPath);
        fileChooser.setInitialDirectory(initialDirectory.exists() ? initialDirectory : null);

        return fileChooser;
    }

    public void setInitialDirectoryForFileChooser(File file) {
        settings.lastBrowsingPath = file.getPath();
        saveSettings();
    }

    public void addSample(Sample sample) {
        samples.put(getSampleItemId(), sample);
    }

    public int getNoteIdOfNextSample() {
        int result = sampleOptions.noteId++;
        if (sampleOptions.noteId >= Notes.NOTE_NAMES_NUMBER) {
            sampleOptions.noteId = Notes.NOTE_NAMES_NUMBER - 1;
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

    private void saveSettings() {
        String settingsJSON = gson.toJson(settings);
        try {
            FileUtils.writeStringToFile(new File(Resources.PATH_SETTINGS), settingsJSON);
        } catch (IOException ex) {
            Logger.getLogger(MainModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadSettings() {
        File file = new File(Resources.PATH_SETTINGS);
        if (file.exists()) {
            try {
                String settingsJSON = FileUtils.readFileToString(file);
                settings = gson.fromJson(settingsJSON, Settings.class);
            } catch (IOException | JsonParseException ex) {
            }
        }
    }
}
