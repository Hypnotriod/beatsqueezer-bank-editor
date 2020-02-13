package com.hypnotriod.beatsqueezereditor.utility;

/**
 *
 * @author Ilya Pikin
 */
public class StringUtils {

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
