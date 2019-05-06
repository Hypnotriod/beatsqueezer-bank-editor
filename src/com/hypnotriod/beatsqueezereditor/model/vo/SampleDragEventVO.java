package com.hypnotriod.beatsqueezereditor.model.vo;

import javafx.scene.input.DragEvent;

/**
 *
 * @author Ilya Pikin
 */
public class SampleDragEventVO {

    public SampleVO sampleVO;
    public DragEvent dragEvent;

    public SampleDragEventVO(SampleVO sampleVO, DragEvent event) {
        this.sampleVO = sampleVO;
        this.dragEvent = event;
    }
}
