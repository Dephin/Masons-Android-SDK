package io.github.dephin.mdp;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Map;

public class WebSocketEndpoint extends WebSocketClient {
    private MDPClient mdpClient;

    public WebSocketEndpoint(URI serverUri, MDPClient mdpClient) {
        super(serverUri);
        this.mdpClient = mdpClient;
    }

    public WebSocketEndpoint(URI serverUri, Draft protocolDraft, MDPClient mdpClient) {
        super(serverUri, protocolDraft);
        this.mdpClient = mdpClient;
    }

    public WebSocketEndpoint(URI serverUri, Draft protocolDraft,
                             Map<String, String> httpHeaders,
                             int connectTimeout, MDPClient mdpClient)
    {
        super(serverUri, protocolDraft, httpHeaders, connectTimeout);
        this.mdpClient = mdpClient;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {

    }

    @Override
    public void onMessage(String s) {

    }

    @Override
    public void onClose(int i, String s, boolean b) {

    }

    @Override
    public void onError(Exception e) {

    }
}
