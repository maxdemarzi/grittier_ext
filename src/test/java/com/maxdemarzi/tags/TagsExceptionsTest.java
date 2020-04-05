package com.maxdemarzi.tags;

import com.maxdemarzi.Exceptions;
import com.maxdemarzi.TestThing;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class TagsExceptionsTest {

    @Test
    public void shouldHaveExceptions() {
        ArrayList<Exceptions> exceptions = new ArrayList<>();

        exceptions.add(TagExceptions.tagNotFound());

        for (Exceptions exception : exceptions) {
            TestThing testThing = new TestThing(exception);
            Assert.assertThrows(Exceptions.class, testThing::chuck);
        }
    }
}
