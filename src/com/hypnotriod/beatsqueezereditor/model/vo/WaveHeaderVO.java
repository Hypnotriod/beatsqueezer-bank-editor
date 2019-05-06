/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hypnotriod.beatsqueezereditor.model.vo;

import com.hypnotriod.beatsqueezereditor.tools.FileInputStreamBinaryReader;
import com.sun.media.sound.WaveFileReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author Ilya Pikin
 */
public class WaveHeaderVO {

    private static final String CHUNK_SMPL = "smpl";
    private static final String CHUNK_CUE = "cue ";
    private static final String CHUNK_LOOP = "loop";
    private static final String CHUNK_DATA = "data";

    public int channels;
    public int sampleRate;
    public int bitDepth;
    public int dataSize;

    public int cuePointsDataSize = 0;
    public int cuePointsNum = 0;
    public CuePointVO[] cuePoints = null;

    public SamplerChunkVO samplerChk = null;

    public WaveHeaderVO(File waveFile) throws FileNotFoundException, Exception {
        String chunkId;
        FileInputStream fileStream;
        FileInputStreamBinaryReader reader = null;

        try {
            parseHeader(waveFile);

            fileStream = new FileInputStream(waveFile);
            reader = new FileInputStreamBinaryReader(fileStream);

            fileStream.skip(36); //Pass Header

            while (true) {
                String[] chunks = {CHUNK_SMPL, CHUNK_CUE, CHUNK_LOOP, CHUNK_DATA};
                chunkId = searchForChunks(reader, chunks, 4);

                if (chunkId == null) {
                    break;
                } else if (samplerChk == null && chunkId.equals(CHUNK_SMPL)) {
                    parseSamplerChunk(reader);
                } else if (cuePoints == null && chunkId.equals(CHUNK_CUE)) {
                    parseCuePoints(reader);
                } else if (samplerChk == null && chunkId.equals(CHUNK_LOOP)) {
                    parseLoopChunk(reader);
                } else if (chunkId.equals(CHUNK_DATA)) {
                    dataSize = reader.readInt32();
                    fileStream.skip(dataSize); //Pass Data Chunk
                }
            }
        } finally {
            if (reader != null) {
                reader.dispose();
            }
        }
    }

    private String searchForChunks(FileInputStreamBinaryReader reader, String[] chunks, int available) throws IOException {
        char tmpChar;
        int charIndex = 0;
        int i = 0;

        while (reader.available() > available) {
            tmpChar = reader.readChar();

            while (true) {
                if (i >= chunks.length) {
                    i = 0;
                    break;
                } else if (chunks[i].charAt(charIndex) == tmpChar) {
                    charIndex++;
                    if (charIndex >= chunks[i].length()) {
                        return chunks[i];
                    } else {
                        tmpChar = reader.readChar();
                    }
                } else if (charIndex > 0) {
                    charIndex = 0;
                    i = 0;
                } else {
                    i++;
                    charIndex = 0;
                }
            }
        }
        return null;
    }

    private void parseHeader(File waveFile) throws IOException, UnsupportedAudioFileException {
        WaveFileReader waveFileReader = new WaveFileReader();
        AudioFormat format = waveFileReader.getAudioFileFormat(waveFile).getFormat();

        channels = format.getChannels();
        sampleRate = (int) format.getSampleRate();
        bitDepth = format.getSampleSizeInBits();
    }

    private void parseCuePoints(FileInputStreamBinaryReader reader) throws IOException {
        cuePointsDataSize = reader.readInt32(); 	// 4
        cuePointsNum = reader.readInt32();              // 4

        cuePoints = new CuePointVO[(int) cuePointsNum];
        for (int i = 0; i < cuePoints.length; i++) {
            cuePoints[i] = new CuePointVO();
            cuePoints[i].cuePointID = reader.readInt32(); 			// 4
            cuePoints[i].playOrderPosition = reader.readInt32();                // 4
            cuePoints[i].dataChunkID = reader.readInt32(); 			// 4
            cuePoints[i].chunkStart = reader.readInt32(); 			// 4
            cuePoints[i].blockStart = reader.readInt32(); 			// 4
            cuePoints[i].frameOffset = reader.readInt32(); 			// 4
        }
    }

    private void parseSamplerChunk(FileInputStreamBinaryReader reader) throws IOException {
        samplerChk = new SamplerChunkVO();
        samplerChk.chunkDataSize = reader.readInt32(); 			// 4
        samplerChk.manufacturer = reader.readInt32(); 			// 4
        samplerChk.product = reader.readInt32();                        // 4
        samplerChk.samplePeriod = reader.readInt32(); 			// 4
        samplerChk.MIDIUnityNote = reader.readInt32(); 			// 4
        samplerChk.MIDIPitchFraction = reader.readInt32(); 		// 4
        samplerChk.SMPTEFormat = reader.readInt32(); 			// 4
        samplerChk.SMPTEOffset = reader.readInt32(); 			// 4
        samplerChk.numSampleLoops = reader.readInt32(); 		// 4
        samplerChk.samplerData = reader.readInt32(); 			// 4

        if (samplerChk.numSampleLoops > 0) {
            samplerChk.sampleLoops = new SampleLoopVO[(int) samplerChk.numSampleLoops];
        }

        for (int i = 0; i < samplerChk.numSampleLoops; i++) {
            samplerChk.sampleLoops[i] = new SampleLoopVO();
            samplerChk.sampleLoops[i].cuePointID = reader.readInt32();	// 4
            samplerChk.sampleLoops[i].type = reader.readInt32();	// 4
            samplerChk.sampleLoops[i].start = reader.readInt32();	// 4
            samplerChk.sampleLoops[i].end = reader.readInt32();         // 4
            samplerChk.sampleLoops[i].fraction = reader.readInt32();	// 4
            samplerChk.sampleLoops[i].playCount = reader.readInt32();	// 4
        }
    }

    private void parseLoopChunk(FileInputStreamBinaryReader reader) throws IOException {
        samplerChk = new SamplerChunkVO();
        samplerChk.sampleLoops = new SampleLoopVO[1];
        samplerChk.sampleLoops[0] = new SampleLoopVO();

        reader.skip(4);
        samplerChk.sampleLoops[0].start = reader.readInt32();
        samplerChk.sampleLoops[0].end = reader.readInt32();
    }

    public void dispose() {
        if (samplerChk != null) {
            samplerChk.dispose();
        }

        cuePoints = null;
        samplerChk = null;
    }
}
