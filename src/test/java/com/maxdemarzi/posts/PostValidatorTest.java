package com.maxdemarzi.posts;

import com.maxdemarzi.Exceptions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

public class PostValidatorTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldHaveExceptions() throws IOException {

        thrown.expect(Exceptions.class);
        PostValidator.validate(null);
    }
}
