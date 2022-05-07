package io.github.dephin.mdp;

import org.json.JSONObject;

public interface MDPHandler {
    void processEventMessage(String event, JSONObject data);
    JSONObject processRPCRequest(String event, JSONObject data);
}
