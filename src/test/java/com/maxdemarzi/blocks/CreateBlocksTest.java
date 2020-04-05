package com.maxdemarzi.blocks;

import com.maxdemarzi.Schema;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.rule.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.HashMap;

import static com.maxdemarzi.Properties.NAME;
import static com.maxdemarzi.Properties.USERNAME;

public class CreateBlocksTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withUnmanagedExtension("/v1", Blocks.class)
            .withUnmanagedExtension("/v1", Schema.class);

    @Test
    public void shouldCreateBlocks() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users/maxdemarzi/blocks/jexp").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldCreateBlocksToo() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users/maxdemarzi/blocks/markhneedham").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(expected2, actual);
    }

    @Test
    public void shouldNotCreateBlocksAlreadyBlocking() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users/jexp/blocks/markhneedham").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Already blocking User.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(NAME));
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
            "CREATE (mark:User {username:'markhneedham', " +
                    "email: 'mark@neo4j.com', " +
                    "name: 'Mark Needham'," +
                    "password: 'jellyfish'})" +
            "CREATE (max)-[:FOLLOWS {time:1490140299}]->(jexp)" +
            "CREATE (max)-[:BLOCKS {time:1490140299}]->(laeg)" +
            "CREATE (laeg)-[:BLOCKS {time:1490140299}]->(mark)" +
            "CREATE (jexp)-[:BLOCKS {time:1490140299}]->(mark)" +
            "CREATE (laeg)-[:FOLLOWS {time:1490140299}]->(mark)" +
            "CREATE (jexp)-[:FOLLOWS {time:1490140299}]->(mark)";

    private static final HashMap<String, Object> expected = new HashMap<String, Object>() {{
        put("username", "jexp");
        put("name", "Michael Hunger");
    }};

    private static final HashMap<String, Object> expected2 = new HashMap<String, Object>() {{
        put("username", "markhneedham");
        put("name", "Mark Needham");
    }};
}
