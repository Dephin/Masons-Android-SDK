package io.github.dephin.connection.models;

public class KnockResult {
    private boolean success = false;
    private String text = null;
    private String sessionID = null;

    public boolean getSuccess() {
        return this.success;
    }

    public String getText() {
        return text;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }
}
