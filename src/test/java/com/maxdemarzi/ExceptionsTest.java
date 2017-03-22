package com.maxdemarzi;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ExceptionsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Test
    public void shouldCreateExceptions() {
        TestThing testThing = new TestThing(Exceptions.invalidInput);
        thrown.expect(Exceptions.class);
        testThing.chuck();

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
