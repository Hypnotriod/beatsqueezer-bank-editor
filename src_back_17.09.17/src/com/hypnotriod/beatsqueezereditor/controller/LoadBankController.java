
package com.hypnotriod.beatsqueezereditor.controller;

import com.hypnotriod.beatsqueezereditor.base.BaseController;
import com.hypnotriod.beatsqueezereditor.constants.CConfig;
import com.hypnotriod.beatsqueezereditor.constants.CNotes;
import com.hypnotriod.beatsqueezereditor.constants.CStrings;
import com.hypnotriod.beatsqueezereditor.facade.Facade;
import com.hypnotriod.beatsqueezereditor.model.vo.SampleVO;
import com.hypnotriod.beatsqueezereditor.model.vo.SustainLoopVO;
import com.hypnotriod.beatsqueezereditor.tools.StringUtils;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import javafx.stage.FileChooser;

/**
 *
 * @author Илья
 */
public class LoadBankController extends BaseController
{
    private final FileChooser.ExtensionFilter filter = 
            new FileChooser.ExtensionFilter(CConfig.BANK_FILE_BROWSE_NAME, CConfig.BANK_FILE_BROWSE_FILTER);
    
    public LoadBankController(Facade facade) {
        super(facade);
    }
    
    public File chooseFile()
    {
        File result;
        FileChooser fileChooser = getMainModel().getFileChooser();
        
        fileChooser.setTitle(CStrings.LOAD_BTSQ_FILE);
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(filter);
        fileChooser.setInitialFileName(null);
        result = fileChooser.showOpenDialog(getFacade().primaryStage);
        
        if(result != null){
            File existDirectory = result.getParentFile();
            fileChooser.setInitialDirectory(existDirectory);
        }
        
        if(result != null)
            getMainModel().optionsVO.fileName = result.getName();
        
        return result;
    }
    
    public void loadBank(File file)
    {
        RandomAccessFile randomAccessFile; 
        
        try {
            randomAccessFile = new RandomAccessFile(file, "r");
            if(managePresetData(randomAccessFile, StringUtils.removeFileExtention(file.getName()))) {
                getMainModel().optionsVO.fileName = file.getName();
            }
        } 
        catch (OutOfMemoryError e) {
            getFacade().mainView.showMessageBoxError(CStrings.OUT_OF_MEMORY_ERROR);
        }
        catch (Error | Exception e) {
           getFacade().mainView.showMessageBoxError(e.getMessage());
        }
    }
    
    private boolean managePresetData(RandomAccessFile reader, String fileName) throws IOException
    {
        int dataStartIndex;
        int firstByteAddress;
        int lastByteAddress;
        int loopByteAddress;
        int headerFirstChunk;
        int headerSecondChunk;
        int version;
        SustainLoopVO loop;
        long dataShift;
        byte[] buffer = new byte[CConfig.CONFIG_HEADER_CHUNK_SIZE];
        byte[] sampleBuffer;
        int[] filtersValues = getMainModel().optionsVO.filtersValues;
        byte tmpByte;
        SampleVO sampleVO;
        int i;

        reader.seek(0);
        reader.read(buffer, 0, CConfig.CONFIG_HEADER_CHUNK_SIZE);

        headerFirstChunk = ((buffer[0] & 0xFF) << 24) | ((buffer[1] & 0xFF) << 16) | ((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF);
        headerSecondChunk = ((buffer[4] & 0xFF) << 24) | ((buffer[5] & 0xFF) << 16) | ((buffer[6] & 0xFF) << 8) | (buffer[7] & 0xFF);
        version = ((buffer[8] & 0xFF) << 8) | (buffer[9] & 0xFF);
        dataStartIndex = ((buffer[10] & 0xFF) << 24) | ((buffer[11] & 0xFF) << 16) | ((buffer[12] & 0xFF) << 8) | (buffer[13] & 0xFF);

        if(headerFirstChunk != CConfig.HEADER_FIRST_CHUNK || headerSecondChunk != CConfig.HEADER_SECOND_CHUNK || version != CConfig.VERSION)
        {
            String message = String.format(CStrings.NOT_A_BANK, CConfig.VERSION);
            getFacade().mainView.showMessageBoxInfo(message);
            return false;
        }

        getMainModel().clearAllSamples();
        
        reader.seek(CConfig.CONFIG_HEADER_CHUNK_SIZE);
        for(i = 0; i < filtersValues.length; i++)
        {
            tmpByte = (byte)reader.readByte();
            filtersValues[i] = ((tmpByte & 0x80) == 0x80) 
                ? (tmpByte & 0x7F) + 1 : -1;
        }

        for(i = 0; i < CNotes.NOTES_NAMES.length; i++)
        {
            dataShift = CConfig.CONFIG_HEADER_CHUNK_SIZE + CConfig.CONFIG_KNOBS_CHUNK_SIZE + i * CConfig.CONFIG_SAMPLE_CHUNK_SIZE;

            reader.seek(dataShift);
            reader.read(buffer, 0, CConfig.CONFIG_SAMPLE_CHUNK_SIZE);

            firstByteAddress = ((buffer[0] & 0xFF) << 24) | ((buffer[1] & 0xFF) << 16) | ((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF);
            lastByteAddress  = ((buffer[4] & 0xFF) << 24) | ((buffer[5] & 0xFF) << 16) | ((buffer[6] & 0xFF) << 8) | (buffer[7] & 0xFF);
            loopByteAddress  = ((buffer[8] & 0xFF) << 24) | ((buffer[9] & 0xFF) << 16) | ((buffer[10] & 0xFF) << 8) | (buffer[11] & 0xFF);

            if(firstByteAddress == 0) continue;

            sampleBuffer = new byte[lastByteAddress - firstByteAddress];
            reader.seek((firstByteAddress));
            reader.read(sampleBuffer, 0, (lastByteAddress - firstByteAddress));

            if(loopByteAddress > 0){
                loopByteAddress -= firstByteAddress;
                loop = new SustainLoopVO();
                loop.start = (int)((loopByteAddress) / CConfig.BYTES_PER_SAMPLE);
                loop.end = sampleBuffer.length / 2;
            }
            else{
                loop = null;
            }

            sampleVO = new SampleVO(i);
            sampleVO.fileName 		= StringUtils.getSampleName(fileName, i);
            sampleVO.fileRealName       = sampleVO.fileName;
            sampleVO.channels           = (buffer[12] & 0xFF) == 255 ? 2 : 1;
            sampleVO.panorama 		= (buffer[12] & 0xFF) == 255 ? 0 : (buffer[12] & 0xFF) - CConfig.PANORAMA_MAX_VALUE;
            sampleVO.groupID  		= (buffer[13] & 0x1F);
            sampleVO.dynamic 		= (buffer[13] & 0x20) == 0x20;
            sampleVO.disableNoteOff 	= (buffer[13] & 0x40) == 0x40;
            sampleVO.samplesData	= sampleBuffer;
            sampleVO.loop               = loop;
            sampleVO.loopEnabled        = ((buffer[13] & 0x80) == 0x80);
            getMainModel().addSampleVO(sampleVO);
            
            if(dataStartIndex == CConfig.DATA_START_INDEX_V1) continue;
            dataShift = CConfig.CONFIG_PF_START_INDEX + i * CConfig.CONFIG_PF_SAMPLE_CHUNK_SIZE;

            reader.seek(dataShift);
            reader.read(buffer, 0, CConfig.CONFIG_PF_SAMPLE_CHUNK_SIZE);
            
            firstByteAddress = lastByteAddress;
            lastByteAddress  = ((buffer[0] & 0xFF) << 24) | ((buffer[1] & 0xFF) << 16) | ((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF);
            loopByteAddress  = ((buffer[4] & 0xFF) << 24) | ((buffer[5] & 0xFF) << 16) | ((buffer[6] & 0xFF) << 8) | (buffer[7] & 0xFF);
            
            if(firstByteAddress != lastByteAddress)
            {
                sampleBuffer = new byte[lastByteAddress - firstByteAddress];
                reader.seek((firstByteAddress));
                reader.read(sampleBuffer, 0, (lastByteAddress - firstByteAddress));

                if(loopByteAddress > 0){
                    loopByteAddress -= firstByteAddress;
                    loop = new SustainLoopVO();
                    loop.start = (int)((loopByteAddress) / CConfig.BYTES_PER_SAMPLE);
                    loop.end = sampleBuffer.length / 2;
                }
                else{
                    loop = null;
                }
                
                sampleVO.samplesDataP	= sampleBuffer;
                sampleVO.loopP          = loop;
            }
            
            firstByteAddress = lastByteAddress;
            lastByteAddress  = ((buffer[8] & 0xFF) << 24) | ((buffer[9] & 0xFF) << 16) | ((buffer[10] & 0xFF) << 8) | (buffer[11] & 0xFF);
            loopByteAddress  = ((buffer[12] & 0xFF) << 24) | ((buffer[13] & 0xFF) << 16) | ((buffer[14] & 0xFF) << 8) | (buffer[15] & 0xFF);
            
            if(lastByteAddress != 0)
            {
                sampleBuffer = new byte[lastByteAddress - firstByteAddress];
                reader.seek((firstByteAddress));
                reader.read(sampleBuffer, 0, (lastByteAddress - firstByteAddress));

                if(loopByteAddress > 0){
                    loopByteAddress -= firstByteAddress;
                    loop = new SustainLoopVO();
                    loop.start = (int)((loopByteAddress) / CConfig.BYTES_PER_SAMPLE);
                    loop.end = sampleBuffer.length / 2;
                }
                else{
                    loop = null;
                }
                
                sampleVO.samplesDataF	= sampleBuffer;
                sampleVO.loopF          = loop;
            }
        }
        
        return true;
    }
}
