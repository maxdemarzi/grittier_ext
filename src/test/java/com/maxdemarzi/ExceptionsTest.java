package com.maxdemarzi;

import org.junit.Assert;
import org.junit.Test;

public class ExceptionsTest {

    @Test
    public void shouldCreateExceptions() {
        TestThing testThing = new TestThing(Exceptions.invalidInput());
        Assert.assertThrows(Exceptions.class, testThing::chuck);
    }

}