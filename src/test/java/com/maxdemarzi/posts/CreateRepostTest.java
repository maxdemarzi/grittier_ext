package com.maxdemarzi.posts;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.HashMap;

import static com.maxdemarzi.Properties.STATUS;

public class CreateRepostTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Posts.class);

    @Test
    public void shouldCreateRepost() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users/maxdemarzi/posts/jexp/1490140299").toString());
        HashMap actual  = response.content();
        expected.put("reposted_time", actual.get("reposted_time"));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldNotCreateRepostTwice() throws InterruptedException {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());
        HTTP.POST(neo4j.httpURI().resolve("/v1/users/maxdemarzi/posts/jexp/1490140299").toString());
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users/maxdemarzi/posts/jexp/1490140299").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Post already Reposted.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(STATUS));
    }

    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'max@neo4j.com', " +
                    "name: 'Max De Marzi'," +
                    "password: 'swordfish'})" +
                    "CREATE (jexp:User {username:'jexp', " +
                    "email: 'michael@neo4j.com', " +
                    "name: 'Michael Hunger'," +
                    "password: 'tunafish'})" +
                    "CREATE (post1:Post {status:'Hello World!', " +
                    "time: 1490140299})" +
                    "CREATE (post2:Post {status:'How are you!', " +
                    "time: 1490208700})" +
                    "CREATE (jexp)-[:POSTED_ON_2017_03_21]->(post1)" +
                    "CREATE (jexp)-[:POSTED_ON_2017_03_22]->(post2)";

    private static final HashMap<String, Object> expected = new HashMap<String, Object>() {{
        put("username", "jexp");
        put("name", "Michael Hunger");
        put("status", "Hello World!");
        put("time", 1490140299);
        put("likes", 0);
        put("reposts", 1);
    }};
}
