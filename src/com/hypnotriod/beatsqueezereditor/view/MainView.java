
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
import com.hypnotriod.beatsqueezereditor.view.components.MainSceneController;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

/**
 *
 * @author ipikin
 */
public class MainView extends BaseView
{
    private MainSceneController _mainSceneController;
    private AnchorPane _mainScene;
    
    public MainView(Facade facade) {
        super(facade);
        
        createMainScene();

        _mainSceneController.refreshListView(true, true);
    }

    private void createMainScene()
    {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource(CResources.PATH_MAIN_SCENE));
            _mainScene = (AnchorPane) loader.load();

            _mainSceneController = loader.getController();
            _mainSceneController.setSampleVOs(
                    getMainModel().sampleVOs,
                    getMainModel().optionsVO);
            _mainSceneController.setView(this);
            
            Scene scene = new Scene(_mainScene);
            scene.setOnDragDropped(onDragDropped);
            scene.setOnDragOver(onDragOver);
            getFacade().primaryStage.setScene(scene);
            getFacade().primaryStage.show();
            
            getFacade().primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream(CResources.PATH_ICON)));
            getFacade().primaryStage.setTitle(CStrings.TITLE);
            getFacade().primaryStage.setMinHeight(CConfig.APP_MIN_HEIGHT);
            getFacade().primaryStage.setMinWidth(CConfig.APP_MIN_WIDTH);
            getFacade().primaryStage.setMaxWidth(CConfig.APP_MAX_WIDTH);
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private EventHandler onDragOver = new EventHandler<DragEvent>() {
        @Override
        public void handle(DragEvent event) {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.MOVE);
            } else {
                event.consume();
            }
        }
    };
    
    private EventHandler onDragDropped = new EventHandler<DragEvent>() {
        @Override
        public void handle(DragEvent event) {
            Dragboard db = event.getDragboard();
            String fileExtention;
            List<File> samples;
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                String filePath = null;
                samples = new ArrayList<>();
                for (File file:db.getFiles()) {
                    filePath = file.getAbsolutePath();
                    fileExtention = "." + new FileName(filePath, '/', '.').extension().toLowerCase();
                    if(fileExtention.equals(CConfig.BANK_FILE_EXTENSION)) {
                        samples.clear();
                        performBankLoading(file);
                        break;
                    }
                    else if(fileExtention.equals(CConfig.WAVE_FILE_EXTENSION) || 
                            fileExtention.equals(CConfig.WAV_FILE_EXTENSION))
                    {
                        samples.add(file);
                    }
                }
                if(samples.size() > 0)
                    performSamplesLoad(samples);
            }
            event.setDropCompleted(success);
            event.consume();
        }
    };

    @Override
    protected void handleVCNotification(String name, Object data) 
    {
        switch(name)
        {
            case MainSceneController.ON_ADD_SAMPLES:
                performSamplesLoad(null);
                break;
                
            case MainSceneController.ON_EXPORT_SAMPLES:
                performExportSamples();
                break;
                
            case MainSceneController.ON_SAVE_BANK:
                performBankSaving();
                break;
                
            case MainSceneController.ON_LOAD_BANK:
                performBankLoading(null);
                break;
                
            case MainSceneController.ON_SAMPLE_DELETE:
                performDeleteSample((String)data);
                break;
                
            case MainSceneController.ON_SAMPLE_PLAY:
                performPlaySample((PlayEventVO)data);
                break;
                
            case MainSceneController.ON_SAMPLE_DRAG:
                performSampleDrag((SampleDragEventVO)data);
                break;
                
            case MainSceneController.ON_SAMPLES_CLEAR:
                performClearSamples();
                break;
        }
    }
    
    private void performSampleDrag(SampleDragEventVO dragEventVO)
    {
        Dragboard db = dragEventVO.dragEvent.getDragboard();
        String fileExtention;
        File sample = null;
        String filePath;
        boolean success = false;
        if (db.hasFiles()) {
            success = true;
            for (File file:db.getFiles()) {
                filePath = file.getAbsolutePath();
                fileExtention = "." + new FileName(filePath, '/', '.').extension().toLowerCase();
                if(fileExtention.equals(CConfig.WAVE_FILE_EXTENSION) 
                || fileExtention.equals(CConfig.WAV_FILE_EXTENSION))
                {
                    sample = file;
                    break;
                }
            }
            if(sample != null)
                performSamplesLoadOnDrag(dragEventVO.sampleVO, sample);
        }
        dragEventVO.dragEvent.setDropCompleted(success);
        dragEventVO.dragEvent.consume();
    }
    
    private void performDeleteSample(String id)
    {
        manageSampleStop();
        getMainModel().deleteSample(id);
        _mainSceneController.refreshListView(true, true);
        _mainScene.requestFocus();
    }
    
    private synchronized void performPlaySample(PlayEventVO playEvent)
    {
        SustainLoopVO loop;
        AudioInputStream audioInputStream;
        
        SampleVO sampleVO = playEvent.sampleVO;
        
        if(sampleVO.isPlaying == false)
        {
            sampleStopInProgress = true;
            try {
                getMainModel().stopAllSamples();
                _mainSceneController.refreshListView(false, false);
                
                audioInputStream = sampleVO.getAudioStream();
                if(audioInputStream != null)
                {
                    sampleVO.isPlaying = true;

                    if(sampleVO.selectedSampleExt.equals(CConfig.EXT_P)) loop = sampleVO.loopP;
                    else if(sampleVO.selectedSampleExt.equals(CConfig.EXT_F)) loop = sampleVO.loopF;  
                    else loop = sampleVO.loop; 
                    RawPCMDataPlayer.play(
                        audioInputStream, 
                        (loop != null && sampleVO.loopEnabled) ? (int)(loop.start / sampleVO.channels)   : 0, 
                        (loop != null && sampleVO.loopEnabled) ? (int)(loop.end / sampleVO.channels) - 1 : 0, 
                        sampleVO.channels == 1 ? ((float)sampleVO.panorama / (float)CConfig.PANORAMA_MAX_VALUE) : 0.0f, 
                        playEvent.position,
                        lineListener);
                    sampleVO.playingSampleExt = sampleVO.selectedSampleExt;
                }
            } 
            catch (Exception e) {
                String message = String.format(CStrings.AUDIO_OUTPUT_DEVICE_PROBLEM, e.getMessage());
                getFacade().mainView.showMessageBoxError(message);
            }
            sampleStopInProgress = false;
        }
        else
        {
            manageSampleStop();
            _mainSceneController.refreshListView(false, false);
        }
        _mainScene.requestFocus();
    }
    
    private boolean sampleStopInProgress = false;
    private LineListener lineListener = new LineListener() {
        @Override
        public void update(LineEvent lineEvent) {
            if(sampleStopInProgress == false && lineEvent.getType() == LineEvent.Type.STOP) {
                manageSampleStop();
            }
        }
    };
    
    private synchronized void manageSampleStop()
    {
        sampleStopInProgress = true;
        getMainModel().stopAllSamples();
        RawPCMDataPlayer.stop();
        sampleStopInProgress = false;
    }
    
    private void performClearSamples()
    {
        getMainModel().clearAllSamples();
        manageSampleStop();
        refresh();
    }
    
    private void performExportSamples()
    {
        if(getFacade().exportSamplesController.checkCondition() == false) return;
        File file = getFacade().exportSamplesController.chooseFile();
        Thread thread;
        if(file != null) {
            manageSampleStop();
            _mainSceneController.refreshListView(false, false);
            _mainSceneController.showLoading();
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    getFacade().exportSamplesController.saveSamples(file);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            _mainSceneController.hideLoading();
                            refresh();
                        }
                    });
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }
    
    private void performSamplesLoadOnDrag(SampleVO sampleVO, File file)
    {
        int pitch = this.getMainModel().optionsVO.pitch;
        Thread thread;
        manageSampleStop();
        _mainSceneController.refreshListView(false, false);
        _mainSceneController.showLoading();
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                getFacade().loadSamplesController.manageAdditionalSample(file, sampleVO.fileRealName + sampleVO.selectedSampleExt, pitch);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        _mainSceneController.hideLoading();
                        refresh();
                    }
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    
    private void performSamplesLoad(List<File> withSamples)
    {
        List<File> files = (withSamples == null) ? getFacade().loadSamplesController.chooseFiles() : withSamples;
        Thread thread;
        int pitchStep = this.getMainModel().optionsVO.pitchStep;
        int pitch = this.getMainModel().optionsVO.pitch;
        if(files != null) {
            manageSampleStop();
            _mainSceneController.refreshListView(false, false);
            _mainSceneController.showLoading();
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    getFacade().loadSamplesController.loadSamples(files, pitchStep, pitch);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            _mainSceneController.hideLoading();
                            refresh();
                        }
                    });
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }
    
    private void performBankSaving()
    {
        if(getFacade().saveBankController.checkCondition() == false) return;
        File file = getFacade().saveBankController.chooseFile();
        Thread thread;
        if(file != null) {
            manageSampleStop();
            _mainSceneController.refreshListView(false, false);
            _mainSceneController.showLoading();
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    getFacade().saveBankController.saveBank(file);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            _mainSceneController.hideLoading();
                            refresh();
                        }
                    });
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }
    
    private void performBankLoading(File fromFile)
    {
        File file = (fromFile == null) ? getFacade().loadBankController.chooseFile() : fromFile;
        Thread thread;
        if(file != null) {
            performClearSamples();
            _mainSceneController.showLoading();
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    getFacade().loadBankController.loadBank(file);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            _mainSceneController.hideLoading();
                            refresh();
                        }
                    });
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }
    
    public void refresh()
    {
        _mainSceneController.refreshListView(true, true);
        _mainSceneController.refreshSelection();
        _mainSceneController.refreshFiltersValues();
        _mainScene.requestFocus();
    }
    
    public void showMessageBoxError(String message)
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(CStrings.ALERT_TITLE_ERROR);
                alert.setHeaderText(CStrings.ALERT_HEADER_ERROR);
                alert.setContentText(message);
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.getIcons().add(new Image(Main.class.getResourceAsStream(CResources.PATH_ICON)));
                alert.showAndWait();
            }
        });
    }
    
    public void showMessageBoxInfo(String message)
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(CStrings.ALERT_TITLE_INFO);
                alert.setHeaderText(message);
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.getIcons().add(new Image(Main.class.getResourceAsStream(CResources.PATH_ICON)));
                alert.showAndWait();
            }
        });
    }
}
