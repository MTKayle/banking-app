package com.example.mobilebanking.models;

/**
 * QuickAction model for dashboard quick actions
 */
public class QuickAction {
    private String title;
    private int iconResId;
    private String actionId;

    public QuickAction(String title, int iconResId, String actionId) {
        this.title = title;
        this.iconResId = iconResId;
        this.actionId = actionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }
}

