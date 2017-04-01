package com.maxdemarzi;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;

public class SchemaTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withExtension("/v1", Schema.class);

    @Test
    public void shouldCreateSchema() {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());
        ArrayList<String> actual = response.content();
        Assert.assertEquals(expected, actual);
    }

    private static final ArrayList expected = new ArrayList<String>() {{
        add("(:User {username}) constraint created");
        add("(:Tag {name}) constraint created");
        add("Schema Created");
    }};
}