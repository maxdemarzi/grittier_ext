package com.maxdemarzi.posts;

import com.maxdemarzi.Exceptions;
import org.junit.Assert;
import org.junit.Test;

public class PostValidatorTest {
    @Test
    public void shouldHaveExceptions() {

        Assert.assertThrows(Exceptions.class, () ->PostValidator.validate(null));
    }
}
