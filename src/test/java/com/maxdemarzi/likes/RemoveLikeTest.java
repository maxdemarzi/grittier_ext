package com.maxdemarzi.likes;

import com.maxdemarzi.Schema;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.rule.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.HashMap;

//import com.sun.jersey.api.client.UniformInterfaceException;

public class RemoveLikeTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(FIXTURE)
            .withUnmanagedExtension("/v1", Likes.class)
            .withUnmanagedExtension("/v1", Schema.class);

    @Test
    public void shouldRemoveLike() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());
        HTTP.request("DELETE", neo4j.httpURI().resolve("/v1/users/maxdemarzi/likes/jexp/1490140299").toString(), null);
    }

    @Test
    public void shouldRemoveLike2() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());
        HTTP.request("DELETE", neo4j.httpURI().resolve("/v1/users/maxdemarzi/likes/laexample/1490208700").toString(), null);
    }

    @Test
    public void shouldNotRemoveLikeUserNotFound() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.request("DELETE", neo4j.httpURI().resolve("/v1/users/max/likes/jexp/1490140299").toString(), null);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("User not Found.", actual.get("error"));
    }

    @Test
    public void shouldNotRemoveLikeAuthorNotFound() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.request("DELETE", neo4j.httpURI().resolve("/v1/users/maxdemarzi/likes/pxej/1490140299").toString(), null);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("User not Found.", actual.get("error"));
    }

    @Test
    public void shouldNotRemoveLikePostNotFound() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.request("DELETE", neo4j.httpURI().resolve("/v1/users/maxdemarzi/likes/jexp/1490140300").toString(), null);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Post not Found.", actual.get("error"));
    }

    @Test
    public void shouldNotRemoveLikeNotFound() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.request("DELETE", neo4j.httpURI().resolve("/v1/users/markhneedham/likes/jexp/1490140299").toString(), null);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Not liking Post.", actual.get("error"));
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
                    "name: 'Mark Needham'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "password: 'jellyfish'})" +
                    "CREATE (post1:Post {status:'Hello World!', " +
                    "time: 1490140299})" +
                    "CREATE (post2:Post {status:'How are you!', " +
                    "time: 1490208700})" +
                    "CREATE (jexp)-[:POSTED_ON_2017_03_21 {time: 1490140299}]->(post1)" +
                    "CREATE (laeg)-[:POSTED_ON_2017_03_22 {time: 1490208700}]->(post2)" +
                    "CREATE (laeg)-[:REPOSTED_ON_2017_03_22 {time: 1490208800}]->(post1)" +
                    "CREATE (max)-[:LIKES {time: 1490209300 }]->(post1)" +
                    "CREATE (max)-[:LIKES {time: 1490209400 }]->(post2)" +
                    "CREATE (jexp)-[:LIKES {time: 1490209400 }]->(post2)" ;
}
