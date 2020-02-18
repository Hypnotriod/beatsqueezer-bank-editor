package com.hypnotriod.beatsqueezereditor.view.controller;

import com.hypnotriod.beatsqueezereditor.constants.Groups;
import com.hypnotriod.beatsqueezereditor.constants.Strings;
import com.hypnotriod.beatsqueezereditor.constants.Styles;
import com.hypnotriod.beatsqueezereditor.model.entity.Sample;
import com.hypnotriod.beatsqueezereditor.model.entity.SampleOptions;
import com.hypnotriod.beatsqueezereditor.utility.ComboBoxUtil;
import com.hypnotriod.beatsqueezereditor.utility.TooltipUtil;
import com.hypnotriod.beatsqueezereditor.view.component.SampleListCellHandler;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author Ilya Pikin
 */
public class SampleListCellViewController extends SampleCanvasWavesController implements Initializable {

    @FXML
    private ComboBox cbNoteId;
    @FXML
    private ComboBox cbGroupId;
    @FXML
    private CheckBox chbDynamic;
    @FXML
    private CheckBox chbDisableNoteOff;
    @FXML
    private CheckBox chbLoop;
    @FXML
    private Slider sliderPan;
    @FXML
    private Label labelSiderValue;
    @FXML
    private Label labelFileName;
    @FXML
    private Label labelPianoSampleName;
    @FXML
    private Label labelForteSampleName;
    @FXML
    private Button btnDelete;
    @FXML
    private TabPane samplesTab;
    @FXML
    private Label labelNote;
    @FXML
    private Label labelCutGroup;
    @FXML
    private Tab tabDefaultSample;
    @FXML
    private Tab tabPianoSample;
    @FXML
    private Tab tabForteSample;

    @Override
    public void update(Sample sample, SampleOptions sampleOptions, String id) {
        updateInProgress = true;
        super.update(sample, sampleOptions, id);

        cbNoteId.getItems().clear();
        cbNoteId.getItems().addAll((Object[]) sampleOptions.noteNamesDisplay);
        cbNoteId.getSelectionModel().select(this.sample.noteId);
        cbGroupId.getSelectionModel().select(this.sample.groupId);
        chbDynamic.setSelected(this.sample.dynamic);
        chbDisableNoteOff.setSelected(this.sample.disableNoteOff);
        chbLoop.setSelected(this.sample.loop != null && this.sample.isLoopEnabled == true);
        chbLoop.setDisable(this.sample.loop == null);
        if (sample.channels == 1) {
            sliderPan.setDisable(false);
            labelSiderValue.setDisable(false);
            sliderPan.setValue(this.sample.panorama);
            updateLabelSliderValue(this.sample.panorama);
        } else {
            sliderPan.setDisable(true);
            labelSiderValue.setDisable(true);
            sliderPan.setValue(0);
            updateLabelSliderValue(0);
        }
        labelFileName.setText(this.sample.fileName);

        labelPianoSampleName.setVisible(sample.samplesDataP == null);
        labelForteSampleName.setVisible(sample.samplesDataF == null);

        labelPianoSampleName.setText(String.format(Strings.PIANO_SAMPLE_NAME_PROMT, this.sample.fileRealName));
        labelForteSampleName.setText(String.format(Strings.FORTE_SAMPLE_NAME_PROMT, this.sample.fileRealName));

        switch (this.sample.selectedSampleExt) {
            case Sample.EXT_P:
                samplesTab.getSelectionModel().select(1);
                break;
            case Sample.EXT_F:
                samplesTab.getSelectionModel().select(2);
                break;
            default:
                samplesTab.getSelectionModel().select(0);
                break;
        }

        updateInProgress = false;
    }

    public void setHandler(SampleListCellHandler handler) {
        this.handler = handler;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        cbGroupId.getItems().addAll((Object[]) Groups.GROUPS_NAMES);

        cbNoteId.getSelectionModel().selectedItemProperty().addListener(cbNoteChangeListener);
        cbGroupId.getSelectionModel().selectedItemProperty().addListener(cbGroupChangeListener);
        chbDynamic.selectedProperty().addListener(chbDynamicChangeListener);
        chbDisableNoteOff.selectedProperty().addListener(chbDisableNoteOffChangeListener);
        chbLoop.selectedProperty().addListener(chbLoopChangeListener);
        sliderPan.valueProperty().addListener(sliderPanChangeListener);

        ComboBoxUtil.provideScrollOnDropDown(cbNoteId);
        ComboBoxUtil.provideScrollOnDropDown(cbGroupId);

        labelPianoSampleName.setOnDragDropped(onSampleDragDropped);
        labelPianoSampleName.setOnDragEntered(onLabelDragEntered);
        labelPianoSampleName.setOnDragExited(onLabelDragExited);
        labelForteSampleName.setOnDragDropped(onSampleDragDropped);
        labelForteSampleName.setOnDragEntered(onLabelDragEntered);
        labelForteSampleName.setOnDragExited(onLabelDragExited);

        labelPianoSampleName.setStyle(Styles.LABEL_P_F_SAMPLE_NAME);
        labelForteSampleName.setStyle(Styles.LABEL_P_F_SAMPLE_NAME);

        initTooltips();
    }

    private void initTooltips() {
        cbNoteId.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_NOTE_SHORT));
        cbGroupId.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_GROUP_ID));
        sliderPan.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_PANORAMA));
        chbDynamic.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_DYNAMIC));
        chbDisableNoteOff.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_DISABLE_NOTE_OFF));
        chbLoop.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_LOOP));

        labelNote.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_NOTE_SHORT));
        labelCutGroup.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_GROUP_ID));
        labelSiderValue.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_PANORAMA));

        tabDefaultSample.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_DEFAULT_SAMPLE));
        tabPianoSample.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_PIANO_SAMPLE));
        tabForteSample.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_FORTE_SAMPLE));

        btnDelete.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_DELETE_SAMPLE));
    }

    private void updateLabelSliderValue(long value) {
        labelSiderValue.setText(String.format(Strings.PAN_VALUE, value));
    }

    private final EventHandler onLabelDragEntered = new EventHandler<DragEvent>() {
        @Override
        public void handle(DragEvent event) {
            event.consume();
            ((Node) event.getTarget()).setStyle(Styles.LABEL_P_F_BACKGROUND_HIGHLIGHT);
            handler.onSampleListDragEntered();
        }
    };

    private final EventHandler onLabelDragExited = new EventHandler<DragEvent>() {
        @Override
        public void handle(DragEvent event) {
            event.consume();
            ((Node) event.getTarget()).setStyle(Styles.LABEL_P_F_BACKGROUND_NORMAL);
            handler.onSampleListDragExited();
        }
    };

    ChangeListener<String> cbNoteChangeListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> selected, String oldValue, String newValue) {
            if (newValue != null) {
                sample.noteId = Strings.getIndexOfStringInArray(newValue, sampleOptions.noteNamesDisplay);
            }
        }
    };

    ChangeListener<String> cbGroupChangeListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> selected, String oldValue, String newValue) {
            sample.groupId = Strings.getIndexOfStringInArray(newValue, Groups.GROUPS_NAMES);
        }
    };

    ChangeListener<Boolean> chbDynamicChangeListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> selected, Boolean oldValue, Boolean newValue) {
            sample.dynamic = newValue;
        }
    };

    ChangeListener<Boolean> chbDisableNoteOffChangeListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> selected, Boolean oldValue, Boolean newValue) {
            sample.disableNoteOff = newValue;
        }
    };

    ChangeListener<Boolean> chbLoopChangeListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> selected, Boolean oldValue, Boolean newValue) {
            if (sample.loop != null && !updateInProgress) {
                sample.isLoopEnabled = newValue;
                updateAdjustLoopStartComponents();
                updateCanvasWaves();
                if (sample.isPlaying) {
                    handler.onSampleListCellPlayStop(sample, 0);
                }
            }
        }
    };

    ChangeListener<Number> sliderPanChangeListener = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> selected, Number oldValue, Number newValue) {
            sample.panorama = newValue.longValue();
            updateLabelSliderValue(newValue.longValue());
        }
    };

    @FXML
    private void handleDeleteButtonClicked(MouseEvent event) {
        handler.onSampleListCellDelete(id);
    }

    @FXML
    private void onTabDefaultSampleClicked(Event event) {
        if (this.sample != null) {
            this.sample.selectedSampleExt = Sample.EXT_DEFAULT;
            updateAdjustLoopStartComponents();
        }
    }

    @FXML
    private void onTabPianoSampleClicked(Event event) {
        if (this.sample != null) {
            this.sample.selectedSampleExt = Sample.EXT_P;
            updateAdjustLoopStartComponents();
        }
    }

    @FXML
    private void onTabForteSampleClicked(Event event) {
        if (this.sample != null) {
            this.sample.selectedSampleExt = Sample.EXT_F;
            updateAdjustLoopStartComponents();
        }
    }
}
