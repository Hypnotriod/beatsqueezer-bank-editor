package com.hypnotriod.beatsqueezereditor.utility;

/**
 *
 * @author Ilya Pikin
 */
public class NoteFrequencyUtil {

    public static final double TWELTH_ROOT_OF_TWO = Math.pow(2.0d, 1.0d / 12.0d);;

    public static float getPitchedNoteFrequency(float currentNoteFrequency, int semitones) {
        return currentNoteFrequency * (float) Math.pow(TWELTH_ROOT_OF_TWO, semitones);
    }

    public static int getPitchedSampleRate(int currentSampleRate, int semitones) {
        return (semitones == 0)
                ? currentSampleRate
                : Math.round(1.0f / getPitchedNoteFrequency(1, semitones) * (float) currentSampleRate);
    }

}
