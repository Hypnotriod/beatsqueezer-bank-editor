package com.hypnotriod.beatsqueezereditor.utility;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.ComboBoxListViewSkin;

/**
 *
 * @author Ilya Pikin
 */
public class ComboBoxUtil {

    public static void provideScrollOnDropDown(ComboBox comboBox) {
        comboBox.setOnShown(event -> {
            ComboBoxListViewSkin skin = (ComboBoxListViewSkin) comboBox.getSkin();
            if (skin != null) {
                int index = Math.max(comboBox.getSelectionModel().getSelectedIndex() - comboBox.getVisibleRowCount() / 2 + 1, 0);
                ((ListView<ComboBoxListViewSkin>) skin.getPopupContent()).scrollTo(index);
            }
        });
    }
}
