package io.github.dephin.connection.models;

public class UtteranceResponse {
    private String text;
    private boolean isEnd;

    public UtteranceResponse(String text, boolean isEnd) {
        this.text = text;
        this.isEnd = isEnd;
    }
}
