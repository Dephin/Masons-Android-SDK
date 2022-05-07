package io.github.dephin;

public class MasonsSDKConfig {
    private String wsUrl = "wss://svc.masons.mrs.ai/api/v1/masons";
    private String nodeUrl = "https://svc.masons.mrs.ai/api/v1/masons/nodes";
    private String agentToken;

    public MasonsSDKConfig(String agentToken) {
        this.agentToken = agentToken;
    }

    public MasonsSDKConfig(String agentToken, String wsUrl, String nodeUrl) {
        this.agentToken = agentToken;
        this.wsUrl = wsUrl;
        this.nodeUrl = nodeUrl;
    }

    public String getWsUrl() {
        return this.wsUrl;
    }

    public String getNodeUrl() {
        return this.nodeUrl;
    }

    public String getAgentToken() {
        return this.agentToken;
    }

    public void setWsUrl(String wsUrl) {
        this.wsUrl = wsUrl;
    }

    public void setNodeUrl(String nodeUrl) {
        this.nodeUrl = nodeUrl;
    }

    public void setAgentToken(String agentToken) {
        this.agentToken = agentToken;
    }
}
