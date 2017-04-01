package com.maxdemarzi.posts;

import com.maxdemarzi.Exceptions;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;

import static com.maxdemarzi.Properties.STATUS;

public class PostValidator {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static HashMap<String, Object> validate(String body) throws IOException {
        HashMap<String, Object> input;

        if ( body == null) {
            throw Exceptions.invalidInput;
        }

        // Parse the input
        try {
            input = objectMapper.readValue(body, HashMap.class);
        } catch (Exception e) {
            throw Exceptions.invalidInput;
        }

        if (!input.containsKey(STATUS)) {
            throw PostExceptions.missingStatusParameter;
        } else {
            String status = (String)input.get(STATUS);
            if (status.equals("")) {
                throw PostExceptions.emptyStatusParameter;
            }
        }

        return input;
    }
}
