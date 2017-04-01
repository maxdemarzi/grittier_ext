package com.maxdemarzi.tags;

import com.maxdemarzi.posts.Posts;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

import static com.maxdemarzi.Properties.*;
import static java.lang.Thread.sleep;

public class CreateTagTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Posts.class)
            .withExtension("/v1", Tags.class);

    @Test
    public void shouldCreateTag() throws InterruptedException {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users/maxdemarzi/posts").toString(), input);
        HashMap actual  = response.content();
        Assert.assertEquals(expected.get(STATUS), actual.get(STATUS));
        Assert.assertTrue(actual.containsKey(TIME));
        sleep(1000); // Needed due to artifact in testing
        response = HTTP.GET(neo4j.httpURI().resolve("/v1/tags/neo4j").toString());
        ArrayList<HashMap> actual2  = response.content();
        expected2.get(0).put(TIME, actual2.get(0).get(TIME));
        Assert.assertEquals(expected2, actual2);

        Assert.assertEquals("maxdemarzi", actual2.get(0).get(USERNAME));
        Assert.assertEquals("Max De Marzi", actual2.get(0).get(NAME));
        Assert.assertEquals("Hello World! #neo4j", actual2.get(0).get(STATUS));
    }

    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'maxdemarzi@hotmail.com', " +
                    "name: 'Max De Marzi'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "password: 'swordfish'})";

    private static final HashMap input = new HashMap<String, Object>() {{
        put(STATUS, "Hello World! #neo4j");
    }};

    private static final HashMap expected = new HashMap<String, Object>() {{
        put(STATUS, "Hello World! #neo4j");
    }};

    private static final ArrayList<HashMap<String, Object>> expected2 = new ArrayList<HashMap<String, Object>>() {
        {
            add(new HashMap<String, Object>() {{
                put("username", "maxdemarzi");
                put("name", "Max De Marzi");
                put("hash", "0bd90aeb51d5982062f4f303a62df935");
                put("status", "Hello World! #neo4j");
                put("likes", 0);
                put("reposts", 0);
            }});
        }};
}
