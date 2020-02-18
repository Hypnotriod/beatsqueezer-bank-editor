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

    public static void adjustLoopPoint(byte[] data, SustainLoop loop, int channels) {
        final short endLoopSample = getSampleByIndex(data.length / Config.BYTES_PER_SAMPLE - 1 * channels, data);
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
        while (i < SAMPLES_SEARCH_LIMIT_NUMBER && currentIndex < data.length * Config.BYTES_PER_SAMPLE) {
            startLoopSample = getSampleByIndex(currentIndex, data);
            samplesDifference = Math.abs(startLoopSample - endLoopSample);
            if (lastSamplesDifference > samplesDifference) {
                bestIndex = currentIndex;
                lastSamplesDifference = samplesDifference;
            }
            currentIndex += channels;
            i++;
        }
        loop.start = bestIndex;
    }
}
