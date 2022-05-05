package com.github.dephin.mdp;

import org.json.JSONException;
import org.json.JSONObject;

public interface MDPProtocol {
    void sendEvent(String event, JSONObject data) throws JSONException;
    JSONObject callEvent(String event, JSONObject data) throws JSONException;
}
