package com.hypnotriod.beatsqueezereditor.base;

/**
 *
 * @author Ilya Pikin
 */
public class BaseViewController {

    private BaseView view;

    public void setView(BaseView baseView) {
        view = baseView;
    }

    protected void sendToView(String type, Object data) {
        view.handleViewControllerNotification(type, data);
    }
}
