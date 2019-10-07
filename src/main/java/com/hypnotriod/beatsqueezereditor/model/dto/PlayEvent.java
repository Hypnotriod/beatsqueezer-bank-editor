package com.hypnotriod.beatsqueezereditor.model.dto;

import com.hypnotriod.beatsqueezereditor.model.entity.Sample;

/**
 *
 * @author Ilya Pikin
 */
public class PlayEvent {

    public Sample sample;
    public double position;

    public PlayEvent(Sample sample, double position) {
        this.sample = sample;
        this.position = position;
    }

}
