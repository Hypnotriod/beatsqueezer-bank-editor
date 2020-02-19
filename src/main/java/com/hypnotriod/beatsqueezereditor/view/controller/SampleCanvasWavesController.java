package com.hypnotriod.beatsqueezereditor.view.controller;

import com.hypnotriod.beatsqueezereditor.constants.Config;
import com.hypnotriod.beatsqueezereditor.constants.Strings;
import com.hypnotriod.beatsqueezereditor.model.entity.Sample;
import com.hypnotriod.beatsqueezereditor.model.entity.SampleOptions;
import com.hypnotriod.beatsqueezereditor.model.entity.SustainLoop;
import com.hypnotriod.beatsqueezereditor.utility.LoopPointAdjustUtil;
import com.hypnotriod.beatsqueezereditor.utility.RawPCMDataPlayer;
import com.hypnotriod.beatsqueezereditor.utility.StringUtils;
import com.hypnotriod.beatsqueezereditor.utility.TooltipUtil;
import com.hypnotriod.beatsqueezereditor.utility.WaveDrawingUtil;
import com.hypnotriod.beatsqueezereditor.view.component.SampleListCellHandler;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

/**
 *
 * @author Ilya Pikin
 */
public abstract class SampleCanvasWavesController implements Initializable {

    @FXML
    private Canvas canvasWave;
    @FXML
    private Canvas canvasWaveP;
    @FXML
    private Canvas canvasWaveF;
    @FXML
    private Button btnPlay;
    @FXML
    private Button btnLoopStartDecrease;
    @FXML
    private Button btnLoopStartIncrease;
    @FXML
    private Label labelLoopStart;

    protected SampleOptions sampleOptions;
    protected String id;
    protected Sample sample;
    protected boolean isLoopPointDraggeg = false;
    protected SampleListCellHandler handler;
    protected Timeline samplePlayTimer = null;
    protected boolean updateInProgress = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        registerWaveCanvasListeners(canvasWave);
        registerWaveCanvasListeners(canvasWaveP);
        registerWaveCanvasListeners(canvasWaveF);

        initTooltips();
    }

    private void initTooltips() {
        btnPlay.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_PLAY));
        btnLoopStartDecrease.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_DECREASE_LOOP_START));
        btnLoopStartIncrease.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_INCREASE_LOOP_START));
        labelLoopStart.setTooltip(TooltipUtil.getTooltipDefault(Strings.TOOLTIP_LOOP_START_POSITION));
    }

    private void registerWaveCanvasListeners(Canvas canvasWave) {
        canvasWave.setOnDragDropped(onSampleDragDropped);
        canvasWave.setOnDragEntered(onCanvasDragEntered);
        canvasWave.setOnDragExited(onCanvasDragExited);
        canvasWave.setOnMouseMoved(onCanvasMouseMoved);
        canvasWave.setOnMouseExited(onCanvasMouseExited);
        canvasWave.setOnMouseDragged(onCanvasMouseDragged);
        canvasWave.setOnMousePressed(onCanvasMousePressed);
        canvasWave.setOnMouseReleased(onCanvasMouseReleased);
    }

    public void update(Sample sample, SampleOptions sampleOptions, String id) {
        this.sample = sample;
        this.sampleOptions = sampleOptions;
        this.id = id;

        canvasWaveP.setVisible(sample.samplesDataP != null);
        canvasWaveF.setVisible(sample.samplesDataF != null);

        updatePlayButtonLabel(this.sample.isPlaying);

        updateCanvasWaves();
        updateAdjustLoopStartComponents();
        startStopTimerSchedule(this.sample.isPlaying);
    }

    protected void updateAdjustLoopStartComponents() {
        SustainLoop loop = sample.getSelectedSustainLoop();
        if (loop != null) {
            labelLoopStart.setText(StringUtils.getLoopStartTime(loop, sample.channels, Config.SAMPLE_RATE));
        } else {
            labelLoopStart.setText("");
        }

        labelLoopStart.setDisable(!sample.isLoopEnabled);
        btnLoopStartDecrease.setDisable(!sample.isLoopEnabled);
        btnLoopStartIncrease.setDisable(!sample.isLoopEnabled);
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

    private void handleLoopPointDragStarted(MouseEvent event) {
        if (!sample.isLoopEnabled) {
            return;
        }

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
        Canvas canvas = getSelectedWaveCanvas();
        byte[] samplesData = sample.getSelectedSampleData();
        SustainLoop loop = sample.getSelectedSustainLoop();

        if (sample.loop != null) {
            long loopPositionOnDrag = getLoopPositionOnDrag(event, canvas, samplesData);
            loopPositionOnDrag = loopPositionOnDrag - loopPositionOnDrag % sample.channels;
            loop.start = loopPositionOnDrag;
            LoopPointAdjustUtil.normalizeLoop(samplesData, loop, sample.channels);
            handleLoopPointChanged(canvas, samplesData, loop);
        }
    }

    private void handleLoopPointDragFinished() {
        Canvas canvas = getSelectedWaveCanvas();
        byte[] samplesData = sample.getSelectedSampleData();
        SustainLoop loop = sample.getSelectedSustainLoop();

        if (sample.loop != null) {
            LoopPointAdjustUtil.adjustLoopStart(samplesData, loop, sample.channels);
            handleLoopPointChanged(canvas, samplesData, loop);
        }
    }

    private void handleLoopPointChanged(Canvas canvas, byte[] samplesData, SustainLoop loop) {
        if (sample.isPlaying && sample.selectedSampleExt.equals(sample.playingSampleExt)) {
            RawPCMDataPlayer.updateLoopPoints((int) (loop.start / sample.channels), (int) (loop.end / sample.channels) - 1);
        } else {
            updateCanvasWave(canvas, samplesData, sample.channels, loop, -1);
        }
        updateAdjustLoopStartComponents();
    }

    private long getLoopPositionOnDrag(MouseEvent event, Canvas canvas, byte[] samplesData) {
        return (long) (event.getX() / canvas.getWidth() * ((double) samplesData.length / Config.BYTES_PER_SAMPLE));
    }

    private void handleCanvasMoseMoved(MouseEvent event) {
        if (!sample.isLoopEnabled) {
            return;
        }

        Canvas canvas = getSelectedWaveCanvas();
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

    private void onCanvasWaveClicked(Canvas canvas, byte[] samplesData, MouseEvent event) {
        if (isLoopPointDraggeg || samplesData == null) {
            return;
        }
        double position = event.getX() / canvas.getWidth();
        sample.isPlaying = false;
        handler.onSampleListCellPlayStop(sample, position);
        startStopTimerSchedule(sample.isPlaying);
        updatePlayButtonLabel(sample.isPlaying);
    }

    private void updatePlayButtonLabel(boolean isPlaying) {
        btnPlay.setText(isPlaying ? Strings.STOP : Strings.PLAY);
    }

    protected void updateCanvasWaves() {
        updateCanvasWave(canvasWave, this.sample.samplesData, this.sample.channels, this.sample.loop, -1);
        updateCanvasWave(canvasWaveP, this.sample.samplesDataP, this.sample.channels, this.sample.loopP, -1);
        updateCanvasWave(canvasWaveF, this.sample.samplesDataF, this.sample.channels, this.sample.loopF, -1);
    }

    private void updateCanvasWave(Canvas canvas, byte[] samplesData, int channels, SustainLoop loop, int framePosition) {
        if (samplesData != null) {
            WaveDrawingUtil.drawWave16Bit(canvas, samplesData, channels, sample.isLoopEnabled ? loop : null, framePosition);
        }
    }

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
                handler.onCursorChange(Cursor.DEFAULT);
                handleLoopPointDragFinished();
                Platform.runLater(() -> {
                    isLoopPointDraggeg = false;
                });
            }
        }
    };

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

    protected final EventHandler onSampleDragDropped = new EventHandler<DragEvent>() {
        @Override
        public void handle(DragEvent event) {
            event.consume();
            handler.onSampleListCellFileDragged(sample, event);
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

    private Canvas getSelectedWaveCanvas() {
        switch (sample.selectedSampleExt) {
            case Sample.EXT_P:
                return canvasWaveP;
            case Sample.EXT_F:
                return canvasWaveF;
            default:
                return canvasWave;
        }
    }

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
    private void handlePlayButtonClicked(MouseEvent event) {
        handler.onSampleListCellPlayStop(sample, 0);
        startStopTimerSchedule(sample.isPlaying);
        updatePlayButtonLabel(sample.isPlaying);
    }

    @FXML
    private void handleLoopStartDecreaseButtonClicked(MouseEvent event) {
        Canvas canvas = getSelectedWaveCanvas();
        byte[] samplesData = sample.getSelectedSampleData();
        SustainLoop loop = sample.getSelectedSustainLoop();

        if (sample.loop != null) {
            LoopPointAdjustUtil.decreaseLoopStart(samplesData, loop, sample.channels);
            handleLoopPointChanged(canvas, samplesData, loop);
        }
    }

    @FXML
    private void handleLoopStartIncreaseButtonClicked(MouseEvent event) {
        Canvas canvas = getSelectedWaveCanvas();
        byte[] samplesData = sample.getSelectedSampleData();
        SustainLoop loop = sample.getSelectedSustainLoop();

        if (sample.loop != null) {
            LoopPointAdjustUtil.increaseLoopStart(samplesData, loop, sample.channels);
            handleLoopPointChanged(canvas, samplesData, loop);
        }
    }
}
