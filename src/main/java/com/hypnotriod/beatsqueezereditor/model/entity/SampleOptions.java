package com.hypnotriod.beatsqueezereditor.model.entity;

import com.hypnotriod.beatsqueezereditor.constants.Config;
import com.hypnotriod.beatsqueezereditor.constants.Strings;

/**
 *
 * @author Ilya Pikin
 */
public class SampleOptions {

    public int noteID = 0;
    public int groupID = 1;
    public int normalizeIndex = 0;
    public boolean isDynamic = true;
    public boolean playThrough = false;
    public boolean loopEnabled = false;
    public long panorama = 0;
    public String fileName = Strings.FILE_NAME_UNTITLED;
    public int pitchStep = 1;
    public int pitch = 0;
    public boolean stereo = false;

    public int[] filtersValues = new int[Config.KNOBS_NUM];
}
