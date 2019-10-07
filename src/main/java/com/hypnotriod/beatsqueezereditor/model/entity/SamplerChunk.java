package com.hypnotriod.beatsqueezereditor.model.entity;

/**
 *
 * @author Ilya Pikin
 */
public class SamplerChunk {

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

    public SampleLoop[] sampleLoops = null;

    public void dispose() {
        sampleLoops = null;
    }
}
