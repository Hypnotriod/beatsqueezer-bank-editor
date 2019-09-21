package com.hypnotriod.beatsqueezereditor.facade;

import com.hypnotriod.beatsqueezereditor.controller.ExportSamplesController;
import com.hypnotriod.beatsqueezereditor.controller.LoadBankController;
import com.hypnotriod.beatsqueezereditor.controller.LoadSamplesController;
import com.hypnotriod.beatsqueezereditor.controller.SaveBankController;
import com.hypnotriod.beatsqueezereditor.model.MainModel;
import com.hypnotriod.beatsqueezereditor.view.MainView;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 *
 * @author Ilya Pikin
 */
public class Facade {

    private final Application mainApplication;
    private final Stage primaryStage;

    private final LoadSamplesController loadSamplesController;
    private final LoadBankController loadBankController;
    private final SaveBankController saveBankController;
    private final ExportSamplesController exportSamplesController;
    private final MainView mainView;
    private final MainModel mainModel;

    public Facade(Application application, Stage stage) {
        primaryStage = stage;
        mainApplication = application;
        mainModel = new MainModel();
        mainView = new MainView(this);
        loadSamplesController = new LoadSamplesController(this);
        saveBankController = new SaveBankController(this);
        loadBankController = new LoadBankController(this);
        exportSamplesController = new ExportSamplesController(this);
    }

    public Application getMainApplication() {
        return mainApplication;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public LoadSamplesController getLoadSamplesController() {
        return loadSamplesController;
    }

    public LoadBankController getLoadBankController() {
        return loadBankController;
    }

    public SaveBankController getSaveBankController() {
        return saveBankController;
    }

    public ExportSamplesController getExportSamplesController() {
        return exportSamplesController;
    }

    public MainView getMainView() {
        return mainView;
    }

    public MainModel getMainModel() {
        return mainModel;
    }
}
