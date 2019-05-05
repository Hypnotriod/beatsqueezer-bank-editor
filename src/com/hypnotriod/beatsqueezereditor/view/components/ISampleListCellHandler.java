
package com.hypnotriod.beatsqueezereditor.view.components;

import com.hypnotriod.beatsqueezereditor.model.vo.SampleVO;
import javafx.scene.input.DragEvent;

/**
 *
 * @author Илья
 */
public interface ISampleListCellHandler {
    public void onSampleListCellDelete(String id);
    public void onSampleListCellPlay(SampleVO sampleVO, double position);
    public void onSampleListCellFileDragged(SampleVO sampleVO, DragEvent event);
}
