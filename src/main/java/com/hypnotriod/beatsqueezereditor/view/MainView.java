package com.hypnotriod.beatsqueezereditor.view;

import com.hypnotriod.beatsqueezereditor.Main;
import com.hypnotriod.beatsqueezereditor.base.BaseView;
import com.hypnotriod.beatsqueezereditor.constants.CConfig;
import com.hypnotriod.beatsqueezereditor.constants.CResources;
import com.hypnotriod.beatsqueezereditor.constants.CStrings;
import com.hypnotriod.beatsqueezereditor.facade.Facade;
import com.hypnotriod.beatsqueezereditor.model.vo.PlayEventVO;
import com.hypnotriod.beatsqueezereditor.model.vo.SampleDragEventVO;
import com.hypnotriod.beatsqueezereditor.model.vo.SampleVO;
import com.hypnotriod.beatsqueezereditor.model.vo.SustainLoopVO;
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

    private MainSceneViewController _mainSceneController;
    private AnchorPane _mainScene;

    public MainView(Facade facade) {
        super(facade);

        createMainScene();
    }

    private void createMainScene() {
        try {
            FXMLLoader loader = new FXMLLoader();
            _mainScene = (AnchorPane) loader.load(getClass().getResourceAsStream(CResources.PATH_MAIN_SCENE));

            _mainSceneController = loader.getController();
            _mainSceneController.setSampleVOs(
                    getMainModel().sampleVOs,
                    getMainModel().optionsVO);
            _mainSceneController.setView(this);

            getFacade().getPrimaryStage().getIcons().add(new Image(Main.class.getResourceAsStream(CResources.PATH_ICON)));
            getFacade().getPrimaryStage().setMinHeight(CConfig.APP_MIN_HEIGHT);
            getFacade().getPrimaryStage().setMinWidth(CConfig.APP_MIN_WIDTH);
            getFacade().getPrimaryStage().setMaxWidth(CConfig.APP_MAX_WIDTH);
            updateTitle();

            Scene scene = new Scene(_mainScene);
            getFacade().getPrimaryStage().setScene(scene);
            getFacade().getPrimaryStage().show();

            _mainSceneController.refreshListView(true, true);
        } catch (IOException e) {
            e.printStackTrace();
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
                performPlaySample((PlayEventVO) data);
                break;

            case MainSceneViewController.ON_SAMPLE_DRAG:
                performSampleDrag((SampleDragEventVO) data);
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
        String fileExtention;
        List<File> samples;
        boolean success = false;
        if (db.hasFiles()) {
            success = true;
            String filePath;
            samples = new ArrayList<>();
            for (File file : db.getFiles()) {
                filePath = file.getAbsolutePath();
                fileExtention = "." + new FileName(filePath, '/', '.').extension().toLowerCase();
                if (fileExtention.equals(CConfig.BANK_FILE_EXTENSION)) {
                    samples.clear();
                    performBankLoad(file);
                    break;
                } else if (fileExtention.equals(CConfig.WAVE_FILE_EXTENSION)
                        || fileExtention.equals(CConfig.WAV_FILE_EXTENSION)) {
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

    private void performSampleDrag(SampleDragEventVO dragEventVO) {
        Dragboard db = dragEventVO.dragEvent.getDragboard();
        String fileExtention;
        File sample = null;
        String filePath;
        boolean success = false;
        if (db.hasFiles()) {
            success = true;
            for (File file : db.getFiles()) {
                filePath = file.getAbsolutePath();
                fileExtention = "." + new FileName(filePath, '/', '.').extension().toLowerCase();
                if (fileExtention.equals(CConfig.WAVE_FILE_EXTENSION)
                        || fileExtention.equals(CConfig.WAV_FILE_EXTENSION)) {
                    sample = file;
                    break;
                }
            }
            if (sample != null) {
                performSamplesLoadOnDrag(dragEventVO.sampleVO, sample);
            }
        }
        dragEventVO.dragEvent.setDropCompleted(success);
        dragEventVO.dragEvent.consume();
    }

    private void performDeleteSample(String id) {
        manageSampleStop();
        getMainModel().deleteSample(id);
        _mainSceneController.refreshListView(true, true);
        _mainScene.requestFocus();
        updateTitle();
    }

    private synchronized void performPlaySample(PlayEventVO playEvent) {
        SustainLoopVO loop;
        AudioInputStream audioInputStream;

        SampleVO sampleVO = playEvent.sampleVO;

        if (sampleVO.isPlaying == false) {
            sampleStopInProgress = true;
            try {
                getMainModel().stopAllSamples();
                _mainSceneController.refreshListView(false, false);

                audioInputStream = sampleVO.getAudioStream();
                if (audioInputStream != null) {
                    sampleVO.isPlaying = true;

                    switch (sampleVO.selectedSampleExt) {
                        case CConfig.EXT_P:
                            loop = sampleVO.loopP;
                            break;
                        case CConfig.EXT_F:
                            loop = sampleVO.loopF;
                            break;
                        default:
                            loop = sampleVO.loop;
                            break;
                    }

                    RawPCMDataPlayer.play(
                            audioInputStream,
                            (loop != null && sampleVO.loopEnabled) ? (int) (loop.start / sampleVO.channels) : 0,
                            (loop != null && sampleVO.loopEnabled) ? (int) (loop.end / sampleVO.channels) - 1 : 0,
                            sampleVO.channels == 1 ? ((float) sampleVO.panorama / (float) CConfig.PANORAMA_MAX_VALUE) : 0.0f,
                            playEvent.position,
                            lineListener);
                    sampleVO.playingSampleExt = sampleVO.selectedSampleExt;
                }
            } catch (IOException | LineUnavailableException e) {
                String message = String.format(CStrings.AUDIO_OUTPUT_DEVICE_PROBLEM, e.getMessage());
                showMessageBoxError(message);
            }
            sampleStopInProgress = false;
        } else {
            manageSampleStop();
            _mainSceneController.refreshListView(false, false);
        }
        _mainScene.requestFocus();
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
            _mainSceneController.refreshListView(false, false);
            _mainSceneController.showLoading();
            thread = new Thread(() -> {
                getFacade().getExportSamplesController().saveSamples(file);
                Platform.runLater(() -> {
                    _mainSceneController.hideLoading();
                    refresh();
                });
            });
            thread.setDaemon(true);
            thread.start();
        }
    }

    private void performSamplesLoadOnDrag(SampleVO sampleVO, File file) {
        int pitch = this.getMainModel().optionsVO.pitch;
        Thread thread;
        manageSampleStop();
        _mainSceneController.refreshListView(false, false);
        _mainSceneController.showLoading();
        thread = new Thread(() -> {
            if (sampleVO.selectedSampleExt.equals(CConfig.EXT_DEFAULT)) {
                sampleVO.fileName = StringUtils.removeFileExtention(file.getName());
                sampleVO.fileRealName = sampleVO.fileName;
            }
            getFacade().getLoadSamplesController().manageAdditionalSample(file, sampleVO.fileRealName + sampleVO.selectedSampleExt, pitch);
            Platform.runLater(() -> {
                _mainSceneController.hideLoading();
                refresh();
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void performSamplesLoad(List<File> withSamples) {
        List<File> files = (withSamples == null) ? getFacade().getLoadSamplesController().chooseFiles() : withSamples;
        Thread thread;
        int pitchStep = this.getMainModel().optionsVO.pitchStep;
        int pitch = this.getMainModel().optionsVO.pitch;
        if (files != null) {
            manageSampleStop();
            _mainSceneController.refreshListView(false, false);
            _mainSceneController.showLoading();
            thread = new Thread(() -> {
                getFacade().getLoadSamplesController().loadSamples(files, pitchStep, pitch);
                Platform.runLater(() -> {
                    _mainSceneController.hideLoading();
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
            _mainSceneController.refreshListView(false, false);
            _mainSceneController.showLoading();
            thread = new Thread(() -> {
                getFacade().getSaveBankController().saveBank(file);
                Platform.runLater(() -> {
                    _mainSceneController.hideLoading();
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
            _mainSceneController.showLoading();
            thread = new Thread(() -> {
                getFacade().getLoadBankController().loadBank(file);
                Platform.runLater(() -> {
                    _mainSceneController.hideLoading();
                    refresh();
                });
            });
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void refresh() {
        _mainSceneController.refreshListView(true, true);
        _mainSceneController.refreshSelection();
        _mainSceneController.refreshFiltersValues();
        _mainScene.requestFocus();
        updateTitle();
    }

    public void updateTitle() {
        getFacade().getPrimaryStage().setTitle(
                String.format(
                        CStrings.TITLE,
                        StringUtils.removeFileExtention(getMainModel().optionsVO.fileName),
                        (float) (getMainModel().getAllSamplesDataSize() + CConfig.DATA_START_INDEX) / (float) CConfig.BYTES_PER_MEGABYTE));
    }

    public void showMessageBoxError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(CStrings.ALERT_TITLE_ERROR);
            alert.setHeaderText(CStrings.ALERT_HEADER_ERROR);
            alert.setContentText(message);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(Main.class.getResourceAsStream(CResources.PATH_ICON)));
            alert.showAndWait();
        });
    }

    public void showMessageBoxInfo(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(CStrings.ALERT_TITLE_INFO);
            alert.setHeaderText(message);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(Main.class.getResourceAsStream(CResources.PATH_ICON)));
            alert.showAndWait();
        });
    }
}