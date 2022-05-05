package io.github.dephin.session;

import io.github.dephin.connection.MasonsConnection;

public abstract class Session {
    protected String sessionID;
    protected String accountKey;
    protected MasonsConnection connection;

    public String getSessionID() {
        return this.sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public String getAccountKey() {
        return accountKey;
    }


    public void setAccountKey(String accountKey) {
        this.accountKey = accountKey;
    }

    public void setConnection(MasonsConnection connection) {
        this.connection = connection;
    }

    public MasonsConnection getConnection() {
        return connection;
    }
}
