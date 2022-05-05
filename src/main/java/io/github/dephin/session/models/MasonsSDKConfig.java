package io.github.dephin.session.models;

public class MasonsSDKConfig {
    private String wsUrl;
    private String nodeUrl;
    private String agentToken;

    public MasonsSDKConfig() {
    }

    public MasonsSDKConfig(String wsUrl, String nodeUrl) {
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
