import io.github.dephin.AbstractMasonsSDK;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.*;


import io.github.dephin.connection.models.KnockResult;
import io.github.dephin.session.models.CreatingSessionOfCallee;
import io.github.dephin.session.models.ExitingSessionOfCaller;
import io.github.dephin.MasonsSDKConfig;
import io.github.dephin.session.models.ReplyFromCallee;
import io.github.dephin.session.models.UtteranceFromCaller;

public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void deserialize() {
        try {
            JSONObject obj = new JSONObject("{\"foo\":\"hi\"}");
            String foo = obj.getString("foo");
            System.out.println(foo);
            if (obj.has("foo")) {
                System.out.println("hi");
            }

            String bar = obj.getString("bar");
            System.out.println(bar);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void startApp() {
        try {
            MasonsSDKConfig config = new MasonsSDKConfig(
                    "b4c149da-c466-4cd3-9170-8b66a882aec9");
            config.setNodeUrl("http://localhost:8400/api/v1/masons/nodes");
            config.setWsUrl("ws://localhost:8400/api/v1/masons");
            AbstractMasonsSDK sdk = new AbstractMasonsSDK(config) {
                @Override
                public void onReceivingUtteranceFromCaller(UtteranceFromCaller utterance) {
                    System.out.print("onReceivingUtteranceFromCaller");
                }

                @Override
                public void onReceivingReplyFromCallee(ReplyFromCallee reply) {
                    System.out.print("onReceivingUtteranceFromCaller");
                }

                @Override
                public void onExitingSessionOfCaller(ExitingSessionOfCaller session) {
                    System.out.print("onReceivingUtteranceFromCaller");
                }

                @Override
                public void onCreatingSessionOfCallee(CreatingSessionOfCallee session) {
                    System.out.print("onReceivingUtteranceFromCaller");
                }
            };
            sdk.start();
            Thread.sleep(2000);
            Map<String, Object> data = new HashMap<>();
            KnockResult result = sdk.broadcastKnock("12345678", "hi", data);
            if (result != null) {
                System.out.print(result.getText());
            }

        } catch (URISyntaxException | JSONException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void simpleWebSocketConnection() {
        try {
            final String uri = "ws://localhost:8400/api/v1/masons";
            Map<String, String> httpHeaders = new HashMap<>();
            httpHeaders.put("Sec-WebSocket-Protocol", "Duplex");
            WebSocketClient ws = new WebSocketClient(new URI(uri), new Draft_6455(), httpHeaders, 1) {

                @Override
                public void onOpen(ServerHandshake handshake) {

                }

                @Override
                public void onMessage(String message) {

                }

                @Override
                public void onClose(int code, String reason, boolean remote) {

                }

                @Override
                public void onError(Exception ex) {

                }
            };
            ws.connect();
            ws.close();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void connectWithMDPClient() {

    }
}
