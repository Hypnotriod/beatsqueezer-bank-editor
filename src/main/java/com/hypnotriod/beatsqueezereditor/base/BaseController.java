package com.hypnotriod.beatsqueezereditor.base;

import com.hypnotriod.beatsqueezereditor.facade.Facade;
import com.hypnotriod.beatsqueezereditor.model.MainModel;

/**
 *
 * @author Ilya Pikin
 */
public class BaseController {

    private final Facade _facade;

    public Facade getFacade() {
        return _facade;
    }

    public BaseController(Facade facade) {
        _facade = facade;
    }

    protected MainModel getMainModel() {
        return _facade.getMainModel();
    }

    protected void showMessageBoxInfo(String message) {
        getFacade().getMainView().showMessageBoxInfo(message);
    }

    protected void showMessageBoxError(String message) {
        getFacade().getMainView().showMessageBoxError(message);
    }
}
