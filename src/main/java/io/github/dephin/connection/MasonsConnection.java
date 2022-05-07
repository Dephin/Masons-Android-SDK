package io.github.dephin.connection;

import io.github.dephin.connection.models.UtteranceResponse;
import io.github.dephin.session.models.CreatingSessionOfCallee;
import io.github.dephin.MasonsSDKConfig;
import io.github.dephin.session.models.ReplyFromCallee;
import io.github.dephin.session.models.UtteranceFromCaller;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import io.github.dephin.AbstractMasonsSDK;
import io.github.dephin.connection.utils.HTTPUtil;
import io.github.dephin.mdp.MDPClient;
import io.github.dephin.mdp.MDPHandler;
import io.github.dephin.session.CalleeSession;
import io.github.dephin.session.CallerSession;

public class MasonsConnection implements MDPHandler {
    private String nodeID;
    private MDPClient mdpClient;
    private AbstractMasonsSDK sdk;
    private MasonsSDKConfig config;

    public MasonsConnection(AbstractMasonsSDK sdk, MasonsSDKConfig config) throws URISyntaxException {
        this.config = config;
        this.sdk = sdk;
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Sec-WebSocket-Protocol", "Duplex");
        headers.put("Access-Token", config.getAgentToken());
        headers.put("Node-ID", config.getNodeUrl());
        this.mdpClient = new MDPClient(new URI(this.config.getWsUrl()), this, headers, 1000);
    }

    private void createNode() throws JSONException {
        String nodeUrl = this.config.getNodeUrl();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Access-Token", this.config.getAgentToken());
        String resp = HTTPUtil.post(nodeUrl, "", headers);
        JSONObject respObj = new JSONObject(resp);
        this.nodeID = respObj.getString("node_id");
    }

    public void start() throws JSONException {
        this.createNode();
        this.mdpClient.connect();
    }

    public void stop() {
        this.mdpClient.close();
    }

    public JSONObject callKnockEvent(JSONObject data) throws JSONException {
        return this.mdpClient.callEvent("knock", data);
    }

    public UtteranceResponse callUtteranceEvent(String sessionID, String text) throws JSONException {
        JSONObject data = new JSONObject();
        data.put("session_id", sessionID);
        data.put("text", text);
        JSONObject resp = this.mdpClient.callEvent("utterance", data);
        String respText = resp.getString("text");
        boolean respIsEnd = resp.getBoolean("is_end");
        return new UtteranceResponse(respText, respIsEnd);
    }

    public void sendReplyEvent(String sessionID, String text, boolean isEnd) throws JSONException {
        JSONObject data = new JSONObject();
        data.put("session_id", sessionID);
        data.put("text", text);
        data.put("isEnd", isEnd);
        this.mdpClient.sendEvent("reply", data);
    }

    public void receiveMessage(String event, JSONObject data) throws Exception {
        String sessionID = data.getString("session_id");

        if (event.equals("utterance")) {
            String text = data.getString("text");
            CallerSession callerSession = this.sdk.getCallerSessionBySessionID(sessionID);
            UtteranceFromCaller utterance = new UtteranceFromCaller(callerSession, text);
            this.sdk.onReceivingUtteranceFromCaller(utterance);
        } else if (event.equals("reply")) {
            ReplyFromCallee reply = new ReplyFromCallee();
            this.sdk.onReceivingReplyFromCallee(reply);
        } else if (event.equals("create")) {
            String accountKey = data.getString("account_key");
            this.sdk.createCalleeSession(sessionID, accountKey);

            CreatingSessionOfCallee sessionWrapper = new CreatingSessionOfCallee();
            sessionWrapper.setAccountKey(accountKey);

            this.sdk.onCreatingSessionOfCallee(sessionWrapper);
        } else if (event.equals("exit")) {
            CalleeSession session = this.sdk.getCalleeSessionBySessionID(sessionID);
            String accountKey = session.getAccountKey();
            this.sdk.removeCalleeSession(sessionID, accountKey);

            CreatingSessionOfCallee sessionWrapper = new CreatingSessionOfCallee();
            sessionWrapper.setAccountKey(accountKey);

            this.sdk.onCreatingSessionOfCallee(sessionWrapper);
        } else {
            throw new Exception("This event is not supported");
        }
    }
}
