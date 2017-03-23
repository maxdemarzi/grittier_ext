package com.maxdemarzi.likes;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

public class GetLikesTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Likes.class);

    @Test
    public void shouldGetLikes() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/likes").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetLikesLimited() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/likes?limit=1").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertTrue(actual.size() == 1);
        Assert.assertEquals(expected.get(0), actual.get(0));
    }

    @Test
    public void shouldGetLikesSince() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/likes?since=1490140300").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertTrue(actual.size() == 1);
        Assert.assertEquals(expected.get(1), actual.get(0));
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
            "CREATE (laeg:User {username:'laexample', " +
                    "email: 'luke@neo4j.com', " +
                    "name: 'Luke Gannon'," +
                    "password: 'cuddlefish'})" +
            "CREATE (post1:Post {status:'Hello World!', " +
                    "time: 1490140299})" +
            "CREATE (post2:Post {status:'How are you!', " +
                    "time: 1490208700})" +
            "CREATE (jexp)-[:POSTED_ON_2017_03_21]->(post1)" +
            "CREATE (laeg)-[:POSTED_ON_2017_03_22]->(post2)" +
            "CREATE (laeg)-[:REPOSTED_ON_2017_03_22]->(post1)" +
            "CREATE (max)-[:LIKES]->(post1)" +
            "CREATE (max)-[:LIKES]->(post2)" ;

    private static final ArrayList<HashMap<String, Object>> expected = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("username", "laexample");
            put("name", "Luke Gannon");
            put("status", "How are you!");
            put("time", 1490208700);
            put("likes", 1);
            put("reposts", 0);
        }});
        add(new HashMap<String, Object>() {{
            put("username", "jexp");
            put("name", "Michael Hunger");
            put("status", "Hello World!");
            put("time", 1490140299);
            put("likes", 1);
            put("reposts", 1);
        }});
    }};
}
