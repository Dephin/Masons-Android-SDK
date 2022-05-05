package io.github.dephin.session;

import org.json.JSONException;

public class CalleeSession extends Session {

    public void reply(String text, boolean isEnd) throws JSONException {
        this.connection.sendReplyEvent(this.sessionID, text, isEnd);
    }

}
