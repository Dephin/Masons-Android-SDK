package io.github.dephin.connection.models;

public class KnockResult {
    private boolean success = false;
    private String text = null;

    public boolean getSuccess() {
        return this.success;
    }

    public String getText() {
        return text;
    }


    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setText(String text) {
        this.text = text;
    }
}
