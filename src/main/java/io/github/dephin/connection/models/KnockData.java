package io.github.dephin.connection.models;

import org.json.JSONObject;

import java.util.Map;

public class KnockData extends JSONObject {
    private String accountKey;
    private Map<String, Object> data;

    public String getAccountKey() {
        return accountKey;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setAccountKey(String accountKey) {
        this.accountKey = accountKey;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
