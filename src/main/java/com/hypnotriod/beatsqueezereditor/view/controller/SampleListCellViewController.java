package com.hypnotriod.beatsqueezereditor.view.controller;

import com.hypnotriod.beatsqueezereditor.constants.Config;
import com.hypnotriod.beatsqueezereditor.constants.Groups;
import com.hypnotriod.beatsqueezereditor.constants.Strings;
import com.hypnotriod.beatsqueezereditor.constants.Styles;
import com.hypnotriod.beatsqueezereditor.model.entity.Sample;
import com.hypnotriod.beatsqueezereditor.model.entity.SampleOptions;
import com.hypnotriod.beatsqueezereditor.model.entity.SustainLoop;
import com.hypnotriod.beatsqueezereditor.tools.ComboBoxUtil;
import com.hypnotriod.beatsqueezereditor.tools.RawPCMDataPlayer;
import com.hypnotriod.beatsqueezereditor.tools.TooltipHelper;
import com.hypnotriod.beatsqueezereditor.tools.WaveDrawingTool;
import com.hypnotriod.beatsqueezereditor.view.component.SampleListCellHandler;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
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
    private Sample sample;
    private SampleOptions sampleOptions;
    private String id;
    private boolean isLoopPointDraggeg = false;

    private Timeline samplePlayTimer = null;

    public void setSampleCellData(Sample sample, SampleOptions sampleOptions, String id) {
        this.sample = sample;
        this.sampleOptions = sampleOptions;
        this.id = id;

        cbNoteId.getItems().clear();
        cbNoteId.getItems().addAll((Object[]) sampleOptions.noteNamesDisplay);
        cbNoteId.getSelectionModel().select(this.sample.noteId);
        cbGroupId.getSelectionModel().select(this.sample.groupId);
        chbDynamic.setSelected(this.sample.dynamic);
        chbDisableNoteOff.setSelected(this.sample.disableNoteOff);
        chbLoop.setSelected(this.sample.loop != null && this.sample.loopEnabled == true);
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
        updatePlayButtonLabel(this.sample.isPlaying);
        updateCanvasWave(canvasWave, this.sample.samplesData, this.sample.channels, this.sample.loop, -1);
        updateCanvasWave(canvasWaveP, this.sample.samplesDataP, this.sample.channels, this.sample.loopP, -1);
        updateCanvasWave(canvasWaveF, this.sample.samplesDataF, this.sample.channels, this.sample.loopF, -1);

        labelPianoSampleName.setVisible(sample.samplesDataP == null);
        labelForteSampleName.setVisible(sample.samplesDataF == null);
        canvasWaveP.setVisible(sample.samplesDataP != null);
        canvasWaveF.setVisible(sample.samplesDataF != null);

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

        startStopTimerSchedule(this.sample.isPlaying);
    }

    public void setHandler(SampleListCellHandler handler) {
        this.handler = handler;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbGroupId.getItems().addAll((Object[]) Groups.GROUPS_NAMES);

        cbNoteId.getSelectionModel().selectedItemProperty().addListener(cbNoteChangeListener);
        cbGroupId.getSelectionModel().selectedItemProperty().addListener(cbGroupChangeListener);
        chbDynamic.selectedProperty().addListener(chbDynamicChangeListener);
        chbDisableNoteOff.selectedProperty().addListener(chbDisableNoteOffChangeListener);
        chbLoop.selectedProperty().addListener(chbLoopChangeListener);
        sliderPan.valueProperty().addListener(sliderPanChangeListener);

        ComboBoxUtil.provideScrollOnDropDown(cbNoteId);
        ComboBoxUtil.provideScrollOnDropDown(cbGroupId);

        canvasWave.setOnDragDropped(onSampleDragDropped);
        canvasWave.setOnDragEntered(onCanvasDragEntered);
        canvasWave.setOnDragExited(onCanvasDragExited);
        canvasWave.setOnMouseMoved(onCanvasMouseMoved);
        canvasWave.setOnMouseExited(onCanvasMouseExited);
        canvasWave.setOnMouseDragged(onCanvasMouseDragged);
        canvasWave.setOnMousePressed(onCanvasMousePressed);
        canvasWave.setOnMouseReleased(onCanvasMouseReleased);
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

        labelPianoSampleName.setStyle(Styles.LABEL_P_F_SAMPLE_NAME);
        labelForteSampleName.setStyle(Styles.LABEL_P_F_SAMPLE_NAME);

        initTooltips();
    }

    private void initTooltips() {
        cbNoteId.setTooltip(TooltipHelper.getTooltip1(Strings.TOOLTIP_NOTE_SHORT));
        cbGroupId.setTooltip(TooltipHelper.getTooltip1(Strings.TOOLTIP_GROUP_ID));
        sliderPan.setTooltip(TooltipHelper.getTooltip1(Strings.TOOLTIP_PANORAMA));
        chbDynamic.setTooltip(TooltipHelper.getTooltip1(Strings.TOOLTIP_DYNAMIC));
        chbDisableNoteOff.setTooltip(TooltipHelper.getTooltip1(Strings.TOOLTIP_DISABLE_NOTE_OFF));
        chbLoop.setTooltip(TooltipHelper.getTooltip1(Strings.TOOLTIP_LOOP));

        labelNote.setTooltip(TooltipHelper.getTooltip1(Strings.TOOLTIP_NOTE_SHORT));
        labelCutGroup.setTooltip(TooltipHelper.getTooltip1(Strings.TOOLTIP_GROUP_ID));
        labelSiderValue.setTooltip(TooltipHelper.getTooltip1(Strings.TOOLTIP_PANORAMA));

        tabDefaultSample.setTooltip(TooltipHelper.getTooltip1(Strings.TOOLTIP_DEFAULT_SAMPLE));
        tabPianoSample.setTooltip(TooltipHelper.getTooltip1(Strings.TOOLTIP_PIANO_SAMPLE));
        tabForteSample.setTooltip(TooltipHelper.getTooltip1(Strings.TOOLTIP_FORTE_SAMPLE));

        btnDelete.setTooltip(TooltipHelper.getTooltip1(Strings.TOOLTIP_DELETE_SAMPLE));
        btnPlay.setTooltip(TooltipHelper.getTooltip1(Strings.TOOLTIP_PLAY));
    }

    private void handleCanvasMoseMoved(MouseEvent event) {
        Canvas canvas = (Canvas) event.getTarget();
        byte[] samplesData = sample.getSelectedSampleData();
        SustainLoop loop = sample.getSelectedSustainLoop();
        long loopPositionOnDrag = getLoopPositionOnDrag(event, canvas, samplesData);
        long loopDragArea = samplesData.length / Config.BYTES_PER_SAMPLE / (long) canvas.getWidth() * 2;

        if (loopPositionOnDrag > loop.start - loopDragArea && loopPositionOnDrag < loop.start + loopDragArea) {
            handler.onCursorChange(Cursor.OPEN_HAND);
        } else {
            handler.onCursorChange(Cursor.DEFAULT);
        }
    }

    private void handleLoopPointDragStarted(MouseEvent event) {
        Canvas canvas = (Canvas) event.getTarget();
        byte[] samplesData = sample.getSelectedSampleData();
        SustainLoop loop = sample.getSelectedSustainLoop();
        long loopPositionOnDrag = getLoopPositionOnDrag(event, canvas, samplesData);
        long loopDragArea = samplesData.length / Config.BYTES_PER_SAMPLE / (long) canvas.getWidth() * 2;

        if (loopPositionOnDrag > loop.start - loopDragArea && loopPositionOnDrag < loop.start + loopDragArea) {
            isLoopPointDraggeg = true;
            handler.onCursorChange(Cursor.CLOSED_HAND);
        }
    }

    private void handleLoopPointDragged(MouseEvent event) {
        Canvas canvas = (Canvas) event.getTarget();
        byte[] samplesData = sample.getSelectedSampleData();
        SustainLoop loop = sample.getSelectedSustainLoop();

        if (sample.loop != null) {
            long loopPositionOnDrag = getLoopPositionOnDrag(event, canvas, samplesData);
            loopPositionOnDrag = loopPositionOnDrag - loopPositionOnDrag % sample.channels;
            if (loopPositionOnDrag < 0) {
                loopPositionOnDrag = 0;
            } else if (loopPositionOnDrag > samplesData.length / Config.BYTES_PER_SAMPLE) {
                loopPositionOnDrag = samplesData.length / Config.BYTES_PER_SAMPLE - (Config.MIN_LOOP_LENGTH_SAMPLES * sample.channels);
            }
            loop.start = loopPositionOnDrag;
            if (sample.isPlaying) {
                RawPCMDataPlayer.updateLoopPoints((int) (loop.start / sample.channels), (int) (loop.end / sample.channels) - 1);
            } else {
                updateCanvasWave(canvas, samplesData, sample.channels, loop, -1);
            }
        }
    }

    private long getLoopPositionOnDrag(MouseEvent event, Canvas canvas, byte[] samplesData) {
        return (long) (event.getX() / canvas.getWidth() * ((double) samplesData.length / Config.BYTES_PER_SAMPLE));
    }

    private void updateCanvasWave(Canvas canvas, byte[] samplesData, int channels, SustainLoop loop, int framePosition) {
        if (samplesData != null) {
            WaveDrawingTool.drawWave16Bit(canvas, samplesData, channels, loop, framePosition);
        }
    }

    private void updateLabelSliderValue(long value) {
        labelSiderValue.setText(String.format(Strings.PAN_VALUE, value));
    }

    private void updatePlayButtonLabel(boolean isPlaying) {
        btnPlay.setText(isPlaying ? Strings.STOP : Strings.PLAY);
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

    private void onCanvasWaveClicked(Canvas canvas, byte[] samplesData, MouseEvent event) {
        if (isLoopPointDraggeg || samplesData == null) {
            return;
        }
        double position = event.getX() / canvas.getWidth();
        sample.isPlaying = false;
        handler.onSampleListCellPlay(sample, position);
        startStopTimerSchedule(sample.isPlaying);
        updatePlayButtonLabel(sample.isPlaying);
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

    private final EventHandler onSampleDragDropped = new EventHandler<DragEvent>() {
        @Override
        public void handle(DragEvent event) {
            event.consume();
            handler.onSampleListCellFileDragged(sample, event);
        }
    };

    private final EventHandler onCanvasMouseMoved = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            handleCanvasMoseMoved(event);
        }
    };

    private final EventHandler onCanvasMouseExited = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if (!isLoopPointDraggeg) {
                handler.onCursorChange(Cursor.DEFAULT);
            }
        }
    };

    private final EventHandler onCanvasMouseDragged = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if (isLoopPointDraggeg) {
                event.consume();
                handleLoopPointDragged(event);
            }
        }
    };

    private final EventHandler onCanvasMousePressed = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            handleLoopPointDragStarted(event);
        }
    };

    private final EventHandler onCanvasMouseReleased = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if (isLoopPointDraggeg) {
                event.consume();
                Platform.runLater(() -> {
                    isLoopPointDraggeg = false;
                });
                handler.onCursorChange(Cursor.DEFAULT);
            }
        }
    };

    private final EventHandler<ActionEvent> timerActionEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            if (!sample.isPlaying) {
                startStopTimerSchedule(sample.isPlaying);
                updatePlayButtonLabel(sample.isPlaying);
            }

            switch (sample.playingSampleExt) {
                case Sample.EXT_P:
                    updateCanvasWave(canvasWaveP, sample.samplesDataP, sample.channels, sample.loopP, RawPCMDataPlayer.getFramePosition());
                    break;
                case Sample.EXT_F:
                    updateCanvasWave(canvasWaveF, sample.samplesDataF, sample.channels, sample.loopF, RawPCMDataPlayer.getFramePosition());
                    break;
                default:
                    updateCanvasWave(canvasWave, sample.samplesData, sample.channels, sample.loop, RawPCMDataPlayer.getFramePosition());
                    break;
            }
        }
    };

    @FXML
    private void handleCanvasWaveClicked(MouseEvent event) {
        onCanvasWaveClicked(canvasWave, sample.samplesData, event);
    }

    @FXML
    private void handleCanvasWavePClicked(MouseEvent event) {
        onCanvasWaveClicked(canvasWaveP, sample.samplesDataP, event);
    }

    @FXML
    private void handleCanvasWaveFClicked(MouseEvent event) {
        onCanvasWaveClicked(canvasWaveF, sample.samplesDataF, event);
    }

    @FXML
    private void handleDeleteButtonClicked(MouseEvent event) {
        handler.onSampleListCellDelete(id);
    }

    @FXML
    private void handlePlayButtonClicked(MouseEvent event) {
        handler.onSampleListCellPlay(sample, 0);
        startStopTimerSchedule(sample.isPlaying);
        updatePlayButtonLabel(sample.isPlaying);
    }

    @FXML
    private void onTabDefaultSampleClicked(Event event) {
        if (this.sample != null) {
            this.sample.selectedSampleExt = Sample.EXT_DEFAULT;
        }
    }

    @FXML
    private void onTabPianoSampleClicked(Event event) {
        if (this.sample != null) {
            this.sample.selectedSampleExt = Sample.EXT_P;
        }
    }

    @FXML
    private void onTabForteSampleClicked(Event event) {
        if (this.sample != null) {
            this.sample.selectedSampleExt = Sample.EXT_F;
        }
    }

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
            if (sample.loop != null) {
                sample.loopEnabled = newValue;
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

}
