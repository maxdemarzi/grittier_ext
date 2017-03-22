package com.maxdemarzi.posts;

import com.maxdemarzi.Exceptions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;

public class PostExceptionsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldHaveExceptions() {
        ArrayList<Exceptions> exceptions = new ArrayList<>();

        exceptions.add(PostExceptions.missingStatusParameter);
        exceptions.add(PostExceptions.emptyStatusParameter);

        for (Exceptions exception : exceptions) {
            PostExceptionsTest.TestThing testThing = new PostExceptionsTest.TestThing(exception);
            thrown.expect(Exceptions.class);
            testThing.chuck();
        }

    }

    private class TestThing {
        private Exceptions e;

        TestThing(Exceptions e) {
            this.e = e;
        }
        void chuck() {
            throw e;
        }
    }
}
