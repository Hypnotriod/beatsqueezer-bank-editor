package com.hypnotriod.beatsqueezereditor.base;

/**
 *
 * @author Ilya Pikin
 */
public class BaseViewController {

    private BaseView _view;

    public void setView(BaseView baseView) {
        _view = baseView;
    }

    protected void sendToView(String type, Object data) {
        _view.handleVCNotification(type, data);
    }
}
