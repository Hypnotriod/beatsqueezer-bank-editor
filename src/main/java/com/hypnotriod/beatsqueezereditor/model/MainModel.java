package com.hypnotriod.beatsqueezereditor.model;

import com.hypnotriod.beatsqueezereditor.base.BaseModel;
import com.hypnotriod.beatsqueezereditor.constants.CNotes;
import com.hypnotriod.beatsqueezereditor.model.vo.OptionsVO;
import com.hypnotriod.beatsqueezereditor.model.vo.SampleVO;
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
    public final HashMap<String, SampleVO> sampleVOs = new HashMap<>();
    public final OptionsVO optionsVO = new OptionsVO();

    private long _itemIDCounter = 0;

    public MainModel() {
        for (int i = 0; i < optionsVO.filtersValues.length; i++) {
            optionsVO.filtersValues[i] = -1;
        }
    }

    public FileChooser getFileChooser() {
        File initialDirectory = fileChooser.getInitialDirectory();
        if (initialDirectory == null || initialDirectory.exists() == false) {
            fileChooser.setInitialDirectory(null);
        }

        return fileChooser;
    }

    public void addSampleVO(SampleVO sampleVO) {
        sampleVOs.put(getSampleVOItemID(), sampleVO);
    }

    public int getNoteIdOfNextSample() {
        int result = optionsVO.noteID++;
        if (optionsVO.noteID >= CNotes.NOTES_NAMES.length) {
            optionsVO.noteID = CNotes.NOTES_NAMES.length - 1;
        }
        return result;
    }

    public void clearAllSamples() {
        for (Map.Entry<String, SampleVO> entry : sampleVOs.entrySet()) {
            entry.getValue().dispose();
        }
        sampleVOs.clear();
    }

    public boolean stopAllSamples() {
        boolean wasPlayed = false;

        for (Map.Entry<String, SampleVO> entry : sampleVOs.entrySet()) {
            if (entry.getValue().isPlaying) {
                wasPlayed = true;
            }
            entry.getValue().isPlaying = false;
        }

        return wasPlayed;
    }

    public void deleteSample(String id) {
        sampleVOs.get(id).dispose();
        sampleVOs.remove(id);
    }

    public long getAllSamplesDataSize() {
        long result = 0;
        SampleVO sampleVO;

        for (Map.Entry<String, SampleVO> entry : sampleVOs.entrySet()) {
            sampleVO = entry.getValue();

            result += sampleVO.samplesData.length;

            if (sampleVO.samplesDataP != null) {
                result += sampleVO.samplesDataP.length;
            }
            if (sampleVO.samplesDataF != null) {
                result += sampleVO.samplesDataF.length;
            }
        }

        return result;
    }

    private String getSampleVOItemID() {
        return String.valueOf(_itemIDCounter++);
    }
}
