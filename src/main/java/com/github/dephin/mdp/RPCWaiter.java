package com.github.dephin.mdp;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class RPCWaiter {
    private String result = null;
    private Semaphore lock = new Semaphore(1);
    private long timeout = 1L;

    public RPCWaiter() {
    }


    public RPCWaiter(long timeout) {
        this.timeout = timeout;
    }

    public void acquire() {
        try {
            this.lock.tryAcquire(this.timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.release();
        }
    }


    public void release() {
        this.lock.release();
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
