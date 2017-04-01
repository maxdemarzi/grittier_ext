package com.maxdemarzi.users;

import com.maxdemarzi.Exceptions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

public class UserValidatorTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldHaveExceptions() throws IOException {

        thrown.expect(Exceptions.class);
        UserValidator.validate(null);
    }
}
