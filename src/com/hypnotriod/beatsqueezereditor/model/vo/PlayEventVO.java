
package com.hypnotriod.beatsqueezereditor.model.vo;

/**
 *
 * @author IPikin
 */
public class PlayEventVO {
    
    public SampleVO sampleVO;
    public double position;
    
    public PlayEventVO(SampleVO sampleVO, double position)
    {
        this.sampleVO = sampleVO;
        this.position = position;
    }
    
}
