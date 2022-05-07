package io.github.dephin.mdp;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft_6455;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.NotYetConnectedException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;


public class MDPClient implements MDPProtocol {
    private boolean connected = false;
    private MDPHandler handler;
    private Map<String, RPCWaiter> rpcWaiters = new HashMap<String, RPCWaiter>();
//    private Map<String, JSONObject> expectedAckMessages = new HashMap<String, JSONObject>();
    private long rpcTimeout;
    private int connectTimeout;
    private URI uri;
    private Timer reconnectController = null;
    private ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<String>();
    private Map<String, String> httpHeaders;
    private WebSocketEndpoint ws = null;

    public MDPClient(String uriStr, MDPHandler handler,
                     Map<String, String> httpHeaders,
                     int connectTimeout, long rpcTimeout) {
        try {
            this.uri = new URI(uriStr);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        this.handler = handler;
        this.httpHeaders = httpHeaders;
        this.rpcTimeout = rpcTimeout;
        this.connectTimeout = connectTimeout;
    }

    @Override
    public void sendMessage(JSONObject msg) {
        String msgID = this.generateUniID();
        msg.put("msg_id", msgID);
//        this.expectedAckMessages.put(msgID, msg);
        this.send(msg.toString());
    }

    @Override
    public void sendEvent(String event, JSONObject data) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("event", event);
        jsonObject.put("data", data);
        this.sendMessage(data);
    }

    @Override
    public JSONObject callRPC(String event, JSONObject data) {
        JSONObject msg = new JSONObject();
        String rpcID = this.generateUniID();
        msg.put("rpc_id", rpcID);
        msg.put("event", event);
        msg.put("data", data);
        this.sendMessage(msg);
        return this.waitForRpcResponse(rpcID);
    }

    @Override
    public void sendError(String err) {
        JSONObject obj = new JSONObject();
        obj.put("error", err);
        this.send(obj.toString());
    }

    @Override
    public void connect() {
        if (null != this.ws) {
            if (this.webSocketIsOpen() || this.webSocketIsConnecting()) {
                return;
            }
            this.ws.close();
            this.ws = null;
        }
        this.ws = new WebSocketEndpoint(
                this.uri, new Draft_6455(),
                this.httpHeaders, this.connectTimeout, this
        );
        this.ws.connect();
    }

    @Override
    public void close() {
        this.reconnectController.cancel();
        this.messageQueue.clear();
        this.ws.close();
    }

    @Override
    public void onOpen() {
        this.connected = true;
        this.reconnectController.cancel();

        if (this.messageQueue.size() > 0) {
            // Drain any messages that came in while the channel was not open.
            Iterator<String> iter = this.messageQueue.iterator();

            for (; iter.hasNext(); ) {
                this.send(iter.next());
            }

            this.messageQueue.clear();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        this.reconnect();
    }

    @Override
    public void onError(Exception ex) {

    }

    @Override
    public void onMessage(String message) {
        if (message.equals("ping")) {
            this.send("pong");
            return;
        }
        if (message.equals("pong")) {
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(message);

            if (jsonObject.has("error")) {
                return;
            }

            if (jsonObject.has("ack")) {
                String msgID = jsonObject.getString("ack");
//                this.expectedAckMessages.remove(msgID);
                return;
            }

            if (!jsonObject.has("msg_id")) {
                this.sendError("'msg_id' is missing");
                return;
            }

            String msgID = jsonObject.getString("msg_id");
            this.replyAck(msgID);

            if (jsonObject.has("rpc_id")) {
                String rpcID = jsonObject.getString("rpc_id");
                if (jsonObject.has("echo")) {
                    Object data = jsonObject.get("echo");
                    this.processRpcResponse(rpcID, (JSONObject) data);
                } else {
                    String event = jsonObject.getString("event");
                    Object data = jsonObject.get("data");
                    this.processRpcRequest(rpcID, event, (JSONObject) data);
                }
                return;
            }

            if (jsonObject.has("event")) {
                String event = jsonObject.getString("event");
                JSONObject data = (JSONObject) jsonObject.get("data");
                this.processEventMessage(event, data);
            } else {
                this.sendError("'event' is missing");
            }
        } catch (JSONException e) {
            this.sendError("Not valid JSON format");
        }

    }

    private void replyAck(String msgID) {
        JSONObject ack = new JSONObject();
        ack.put("ack", msgID);
        this.send(ack.toString());
    }

    private void send(String msg) {
        try {
            if (null == this.ws) {
                throw new NotYetConnectedException();
            }
            this.ws.send(msg);
        } catch (NotYetConnectedException e) {
            this.reconnect();
            this.messageQueue.add(msg);
        }
    }

    private void reconnect() {
        if (null != this.ws) {
            this.ws.close();
            this.ws = null;
        }

        this.connected = false;

        if (null != this.reconnectController) {
            this.reconnectController.cancel();
        }

        this.reconnectController = new Timer();
        this.reconnectController.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        doReconnect();
                    }
                }, 0L, 2000L
        );
    }

    private synchronized void doReconnect() {
        if (this.connected) {
            this.reconnectController.cancel();
            return;
        }
        this.connect();
    }

    private boolean webSocketIsOpen() {
        return this.ws.getReadyState() == WebSocket.READYSTATE.OPEN;
    }

    private boolean webSocketIsConnecting() {
        return this.ws.getReadyState() == WebSocket.READYSTATE.CONNECTING;
    }

    private JSONObject waitForRpcResponse(String rpcID) {
        RPCWaiter waiter = new RPCWaiter(this.rpcTimeout);
        this.rpcWaiters.put(rpcID, waiter);
        waiter.acquire();
        JSONObject result = waiter.getResult();
        this.rpcWaiters.remove(rpcID);
        return result;
    }

    private void processRpcRequest(String rpcID, String event, JSONObject req) {
        JSONObject resp = this.handler.processRPCRequest(event, req);
        resp.put("rpc_id", rpcID);
        resp.put("event", event);
        resp.put("data", resp);
        this.sendMessage(resp);
    }

    private void processRpcResponse(String rpcID, JSONObject data) {
        RPCWaiter waiter = this.rpcWaiters.get(rpcID);
        if (null != waiter) {
            waiter.setResult(data);
            waiter.release();
        }
    }

    private void processEventMessage(String event, JSONObject data) {
        this.handler.processEventMessage(event, data);
    }

    private String generateUniID() {
        return UUID.randomUUID().toString();
    }

//    private void expectAck() {
//        // TODO: use thread pool
//        new Thread(() -> {
//            try {
//                Thread.sleep(1000);
//                if (expectedAckMessages.containsKey(msgID)) {
//                    JSONObject msg = expectedAckMessages.get(msgID);
//                    send(msg.toString());
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }).start();
//    }
}
