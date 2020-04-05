package com.maxdemarzi.posts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxdemarzi.Exceptions;

import java.util.HashMap;

import static com.maxdemarzi.Properties.STATUS;

public class PostValidator {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static HashMap validate(String body) {
        HashMap input;

        if ( body == null) {
            throw Exceptions.invalidInput();
        }

        // Parse the input
        try {
            input = objectMapper.readValue(body, HashMap.class);
        } catch (Exception e) {
            throw Exceptions.invalidInput();
        }

        if (!input.containsKey(STATUS)) {
            throw PostExceptions.missingStatusParameter();
        } else {
            String status = (String)input.get(STATUS);
            if (status.equals("")) {
                throw PostExceptions.emptyStatusParameter();
            }
        }

        return input;
    }
}
