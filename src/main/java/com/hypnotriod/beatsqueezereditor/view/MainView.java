package com.hypnotriod.beatsqueezereditor.view;

import com.hypnotriod.beatsqueezereditor.Main;
import com.hypnotriod.beatsqueezereditor.base.BaseView;
import com.hypnotriod.beatsqueezereditor.constants.Config;
import com.hypnotriod.beatsqueezereditor.constants.FileExtensions;
import com.hypnotriod.beatsqueezereditor.constants.Resources;
import com.hypnotriod.beatsqueezereditor.constants.Strings;
import com.hypnotriod.beatsqueezereditor.constants.UIConfig;
import com.hypnotriod.beatsqueezereditor.facade.Facade;
import com.hypnotriod.beatsqueezereditor.model.dto.PlayEvent;
import com.hypnotriod.beatsqueezereditor.model.dto.SampleDragEvent;
import com.hypnotriod.beatsqueezereditor.model.entity.Sample;
import com.hypnotriod.beatsqueezereditor.model.entity.SustainLoop;
import com.hypnotriod.beatsqueezereditor.tools.FileName;
import com.hypnotriod.beatsqueezereditor.tools.RawPCMDataPlayer;
import com.hypnotriod.beatsqueezereditor.tools.StringUtils;
import com.hypnotriod.beatsqueezereditor.view.controller.MainSceneViewController;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;

/**
 *
 * @author Ilya Pikin
 */
public class MainView extends BaseView {
    
    public static final int  BYTES_PER_MEGABYTE = 1024 * 1024;

    private MainSceneViewController mainSceneController;
    private AnchorPane mainScene;

    public MainView(Facade facade) {
        super(facade);

        createMainScene();
    }

    private void createMainScene() {
        try {
            FXMLLoader loader = new FXMLLoader();
            mainScene = (AnchorPane) loader.load(getClass().getResourceAsStream(Resources.PATH_MAIN_SCENE));

            mainSceneController = loader.getController();
            mainSceneController.setSamples(getMainModel().samples,
                    getMainModel().sampleOptions);
            mainSceneController.setView(this);

            getFacade().getPrimaryStage().getIcons().add(new Image(Main.class.getResourceAsStream(Resources.PATH_ICON)));
            getFacade().getPrimaryStage().setMinHeight(UIConfig.APP_MIN_HEIGHT);
            getFacade().getPrimaryStage().setMinWidth(UIConfig.APP_MIN_WIDTH);
            getFacade().getPrimaryStage().setMaxWidth(UIConfig.APP_MAX_WIDTH);
            updateTitle();

            Scene scene = new Scene(mainScene);
            getFacade().getPrimaryStage().setScene(scene);
            getFacade().getPrimaryStage().show();

            mainSceneController.refreshListView(true, true);
        } catch (IOException e) {
        }
    }

    @Override
    protected void handleViewControllerNotification(String name, Object data) {
        switch (name) {
            case MainSceneViewController.ON_ADD_SAMPLES:
                performSamplesLoad((List<File>) data);
                break;

            case MainSceneViewController.ON_EXPORT_SAMPLES:
                performExportSamples();
                break;

            case MainSceneViewController.ON_SAVE_BANK:
                performBankSave();
                break;

            case MainSceneViewController.ON_LOAD_BANK:
                performBankLoad((File) data);
                break;

            case MainSceneViewController.ON_SAMPLE_DELETE:
                performDeleteSample((String) data);
                break;

            case MainSceneViewController.ON_SAMPLE_PLAY:
                performPlaySample((PlayEvent) data);
                break;

            case MainSceneViewController.ON_SAMPLE_DRAG:
                performSampleDrag((SampleDragEvent) data);
                break;

            case MainSceneViewController.ON_SAMPLES_CLEAR:
                performClearSamples();
                break;

            case MainSceneViewController.ON_FILES_DRAG:
                performFilesDrag((DragEvent) data);
                break;
        }
    }

    private void performFilesDrag(DragEvent event) {
        Dragboard db = event.getDragboard();
        String fileExtension;
        List<File> samples;
        boolean success = false;
        if (db.hasFiles()) {
            success = true;
            String filePath;
            samples = new ArrayList<>();
            for (File file : db.getFiles()) {
                filePath = file.getAbsolutePath();
                fileExtension = "." + new FileName(filePath, '/', '.').extension().toLowerCase();
                if (fileExtension.equals(FileExtensions.BANK_FILE_EXTENSION)) {
                    samples.clear();
                    performBankLoad(file);
                    break;
                } else if (fileExtension.equals(FileExtensions.WAVE_FILE_EXTENSION)
                        || fileExtension.equals(FileExtensions.WAV_FILE_EXTENSION)) {
                    samples.add(file);
                }
            }
            if (samples.size() > 0) {
                performSamplesLoad(samples);
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private void performSampleDrag(SampleDragEvent dragEvent) {
        Dragboard db = dragEvent.event.getDragboard();
        String fileExtension;
        File sample = null;
        String filePath;
        boolean success = false;
        if (db.hasFiles()) {
            success = true;
            for (File file : db.getFiles()) {
                filePath = file.getAbsolutePath();
                fileExtension = "." + new FileName(filePath, '/', '.').extension().toLowerCase();
                if (fileExtension.equals(FileExtensions.WAVE_FILE_EXTENSION)
                        || fileExtension.equals(FileExtensions.WAV_FILE_EXTENSION)) {
                    sample = file;
                    break;
                }
            }
            if (sample != null) {
                performSamplesLoadOnDrag(dragEvent.sample, sample);
            }
        }
        dragEvent.event.setDropCompleted(success);
        dragEvent.event.consume();
    }

    private void performDeleteSample(String id) {
        manageSampleStop();
        getMainModel().deleteSample(id);
        mainSceneController.refreshListView(true, true);
        mainScene.requestFocus();
        updateTitle();
    }

    private synchronized void performPlaySample(PlayEvent playEvent) {
        SustainLoop loop;
        AudioInputStream audioInputStream;

        Sample sample = playEvent.sample;

        if (sample.isPlaying == false) {
            sampleStopInProgress = true;
            try {
                getMainModel().stopAllSamples();
                mainSceneController.refreshListView(false, false);

                audioInputStream = sample.getAudioStream();
                if (audioInputStream != null) {
                    sample.isPlaying = true;

                    switch (sample.selectedSampleExt) {
                        case Sample.EXT_P:
                            loop = sample.loopP;
                            break;
                        case Sample.EXT_F:
                            loop = sample.loopF;
                            break;
                        default:
                            loop = sample.loop;
                            break;
                    }

                    RawPCMDataPlayer.play(audioInputStream,
                            (loop != null && sample.loopEnabled) ? (int) (loop.start / sample.channels) : 0,
                            (loop != null && sample.loopEnabled) ? (int) (loop.end / sample.channels) - 1 : 0,
                            sample.channels == 1 ? ((float) sample.panorama / (float) Config.PANORAMA_MAX_VALUE) : 0.0f,
                            playEvent.position,
                            lineListener);
                    sample.playingSampleExt = sample.selectedSampleExt;
                }
            } catch (IOException | LineUnavailableException e) {
                String message = String.format(Strings.AUDIO_OUTPUT_DEVICE_PROBLEM, e.getMessage());
                showMessageBoxError(message);
            }
            sampleStopInProgress = false;
        } else {
            manageSampleStop();
            mainSceneController.refreshListView(false, false);
        }
        mainScene.requestFocus();
    }

    private boolean sampleStopInProgress = false;
    private final LineListener lineListener = new LineListener() {
        @Override
        public void update(LineEvent lineEvent) {
            if (sampleStopInProgress == false && lineEvent.getType() == LineEvent.Type.STOP) {
                manageSampleStop();
            }
        }
    };

    private synchronized void manageSampleStop() {
        sampleStopInProgress = true;
        getMainModel().stopAllSamples();
        RawPCMDataPlayer.stop();
        sampleStopInProgress = false;
    }

    private void performClearSamples() {
        getMainModel().clearAllSamples();
        manageSampleStop();
        refresh();
    }

    private void performExportSamples() {
        if (getFacade().getExportSamplesController().checkCondition() == false) {
            return;
        }
        File file = getFacade().getExportSamplesController().chooseFile();
        Thread thread;
        if (file != null) {
            manageSampleStop();
            mainSceneController.refreshListView(false, false);
            mainSceneController.showLoading();
            thread = new Thread(() -> {
                getFacade().getExportSamplesController().saveSamples(file);
                Platform.runLater(() -> {
                    mainSceneController.hideLoading();
                    refresh();
                });
            });
            thread.setDaemon(true);
            thread.start();
        }
    }

    private void performSamplesLoadOnDrag(Sample sample, File file) {
        int pitch = this.getMainModel().sampleOptions.pitch;
        Thread thread;
        manageSampleStop();
        mainSceneController.refreshListView(false, false);
        mainSceneController.showLoading();
        thread = new Thread(() -> {
            if (sample.selectedSampleExt.equals(Sample.EXT_DEFAULT)) {
                sample.fileName = StringUtils.removeFileExtension(file.getName());
                sample.fileRealName = sample.fileName;
            }
            getFacade().getLoadSamplesController().manageAdditionalSample(file, sample.fileRealName + sample.selectedSampleExt, pitch);
            Platform.runLater(() -> {
                mainSceneController.hideLoading();
                refresh();
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void performSamplesLoad(List<File> withSamples) {
        List<File> files = (withSamples == null) ? getFacade().getLoadSamplesController().chooseFiles() : withSamples;
        Thread thread;
        int pitchStep = this.getMainModel().sampleOptions.pitchStep;
        int pitch = this.getMainModel().sampleOptions.pitch;
        if (files != null) {
            manageSampleStop();
            mainSceneController.refreshListView(false, false);
            mainSceneController.showLoading();
            thread = new Thread(() -> {
                getFacade().getLoadSamplesController().loadSamples(files, pitchStep, pitch);
                Platform.runLater(() -> {
                    mainSceneController.hideLoading();
                    refresh();
                });
            });
            thread.setDaemon(true);
            thread.start();
        }
    }

    private void performBankSave() {
        if (getFacade().getSaveBankController().checkCondition() == false) {
            return;
        }
        File file = getFacade().getSaveBankController().chooseFile();
        Thread thread;
        if (file != null) {
            manageSampleStop();
            mainSceneController.refreshListView(false, false);
            mainSceneController.showLoading();
            thread = new Thread(() -> {
                getFacade().getSaveBankController().saveBank(file);
                Platform.runLater(() -> {
                    mainSceneController.hideLoading();
                    refresh();
                });
            });
            thread.setDaemon(true);
            thread.start();
        }
    }

    private void performBankLoad(File fromFile) {
        File file = (fromFile == null) ? getFacade().getLoadBankController().chooseFile() : fromFile;
        Thread thread;
        if (file != null) {
            performClearSamples();
            mainSceneController.showLoading();
            thread = new Thread(() -> {
                getFacade().getLoadBankController().loadBank(file);
                Platform.runLater(() -> {
                    mainSceneController.hideLoading();
                    refresh();
                });
            });
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void refresh() {
        mainSceneController.refreshListView(true, true);
        mainSceneController.refreshSelection();
        mainSceneController.refreshFiltersValues();
        mainScene.requestFocus();
        updateTitle();
    }

    public void updateTitle() {
        getFacade().getPrimaryStage().setTitle(String.format(Strings.TITLE,
                        StringUtils.removeFileExtension(getMainModel().sampleOptions.fileName),
                        (float) (getMainModel().getAllSamplesDataSize() + Config.DATA_START_INDEX) / (float) BYTES_PER_MEGABYTE));
    }

    public void showMessageBoxError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(Strings.ALERT_TITLE_ERROR);
            alert.setHeaderText(Strings.ALERT_HEADER_ERROR);
            alert.setContentText(message);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(Main.class.getResourceAsStream(Resources.PATH_ICON)));
            alert.showAndWait();
        });
    }

    public void showMessageBoxInfo(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(Strings.ALERT_TITLE_INFO);
            alert.setHeaderText(message);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(Main.class.getResourceAsStream(Resources.PATH_ICON)));
            alert.showAndWait();
        });
    }
}
