package com.hypnotriod.beatsqueezereditor.view.controller;

import com.hypnotriod.beatsqueezereditor.constants.CConfig;
import com.hypnotriod.beatsqueezereditor.constants.CGroups;
import com.hypnotriod.beatsqueezereditor.constants.CNotes;
import com.hypnotriod.beatsqueezereditor.constants.CStrings;
import com.hypnotriod.beatsqueezereditor.constants.CStyle;
import com.hypnotriod.beatsqueezereditor.model.vo.SampleVO;
import com.hypnotriod.beatsqueezereditor.model.vo.SustainLoopVO;
import com.hypnotriod.beatsqueezereditor.tools.RawPCMDataPlayer;
import com.hypnotriod.beatsqueezereditor.tools.TooltipHelper;
import com.hypnotriod.beatsqueezereditor.tools.WaveDrawingTool;
import com.hypnotriod.beatsqueezereditor.view.component.SampleListCellHandler;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

/**
 *
 * @author Ilya Pikin
 */
public class SampleListCellViewController implements Initializable {

    @FXML
    private ComboBox cbNoteID;
    @FXML
    private ComboBox cbGroupID;
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
    private Button btnPlay;
    @FXML
    private Button btnDelete;
    @FXML
    private Canvas canvasWave;
    @FXML
    private Canvas canvasWaveP;
    @FXML
    private Canvas canvasWaveF;
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

    private SampleListCellHandler handler;
    private SampleVO sampleVO;
    private String id;

    private Timeline samplePlayTimer = null;

    public void setSampleVO(SampleVO sampleVO, String id) {
        this.sampleVO = sampleVO;
        this.id = id;

        cbNoteID.getItems().clear();
        cbNoteID.getItems().addAll((Object[]) CNotes.NOTES_NAMES);
        cbNoteID.getSelectionModel().select(this.sampleVO.noteID);
        cbGroupID.getSelectionModel().select(this.sampleVO.groupID);
        chbDynamic.setSelected(this.sampleVO.dynamic);
        chbDisableNoteOff.setSelected(this.sampleVO.disableNoteOff);
        chbLoop.setSelected(this.sampleVO.loop != null && this.sampleVO.loopEnabled == true);
        chbLoop.setDisable(this.sampleVO.loop == null);
        if (sampleVO.channels == 1) {
            sliderPan.setDisable(false);
            labelSiderValue.setDisable(false);
            sliderPan.setValue(this.sampleVO.panorama);
            updateLabelSliderValue(this.sampleVO.panorama);
        } else {
            sliderPan.setDisable(true);
            labelSiderValue.setDisable(true);
            sliderPan.setValue(0);
            updateLabelSliderValue(0);
        }
        labelFileName.setText(this.sampleVO.fileName);
        updatePlayButtonLabel(this.sampleVO.isPlaying);
        updateCanvasWave(canvasWave, this.sampleVO.samplesData, this.sampleVO.channels, this.sampleVO.loop, -1);
        updateCanvasWave(canvasWaveP, this.sampleVO.samplesDataP, this.sampleVO.channels, this.sampleVO.loopP, -1);
        updateCanvasWave(canvasWaveF, this.sampleVO.samplesDataF, this.sampleVO.channels, this.sampleVO.loopF, -1);

        labelPianoSampleName.setVisible(sampleVO.samplesDataP == null);
        labelForteSampleName.setVisible(sampleVO.samplesDataF == null);
        canvasWaveP.setVisible(sampleVO.samplesDataP != null);
        canvasWaveF.setVisible(sampleVO.samplesDataF != null);

        labelPianoSampleName.setText(String.format(CStrings.PIANO_SAMPLE_NAME_PROMT, this.sampleVO.fileRealName));
        labelForteSampleName.setText(String.format(CStrings.FORTE_SAMPLE_NAME_PROMT, this.sampleVO.fileRealName));

        switch (this.sampleVO.selectedSampleExt) {
            case CConfig.EXT_P:
                samplesTab.getSelectionModel().select(1);
                break;
            case CConfig.EXT_F:
                samplesTab.getSelectionModel().select(2);
                break;
            default:
                samplesTab.getSelectionModel().select(0);
                break;
        }

        startStopTimerSchedule(this.sampleVO.isPlaying);
    }

    public void setHandler(SampleListCellHandler handler) {
        this.handler = handler;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbNoteID.getItems().addAll((Object[]) CNotes.NOTES_NAMES);
        cbGroupID.getItems().addAll((Object[]) CGroups.GROUPS_NAMES);

        cbNoteID.getSelectionModel().selectedItemProperty().addListener(cbNoteChangeListener);
        cbGroupID.getSelectionModel().selectedItemProperty().addListener(cbGroupChangeListener);
        chbDynamic.selectedProperty().addListener(chbDynamicChangeListener);
        chbDisableNoteOff.selectedProperty().addListener(chbDisableNoteOffChangeListener);
        chbLoop.selectedProperty().addListener(chbLoopChangeListener);
        sliderPan.valueProperty().addListener(sliderPanChangeListener);

        canvasWave.setOnDragDropped(onSampleDragDropped);
        canvasWave.setOnDragEntered(onCanvasDragEntered);
        canvasWave.setOnDragExited(onCanvasDragExited);
        canvasWaveP.setOnDragDropped(onSampleDragDropped);
        canvasWaveP.setOnDragEntered(onCanvasDragEntered);
        canvasWaveP.setOnDragExited(onCanvasDragExited);
        canvasWaveF.setOnDragDropped(onSampleDragDropped);
        canvasWaveF.setOnDragEntered(onCanvasDragEntered);
        canvasWaveF.setOnDragExited(onCanvasDragExited);

        labelPianoSampleName.setOnDragDropped(onSampleDragDropped);
        labelPianoSampleName.setOnDragEntered(onLabelDragEntered);
        labelPianoSampleName.setOnDragExited(onLabelDragExited);
        labelForteSampleName.setOnDragDropped(onSampleDragDropped);
        labelForteSampleName.setOnDragEntered(onLabelDragEntered);
        labelForteSampleName.setOnDragExited(onLabelDragExited);

        labelPianoSampleName.setStyle(CStyle.LABEL_P_F_SAMPLE_NAME);
        labelForteSampleName.setStyle(CStyle.LABEL_P_F_SAMPLE_NAME);

        initTooltips();
    }

    private void initTooltips() {
        cbNoteID.setTooltip(TooltipHelper.getTooltip1(CStrings.TOOLTIP_NOTE_SHORT));
        cbGroupID.setTooltip(TooltipHelper.getTooltip1(CStrings.TOOLTIP_GROUP_ID));
        sliderPan.setTooltip(TooltipHelper.getTooltip1(CStrings.TOOLTIP_PANORAMA));
        chbDynamic.setTooltip(TooltipHelper.getTooltip1(CStrings.TOOLTIP_DYNAMIC));
        chbDisableNoteOff.setTooltip(TooltipHelper.getTooltip1(CStrings.TOOLTIP_DISABLE_NOTE_OFF));
        chbLoop.setTooltip(TooltipHelper.getTooltip1(CStrings.TOOLTIP_LOOP));

        labelNote.setTooltip(TooltipHelper.getTooltip1(CStrings.TOOLTIP_NOTE_SHORT));
        labelCutGroup.setTooltip(TooltipHelper.getTooltip1(CStrings.TOOLTIP_GROUP_ID));
        labelSiderValue.setTooltip(TooltipHelper.getTooltip1(CStrings.TOOLTIP_PANORAMA));

        tabDefaultSample.setTooltip(TooltipHelper.getTooltip1(CStrings.TOOLTIP_DEFAULT_SAMPLE));
        tabPianoSample.setTooltip(TooltipHelper.getTooltip1(CStrings.TOOLTIP_PIANO_SAMPLE));
        tabForteSample.setTooltip(TooltipHelper.getTooltip1(CStrings.TOOLTIP_FORTE_SAMPLE));

        btnDelete.setTooltip(TooltipHelper.getTooltip1(CStrings.TOOLTIP_DELETE_SAMPLE));
        btnPlay.setTooltip(TooltipHelper.getTooltip1(CStrings.TOOLTIP_PLAY));
    }

    private final EventHandler onCanvasDragEntered = new EventHandler<DragEvent>() {
        @Override
        public void handle(DragEvent event) {
            event.consume();
            ((Node) event.getTarget()).setOpacity(0.5f);
            handler.onSampleListDragEntered();
        }
    };

    private final EventHandler onCanvasDragExited = new EventHandler<DragEvent>() {
        @Override
        public void handle(DragEvent event) {
            event.consume();
            ((Node) event.getTarget()).setOpacity(1);
            handler.onSampleListDragExited();
        }
    };

    private final EventHandler onLabelDragEntered = new EventHandler<DragEvent>() {
        @Override
        public void handle(DragEvent event) {
            event.consume();
            ((Node) event.getTarget()).setStyle(CStyle.LABEL_P_F_BACKGROUND_HIGHLIGHT);
            handler.onSampleListDragEntered();
        }
    };

    private final EventHandler onLabelDragExited = new EventHandler<DragEvent>() {
        @Override
        public void handle(DragEvent event) {
            event.consume();
            ((Node) event.getTarget()).setStyle(CStyle.LABEL_P_F_BACKGROUND_NORMAL);
            handler.onSampleListDragExited();
        }
    };

    private final EventHandler onSampleDragDropped = new EventHandler<DragEvent>() {
        @Override
        public void handle(DragEvent event) {
            event.consume();
            handler.onSampleListCellFileDragged(sampleVO, event);
        }
    };

    private void updateCanvasWave(Canvas canvas, byte[] samplesData, int channels, SustainLoopVO loopVO, int framePosition) {
        if (samplesData != null) {
            WaveDrawingTool.drawWave16Bit(canvas, samplesData, channels, loopVO, framePosition);
        }
    }

    private void updateLabelSliderValue(long value) {
        labelSiderValue.setText(String.format(CStrings.PAN_VALUE, value));
    }

    private void updatePlayButtonLabel(boolean isPlaying) {
        btnPlay.setText(isPlaying ? CStrings.STOP : CStrings.PLAY);
    }

    private void startStopTimerSchedule(boolean run) {
        if (samplePlayTimer == null) {
            samplePlayTimer = new Timeline(new KeyFrame(Duration.millis(50), timerActionEvent));
            samplePlayTimer.setCycleCount(Timeline.INDEFINITE);
        }

        if (run == true) {
            samplePlayTimer.play();
        } else {
            samplePlayTimer.stop();
        }
    }

    private final EventHandler<ActionEvent> timerActionEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            if (!sampleVO.isPlaying) {
                startStopTimerSchedule(sampleVO.isPlaying);
                updatePlayButtonLabel(sampleVO.isPlaying);
            }

            switch (sampleVO.playingSampleExt) {
                case CConfig.EXT_P:
                    updateCanvasWave(canvasWaveP, sampleVO.samplesDataP, sampleVO.channels, sampleVO.loopP, RawPCMDataPlayer.getFramePosition());
                    break;
                case CConfig.EXT_F:
                    updateCanvasWave(canvasWaveF, sampleVO.samplesDataF, sampleVO.channels, sampleVO.loopF, RawPCMDataPlayer.getFramePosition());
                    break;
                default:
                    updateCanvasWave(canvasWave, sampleVO.samplesData, sampleVO.channels, sampleVO.loop, RawPCMDataPlayer.getFramePosition());
                    break;
            }
        }
    };

    @FXML
    private void handleCanvasWaveClicked(MouseEvent event) {
        onCanvasWaveClicked(canvasWave, sampleVO.samplesData, event);
    }

    @FXML
    private void handleCanvasWavePClicked(MouseEvent event) {
        onCanvasWaveClicked(canvasWaveP, sampleVO.samplesDataP, event);
    }

    @FXML
    private void handleCanvasWaveFClicked(MouseEvent event) {
        onCanvasWaveClicked(canvasWaveF, sampleVO.samplesDataF, event);
    }

    private void onCanvasWaveClicked(Canvas canvas, byte[] samplesData, MouseEvent event) {
        if (samplesData == null) {
            return;
        }
        double position = event.getX() / canvas.getWidth();
        sampleVO.isPlaying = false;
        handler.onSampleListCellPlay(sampleVO, position);
        startStopTimerSchedule(sampleVO.isPlaying);
        updatePlayButtonLabel(sampleVO.isPlaying);
    }

    @FXML
    private void handleDeleteButtonClicked(MouseEvent event) {
        handler.onSampleListCellDelete(id);
    }

    @FXML
    private void handlePlayButtonClicked(MouseEvent event) {
        handler.onSampleListCellPlay(sampleVO, 0);
        startStopTimerSchedule(sampleVO.isPlaying);
        updatePlayButtonLabel(sampleVO.isPlaying);
    }

    @FXML
    private void onTabDefaultSampleClicked(Event event) {
        if (this.sampleVO != null) {
            this.sampleVO.selectedSampleExt = CConfig.EXT_DEFAULT;
        }
    }

    @FXML
    private void onTabPianoSampleClicked(Event event) {
        if (this.sampleVO != null) {
            this.sampleVO.selectedSampleExt = CConfig.EXT_P;
        }
    }

    @FXML
    private void onTabForteSampleClicked(Event event) {
        if (this.sampleVO != null) {
            this.sampleVO.selectedSampleExt = CConfig.EXT_F;
        }
    }

    ChangeListener<String> cbNoteChangeListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> selected, String oldValue, String newValue) {
            if (newValue != null) {
                sampleVO.noteID = CStrings.getIndexOfStringInArray(newValue, CNotes.NOTES_NAMES);
            }
        }
    };

    ChangeListener<String> cbGroupChangeListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> selected, String oldValue, String newValue) {
            sampleVO.groupID = CStrings.getIndexOfStringInArray(newValue, CGroups.GROUPS_NAMES);
        }
    };

    ChangeListener<Boolean> chbDynamicChangeListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> selected, Boolean oldValue, Boolean newValue) {
            sampleVO.dynamic = newValue;
        }
    };
    ChangeListener<Boolean> chbDisableNoteOffChangeListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> selected, Boolean oldValue, Boolean newValue) {
            sampleVO.disableNoteOff = newValue;
        }
    };
    ChangeListener<Boolean> chbLoopChangeListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> selected, Boolean oldValue, Boolean newValue) {
            if (sampleVO.loop != null) {
                sampleVO.loopEnabled = newValue;
            }
        }
    };

    ChangeListener<Number> sliderPanChangeListener = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> selected, Number oldValue, Number newValue) {
            sampleVO.panorama = newValue.longValue();
            updateLabelSliderValue(newValue.longValue());
        }
    };

}
