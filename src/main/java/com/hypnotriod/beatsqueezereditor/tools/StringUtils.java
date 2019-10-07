package com.hypnotriod.beatsqueezereditor.tools;

import com.hypnotriod.beatsqueezereditor.constants.Notes;

/**
 *
 * @author Ilya Pikin
 */
public class StringUtils {

    public static String removeFileExtension(String fileName) {
        return fileName.replaceFirst("[.][^.]+$", "");
    }

    public static String getSampleName(String prefixName, int noteID) {
        return prefixName + "_"
                + String.format("%03d", noteID) + "_"
                + Notes.NOTES_NAMES[noteID];
    }

    public static String getSampleNameF(String prefixName, int noteID) {
        return prefixName + "_"
                + String.format("%03d", noteID) + "_"
                + Notes.NOTES_NAMES[noteID]
                + "_f";
    }

    public static String getSampleNameP(String prefixName, int noteID) {
        return prefixName + "_"
                + String.format("%03d", noteID) + "_"
                + Notes.NOTES_NAMES[noteID]
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
