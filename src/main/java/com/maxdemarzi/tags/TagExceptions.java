package com.maxdemarzi.tags;

import com.maxdemarzi.Exceptions;

public class TagExceptions extends Exceptions {

    public static final Exceptions tagNotFound = new Exceptions(400, "Tag Not Found.");

    private TagExceptions(int code, String error) {
        super(code, error);
    }
}