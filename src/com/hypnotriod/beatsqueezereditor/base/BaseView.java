package com.hypnotriod.beatsqueezereditor.base;

import com.hypnotriod.beatsqueezereditor.facade.Facade;
import com.hypnotriod.beatsqueezereditor.model.MainModel;

/**
 *
 * @author Ilya Pikin
 */
public abstract class BaseView {

    private final Facade _facade;

    public Facade getFacade() {
        return _facade;
    }

    public BaseView(Facade facade) {
        _facade = facade;
    }

    protected MainModel getMainModel() {
        return _facade.mainModel;
    }

    protected abstract void handleVCNotification(String name, Object data);
}
