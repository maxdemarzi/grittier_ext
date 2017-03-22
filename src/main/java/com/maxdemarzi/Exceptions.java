package com.maxdemarzi;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class Exceptions extends WebApplicationException {

    public static final Exceptions invalidInput = new Exceptions(400, "Invalid Input");

    public Exceptions(int code, String error) {
        super(new Throwable(error), Response.status(code)
                .entity("{\"error\":\"" + error + "\"}")
                .type(MediaType.APPLICATION_JSON)
                .build());

    }
}