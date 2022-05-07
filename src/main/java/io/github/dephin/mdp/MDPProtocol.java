package io.github.dephin.mdp;

import org.json.JSONException;
import org.json.JSONObject;

public interface MDPProtocol {
    void sendMessage(JSONObject msg);
    void sendEvent(String event, JSONObject data);
    JSONObject callRPC(String event, JSONObject data);
    void sendError(String err);
    void connect();
    void close();
    void onOpen();
    void onError(Exception e);
    void onClose(int code, String reason, boolean remote);
    void onMessage(String message);
}
