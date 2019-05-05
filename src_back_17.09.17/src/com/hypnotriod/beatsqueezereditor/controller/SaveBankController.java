
package com.hypnotriod.beatsqueezereditor.controller;

import com.hypnotriod.beatsqueezereditor.base.BaseController;
import com.hypnotriod.beatsqueezereditor.constants.CConfig;
import com.hypnotriod.beatsqueezereditor.constants.CNotes;
import com.hypnotriod.beatsqueezereditor.constants.CStrings;
import com.hypnotriod.beatsqueezereditor.facade.Facade;
import com.hypnotriod.beatsqueezereditor.model.vo.SampleVO;
import com.hypnotriod.beatsqueezereditor.tools.ByteArrayTool;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Map;
import javafx.stage.FileChooser;

/**
 *
 * @author Илья
 */
public class SaveBankController extends BaseController {

    private final FileChooser.ExtensionFilter filter = 
            new FileChooser.ExtensionFilter(CConfig.BANK_FILE_BROWSE_NAME, CConfig.BANK_FILE_BROWSE_FILTER);
    
    public SaveBankController(Facade facade) {
        super(facade);
    }
    
    public boolean checkCondition()
    {
        String message;
        ArrayList<String> notesIDsMatches = new ArrayList<>();
        for(Map.Entry<String, SampleVO> entry : getMainModel().sampleVOs.entrySet())
        {
            SampleVO sampleVO = entry.getValue();
            if(notesIDsMatches.contains(CNotes.NOTES_NAMES[sampleVO.noteID]))
            {
                message = String.format(CStrings.NOTE_IS_DUBLICATED, CNotes.NOTES_NAMES[sampleVO.noteID]);
                getFacade().mainView.showMessageBoxInfo(message);
                return false;
            }

            if(   sampleVO.loop != null 
               && sampleVO.loopEnabled 
               && sampleVO.samplesData.length - (sampleVO.loop.start * CConfig.BYTES_PER_SAMPLE) <= CConfig.MIN_LOOP_SHIFT_BYTES)
            {
                message = String.format(CStrings.LOOP_TIME_ERROR, sampleVO.fileName);
                getFacade().mainView.showMessageBoxInfo(message);
                return false;
            }

            notesIDsMatches.add(CNotes.NOTES_NAMES[sampleVO.noteID]);
        }

        if(getMainModel().sampleVOs.isEmpty())
        {
            getFacade().mainView.showMessageBoxInfo(CStrings.BANK_IS_EMPTY);
            return false;
        }
        
        return true;
    }
    
    public File chooseFile()
    {
        File result;
        FileChooser fileChooser = getMainModel().getFileChooser();
        
        fileChooser.setTitle(CStrings.SAVE_BTSQ_FILE);
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(filter);
        fileChooser.setInitialFileName(getMainModel().optionsVO.fileName);
        result = fileChooser.showSaveDialog(getFacade().primaryStage);
        
        if(result != null) {
            getMainModel().optionsVO.fileName = result.getName();
            
            File existDirectory = result.getParentFile();
            fileChooser.setInitialDirectory(existDirectory);
        }
        
        return result;
    }
    
    public void saveBank(File file)
    {
        performSaveBank(file);
    }
    
    private void performSaveBank(File file)
    {
        byte[] data;
        long sampleLength;
        long loopAddress;
        long allSamplesDataOffset;
        long dataShift;
        int[] filtersValues = getMainModel().optionsVO.filtersValues;

        allSamplesDataOffset = CConfig.DATA_START_INDEX;
        try {
            data = new byte[(int)getMainModel().getAllSamplesDataSize() + CConfig.DATA_START_INDEX];
        } catch (OutOfMemoryError e) {
            getFacade().mainView.showMessageBoxError(CStrings.OUT_OF_MEMORY_ERROR);
            return;
        }

        // Add header
        ByteArrayTool.writeValueToByteArray(data, CConfig.HEADER_FIRST_CHUNK,  0, 4);
        ByteArrayTool.writeValueToByteArray(data, CConfig.HEADER_SECOND_CHUNK, 4, 4);

        // Add version
        ByteArrayTool.writeValueToByteArray(data, CConfig.VERSION, 8, 2);

        // Add all samples data begin
        ByteArrayTool.writeValueToByteArray(data, CConfig.DATA_START_INDEX, 10, 4);

        // Add all samples data size
        ByteArrayTool.writeValueToByteArray(data, getMainModel().getAllSamplesDataSize(), 14, 4);

        // Add all filters values
        for(int i = 0; i < filtersValues.length; i++)
        {
            data[i + CConfig.CONFIG_HEADER_CHUNK_SIZE] 
                = filtersValues[i] >= 0 ? (byte)(filtersValues[i] | 0x80) : 0x00;
        }

        // Add samples info and data
        for(Map.Entry<String, SampleVO> entry : getMainModel().sampleVOs.entrySet())
        {
            SampleVO sampleVO = entry.getValue();
            dataShift = CConfig.CONFIG_HEADER_CHUNK_SIZE + CConfig.CONFIG_KNOBS_CHUNK_SIZE + sampleVO.noteID * CConfig.CONFIG_SAMPLE_CHUNK_SIZE;

            // Add sample data first byte address
            ByteArrayTool.writeValueToByteArray(
                data, 
                allSamplesDataOffset, 
                dataShift, 
                4);

            // Add sample data last byte address
            sampleLength = sampleVO.samplesData.length;
            ByteArrayTool.writeValueToByteArray(
                data, 
                (sampleLength + allSamplesDataOffset),
                dataShift + 4, 
                4);

            // Add sample data loop byte address
            loopAddress = (sampleVO.loop != null) ? (sampleVO.loop.start * CConfig.BYTES_PER_SAMPLE * CConfig.CHANNELS_NUM + allSamplesDataOffset) : 0;
            ByteArrayTool.writeValueToByteArray(
                data, 
                loopAddress,
                dataShift + 8, 
                4);

            // Add panorama value
            ByteArrayTool.writeValueToByteArray(
                data, 
                sampleVO.channels == 1 ? (sampleVO.panorama + CConfig.PANORAMA_MAX_VALUE) : 255,
                dataShift + 12, 
                1);

            // Add config
            ByteArrayTool.writeValueToByteArray(
                data, 
                sampleVO.groupID |
                ((sampleVO.dynamic == true) ? 0x20 : 0x00) |
                ((sampleVO.disableNoteOff == true) ? 0x40 : 0x00) |
                ((sampleVO.loop != null && sampleVO.loopEnabled == true) ? 0x80 : 0x00),
                dataShift + 13, 
                1);

            // Write sample data
            allSamplesDataOffset = writeSampleData(allSamplesDataOffset, data, sampleVO.samplesData);
            
            dataShift = CConfig.CONFIG_PF_START_INDEX + sampleVO.noteID * CConfig.CONFIG_PF_SAMPLE_CHUNK_SIZE;
            if(sampleVO.samplesDataP != null) 
            {
                // Add sample Piano data last byte address
                sampleLength = sampleVO.samplesDataP.length;
                ByteArrayTool.writeValueToByteArray(
                    data, 
                    (sampleLength + allSamplesDataOffset),
                    dataShift, 
                    4);

                // Add sample Piano data loop byte address
                loopAddress = (sampleVO.loopP != null) ? (sampleVO.loopP.start * CConfig.BYTES_PER_SAMPLE * CConfig.CHANNELS_NUM + allSamplesDataOffset) : 0;
                ByteArrayTool.writeValueToByteArray(
                    data, 
                    loopAddress,
                    dataShift + 4, 
                    4);
                
                // Write Piano sample data
                allSamplesDataOffset = writeSampleData(allSamplesDataOffset, data, sampleVO.samplesDataP);
            }
            else
            {
                // Add sample Piano data last byte address
                ByteArrayTool.writeValueToByteArray(
                    data, 
                    allSamplesDataOffset,
                    dataShift, 
                    4);
            }
            
            if(sampleVO.samplesDataF != null) 
            {
                // Add sample Forte data last byte address
                sampleLength = sampleVO.samplesDataF.length;
                ByteArrayTool.writeValueToByteArray(
                    data, 
                    (sampleLength + allSamplesDataOffset),
                    dataShift + 8, 
                    4);

                // Add sample Forte data loop byte address
                loopAddress = (sampleVO.loopF != null) ? (sampleVO.loopF.start * CConfig.BYTES_PER_SAMPLE * CConfig.CHANNELS_NUM + allSamplesDataOffset) : 0;
                ByteArrayTool.writeValueToByteArray(
                    data, 
                    loopAddress,
                    dataShift + 12, 
                    4);
                
                // Write Forte sample data
                allSamplesDataOffset = writeSampleData(allSamplesDataOffset, data, sampleVO.samplesDataF);
            }
        }
        
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file.getPath());
            fos.write(data);
            fos.close();
        } catch (Exception e) {
            getFacade().mainView.showMessageBoxError(e.getMessage());
        }
    }
    
    private long writeSampleData(long allSamplesDataOffset, byte[] data, byte[] samplesData)
    {
        long currentSampleDataOffset;
        
        currentSampleDataOffset = 0;
        while(true)
        {
            if(currentSampleDataOffset < samplesData.length)
            {
                data[(int)allSamplesDataOffset] = samplesData[(int)currentSampleDataOffset];
            }
            /*else if(currentSampleDataOffset % CConfig.BLOCK_SIZE > 0)
            {
                data[(int)allSamplesDataOffset] = 0x00;
            }*/
            else
            {
                break;
            }

            currentSampleDataOffset++;
            allSamplesDataOffset++;
        }
        
        return allSamplesDataOffset;
    }
    
}
