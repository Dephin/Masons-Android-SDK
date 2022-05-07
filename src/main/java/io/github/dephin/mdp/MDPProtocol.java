package io.github.dephin.mdp;

import org.json.JSONException;
import org.json.JSONObject;

public interface MDPProtocol {
    void sendEvent(String event, JSONObject data) throws JSONException;
    JSONObject callEvent(String event, JSONObject data) throws JSONException;
    void connect();
    void close();
    void onOpen();
    void onError(Exception e);
    void onClose(int code, String reason, boolean remote);
    void onMessage(String message);
}
