package com.maxdemarzi.users;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.HashMap;

public class CreateUserTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withExtension("/v1", Users.class);

    @Test
    public void shouldCreateUser() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), input);
        HashMap actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    private static final HashMap input = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "Max De Marzi");
        put("password", "swordfish");
    }};

    private static final HashMap expected = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "Max De Marzi");
        put("password", "swordfish");
        put("hash","58750f2179edbd650b471280aa66fee5");
    }};
}