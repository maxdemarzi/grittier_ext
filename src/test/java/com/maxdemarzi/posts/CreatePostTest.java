package com.maxdemarzi.posts;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.HashMap;

public class CreatePostTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Posts.class);

    @Test
    public void shouldCreatePost() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users/maxdemarzi/posts").toString(), input);
        HashMap actual  = response.content();
        Assert.assertEquals(expected.get("status"), actual.get("status"));
        Assert.assertTrue(actual.containsKey("time"));
    }

    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'maxdemarzi@hotmail.com', " +
                    "name: 'Max De Marzi'," +
                    "password: 'swordfish'})";

    private static final HashMap input = new HashMap<String, Object>() {{
        put("status", "Hello World!");
    }};

    private static final HashMap expected = new HashMap<String, Object>() {{
        put("status", "Hello World!");
    }};
}
