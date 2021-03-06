package com.hypnotriod.beatsqueezereditor.controller;

import com.hypnotriod.beatsqueezereditor.base.BaseController;
import com.hypnotriod.beatsqueezereditor.constants.Config;
import com.hypnotriod.beatsqueezereditor.constants.FileExtensions;
import com.hypnotriod.beatsqueezereditor.constants.Strings;
import com.hypnotriod.beatsqueezereditor.facade.Facade;
import com.hypnotriod.beatsqueezereditor.model.entity.Sample;
import com.hypnotriod.beatsqueezereditor.utility.ByteArrayUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import javafx.stage.FileChooser;

/**
 *
 * @author Ilya Pikin
 */
public class SaveBankController extends BaseController {

    private final FileChooser.ExtensionFilter extensionFilter
            = new FileChooser.ExtensionFilter(FileExtensions.BANK_FILE_BROWSE_NAME, FileExtensions.BANK_FILE_BROWSE_FILTER);

    public SaveBankController(Facade facade) {
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

            if (sample.loop != null
                    && sample.isLoopEnabled
                    && sample.samplesData.length - (sample.loop.start * Config.BYTES_PER_SAMPLE) < Config.MIN_LOOP_LENGTH_BYTES) {
                message = String.format(Strings.LOOP_TIME_ERROR, sample.fileName);
                showMessageBoxInfo(message);
                return false;
            }

            notesIdsMatches.add(notesNames[sample.noteId]);
        }

        if (getMainModel().samples.isEmpty()) {
            showMessageBoxInfo(Strings.BANK_IS_EMPTY);
            return false;
        }

        return true;
    }

    public File chooseFile() {
        File result;
        FileChooser fileChooser = getMainModel().getFileChooser();

        fileChooser.setTitle(Strings.SAVE_BTSQ_FILE);
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(extensionFilter);
        fileChooser.setInitialFileName(getMainModel().sampleOptions.fileName);
        result = fileChooser.showSaveDialog(getFacade().getPrimaryStage());

        if (result != null) {
            getMainModel().setInitialDirectoryForFileChooser(result.getParentFile());
        }

        return result;
    }

    public void saveBank(File file) {
        getMainModel().sampleOptions.fileName = file.getName();
        performSaveBank(file);
    }

    private void performSaveBank(File file) {
        byte[] data;
        long sampleLength;
        long loopAddress;
        long allSamplesDataOffset;
        long dataShift;
        int[] filtersValues = getMainModel().sampleOptions.filtersValues;

        allSamplesDataOffset = Config.DATA_START_INDEX;
        try {
            data = new byte[(int) getMainModel().getAllSamplesDataSize() + Config.DATA_START_INDEX];
        } catch (OutOfMemoryError e) {
            showMessageBoxError(Strings.OUT_OF_MEMORY_ERROR);
            return;
        }

        // Add header
        ByteArrayUtil.writeValueToByteArray(data, Config.HEADER_FIRST_CHUNK, 0, 4);
        ByteArrayUtil.writeValueToByteArray(data, Config.HEADER_SECOND_CHUNK, 4, 4);

        // Add version
        ByteArrayUtil.writeValueToByteArray(data, Config.VERSION, 8, 2);

        // Add all samples data begin
        ByteArrayUtil.writeValueToByteArray(data, Config.DATA_START_INDEX, 10, 4);

        // Add all samples data size
        ByteArrayUtil.writeValueToByteArray(data, getMainModel().getAllSamplesDataSize(), 14, 4);

        // Add all filters values
        for (int i = 0; i < filtersValues.length; i++) {
            data[i + Config.HEADER_CHUNK_SIZE]
                    = filtersValues[i] >= 0 ? (byte) (filtersValues[i] | 0x80) : 0x00;
        }

        // Add samples info and data
        for (Map.Entry<String, Sample> entry : getMainModel().samples.entrySet()) {
            Sample sample = entry.getValue();
            dataShift = Config.HEADER_CHUNK_SIZE + Config.KNOBS_CHUNK_SIZE + sample.noteId * Config.SAMPLE_CHUNK_SIZE;

            // Add sample data first byte address
            ByteArrayUtil.writeValueToByteArray(
                    data,
                    allSamplesDataOffset,
                    dataShift,
                    4);

            // Add sample data last byte address
            sampleLength = sample.samplesData.length;
            ByteArrayUtil.writeValueToByteArray(
                    data,
                    (sampleLength + allSamplesDataOffset),
                    dataShift + 4,
                    4);

            // Add sample data loop byte address
            loopAddress = (sample.loop != null) ? (sample.loop.start * Config.BYTES_PER_SAMPLE * Config.CHANNELS_NUM + allSamplesDataOffset) : 0;
            ByteArrayUtil.writeValueToByteArray(
                    data,
                    loopAddress,
                    dataShift + 8,
                    4);

            // Add panorama value
            ByteArrayUtil.writeValueToByteArray(data,
                    sample.channels == 1 ? (sample.panorama + Config.PANORAMA_MAX_VALUE) : 255,
                    dataShift + 12,
                    1);

            // Add config
            ByteArrayUtil.writeValueToByteArray(
                    data,
                    sample.groupId
                    | ((sample.dynamic == true) ? 0x20 : 0x00)
                    | ((sample.disableNoteOff == true) ? 0x40 : 0x00)
                    | ((sample.loop != null && sample.isLoopEnabled == true) ? 0x80 : 0x00),
                    dataShift + 13,
                    1);

            // Write sample data
            allSamplesDataOffset = writeSampleData(allSamplesDataOffset, data, sample.samplesData);

            dataShift = Config.PIANO_FORTE_START_INDEX + sample.noteId * Config.PIANO_FORTE_SAMPLE_CHUNK_SIZE;
            if (sample.samplesDataP != null) {
                // Add sample Piano data last byte address
                sampleLength = sample.samplesDataP.length;
                ByteArrayUtil.writeValueToByteArray(
                        data,
                        (sampleLength + allSamplesDataOffset),
                        dataShift,
                        4);

                // Add sample Piano data loop byte address
                loopAddress = (sample.loopP != null) ? (sample.loopP.start * Config.BYTES_PER_SAMPLE * Config.CHANNELS_NUM + allSamplesDataOffset) : 0;
                ByteArrayUtil.writeValueToByteArray(
                        data,
                        loopAddress,
                        dataShift + 4,
                        4);

                // Write Piano sample data
                allSamplesDataOffset = writeSampleData(allSamplesDataOffset, data, sample.samplesDataP);
            } else {
                // Add sample Piano data last byte address
                ByteArrayUtil.writeValueToByteArray(
                        data,
                        allSamplesDataOffset,
                        dataShift,
                        4);
            }

            if (sample.samplesDataF != null) {
                // Add sample Forte data last byte address
                sampleLength = sample.samplesDataF.length;
                ByteArrayUtil.writeValueToByteArray(
                        data,
                        (sampleLength + allSamplesDataOffset),
                        dataShift + 8,
                        4);

                // Add sample Forte data loop byte address
                loopAddress = (sample.loopF != null) ? (sample.loopF.start * Config.BYTES_PER_SAMPLE * Config.CHANNELS_NUM + allSamplesDataOffset) : 0;
                ByteArrayUtil.writeValueToByteArray(
                        data,
                        loopAddress,
                        dataShift + 12,
                        4);

                // Write Forte sample data
                allSamplesDataOffset = writeSampleData(allSamplesDataOffset, data, sample.samplesDataF);
            }
        }

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file.getPath());
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            showMessageBoxError(e.getMessage());
        }
    }

    private long writeSampleData(long allSamplesDataOffset, byte[] data, byte[] samplesData) {
        long currentSampleDataOffset;

        currentSampleDataOffset = 0;
        while (true) {
            if (currentSampleDataOffset < samplesData.length) {
                data[(int) allSamplesDataOffset] = samplesData[(int) currentSampleDataOffset];
            } /*else if(currentSampleDataOffset % Config.BLOCK_SIZE > 0)
            {
                data[(int)allSamplesDataOffset] = 0x00;
            }*/ else {
                break;
            }

            currentSampleDataOffset++;
            allSamplesDataOffset++;
        }

        return allSamplesDataOffset;
    }

}
