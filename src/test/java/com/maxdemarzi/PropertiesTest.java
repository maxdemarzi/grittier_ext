package com.maxdemarzi;

import org.junit.Test;

import static com.maxdemarzi.Properties.*;
import static org.junit.Assert.assertEquals;

public class PropertiesTest {

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
