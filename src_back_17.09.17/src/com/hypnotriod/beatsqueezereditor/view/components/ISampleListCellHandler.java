
package com.hypnotriod.beatsqueezereditor.view.components;

import com.hypnotriod.beatsqueezereditor.model.vo.SampleVO;
import javafx.scene.input.DragEvent;

/**
 *
 * @author Илья
 */
public interface ISampleListCellHandler {
    public void onSampleListCellDelete(String id);
    public void onSampleListCellPlay(SampleVO sampleVO);
    public void onSampleListCellFileDragged(SampleVO sampleVO, DragEvent event);
}
