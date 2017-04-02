package com.maxdemarzi.mentions;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

public class GetMentionsTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Mentions.class);

    @Test
    public void shouldGetMentions() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/jexp/mentions").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetMentionsLimited() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/jexp/mentions?limit=1").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertTrue(actual.size() == 1);
        Assert.assertEquals(expected.get(0), actual.get(0));
    }

    @Test
    public void shouldGetMentionsSince() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/jexp/mentions?since=1490208600").toString());
        ArrayList<HashMap> actual  = response.content();
        Assert.assertTrue(actual.size() == 1);
        Assert.assertEquals(expected.get(1), actual.get(0));
    }

    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'max@neo4j.com', " +
                    "name: 'Max De Marzi'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "password: 'swordfish'})" +
                    "CREATE (jexp:User {username:'jexp', " +
                    "email: 'michael@neo4j.com', " +
                    "name: 'Michael Hunger'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "password: 'tunafish'})" +
                    "CREATE (laeg:User {username:'laexample', " +
                    "email: 'luke@neo4j.com', " +
                    "name: 'Luke Gannon'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "password: 'cuddlefish'})" +
                    "CREATE (post1:Post {status:'Hello @jexp', " +
                    "time: 1490140299})" +
                    "CREATE (post2:Post {status:'Hi @jexp', " +
                    "time: 1490208700})" +
                    "CREATE (max)-[:POSTED_ON_2017_03_21 {time: 1490140299}]->(post1)" +
                    "CREATE (laeg)-[:POSTED_ON_2017_03_22 {time: 1490208700}]->(post2)" +
                    "CREATE(post1)-[:MENTIONED_ON_2017_03_21 {time: 1490140299}]->(jexp)" +
                    "CREATE(post2)-[:MENTIONED_ON_2017_03_22 {time: 1490208700}]->(jexp)" +
                    "CREATE (laeg)-[:REPOSTED_ON_2017_03_22 {time: 1490208800}]->(post1)" +
                    "CREATE (max)-[:LIKES {time: 1490208800 }]->(post2)";

    private static final ArrayList<HashMap<String, Object>> expected = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("username", "laexample");
            put("name", "Luke Gannon");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Hi @jexp");
            put("time", 1490208700);
            put("likes", 1);
            put("reposts", 0);
        }});
        add(new HashMap<String, Object>() {{
            put("username", "maxdemarzi");
            put("name", "Max De Marzi");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Hello @jexp");
            put("time", 1490140299);
            put("likes", 0);
            put("reposts", 1);
        }});
    }};
}
