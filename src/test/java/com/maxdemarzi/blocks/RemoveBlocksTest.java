package com.maxdemarzi.blocks;

import com.sun.jersey.api.client.UniformInterfaceException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.HashMap;

public class RemoveBlocksTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Blocks.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldRemoveBlock() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());
        thrown.expect(UniformInterfaceException.class);
        HTTP.request("DELETE", neo4j.httpURI().resolve("/v1/users/maxdemarzi/blocks/jexp").toString(), null);
    }

    @Test
    public void shouldRemoveBlockToo() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());
        thrown.expect(UniformInterfaceException.class);
        HTTP.request("DELETE", neo4j.httpURI().resolve("/v1/users/jexp/blocks/laexample").toString(), null);
    }

    @Test
    public void shouldNotRemoveBlockUserNotFound() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.request("DELETE", neo4j.httpURI().resolve("/v1/users/max/blocks/jexp").toString(), null);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("User not Found.", actual.get("error"));
    }

    @Test
    public void shouldNotRemoveBlockUser2NotFound() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.request("DELETE", neo4j.httpURI().resolve("/v1/users/maxdemarzi/blocks/pxej").toString(), null);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("User not Found.", actual.get("error"));
    }

    @Test
    public void shouldNotRemoveLikeBlockNotFound() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.request("DELETE", neo4j.httpURI().resolve("/v1/users/maxdemarzi/blocks/laexample").toString(), null);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Not blocking User.", actual.get("error"));
    }

    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'max@neo4j.com', " +
                    "name: 'Max De Marzi'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "password: 'swordfish'})" +
            "CREATE (jexp:User {username:'jexp', " +
                    "email: 'michael@neo4j.com', " +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "name: 'Michael Hunger'," +
                    "password: 'tunafish'})" +
            "CREATE (laeg:User {username:'laexample', " +
                    "email: 'luke@neo4j.com', " +
                    "name: 'Luke Gannon'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "password: 'cuddlefish'})" +
            "CREATE (mark:User {username:'markhneedham', " +
                    "email: 'mark@neo4j.com', " +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "name: 'Mark Needham'," +
                    "password: 'jellyfish'})" +
            "CREATE (max)-[:BLOCKS {time:1490140299}]->(jexp)" +
            "CREATE (mark)-[:BLOCKS {time:1490140299}]->(laeg)" +
            "CREATE (jexp)-[:BLOCKS {time:1490140299}]->(laeg)";
}
