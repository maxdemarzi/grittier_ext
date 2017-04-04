package com.maxdemarzi.posts;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.HashMap;

import static com.maxdemarzi.Properties.STATUS;

public class UpdatePostTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Posts.class);

    @Test
    public void shouldUpdatePost() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.request("PUT", neo4j.httpURI().resolve("/v1/users/maxdemarzi/posts/1490140299").toString(), input);
        HashMap actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    private static final HashMap input = new HashMap<String, Object>() {{
        put(STATUS, "Hello Again!");
    }};

    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'max@neo4j.com', " +
                    "name: 'Max De Marzi'," +
                    "password: 'swordfish'})" +
                    "CREATE (post:Post {status:'Hello World!', " +
                    "time: 1490140299})" +
                    "CREATE (max)-[:POSTED_ON_2017_03_21]->(post)";

    private static final HashMap<String, Object> expected = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("name", "Max De Marzi");
        put("status", "Hello Again!");
        put("time", 1490140299);
        put("likes", 0);
        put("reposts", 0);
    }};
}
