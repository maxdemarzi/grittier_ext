package com.maxdemarzi.users;

import com.maxdemarzi.Exceptions;
import com.maxdemarzi.TestThing;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class UserExceptionsTest {

    @Test
    public void shouldHaveExceptions() {
        ArrayList<Exceptions> exceptions = new ArrayList<>();

        exceptions.add(UserExceptions.emptyUsernameParameter());
        exceptions.add(UserExceptions.emptyEmailParameter());
        exceptions.add(UserExceptions.emptyUsernameParameter());
        exceptions.add(UserExceptions.existingEmailParameter());
        exceptions.add(UserExceptions.existingUsernameParameter());
        exceptions.add(UserExceptions.invalidUsernameParameter());
        exceptions.add(UserExceptions.missingEmailParameter());
        exceptions.add(UserExceptions.missingUsernameParameter());
        exceptions.add(UserExceptions.userNotFound());


        for (Exceptions exception : exceptions) {
            TestThing testThing = new TestThing(exception);
            Assert.assertThrows(Exceptions.class, testThing::chuck);
        }
    }
}
