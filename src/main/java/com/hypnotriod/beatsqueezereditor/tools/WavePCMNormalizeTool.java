package com.hypnotriod.beatsqueezereditor.tools;

/**
 *
 * @author Ilya Pikin
 */
public class WavePCMNormalizeTool {

    /*
        Math.log10(sampleValue) * 20;   // sampleValue to dbValue
        Math.pow(10, dbValue / 20);     // dbValue to sampleValue
     */
    public static final double AMPLITUDE_16_BIT_MAX_DB = Math.log10(Short.MAX_VALUE) * 20;

    public static void normalize16Bit(byte[] data, double toDB) {
        int maxNormalizedSampleValue = (int) Math.pow(10, (AMPLITUDE_16_BIT_MAX_DB - Math.abs(toDB)) / 20);
        int minNormalizedSampleValue = -maxNormalizedSampleValue;
        int maxSampleValue = 1;
        int minSampleValue = -1;
        float amplitudeNormalizeScale;
        int sample;

        for (int i = 0; i < data.length; i += 2) {
            sample = (short) (((data[i + 1] & 0xFF) << 8) | (data[i] & 0xFF));
            if (maxSampleValue < sample) {
                maxSampleValue = sample;
            }
            if (minSampleValue > sample) {
                minSampleValue = sample;
            }
        }

        if (Math.abs(maxSampleValue) > Math.abs(minSampleValue)) {
            amplitudeNormalizeScale = (float) maxNormalizedSampleValue / (float) maxSampleValue;
        } else {
            amplitudeNormalizeScale = (float) -minNormalizedSampleValue / (float) -minSampleValue;
        }

        for (int i = 0; i < data.length; i += 2) {
            sample = (short) (((data[i + 1] & 0xFF) << 8) | (data[i] & 0xFF));
            sample = (int) ((float) sample * amplitudeNormalizeScale);
            data[i] = (byte) (sample & 0xFF);
            data[i + 1] = (byte) ((sample >> 8) & 0xFF);
        }
    }

}
