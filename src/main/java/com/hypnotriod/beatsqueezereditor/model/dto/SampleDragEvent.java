package com.hypnotriod.beatsqueezereditor.model.dto;

import com.hypnotriod.beatsqueezereditor.model.entity.Sample;
import javafx.scene.input.DragEvent;

/**
 *
 * @author Ilya Pikin
 */
public class SampleDragEvent {

    public Sample sample;
    public DragEvent event;

    public SampleDragEvent(Sample sample, DragEvent event) {
        this.sample = sample;
        this.event = event;
    }
}
