package com.hypnotriod.beatsqueezereditor.view.component;

import com.hypnotriod.beatsqueezereditor.view.controller.SampleListCellViewController;
import com.hypnotriod.beatsqueezereditor.Main;
import com.hypnotriod.beatsqueezereditor.constants.CResources;
import com.hypnotriod.beatsqueezereditor.model.vo.SampleVO;
import java.io.IOException;
import java.util.HashMap;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;

/**
 *
 * @author Ilya Pikin
 */
public class SampleListCell extends ListCell<String> {

    private final SampleListCellHandler handler;
    private final HashMap<String, SampleVO> sampleVOs;
    private AnchorPane content = null;
    private SampleListCellViewController cellController = null;

    public SampleListCell(HashMap<String, SampleVO> sampleVOs, SampleListCellHandler handler) {
        this.sampleVOs = sampleVOs;
        this.handler = handler;
    }

    @Override
    public void updateItem(String key, boolean empty) {
        super.updateItem(key, empty);
        if (key != null && empty == false && content == null) {
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(Main.class.getResource(CResources.PATH_SAMPLE_LIST_CELL));
                content = loader.load();
                cellController = loader.getController();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (content != null) {
            setGraphic(content);
            content.setDisable(empty);
            content.setFocusTraversable(false);
        }

        if (key != null && cellController != null && sampleVOs.get(key) != null) {
            cellController.setHandler(handler);
            cellController.setSampleVO(sampleVOs.get(key), key);
        }
    }
}
