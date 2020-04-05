package com.maxdemarzi.recommendations;

import com.maxdemarzi.Schema;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.rule.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

public class RecommendFollowsTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withUnmanagedExtension("/v1", Recommendations.class)
            .withUnmanagedExtension("/v1", Schema.class);

    @Test
    public void shouldRecommendFollows() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/recommendations/follows").toString());
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
            "CREATE (laeg:User {username:'laexample', " +
                    "email: 'luke@neo4j.com', " +
                    "name: 'Luke Gannon'," +
                    "password: 'cuddlefish'})" +
            "CREATE (stefan:User {username:'darthvader42', " +
                    "email: 'stefan@neo4j.com', " +
                    "name: 'Stefan Armbruster'," +
                    "password: 'catfish'})" +
            "CREATE (mark:User {username:'markhneedham', " +
                    "email: 'mark@neo4j.com', " +
                    "name: 'Mark Needham'," +
                    "password: 'jellyfish'})" +

            "CREATE (max)-[:FOLLOWS]->(laeg)" +
            "CREATE (max)-[:FOLLOWS]->(jexp)" +
            "CREATE (laeg)-[:FOLLOWS]->(stefan)" +
            "CREATE (laeg)-[:FOLLOWS]->(mark)" +
            "CREATE (jexp)-[:FOLLOWS]->(mark)";

    private static final ArrayList<HashMap<String, Object>> expected = new ArrayList<HashMap<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("username", "markhneedham");
            put("name", "Mark Needham");
            put("i_follow", false);
        }});
        add(new HashMap<String, Object>() {{
            put("username", "darthvader42");
            put("name", "Stefan Armbruster");
            put("i_follow", false);
        }});

    }};
}
