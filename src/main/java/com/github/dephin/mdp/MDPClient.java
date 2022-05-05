package com.github.dephin.mdp;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.*;


public class MDPClient extends WebSocketClient implements MDPProtocol {
    private MDPHandler handler;
    private Map<String, RPCWaiter> rpcWaiters = new HashMap<String, RPCWaiter>();
    private Set<String> ackCache = new HashSet<String>();
    private long rpcTimeout = 1L;

    public MDPClient(URI uri, MDPHandler handler) {
        super(uri);
        this.handler = handler;
    }

    public MDPClient(URI uri, MDPHandler handler, Map<String, String> headers, int connectTimeout) {
        super(uri, new Draft_6455(), headers, connectTimeout);
        this.handler = handler;
    }

    public MDPClient(URI uri, MDPHandler handler, Map<String, String> headers, int connectTimeout, long rpcTimeout) {
        super(uri, new Draft_6455(), headers, connectTimeout);
        this.handler = handler;
        this.rpcTimeout = rpcTimeout;
    }

    public void sendEvent(String event, JSONObject data) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg_id", this.generateUniID());
        jsonObject.put("event", event);
        jsonObject.put("data", data);
        this.send(data.toString());
    }

    public JSONObject callEvent(String event, JSONObject data) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        String rpcID = this.generateUniID();
        jsonObject.put("msg_id", this.generateUniID());
        jsonObject.put("rpc_id", rpcID);
        jsonObject.put("event", event);
        jsonObject.put("data", data);
        this.send(jsonObject.toString());
        String result = this.waitForRpcResponse(rpcID);
        if (null != result) {
            return new JSONObject(result);
        }
        return null;
    }


    @Override
    public void onOpen(ServerHandshake handshakedata) {

    }

    @Override
    public void onMessage(String message) {
        try {
            this.processMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {

    }

    private void processMessage(String message) throws Exception {
        if (message.equals("0xa")) {
            this.send("0xb");
            return;
        }

        JSONObject jsonObject = new JSONObject(message);

        if (jsonObject.has("msg_id")) {
            String msgID = jsonObject.getString("msg_id");
            if (this.ackCache.contains(msgID)) {
                JSONObject ack = new JSONObject();
                ack.put("ack", msgID);
                this.send(ack.toString());
                this.ackCache.remove(msgID);
            }
        }

        if (jsonObject.has("ack")) {
            String ack = jsonObject.getString("ack");
            JSONObject reply = new JSONObject();
            reply.put("ack", ack);
            this.send(reply.toString());
            return;
        }

        if (jsonObject.has("rpc_id")) {
            String rpcID = jsonObject.getString("rpc_id");
            Object data = jsonObject.get("data");
            this.receiveRpcResponse(rpcID, (JSONObject) data);
            return;
        }

        if (jsonObject.has("event")) {
            String event = jsonObject.getString("event");
            JSONObject data = (JSONObject) jsonObject.get("data");
            this.receiveEvent(event, data);
        }
    }

    private String waitForRpcResponse(String rpcID) {
        RPCWaiter waiter = new RPCWaiter(this.rpcTimeout);
        this.rpcWaiters.put(rpcID, waiter);
        waiter.acquire();
        String result = waiter.getResult();
        this.rpcWaiters.remove(rpcID);
        return result;
    }

    private void receiveRpcResponse(String rpcID, JSONObject data) {
        RPCWaiter waiter = this.rpcWaiters.get(rpcID);
        if (null != waiter) {
            waiter.release();
        }
    }

    private void receiveEvent(String event, JSONObject data) throws Exception {
        this.handler.receiveMessage(event, data);
    }

    private String generateUniID() {
        return UUID.randomUUID().toString();
    }
}
