package com.maxdemarzi;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ExceptionsMapper implements ExceptionMapper<Exceptions> {
    @Override
    public Response toResponse(Exceptions exceptions) {
        return exceptions.getResponse();
    }
}
