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

    public Application mainApplication;
    public Stage primaryStage;

    public LoadSamplesController loadSamplesController;
    public LoadBankController loadBankController;
    public SaveBankController saveBankController;
    public ExportSamplesController exportSamplesController;
    public MainView mainView;
    public MainModel mainModel;

    public Facade(Application application, Stage stage) {
        primaryStage = stage;
        mainApplication = application;

        initModel();
        initViews();
        initControllers();
    }

    private void initModel() {
        mainModel = new MainModel();
    }

    private void initViews() {
        mainView = new MainView(this);
    }

    private void initControllers() {
        loadSamplesController = new LoadSamplesController(this);
        saveBankController = new SaveBankController(this);
        loadBankController = new LoadBankController(this);
        exportSamplesController = new ExportSamplesController(this);
    }
}
