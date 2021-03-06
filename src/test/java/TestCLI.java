import io.github.dephin.AbstractMasonsSDK;
import io.github.dephin.MasonsSDKConfig;
import io.github.dephin.connection.models.KnockResult;
import io.github.dephin.connection.models.UtteranceResponse;
import io.github.dephin.session.CallerSession;
import io.github.dephin.session.models.CreatingSessionOfCallee;
import io.github.dephin.session.models.ExitingSessionOfCaller;
import io.github.dephin.session.models.ReplyFromCallee;
import io.github.dephin.session.models.UtteranceFromCaller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class TestCLI {
    private static final String accountKey = "12345678";

    public static void main(String[] args) {
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
                System.out.print("onReceivingUtteranceFromCaller" + reply.getText());
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

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while (true) {
                System.out.print("In: ");
                line = reader.readLine();
                if (line.equals("quit")) {
                    sdk.stop();
                    break;
                } else if (line.equals("knock")) {
                    Map<String, Object> data = new HashMap<>();
                    KnockResult result = sdk.broadcastKnock(TestCLI.accountKey, "hi", data);
                    if (result.getSuccess()) {
                        if (null != result.getText()) {
                            System.out.println("Out: Knock successfully, text(" + result.getText() + ")");
                        } else {
                            System.out.println("Out: Knock successfully, no text");
                        }
                    } else {
                        System.out.println("Out: Knock failed");
                    }
                } else {
                    CallerSession session = sdk.getCallerSessionByAccountKey(TestCLI.accountKey);
                    UtteranceResponse reply = session.utter(line);
                    System.out.println("Out: " + reply.getText());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
