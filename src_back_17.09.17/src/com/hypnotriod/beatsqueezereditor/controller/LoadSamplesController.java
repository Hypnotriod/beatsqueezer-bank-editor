
package com.hypnotriod.beatsqueezereditor.controller;

import com.hypnotriod.beatsqueezereditor.base.BaseController;
import com.hypnotriod.beatsqueezereditor.constants.CConfig;
import com.hypnotriod.beatsqueezereditor.constants.CStrings;
import com.hypnotriod.beatsqueezereditor.facade.Facade;
import com.hypnotriod.beatsqueezereditor.model.vo.SampleLoopVO;
import com.hypnotriod.beatsqueezereditor.model.vo.SampleVO;
import com.hypnotriod.beatsqueezereditor.model.vo.SustainLoopVO;
import com.hypnotriod.beatsqueezereditor.model.vo.WaveHeaderVO;
import com.hypnotriod.beatsqueezereditor.tools.NoteFrequencyUtil;
import com.hypnotriod.beatsqueezereditor.tools.StringUtils;
import com.sun.media.sound.WaveFileReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.stage.FileChooser;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author Илья
 */
public class LoadSamplesController extends BaseController
{
    private boolean _truncatedSampleMessageShown = false;
    
    private final FileChooser.ExtensionFilter filter = 
            new FileChooser.ExtensionFilter(CConfig.WAVE_FILE_BROWSE_NAME, CConfig.WAVE_FILE_BROWSE_FILTER_WAV, CConfig.WAVE_FILE_BROWSE_FILTER_WAVE);
    
    public LoadSamplesController(Facade facade) {
        super(facade);
    }
    
    public List<File> chooseFiles()
    {
        List<File> result;
        FileChooser fileChooser = getMainModel().getFileChooser();
        
        fileChooser.setTitle(CStrings.LOAD_SAMPLES);
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(filter);
        fileChooser.setInitialFileName(null);
        
        result = fileChooser.showOpenMultipleDialog(getFacade().primaryStage);
        
        if(result != null && result.size() > 0){
            File existDirectory = result.get(0).getParentFile();
            fileChooser.setInitialDirectory(existDirectory);
        }
        
        return result;
    }
    
    public void loadSamples(List<File> files, int pitchStep)
    {
        if(files != null && files.size() > 0) {
            parseFiles(files, pitchStep);
        }
    }
    
    private void parseFiles(List<File> files, int pitchStep)
    {
        int i;
        SampleVO sampleVO;
        WaveHeaderVO waveHeaderVO;
        int sampleRate;
        File additionalFile;
        ArrayList<File> additionalFiles = new ArrayList<>();
        
        for(File file : files)
        {
            additionalFile = checkIsAdditionalSample(file);
            
            if(additionalFile != null)
            {
                additionalFiles.add(additionalFile);
            }
            else
            {
                for(i = 0; i < pitchStep; i++)
                {
                    try {
                        sampleRate = NoteFrequencyUtil.getPitchedSampleRate(CConfig.SAMPLE_RATE, i);
                        waveHeaderVO = new WaveHeaderVO(file);
                        sampleVO = new SampleVO(getMainModel().getNoteIdOfNextSample());
                        sampleVO.pitch = i;
                        sampleVO.groupID = getMainModel().optionsVO.groupID;
                        sampleVO.dynamic = getMainModel().optionsVO.isDynamic;
                        sampleVO.panorama = getMainModel().optionsVO.panorama;
                        sampleVO.disableNoteOff = getMainModel().optionsVO.playThrough;
                        sampleVO.fileName = StringUtils.removeFileExtention(file.getName());
                        sampleVO.fileRealName = sampleVO.fileName;
                        sampleVO.filePath = file.getPath();
                        sampleVO.waveHeader = waveHeaderVO;
                        sampleVO.channels = (getMainModel().optionsVO.stereo && sampleVO.waveHeader.channels == 2) ? 2 : 1;
                        sampleVO.samplesData =  convertWAVEData(file, sampleRate, sampleVO.channels);
                        parseLoopPoints(sampleVO, sampleRate, CConfig.EXT_DEFAULT, sampleVO.channels);
                        if(sampleVO.loop != null)
                            sampleVO.loopEnabled = getMainModel().optionsVO.loopEnabled;
                        if(i > 0)
                            sampleVO.fileName += String.format(CStrings.SAMPLE_PITCHED, i);

                        getMainModel().addSampleVO(sampleVO);
                    } 
                    catch (OutOfMemoryError e) {
                        getFacade().mainView.showMessageBoxError(CStrings.OUT_OF_MEMORY_ERROR);
                        return;
                    }
                    catch (Error | Exception e) {
                        String message = String.format(CStrings.WAV_FILE_ERROR, e.getMessage());
                        getFacade().mainView.showMessageBoxError(message);
                        break;
                    }
                }
            }
        }
        
        for(File file : additionalFiles)
        {
            manageAdditionalSample(file, null);
        }
    }
    
    private File checkIsAdditionalSample(File file)
    {
        String extStr;
        String fileName;
        
        fileName = StringUtils.removeFileExtention(file.getName());
        extStr = fileName.substring(fileName.length() - 2, fileName.length());
        
        return(extStr.equals(CConfig.EXT_P) || extStr.equals(CConfig.EXT_F)) ? file : null;
    }
    
    public void manageAdditionalSample(File file, String fileName)
    {
        String extStr;
        String fileNameNoExt;
        int sampleRate;
        WaveHeaderVO waveHeaderVO;
        SampleVO sampleVO;
        boolean managed = false;
        
        if(fileName == null)
            fileName = StringUtils.removeFileExtention(file.getName());
        extStr = fileName.substring(fileName.length() - 2, fileName.length());
                
        fileNameNoExt = fileName.substring(0, fileName.length() - 2);
        for (String key : getMainModel().sampleVOs.keySet()) 
        {
            sampleVO = getMainModel().sampleVOs.get(key);
            if(sampleVO.fileRealName.equals(fileNameNoExt))
            {
                try {
                    sampleRate = NoteFrequencyUtil.getPitchedSampleRate(CConfig.SAMPLE_RATE, sampleVO.pitch);
                    waveHeaderVO = new WaveHeaderVO(file);
                    if(extStr.equals(CConfig.EXT_P)) {
                        sampleVO.waveHeaderP = waveHeaderVO;
                        sampleVO.samplesDataP =  convertWAVEData(file, sampleRate, sampleVO.channels);
                    } else{
                        sampleVO.waveHeaderF = waveHeaderVO;
                        sampleVO.samplesDataF =  convertWAVEData(file, sampleRate, sampleVO.channels);
                    }
                    parseLoopPoints(sampleVO, sampleRate, extStr, sampleVO.channels);
                    
                    managed = true;
                }
                catch (Exception e) {
                    String message = String.format(CStrings.WAV_FILE_ERROR, e.getMessage());
                    getFacade().mainView.showMessageBoxError(message);
                    break;
                }
            }
        }
        
        if(managed == false) 
        {
            String message = String.format(CStrings.DEFAULT_SAMPLE_NOT_FOUND, fileName);
            getFacade().mainView.showMessageBoxInfo(message);
        }
    }
    
    private void parseLoopPoints(SampleVO sampleVO, int sampleRate, String extStr, int channels)
    {
        byte[] samplesData;
        WaveHeaderVO waveHeader;
        SustainLoopVO loop = null;
        long endLoopByte;
        
        if(extStr.equals(CConfig.EXT_P)) waveHeader = sampleVO.waveHeaderP;
        else if(extStr.equals(CConfig.EXT_F)) waveHeader = sampleVO.waveHeaderF;
        else waveHeader = sampleVO.waveHeader;
        
        if(waveHeader.cuePoints != null && waveHeader.cuePoints.length > 0) 
        {
            loop = new SustainLoopVO();
            loop.start
                    = (long)((float)sampleRate / (float)waveHeader.sampleRate * (float)waveHeader.cuePoints[0].frameOffset * channels);
            loop.end
                    = (long)((float)sampleRate / (float)waveHeader.sampleRate * (float)waveHeader.dataSize) / waveHeader.channels / (waveHeader.bitDepth / 8) * channels;
        }
        if(waveHeader.samplerChk != null && waveHeader.samplerChk.sampleLoops != null && waveHeader.samplerChk.sampleLoops.length > 0) 
        {
            for (SampleLoopVO sampleLoop : waveHeader.samplerChk.sampleLoops) {
                if (sampleLoop.playCount == 0) {
                    loop = new SustainLoopVO();
                    loop.start = (long) ((float)sampleRate / (float)waveHeader.sampleRate * (float) sampleLoop.start * channels);
                    loop.end = (long) ((float)sampleRate / (float)waveHeader.sampleRate * (float) sampleLoop.end * channels) + (1 * channels);
                    break;
                }
            }
        }
        
        if(extStr.equals(CConfig.EXT_P)) sampleVO.loopP = loop;
        else if(extStr.equals(CConfig.EXT_F)) sampleVO.loopF = loop;
        else sampleVO.loop = loop;
        
        // Truncate sample by end of loop 
        if(loop != null)
        {
            if(extStr.equals(CConfig.EXT_P)) samplesData = sampleVO.samplesDataP;
            else if(extStr.equals(CConfig.EXT_F)) samplesData = sampleVO.samplesDataF;
            else samplesData = sampleVO.samplesData;
            endLoopByte = loop.end * CConfig.BYTES_PER_SAMPLE;
            if(samplesData.length > endLoopByte)
            {
                byte[] tmp = samplesData;

                samplesData = new byte[(int)endLoopByte];
                System.arraycopy(tmp, 0, samplesData, 0, (int)endLoopByte);

                if(!_truncatedSampleMessageShown && sampleRate == CConfig.SAMPLE_RATE) {
                    _truncatedSampleMessageShown = true;
                    getFacade().mainView.showMessageBoxInfo(CStrings.TRUNCATED_SAMPLE_ERROR);
                }
            }
        }
    }
    
    private byte[] convertWAVEData(File waveFile, int sampleRate, int channels) throws UnsupportedAudioFileException, IOException
    {
        byte[] result;
        List<Byte> arrayList = new ArrayList<>();
        int readBytesCount;
        byte[] buffer = new byte[CConfig.RESAMPLER_BUFFER_SIZE];
        
        WaveFileReader reader = new WaveFileReader();
        try (AudioInputStream audioIn = reader.getAudioInputStream(waveFile))
        {
            AudioFormat format = new AudioFormat(sampleRate, CConfig.BIT_RATE, channels, true, false);
            try (AudioInputStream resampler = AudioSystem.getAudioInputStream(format, audioIn)) {
                while(true)
                {
                    readBytesCount = resampler.read(buffer, 0, CConfig.RESAMPLER_BUFFER_SIZE);
                    for(int i = 0; i < readBytesCount; i++)
                        arrayList.add(buffer[i]);
                    if(readBytesCount != CConfig.RESAMPLER_BUFFER_SIZE) break;
                }
                
                result = toPrimitives(arrayList.toArray(new Byte[arrayList.size()]));
                
                resampler.close();
            }
            
            audioIn.close();
        }
        
        arrayList.clear();
        
        return result;
    }
    
    private byte[] toPrimitives(Byte[] oBytes)
    {
        byte[] bytes = new byte[oBytes.length];
        
        for(int i = 0; i < oBytes.length; i++) {
            bytes[i] = oBytes[i];
        }

        return bytes;
    }
}
