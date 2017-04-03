package com.maxdemarzi.posts;

import com.maxdemarzi.Exceptions;

public class PostExceptions extends Exceptions {

    public static final Exceptions missingStatusParameter = new Exceptions(400, "Missing status Parameter.");
    public static final Exceptions emptyStatusParameter = new Exceptions(400, "Empty status Parameter.");
    public static final Exceptions postNotFound = new Exceptions(400, "Post not Found.");

    private PostExceptions(int code, String error) {
        super(code, error);
    }
}
