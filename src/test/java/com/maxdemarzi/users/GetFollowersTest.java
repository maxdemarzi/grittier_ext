package com.maxdemarzi.users;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

public class GetFollowersTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Users.class);

    @Test
    public void shouldGetFollowers() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/followers").toString());
        ArrayList<HashMap> actual  = response.content();
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
            "CREATE (max)<-[:FOLLOWS]-(jexp)";

    private static final ArrayList<HashMap<String, Object>> expected = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("username", "jexp");
            put("name", "Michael Hunger");
            put("followers", 0);
            put("following", 1);
            put("posts", 0);
            put("likes", 0);
            }});
        }};
}

