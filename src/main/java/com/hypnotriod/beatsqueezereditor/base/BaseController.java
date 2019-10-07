package com.hypnotriod.beatsqueezereditor.base;

import com.hypnotriod.beatsqueezereditor.facade.Facade;
import com.hypnotriod.beatsqueezereditor.model.MainModel;

/**
 *
 * @author Ilya Pikin
 */
public class BaseController {

    private final Facade facade;

    public Facade getFacade() {
        return facade;
    }

    public BaseController(Facade facade) {
        this.facade = facade;
    }

    protected MainModel getMainModel() {
        return facade.getMainModel();
    }

    protected void showMessageBoxInfo(String message) {
        getFacade().getMainView().showMessageBoxInfo(message);
    }

    protected void showMessageBoxError(String message) {
        getFacade().getMainView().showMessageBoxError(message);
    }
}
