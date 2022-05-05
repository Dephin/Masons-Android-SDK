package com.github.dephin.session.models;

import com.github.dephin.session.CallerSession;

public class UtteranceFromCaller {

    private CallerSession session;
    private String text;

    public UtteranceFromCaller(CallerSession session, String text) {
        this.session = session;
        this.text = text;
    }

    public void reply(String text, boolean isEnd) {

    }
}
