package com.hypnotriod.beatsqueezereditor.utility;

import com.hypnotriod.beatsqueezereditor.model.entity.SustainLoop;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Ilya Pikin
 */
public class StringUtils {

    public static String getLoopStartTime(SustainLoop loop, int channels, int sampleRate) {
        long milliseconds = (long) ((float) loop.start / (float) channels / (float) sampleRate * 1000.0f);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        long hours = TimeUnit.MICROSECONDS.toHours(milliseconds);

        return String.format("%01d:%02d:%02d.%03d", hours, minutes % 60, seconds % 60, milliseconds % 1000);
    }

    public static String removeFileExtension(String fileName) {
        return fileName.replaceFirst("[.][^.]+$", "");
    }

    public static String getSampleName(String prefixName, int noteId, String[] notesNames) {
        return prefixName + "_"
                + String.format("%03d", noteId) + "_"
                + notesNames[noteId];
    }

    public static String getSampleNameF(String prefixName, int noteId, String[] notesNames) {
        return prefixName + "_"
                + String.format("%03d", noteId) + "_"
                + notesNames[noteId]
                + "_f";
    }

    public static String getSampleNameP(String prefixName, int noteId, String[] notesNames) {
        return prefixName + "_"
                + String.format("%03d", noteId) + "_"
                + notesNames[noteId]
                + "_p";
    }

    public static int getIndexOfStringInArray(String str, String[] strArray) {
        for (int i = 0; i < strArray.length; i++) {
            if (strArray[i].equals(str)) {
                return i;
            }
        }
        return -1;
    }
}
