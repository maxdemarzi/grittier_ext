package com.maxdemarzi.posts;

import com.maxdemarzi.Exceptions;

public class PostExceptions extends Exceptions {

    public static final Exceptions missingStatusParameter() {
        return new Exceptions(400, "Missing status Parameter.");
    }
    public static final Exceptions emptyStatusParameter() {
        return new Exceptions(400, "Empty status Parameter.");
    }
    public static final Exceptions postNotFound() {
        return new Exceptions(400, "Post not Found.");
    }
    public static final Exceptions postAlreadyReposted() {
        return new Exceptions(400, "Post already Reposted.");
    }

    private PostExceptions(int code, String error) {
        super(code, error);
    }
}
