package com.hypnotriod.beatsqueezereditor.view.component;

import com.hypnotriod.beatsqueezereditor.model.vo.SampleVO;
import javafx.scene.input.DragEvent;

/**
 *
 * @author Ilya Pikin
 */
public interface SampleListCellHandler {

    public void onSampleListCellDelete(String id);

    public void onSampleListCellPlay(SampleVO sampleVO, double position);

    public void onSampleListDragEntered();

    public void onSampleListDragExited();

    public void onSampleListCellFileDragged(SampleVO sampleVO, DragEvent event);
}
