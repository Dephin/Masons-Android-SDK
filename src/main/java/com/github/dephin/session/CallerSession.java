package com.github.dephin.session;

import com.github.dephin.connection.models.UtteranceResponse;
import org.json.JSONException;

public class CallerSession extends Session {

    public UtteranceResponse utter(String text) throws JSONException {
        return this.connection.callUtteranceEvent(this.sessionID, text);
    }
}
