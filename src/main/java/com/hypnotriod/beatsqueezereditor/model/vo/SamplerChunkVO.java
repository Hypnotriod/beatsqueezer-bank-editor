package com.hypnotriod.beatsqueezereditor.model.vo;

/**
 *
 * @author Ilya Pikin
 */
public class SamplerChunkVO {

    public long chunkDataSize;
    public long manufacturer;
    public long product;
    public long samplePeriod;
    public long MIDIUnityNote;
    public long MIDIPitchFraction;
    public long SMPTEFormat;
    public long SMPTEOffset;
    public long numSampleLoops;
    public long samplerData;

    public SampleLoopVO[] sampleLoops = null;

    public void dispose() {
        sampleLoops = null;
    }
}
