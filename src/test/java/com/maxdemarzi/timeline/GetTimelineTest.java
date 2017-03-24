package com.maxdemarzi.timeline;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

public class GetTimelineTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Timeline.class);

    @Test
    public void shouldGetTimeline() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/timeline").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetTimelineLimited() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/timeline?limit=1").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertTrue(actual.size() == 1);
        Assert.assertEquals(expected.get(0), actual.get(0));
    }

    @Test
    public void shouldGetTimelineSince() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/timeline?since=1490140300").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertTrue(actual.size() == 1);
        Assert.assertEquals(unReposted, actual.get(0));
    }

    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'max@neo4j.com', " +
                    "hash: 'hash', " +
                    "name: 'Max De Marzi'," +
                    "password: 'swordfish'})" +
            "CREATE (jexp:User {username:'jexp', " +
                    "email: 'michael@neo4j.com', " +
                    "hash: 'hash', " +
                    "name: 'Michael Hunger'," +
                    "password: 'tunafish'})" +
            "CREATE (laeg:User {username:'laexample', " +
                    "email: 'luke@neo4j.com', " +
                    "hash: 'hash', " +
                    "name: 'Luke Gannon'," +
                    "password: 'cuddlefish'})" +

            "CREATE (max)-[:FOLLOWS]->(jexp)" +
            "CREATE (max)-[:FOLLOWS]->(laeg)" +

            "CREATE (post1:Post {status:'Hello World!', " +
                    "time: 1490140299})" +
            "CREATE (post2:Post {status:'How are you!', " +
                    "time: 1490208700})" +
            "CREATE (post3:Post {status:'Doing fine!', " +
                    "time: 1490208800})" +
            "CREATE (jexp)-[:POSTED_ON_2017_03_21]->(post1)" +
            "CREATE (laeg)-[:POSTED_ON_2017_03_22]->(post2)" +
            "CREATE (max)-[:POSTED_ON_2017_03_22]->(post3)" +
            "CREATE (laeg)-[:LIKES]->(post1)" +
            "CREATE (laeg)-[:REPOSTED_ON_2017_03_22]->(post1)";

    private static final ArrayList<HashMap<String, Object>> expected = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("username", "maxdemarzi");
            put("name", "Max De Marzi");
            put("hash", "hash");
            put("status", "Doing fine!");
            put("time", 1490208800);
            put("likes", 0);
            put("reposts", 0);
        }});
        add(new HashMap<String, Object>() {{
            put("username", "laexample");
            put("name", "Luke Gannon");
            put("hash", "hash");
            put("status", "How are you!");
            put("time", 1490208700);
            put("likes", 0);
            put("reposts", 0);
        }});
        add(new HashMap<String, Object>() {{
            put("reposter_username", "laexample");
            put("reposter_name", "Luke Gannon");
            put("hash", "hash");
            put("username", "jexp");
            put("name", "Michael Hunger");
            put("status", "Hello World!");
            put("time", 1490140299);
            put("likes", 1);
            put("reposts", 1);
        }});
    }};

    private static final HashMap<String, Object> unReposted = new HashMap<String, Object>() {{
            put("username", "jexp");
            put("name", "Michael Hunger");
            put("hash", "hash");
            put("status", "Hello World!");
            put("time", 1490140299);
            put("likes", 1);
            put("reposts", 1);
        }};

}
