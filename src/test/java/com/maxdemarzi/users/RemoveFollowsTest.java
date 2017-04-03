package com.maxdemarzi.users;

import com.sun.jersey.api.client.UniformInterfaceException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

public class RemoveFollowsTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withExtension("/v1", Users.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldRemoveFollows() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());
        thrown.expect(UniformInterfaceException.class);
        HTTP.request("DELETE", neo4j.httpURI().resolve("/v1/users/maxdemarzi/follows/jexp").toString(), null);
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
            "CREATE (max)-[:FOLLOWS {time:1490140299}]->(jexp)";
}