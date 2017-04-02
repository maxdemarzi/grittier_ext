package com.maxdemarzi.likes;

import com.maxdemarzi.RelationshipTypes;
import com.maxdemarzi.users.Users;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.*;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

import static com.maxdemarzi.Properties.*;
import static com.maxdemarzi.Time.utc;
import static com.maxdemarzi.posts.Posts.getAuthor;
import static com.maxdemarzi.users.Users.getPost;
import static java.util.Collections.reverseOrder;

@Path("/users/{username}/likes")
public class Likes {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GET
    public Response getLikes(@PathParam("username") final String username,
                             @QueryParam("limit") @DefaultValue("25") final Integer limit,
                             @QueryParam("since") final Long since,
                             @Context GraphDatabaseService db) throws IOException {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        LocalDateTime dateTime;
        if (since == null) {
            dateTime = LocalDateTime.now(utc);
        } else {
            dateTime = LocalDateTime.ofEpochSecond(since, 0, ZoneOffset.UTC);
        }
        Long latest = dateTime.toEpochSecond(ZoneOffset.UTC);

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.LIKES)) {
                Node post = r1.getEndNode();
                Map<String, Object> properties = post.getAllProperties();
                Long time = (Long)r1.getProperty("time");
                if(time < latest) {
                    Node author = getAuthor(post, (Long)properties.get(TIME));
                    properties.put(LIKEDTIME, time);
                    properties.put(USERNAME, author.getProperty(USERNAME));
                    properties.put(NAME, author.getProperty(NAME));
                    properties.put(HASH, author.getProperty(HASH));
                    properties.put(LIKES, post.getDegree(RelationshipTypes.LIKES));
                    properties.put(REPOSTS, post.getDegree() - 1 - post.getDegree(RelationshipTypes.LIKES));

                    results.add(properties);
                }
            }
            tx.success();
        }

        results.sort(Comparator.comparing(m -> (Long) m.get(LIKEDTIME), reverseOrder()));

        return Response.ok().entity(objectMapper.writeValueAsString(
                results.subList(0, Math.min(results.size(), limit))))
                .build();
    }

    @POST
    @Path("/{username2}/{time}")
    public Response createLike(@PathParam("username") final String username,
                               @PathParam("username2") final String username2,
                               @PathParam("time") final Long time,
                               @Context GraphDatabaseService db) throws IOException {
        Map<String, Object> results;

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);
            Node user2 = Users.findUser(username2, db);
            Node post = getPost(user2, time);
            Relationship like = user.createRelationshipTo(post, RelationshipTypes.LIKES);
            LocalDateTime dateTime = LocalDateTime.now(utc);
            like.setProperty(TIME, dateTime.toEpochSecond(ZoneOffset.UTC));
            results = post.getAllProperties();
            results.put(USERNAME, user2.getProperty(USERNAME));
            results.put(NAME, user2.getProperty(NAME));
            results.put(LIKES, post.getDegree(RelationshipTypes.LIKES));
            results.put(REPOSTS, post.getDegree() - 1 - post.getDegree(RelationshipTypes.LIKES));

            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }
}
