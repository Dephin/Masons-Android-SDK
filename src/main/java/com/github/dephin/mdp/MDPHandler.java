package com.github.dephin.mdp;

import org.json.JSONObject;

public interface MDPHandler {
    void receiveMessage(String event, JSONObject data) throws Exception;
}
