package io.github.dephin;

public class MasonsSDKConfig {
    private String wsUrl = "wss://svc.masons.mrs.ai/api/v1/masons";
    private String nodeUrl = "https://svc.masons.mrs.ai/api/v1/masons/nodes";
    private String agentToken;
    private long rpcTimeout = 1L;
    private int connectTimeout = 1000;

    public MasonsSDKConfig(String agentToken) {
        this.agentToken = agentToken;
    }

    public MasonsSDKConfig(String agentToken, String wsUrl, String nodeUrl) {
        this.agentToken = agentToken;
        this.wsUrl = wsUrl;
        this.nodeUrl = nodeUrl;
    }

    public MasonsSDKConfig(String agentToken, String wsUrl, String nodeUrl,
                           long rpcTimeout, int connectTimeout) {
        this.agentToken = agentToken;
        this.wsUrl = wsUrl;
        this.nodeUrl = nodeUrl;
        this.rpcTimeout = rpcTimeout;
        this.connectTimeout = connectTimeout;
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

    public long getRpcTimeout() {
        return rpcTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
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

    public void setRpcTimeout(long rpcTimeout) {
        this.rpcTimeout = rpcTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
}
