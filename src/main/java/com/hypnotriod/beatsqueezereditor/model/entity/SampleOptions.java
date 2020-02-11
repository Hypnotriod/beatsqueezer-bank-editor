package com.hypnotriod.beatsqueezereditor.model.entity;

import com.hypnotriod.beatsqueezereditor.constants.Config;
import com.hypnotriod.beatsqueezereditor.constants.Notes;
import com.hypnotriod.beatsqueezereditor.constants.Strings;

/**
 *
 * @author Ilya Pikin
 */
public class SampleOptions {

    public int noteId = Notes.MIDDLE_C_INDEX;
    public int groupId = 1;
    public int normalizeIndex = 0;
    public boolean isDynamic = true;
    public boolean playThrough = false;
    public boolean loopEnabled = false;
    public long panorama = 0;
    public String fileName = Strings.FILE_NAME_UNTITLED;
    public int pitchStep = 1;
    public int pitch = 0;
    public boolean stereo = false;
    public String[] noteNamesDisplay = Notes.NOTES_NAMES_C5;

    public int[] filtersValues = new int[Config.KNOBS_NUM];
}
