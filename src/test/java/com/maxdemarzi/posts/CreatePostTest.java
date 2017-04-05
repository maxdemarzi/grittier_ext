package com.maxdemarzi.posts;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.HashMap;

import static com.maxdemarzi.Properties.*;

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
        Assert.assertEquals(expected.get(STATUS), actual.get(STATUS));
        Assert.assertTrue(actual.containsKey(TIME));
        Assert.assertEquals("maxdemarzi", actual.get(USERNAME));
        Assert.assertEquals("Max De Marzi", actual.get(NAME));
    }

    @Test
    public void shouldNotCreatePostInvalid() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users/maxdemarzi/posts").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Invalid Input", actual.get("error"));
        Assert.assertFalse(actual.containsKey(STATUS));
        Assert.assertFalse(actual.containsKey(TIME));
    }

    @Test
    public void shouldNotCreatePostEmpty() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users/maxdemarzi/posts").toString(), emptyInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Empty status Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(STATUS));
        Assert.assertFalse(actual.containsKey(TIME));
    }

    @Test
    public void shouldNotCreatePostMissing() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users/maxdemarzi/posts").toString(), missingParameterInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Missing status Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(STATUS));
        Assert.assertFalse(actual.containsKey(TIME));
    }

    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'maxdemarzi@hotmail.com', " +
                    "name: 'Max De Marzi'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "password: 'swordfish'})";

    private static final HashMap input = new HashMap<String, Object>() {{
        put(STATUS, "Hello World!");
    }};

    private static final HashMap emptyInput = new HashMap<String, Object>() {{
        put(STATUS, "");
    }};

    private static final HashMap missingParameterInput = new HashMap<String, Object>() {{
        put("not_status", "Hello World!");
    }};

    private static final HashMap expected = new HashMap<String, Object>() {{
        put(STATUS, "Hello World!");
    }};
}
