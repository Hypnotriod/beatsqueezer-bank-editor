package com.hypnotriod.beatsqueezereditor.utility;

import com.hypnotriod.beatsqueezereditor.constants.Config;
import com.hypnotriod.beatsqueezereditor.model.entity.SustainLoop;

/**
 *
 * @author Ilya Pikin
 */
public class LoopPointAdjustUtil {

    public static final int SAMPLES_SEARCH_LIMIT_NUMBER = 300;

    private static short getSampleByIndex(int index, byte[] data) {
        return (short) (((data[index * Config.BYTES_PER_SAMPLE + 1] & 0xFF) << 8) | (data[index * Config.BYTES_PER_SAMPLE] & 0xFF));
    }

    public static void normalizeLoop(byte[] samplesData, SustainLoop loop, int channels) {
        long loopPositionMax = samplesData.length / Config.BYTES_PER_SAMPLE - (Config.MIN_LOOP_LENGTH_SAMPLES * channels);
        if (loop.start < 0) {
            loop.start = 0;
        } else if (loop.start > loopPositionMax) {
            loop.start = loopPositionMax;
        }
    }

    public static void decreaseLoopStart(byte[] samplesData, SustainLoop loop, int channels) {
        loop.start -= (SAMPLES_SEARCH_LIMIT_NUMBER * channels);
        normalizeLoop(samplesData, loop, channels);
        if (loop.start != 0) {
            adjustLoopStart(samplesData, loop, channels);
        }
    }

    public static void increaseLoopStart(byte[] samplesData, SustainLoop loop, int channels) {
        loop.start += (SAMPLES_SEARCH_LIMIT_NUMBER * channels);
        normalizeLoop(samplesData, loop, channels);
        adjustLoopStart(samplesData, loop, channels);
    }

    public static void adjustLoopStart(byte[] samplesData, SustainLoop loop, int channels) {
        final short endLoopSample = (channels == 2)
                ? (short) ((getSampleByIndex((int) loop.end - 1, samplesData)
                + getSampleByIndex((int) loop.end - 2, samplesData)) / 2)
                : getSampleByIndex((int) loop.end - 1, samplesData);
        int lastSamplesDifference = Integer.MAX_VALUE;
        int bestIndex = 0;
        int currentIndex;
        short startLoopSample;
        int samplesDifference;

        currentIndex = (int) loop.start - SAMPLES_SEARCH_LIMIT_NUMBER / 2 * channels;
        if (currentIndex < 0) {
            currentIndex = 0;
        }
        int i = 0;
        while (i < SAMPLES_SEARCH_LIMIT_NUMBER && currentIndex < samplesData.length * Config.BYTES_PER_SAMPLE) {
            startLoopSample = (channels == 2)
                    ? (short) ((getSampleByIndex(currentIndex, samplesData) + getSampleByIndex(currentIndex + 1, samplesData)) / 2)
                    : getSampleByIndex(currentIndex, samplesData);
            samplesDifference = Math.abs(startLoopSample - endLoopSample);
            if (lastSamplesDifference > samplesDifference) {
                bestIndex = currentIndex;
                lastSamplesDifference = samplesDifference;
            }
            currentIndex += channels;
            i++;
        }
        loop.start = bestIndex;

        normalizeLoop(samplesData, loop, channels);
    }
}
