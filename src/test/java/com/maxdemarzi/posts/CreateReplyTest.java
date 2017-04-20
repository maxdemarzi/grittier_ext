package com.maxdemarzi.posts;

import com.maxdemarzi.Schema;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.HashMap;

import static com.maxdemarzi.Properties.STATUS;

public class CreateReplyTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Posts.class)
            .withExtension("/v1", Schema.class);

    @Test
    public void shouldCreateReply() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users/maxdemarzi/posts/jexp/1490140299/reply").toString(), input);
        HashMap actual  = response.content();
        expected.put("time", actual.get("time"));
        Assert.assertEquals(expected, actual);
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
                    "CREATE (jexp)-[:POSTED_ON_2017_03_21]->(post1)" ;

    private static final HashMap input = new HashMap<String, Object>() {{
        put(STATUS, "How are you!");
    }};

    private static final HashMap<String, Object> expected = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("name", "Max De Marzi");
        put("status", "How are you!");
        put("likes", 0);
        put("reposts", 0);
    }};
}
