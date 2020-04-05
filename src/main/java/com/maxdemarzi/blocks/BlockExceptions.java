package com.maxdemarzi.blocks;

import com.maxdemarzi.Exceptions;

public class BlockExceptions extends Exceptions {

    public static final Exceptions alreadyBlockingUser() {
        return new Exceptions(400, "Already blocking User.");
    }
    public static final Exceptions notBlockingUser() {
        return new Exceptions(400, "Not blocking User.");
    }
    private BlockExceptions(int code, String error) {
        super(code, error);
    }
}
