package com.hypnotriod.beatsqueezereditor.view.component;

import com.hypnotriod.beatsqueezereditor.model.entity.Sample;
import javafx.scene.input.DragEvent;

/**
 *
 * @author Ilya Pikin
 */
public interface SampleListCellHandler {

    public void onSampleListCellDelete(String id);

    public void onSampleListCellPlay(Sample sample, double position);

    public void onSampleListDragEntered();

    public void onSampleListDragExited();

    public void onSampleListCellFileDragged(Sample sample, DragEvent event);
}
