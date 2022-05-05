package io.github.dephin;

import io.github.dephin.connection.MasonsConnection;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import io.github.dephin.connection.models.KnockResult;
import io.github.dephin.session.models.CreatingSessionOfCallee;
import io.github.dephin.session.models.ExitingSessionOfCaller;
import io.github.dephin.session.models.MasonsSDKConfig;
import io.github.dephin.session.models.ReplyFromCallee;
import io.github.dephin.session.models.UtteranceFromCaller;
import io.github.dephin.session.CalleeSession;
import io.github.dephin.session.CallerSession;

public abstract class AbstractMasonsSDK {
    private Map<String, CallerSession> accountKey2CallerSessionMapping = new HashMap<String, CallerSession>();
    private Map<String, CallerSession> sessionID2CallerSessionMapping= new HashMap<String, CallerSession>();
    private Map<String, CalleeSession> accountKey2CalleeSessionMapping= new HashMap<String, CalleeSession>();
    private Map<String, CalleeSession> sessionID2CalleeSessionMapping= new HashMap<String, CalleeSession>();
    private MasonsConnection connection;

    AbstractMasonsSDK(MasonsSDKConfig config) throws URISyntaxException {
        this.connection = new MasonsConnection(this, config);
    }

    public void start() throws JSONException {
        this.connection.start();
    }

    public void stop() {
        this.connection.stop();
    }

    public KnockResult broadcastKnock(String accountKey, String text, Map<String, Object> data) throws JSONException {
        JSONObject req = new JSONObject();
        req.put("account_key", accountKey);
        req.put("text", text);
        req.put("data", data);

        JSONObject resp = this.connection.callKnockEvent(req);

        if (resp == null) {
            return new KnockResult();
        }

        String sessionID = resp.getString("session_id");
        boolean success = resp.getBoolean("success");
        String respText = resp.getString("text");

        if (success) {
            this.createCallerSession(sessionID, accountKey);
        }

        KnockResult result = new KnockResult();
        result.setSuccess(success);
        result.setText(respText);

        return result;
    }

    public CallerSession getCallerSessionByAccountKey(String accountKey) {
        return this.accountKey2CallerSessionMapping.get(accountKey);
    }

    public CallerSession getCallerSessionBySessionID(String sessionID) {
        return this.sessionID2CallerSessionMapping.get(sessionID);
    }

    public CalleeSession getCalleeSessionByAccountKey(String accountKey) {
        return this.accountKey2CalleeSessionMapping.get(accountKey);
    }

    public CalleeSession getCalleeSessionBySessionID(String accountKey) {
        return this.sessionID2CalleeSessionMapping.get(accountKey);
    }

    public void createCallerSession(String sessionID, String accountKey) {
        CallerSession session = new CallerSession();
        session.setSessionID(sessionID);
        session.setAccountKey(accountKey);
        session.setConnection(this.connection);
        this.accountKey2CallerSessionMapping.put(accountKey, session);
        this.sessionID2CallerSessionMapping.put(sessionID, session);
    }

    public void createCalleeSession(String sessionID, String accountKey) {
        CalleeSession session = new CalleeSession();
        session.setSessionID(sessionID);
        session.setAccountKey(accountKey);
        session.setConnection(this.connection);
        this.accountKey2CalleeSessionMapping.put(accountKey, session);
        this.sessionID2CalleeSessionMapping.put(sessionID, session);
    }

    public void removeCallerSession(String sessionID, String accountKey) {
        CallerSession session = this.accountKey2CallerSessionMapping.get(accountKey);
        if (sessionID.equals(session.getSessionID())) {
            this.accountKey2CallerSessionMapping.remove(accountKey);
        }
        this.sessionID2CallerSessionMapping.remove(accountKey);
    }

    public void removeCalleeSession(String sessionID, String accountKey) {
        CalleeSession session = this.accountKey2CalleeSessionMapping.get(accountKey);
        if (sessionID.equals(session.getSessionID())) {
            this.accountKey2CalleeSessionMapping.remove(accountKey);
        }
        this.sessionID2CalleeSessionMapping.remove(accountKey);
    }

    public abstract void onReceivingUtteranceFromCaller(UtteranceFromCaller utterance);

    public abstract void onReceivingReplyFromCallee(ReplyFromCallee reply);

    public abstract void onExitingSessionOfCaller(ExitingSessionOfCaller session);

    public abstract void onCreatingSessionOfCallee(CreatingSessionOfCallee session);
}
