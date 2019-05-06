package com.hypnotriod.beatsqueezereditor;

import com.hypnotriod.beatsqueezereditor.facade.Facade;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 *
 * @author Ilya Pikin
 */
public class Main extends Application {

    Facade _facade;

    @Override
    public void start(Stage primaryStage) {
        _facade = new Facade(this, primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
