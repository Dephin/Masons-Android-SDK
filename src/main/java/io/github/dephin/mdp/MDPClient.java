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
    private Set<String> ackCache = new HashSet<String>();
    private long rpcTimeout = 1L;
    private int connectTimeout = 100;
    private URI uri;
    private Timer reconnectController = new Timer();
    private ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<String>();
    private Map<String, String> httpHeaders;
    private WebSocketEndpoint ws = null;

    public MDPClient(String uriStr, MDPHandler handler, Map<String, String> httpHeaders) {
        try {
            this.uri = new URI(uriStr);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        this.handler = handler;
        this.httpHeaders = httpHeaders;
    }

    public MDPClient(String uriStr, MDPHandler handler, Map<String, String> httpHeaders,
                     int connectTimeout, long rpcTimeout) {
        this(uriStr, handler, httpHeaders);
        this.rpcTimeout = rpcTimeout;
        this.connectTimeout = connectTimeout;
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

    public void close() {
        this.reconnectController.cancel();
        this.messageQueue.clear();
        this.ws.close();
    }

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


    public void onClose(int code, String reason, boolean remote) {
        this.reconnect();
    }

    public void onError(Exception ex) {

    }

    public void onMessage(String message) {
        try {

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
        } catch (Exception e) {
            e.printStackTrace();
        }
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
