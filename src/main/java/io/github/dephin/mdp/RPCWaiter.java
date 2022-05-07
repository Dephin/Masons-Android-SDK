package io.github.dephin.mdp;

import org.json.JSONObject;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class RPCWaiter {
    private JSONObject result = null;
    private Semaphore lock = new Semaphore(0);
    private long timeout;

    public RPCWaiter() {
    }


    public RPCWaiter(long timeout) {
        this.timeout = timeout;
    }

    public void acquire() {
        try {
            this.lock.tryAcquire(this.timeout, TimeUnit.MILLISECONDS);
//            this.lock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.release();
        }
    }


    public void release() {
        this.lock.release();
    }

    public JSONObject getResult() {
        return result;
    }

    public void setResult(JSONObject result) {
        this.result = result;
    }
}
