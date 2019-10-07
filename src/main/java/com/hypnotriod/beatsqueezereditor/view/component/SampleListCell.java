package com.hypnotriod.beatsqueezereditor.view.component;

import com.hypnotriod.beatsqueezereditor.view.controller.SampleListCellViewController;
import com.hypnotriod.beatsqueezereditor.Main;
import com.hypnotriod.beatsqueezereditor.constants.Resources;
import com.hypnotriod.beatsqueezereditor.model.entity.Sample;
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
    private final HashMap<String, Sample> samples;
    private AnchorPane content = null;
    private SampleListCellViewController cellController = null;

    public SampleListCell(HashMap<String, Sample> samples, SampleListCellHandler handler) {
        this.samples = samples;
        this.handler = handler;
    }

    @Override
    public void updateItem(String key, boolean empty) {
        super.updateItem(key, empty);
        if (key != null && empty == false && content == null) {
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(Main.class.getResource(Resources.PATH_SAMPLE_LIST_CELL));
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

        if (key != null && cellController != null && samples.get(key) != null) {
            cellController.setHandler(handler);
            cellController.setSample(samples.get(key), key);
        }
    }
}
