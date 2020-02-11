package com.hypnotriod.beatsqueezereditor.controller;

import com.hypnotriod.beatsqueezereditor.base.BaseController;
import com.hypnotriod.beatsqueezereditor.constants.Config;
import com.hypnotriod.beatsqueezereditor.constants.FileExtensions;
import com.hypnotriod.beatsqueezereditor.constants.Notes;
import com.hypnotriod.beatsqueezereditor.constants.Strings;
import com.hypnotriod.beatsqueezereditor.facade.Facade;
import com.hypnotriod.beatsqueezereditor.model.entity.Sample;
import com.hypnotriod.beatsqueezereditor.model.entity.SustainLoop;
import com.hypnotriod.beatsqueezereditor.tools.StringUtils;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import javafx.stage.FileChooser;

/**
 *
 * @author Ilya Pikin
 */
public class LoadBankController extends BaseController {

    private final FileChooser.ExtensionFilter extensionFilter
            = new FileChooser.ExtensionFilter(FileExtensions.BANK_FILE_BROWSE_NAME, FileExtensions.BANK_FILE_BROWSE_FILTER);

    public LoadBankController(Facade facade) {
        super(facade);
    }

    public File chooseFile() {
        File result;
        FileChooser fileChooser = getMainModel().getFileChooser();

        fileChooser.setTitle(Strings.LOAD_BTSQ_FILE);
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(extensionFilter);
        fileChooser.setInitialFileName(null);
        result = fileChooser.showOpenDialog(getFacade().getPrimaryStage());

        return result;
    }

    public void loadBank(File file) {
        RandomAccessFile randomAccessFile;
        getMainModel().getFileChooser().setInitialDirectory(file.getParentFile());
        getMainModel().sampleOptions.fileName = file.getName();

        try {
            randomAccessFile = new RandomAccessFile(file, "r");
            if (managePresetData(randomAccessFile, StringUtils.removeFileExtension(file.getName()))) {
                getMainModel().sampleOptions.fileName = file.getName();
            }
        } catch (OutOfMemoryError e) {
            showMessageBoxError(Strings.OUT_OF_MEMORY_ERROR);
        } catch (Error | Exception e) {
            showMessageBoxError(e.getMessage());
        }
    }

    private boolean managePresetData(RandomAccessFile reader, String fileName) throws IOException {
        int dataStartIndex;
        int firstByteAddress;
        int lastByteAddress;
        int loopByteAddress;
        int headerFirstChunk;
        int headerSecondChunk;
        int version;
        SustainLoop loop;
        long dataShift;
        String[] notesNames = getMainModel().getNoteNamesDisplay();
        byte[] buffer = new byte[Config.HEADER_CHUNK_SIZE];
        byte[] sampleBuffer;
        int[] filtersValues = getMainModel().sampleOptions.filtersValues;
        byte tmpByte;
        Sample sample;
        int i;

        reader.seek(0);
        reader.read(buffer, 0, Config.HEADER_CHUNK_SIZE);

        headerFirstChunk = ((buffer[0] & 0xFF) << 24) | ((buffer[1] & 0xFF) << 16) | ((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF);
        headerSecondChunk = ((buffer[4] & 0xFF) << 24) | ((buffer[5] & 0xFF) << 16) | ((buffer[6] & 0xFF) << 8) | (buffer[7] & 0xFF);
        version = ((buffer[8] & 0xFF) << 8) | (buffer[9] & 0xFF);
        dataStartIndex = ((buffer[10] & 0xFF) << 24) | ((buffer[11] & 0xFF) << 16) | ((buffer[12] & 0xFF) << 8) | (buffer[13] & 0xFF);

        if (headerFirstChunk != Config.HEADER_FIRST_CHUNK || headerSecondChunk != Config.HEADER_SECOND_CHUNK || version != Config.VERSION) {
            String message = String.format(Strings.NOT_A_BANK, Config.VERSION);
            showMessageBoxInfo(message);
            return false;
        }

        getMainModel().clearAllSamples();

        reader.seek(Config.HEADER_CHUNK_SIZE);
        for (i = 0; i < filtersValues.length; i++) {
            tmpByte = (byte) reader.readByte();
            filtersValues[i] = ((tmpByte & 0x80) == 0x80) ? (tmpByte & 0x7F) : -1;
        }

        for (i = 0; i < Notes.NOTE_NAMES_NUMBER; i++) {
            dataShift = Config.HEADER_CHUNK_SIZE + Config.KNOBS_CHUNK_SIZE + i * Config.SAMPLE_CHUNK_SIZE;

            reader.seek(dataShift);
            reader.read(buffer, 0, Config.SAMPLE_CHUNK_SIZE);

            firstByteAddress = ((buffer[0] & 0xFF) << 24) | ((buffer[1] & 0xFF) << 16) | ((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF);
            lastByteAddress = ((buffer[4] & 0xFF) << 24) | ((buffer[5] & 0xFF) << 16) | ((buffer[6] & 0xFF) << 8) | (buffer[7] & 0xFF);
            loopByteAddress = ((buffer[8] & 0xFF) << 24) | ((buffer[9] & 0xFF) << 16) | ((buffer[10] & 0xFF) << 8) | (buffer[11] & 0xFF);

            if (firstByteAddress == 0) {
                continue;
            }

            sampleBuffer = new byte[lastByteAddress - firstByteAddress];
            reader.seek((firstByteAddress));
            reader.read(sampleBuffer, 0, (lastByteAddress - firstByteAddress));

            if (loopByteAddress > 0 && loopByteAddress - firstByteAddress < sampleBuffer.length) {
                loopByteAddress -= firstByteAddress;
                loop = new SustainLoop();
                loop.start = (int) ((loopByteAddress) / Config.BYTES_PER_SAMPLE);
                loop.end = sampleBuffer.length / 2;
            } else {
                loop = null;
            }

            sample = new Sample();
            sample.noteId = i;
            sample.fileName = StringUtils.getSampleName(fileName, i, notesNames);
            sample.fileRealName = sample.fileName;
            sample.channels = (buffer[12] & 0xFF) == 255 ? 2 : 1;
            sample.panorama = (buffer[12] & 0xFF) == 255 ? 0 : (buffer[12] & 0xFF) - Config.PANORAMA_MAX_VALUE;
            sample.groupId = (buffer[13] & 0x1F);
            sample.dynamic = (buffer[13] & 0x20) == 0x20;
            sample.disableNoteOff = (buffer[13] & 0x40) == 0x40;
            sample.samplesData = sampleBuffer;
            sample.loop = loop;
            sample.loopEnabled = ((buffer[13] & 0x80) == 0x80);
            getMainModel().addSample(sample);

            if (dataStartIndex == Config.DATA_START_INDEX_V1) {
                continue;
            }
            dataShift = Config.PIANO_FORTE_START_INDEX + i * Config.PIANO_FORTE_SAMPLE_CHUNK_SIZE;

            reader.seek(dataShift);
            reader.read(buffer, 0, Config.PIANO_FORTE_SAMPLE_CHUNK_SIZE);

            firstByteAddress = lastByteAddress;
            lastByteAddress = ((buffer[0] & 0xFF) << 24) | ((buffer[1] & 0xFF) << 16) | ((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF);
            loopByteAddress = ((buffer[4] & 0xFF) << 24) | ((buffer[5] & 0xFF) << 16) | ((buffer[6] & 0xFF) << 8) | (buffer[7] & 0xFF);

            if (firstByteAddress != lastByteAddress) {
                sampleBuffer = new byte[lastByteAddress - firstByteAddress];
                reader.seek((firstByteAddress));
                reader.read(sampleBuffer, 0, (lastByteAddress - firstByteAddress));

                if (loopByteAddress > 0) {
                    loopByteAddress -= firstByteAddress;
                    loop = new SustainLoop();
                    loop.start = (int) ((loopByteAddress) / Config.BYTES_PER_SAMPLE);
                    loop.end = sampleBuffer.length / 2;
                } else {
                    loop = null;
                }

                sample.samplesDataP = sampleBuffer;
                sample.loopP = loop;
            }

            firstByteAddress = lastByteAddress;
            lastByteAddress = ((buffer[8] & 0xFF) << 24) | ((buffer[9] & 0xFF) << 16) | ((buffer[10] & 0xFF) << 8) | (buffer[11] & 0xFF);
            loopByteAddress = ((buffer[12] & 0xFF) << 24) | ((buffer[13] & 0xFF) << 16) | ((buffer[14] & 0xFF) << 8) | (buffer[15] & 0xFF);

            if (lastByteAddress != 0) {
                sampleBuffer = new byte[lastByteAddress - firstByteAddress];
                reader.seek((firstByteAddress));
                reader.read(sampleBuffer, 0, (lastByteAddress - firstByteAddress));

                if (loopByteAddress > 0) {
                    loopByteAddress -= firstByteAddress;
                    loop = new SustainLoop();
                    loop.start = (int) ((loopByteAddress) / Config.BYTES_PER_SAMPLE);
                    loop.end = sampleBuffer.length / 2;
                } else {
                    loop = null;
                }

                sample.samplesDataF = sampleBuffer;
                sample.loopF = loop;
            }
        }

        return true;
    }
}
