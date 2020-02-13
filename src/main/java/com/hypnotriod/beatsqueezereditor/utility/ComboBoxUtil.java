package com.hypnotriod.beatsqueezereditor.tools;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.control.ComboBox;

/**
 *
 * @author Ilya Pikin
 */
public class ComboBoxUtil {

    public static void provideScrollOnDropDown(ComboBox comboBox) {
        comboBox.setOnShown(event -> {
            ComboBoxListViewSkin skin = (ComboBoxListViewSkin) comboBox.getSkin();
            skin.getListView().scrollTo(comboBox.getSelectionModel().getSelectedItem());
        });
    }
}
