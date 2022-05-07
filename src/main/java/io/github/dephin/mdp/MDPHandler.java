package io.github.dephin.mdp;

import org.json.JSONObject;

public interface MDPHandler {
    void receiveEventMessage(String event, JSONObject data) throws Exception;
    void receiveRPCRequest(String event, JSONObject data) throws Exception;
}
