package com.hypnotriod.beatsqueezereditor.tools;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;

/**
 *
 * @author Ilya Pikin
 */
public class ComboBoxUtil {

    public static void select(ComboBox comboBox, int index) {
        comboBox.getSelectionModel().select(index);
        ComboBoxListViewSkin skin = (ComboBoxListViewSkin) comboBox.getSkin();
        if (skin != null) {
            ListView listView = (ListView) skin.getPopupContent();
            listView.scrollTo(index);
        }
    }
}
