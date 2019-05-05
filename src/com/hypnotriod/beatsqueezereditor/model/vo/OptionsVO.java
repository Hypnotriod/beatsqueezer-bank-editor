
package com.hypnotriod.beatsqueezereditor.model.vo;

import com.hypnotriod.beatsqueezereditor.constants.CConfig;

/**
 *
 * @author Илья
 */
public class OptionsVO {
    public int noteID = 0;
    public int groupID = 1;
    public int normalizeIndex = 0;
    public boolean isDynamic = true;
    public boolean playThrough = false;
    public boolean loopEnabled = false;
    public long panorama = 0;
    public String fileName = "";
    public int pitchStep = 1;
    public int pitch = 0;
    public boolean stereo = false;
    
    public int[] filtersValues = new int[CConfig.KNOBS_NUM];
}