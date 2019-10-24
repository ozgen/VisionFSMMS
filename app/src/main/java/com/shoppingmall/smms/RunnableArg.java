package com.shoppingmall.smms;

public abstract class RunnableArg<T> implements Runnable {

    private T _arg;

    public RunnableArg() {
    }

    public void run(T arg) {
        setArgs(arg);
        run();
    }

    public void setArgs(T arg) {
        this._arg = arg;
    }

    public T getArg() {
        return this._arg;
    }
}