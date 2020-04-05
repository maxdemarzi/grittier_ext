package com.maxdemarzi.users;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.rule.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

public class GetProfileTest {

    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withUnmanagedExtension("/v1", Users.class);

    @Test
    public void shouldGetProfile() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/profile").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetProfileSecondUser() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/users/maxdemarzi/profile?username2=jexp").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(expected2, actual);
    }

    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'max@neo4j.com', " +
                    "name: 'Max De Marzi'," +
                    "password: 'swordfish'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'})" +
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
            "CREATE (max)-[:FOLLOWS]->(jexp)" +
            "CREATE (max)-[:FOLLOWS]->(stefan)" +
            "CREATE (max)-[:FOLLOWS]->(mark)" +
            "CREATE (max)<-[:FOLLOWS]-(laeg)" +
            "CREATE (jexp)-[:FOLLOWS]->(stefan)" +
            "CREATE (jexp)-[:FOLLOWS]->(mark)" +
            "CREATE (post1:Post {status:'Hello World!', " +
                    "time: 1490140299})" +
            "CREATE (post2:Post {status:'How are you!', " +
                    "time: 1490208700})" +
            "CREATE (post3:Post {status:'Doing fine thanks!', " +
                    "time: 1490290191})" +
            "CREATE (jexp)-[:POSTED_ON_2017_03_21]->(post1)" +
            "CREATE (max)-[:POSTED_ON_2017_03_22]->(post2)" +
            "CREATE (max)-[:POSTED_ON_2017_03_23]->(post3)" +
            "CREATE (max)-[:LIKES]->(post1)" +
            "CREATE (laeg)-[:REPOSTED_ON_2017_03_22]->(post1)";

    private static final HashMap expected = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("name", "Max De Marzi");
        put("posts", 2);
        put("likes", 1);
        put("followers", 1);
        put("following", 3);
        put("hash", "0bd90aeb51d5982062f4f303a62df935");
    }};

    private static final HashMap expected2 = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("name", "Max De Marzi");
        put("posts", 2);
        put("likes", 1);
        put("followers", 1);
        put("following", 3);
        put("i_follow", false);
        put("follows_me", true);
        put("followers_you_know_count", 2);
        put("followers_you_know", new ArrayList<HashMap<String, Object>>(){{
            add(new HashMap<String, Object> () {{
                put("name", "Stefan Armbruster");
                put("username", "darthvader42");
            }});
            add(new HashMap<String, Object> () {{
                put("name", "Mark Needham");
                put("username", "markhneedham");
            }});

        }});
        put("hash", "0bd90aeb51d5982062f4f303a62df935");
    }};
}
