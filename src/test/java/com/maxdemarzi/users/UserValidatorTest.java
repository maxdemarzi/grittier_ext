package com.maxdemarzi.users;

import com.maxdemarzi.Exceptions;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class UserValidatorTest {

    @Test
    public void shouldHaveExceptions() throws IOException {

        Assert.assertThrows(Exceptions.class, () -> UserValidator.validate(null));
    }
}
