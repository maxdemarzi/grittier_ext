package com.maxdemarzi.users;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.HashMap;

public class GetUserTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Users.class);

    @Test
    public void shouldGetUser() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(expected, actual);
    }
    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'maxdemarzi@hotmail.com', " +
                    "name: 'Max De Marzi'," +
                    "password: 'swordfish'})";

    private static final HashMap expected = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "Max De Marzi");
        put("password", "swordfish");
    }};
}
