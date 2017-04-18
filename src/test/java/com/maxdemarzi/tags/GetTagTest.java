package com.maxdemarzi.tags;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

import static com.maxdemarzi.Properties.*;

public class GetTagTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Tags.class);

    @Test
    public void shouldGetTag() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/tags/neo4j").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetTagWithUser() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/tags/neo4j?username=maxdemarzi").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertEquals(expected2, actual);
    }

    @Test
    public void shouldGetTagLimited() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/tags/neo4j?limit=1").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertTrue(actual.size() == 1);
        Assert.assertEquals(expected.get(0), actual.get(0));
    }

    @Test
    public void shouldGetTagSince() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/tags/neo4j?since=1490208700").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertTrue(actual.size() == 1);
        Assert.assertEquals(expected.get(1), actual.get(0));
    }

    @Test
    public void shouldNotGetTagNotFound() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/tags/notneo4j").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Tag Not Found.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
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
                    "password: 'tunafish'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'})" +
                    "CREATE (laeg:User {username:'laexample', " +
                    "email: 'luke@neo4j.com', " +
                    "name: 'Luke Gannon'," +
                    "password: 'cuddlefish'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'})" +
                    "CREATE (post1:Post {status:'Hello World! #neo4j', " +
                    "time: 1490140299})" +
                    "CREATE (post2:Post {status:'How are you! #neo4j', " +
                    "time: 1490208700})" +
                    "CREATE (neo4j:Tag {name:'neo4j', time: 1490054400})" +
                    "CREATE (jexp)-[:POSTED_ON_2017_03_21 {time: 1490140299}]->(post1)" +
                    "CREATE (laeg)-[:POSTED_ON_2017_03_22 {time: 1490208700}]->(post2)" +
                    "CREATE (laeg)-[:REPOSTED_ON_2017_03_22 {time: 1490208800}]->(post1)" +
                    "CREATE (max)-[:LIKES {time: 1490209300 }]->(post1)" +
                    "CREATE (max)-[:LIKES {time: 1490209400 }]->(post2)" +
                    "CREATE (post1)-[:TAGGED_ON_2017_03_21 {time: 1490140299 }]->(neo4j)" +
                    "CREATE (post2)-[:TAGGED_ON_2017_03_21 {time: 1490208700 }]->(neo4j)" ;

    private static final ArrayList<HashMap<String, Object>> expected = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("username", "laexample");
            put("name", "Luke Gannon");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "How are you! #neo4j");
            put("time", 1490208700);
            put("likes", 1);
            put("reposts", 0);
        }});
        add(new HashMap<String, Object>() {{
            put("username", "jexp");
            put("name", "Michael Hunger");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Hello World! #neo4j");
            put("time", 1490140299);
            put("likes", 1);
            put("reposts", 1);
        }});
    }};

    private static final ArrayList<HashMap<String, Object>> expected2 = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("username", "laexample");
            put("name", "Luke Gannon");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "How are you! #neo4j");
            put("time", 1490208700);
            put("likes", 1);
            put("reposts", 0);
            put("liked", true);
            put("reposted", false);

        }});
        add(new HashMap<String, Object>() {{
            put("username", "jexp");
            put("name", "Michael Hunger");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Hello World! #neo4j");
            put("time", 1490140299);
            put("likes", 1);
            put("reposts", 1);
            put("liked", true);
            put("reposted", false);

        }});
    }};

}
