package com.maxdemarzi;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.maxdemarzi.Properties.*;
import static org.junit.Assert.assertEquals;

public class PropertiesTest {

    @Test
    public void shouldNotLetYouCallConstructor() throws NoSuchMethodException {
        Constructor<Properties> constructor;
        constructor = Properties.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Assert.assertThrows(InvocationTargetException.class, constructor::newInstance);
    }

    @Test
    public void shouldTestProperties() {
        assertEquals("email", EMAIL);
        assertEquals("name", NAME);
        assertEquals("password", PASSWORD);
        assertEquals("username", USERNAME);
        assertEquals("status", STATUS);
        assertEquals("time", TIME);
    }

}
