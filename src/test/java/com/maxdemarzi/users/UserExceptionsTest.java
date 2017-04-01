package com.maxdemarzi.users;

import com.maxdemarzi.Exceptions;
import com.maxdemarzi.TestThing;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;

public class UserExceptionsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldHaveExceptions() {
        ArrayList<Exceptions> exceptions = new ArrayList<>();

        exceptions.add(UserExceptions.emptyUsernameParameter);
        exceptions.add(UserExceptions.emptyEmailParameter);
        exceptions.add(UserExceptions.emptyUsernameParameter);
        exceptions.add(UserExceptions.existingEmailParameter);
        exceptions.add(UserExceptions.existingUsernameParameter);
        exceptions.add(UserExceptions.invalidUsernameParameter);
        exceptions.add(UserExceptions.missingEmailParameter);
        exceptions.add(UserExceptions.missingUsernameParameter);
        exceptions.add(UserExceptions.userNotFound);


        for (Exceptions exception : exceptions) {
            TestThing testThing = new TestThing(exception);
            thrown.expect(Exceptions.class);
            testThing.chuck();
        }
    }
}
