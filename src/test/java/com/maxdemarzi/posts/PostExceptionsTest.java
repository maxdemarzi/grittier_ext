package com.maxdemarzi.posts;

import com.maxdemarzi.Exceptions;
import com.maxdemarzi.TestThing;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class PostExceptionsTest {

    @Test
    public void shouldHaveExceptions() {
        ArrayList<Exceptions> exceptions = new ArrayList<>();

        exceptions.add(PostExceptions.missingStatusParameter());
        exceptions.add(PostExceptions.emptyStatusParameter());

        for (Exceptions exception : exceptions) {
            TestThing testThing = new TestThing(exception);
            Assert.assertThrows(Exceptions.class, testThing::chuck);
        }
    }
}
