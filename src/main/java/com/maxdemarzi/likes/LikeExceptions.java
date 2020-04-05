package com.maxdemarzi.likes;

import com.maxdemarzi.Exceptions;

public class LikeExceptions extends Exceptions {

    public static final Exceptions alreadyLikesPost() {
        return new Exceptions(400, "Already likes Post.");
    }
    public static final Exceptions notLikingPost() {
        return new Exceptions(400, "Not liking Post.");
    }

    private LikeExceptions(int code, String error) {
        super(code, error);
    }
}