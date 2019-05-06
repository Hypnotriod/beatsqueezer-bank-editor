package com.hypnotriod.beatsqueezereditor.tools;

/**
 *
 * @author Ilya Pikin
 */
public class NoteFrequencyUtil {

    public static double TWELTH_ROOT_OF_TWO = 1.059463094359;

    public static float getPitchedNoteFrequency(float currentNoteFrequency, int semitones) {
        return currentNoteFrequency * (float) Math.pow(TWELTH_ROOT_OF_TWO, semitones);
    }

    public static int getPitchedSampleRate(int currentSampleRate, int semitones) {
        return (semitones == 0)
                ? currentSampleRate
                : Math.round(1.0f / getPitchedNoteFrequency(1, semitones) * (float) currentSampleRate);
    }

}
