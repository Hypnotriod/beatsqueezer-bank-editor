package com.hypnotriod.beatsqueezereditor.tools;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author Ilya Pikin
 */
public class WavFileWriter {

    public static void writeWavSampleFile_16_44100(byte[] data, String fullPath, int channelsCount, long[] cuePointsPositions, int noteId) throws FileNotFoundException, IOException {
        boolean cuePoints = (cuePointsPositions != null && cuePointsPositions.length > 0);
        FileOutputStream fileStream = new FileOutputStream(fullPath);
        FileOutputStreamBinaryWriter binaryWriter = new FileOutputStreamBinaryWriter(fileStream);
        writeWavHeader(binaryWriter, false, channelsCount, 16, 44100, (data.length / 2), cuePoints ? cuePointsPositions.length : 0, cuePoints);
        binaryWriter.write(data, 0, data.length);
        if (cuePoints) {
            writeCuePoints(binaryWriter, cuePointsPositions, channelsCount);
            writeSustainLoop(binaryWriter, cuePointsPositions[0], (data.length / 2) - 1, channelsCount, noteId);
        }

        binaryWriter.flush();
        binaryWriter.dispose();
    }

    public static void writeWavHeader(FileOutputStreamBinaryWriter binaryWriter, boolean isFloatingPoint, int channelsCount, int bitDepth, int sampleRate, int totalSampleCount, int cuePointsNum, boolean sustainLoop) throws IOException {
        int chunkSize = ((bitDepth / 8) * totalSampleCount) + 36
                + ((cuePointsNum > 0) ? 12 + 24 * cuePointsNum : 0)
                + (sustainLoop ? 68 : 0);

        binaryWriter.writeString("RIFF");                                       // RIFF header.
        binaryWriter.writeInt32(chunkSize);                                     // Chunk size.
        binaryWriter.writeString("WAVE");                                       // Format.
        binaryWriter.writeString("fmt ");                                       // Sub-chunk 1. Sub-chunk 1 ID.
        binaryWriter.writeInt32(16);                                            // Sub-chunk 1 size.
        binaryWriter.writeInt16(isFloatingPoint ? (short) 3 : (short) 1);         // Audio format (floating point (3) or PCM (1)). Any other format indicates compression.
        binaryWriter.writeInt16((short) channelsCount);                          // Channels.
        binaryWriter.writeInt32(sampleRate);                                    // Sample rate.
        binaryWriter.writeInt32(sampleRate * channelsCount * (bitDepth / 8));   // Bytes rate.
        binaryWriter.writeInt16((short) (channelsCount * (bitDepth / 8)));       // Block align.
        binaryWriter.writeInt16((short) bitDepth);                               // Bits per sample.
        binaryWriter.writeString("data");                                       // Sub-chunk 2. Sub-chunk 2 ID.
        binaryWriter.writeInt32((bitDepth / 8) * totalSampleCount);             // Sub-chunk 2 size.
    }

    public static void writeCuePoints(FileOutputStreamBinaryWriter binaryWriter, long[] positions, int channelsCount) throws IOException {
        binaryWriter.writeString("cue ");                           // "cue "
        binaryWriter.writeInt32((int) (4 + 24 * positions.length));  // chunk size = 4 + (24 * # of cues)
        binaryWriter.writeInt32((int) positions.length);             // # of cues
        for (int i = 0; i < positions.length; i++) {                 // cue points
            binaryWriter.writeInt32((int) (i + 1));                    // cue ID
            binaryWriter.writeInt32((int) (positions[i] / channelsCount));             // position
            binaryWriter.writeString("data");                       // "data"
            binaryWriter.writeInt32((int) 0);                        // chunk start
            binaryWriter.writeInt32((int) 0);                        // block start
            binaryWriter.writeInt32((int) (positions[i] / channelsCount));             // sample offset
        }
    }

    public static void writeSustainLoop(FileOutputStreamBinaryWriter binaryWriter, long start, long end, int channelsCount, int noteId) throws IOException {
        binaryWriter.writeString("smpl");
        binaryWriter.writeInt32((int) 60);       // chunk size = 36 + (24 * # of sample loops)
        binaryWriter.writeInt32((int) 1);        // manufacturer
        binaryWriter.writeInt32((int) 1);        // product
        binaryWriter.writeInt32((int) 0);        // sample period
        binaryWriter.writeInt32((int) noteId);   // note
        binaryWriter.writeInt32((int) 0);        // pitch
        binaryWriter.writeInt32((int) 0);        // SMPTE Format
        binaryWriter.writeInt32((int) 0);        // SMPTE Offset
        binaryWriter.writeInt32((int) 1);        // Num Sample Loops
        binaryWriter.writeInt32((int) 0);        // Sampler Data

        // loop point
        binaryWriter.writeInt32((int) 1);                        // Cue Point ID
        binaryWriter.writeInt32((int) 0);                        // Type
        binaryWriter.writeInt32((int) (start / channelsCount));  // Start
        binaryWriter.writeInt32((int) (end / channelsCount));    // End
        binaryWriter.writeInt32((int) 0);                        // Fraction
        binaryWriter.writeInt32((int) 0);                        // Play Count
    }
}
