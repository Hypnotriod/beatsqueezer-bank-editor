package com.hypnotriod.beatsqueezereditor.base;

import com.hypnotriod.beatsqueezereditor.facade.Facade;
import com.hypnotriod.beatsqueezereditor.model.MainModel;

/**
 *
 * @author Ilya Pikin
 */
public abstract class BaseView {

    private final Facade facade;

    public Facade getFacade() {
        return facade;
    }

    public BaseView(Facade facade) {
        this.facade = facade;
    }

    protected MainModel getMainModel() {
        return facade.getMainModel();
    }

    protected abstract void handleViewControllerNotification(String name, Object data);
}
