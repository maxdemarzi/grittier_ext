package com.maxdemarzi.likes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxdemarzi.RelationshipTypes;
import com.maxdemarzi.users.Users;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.graphdb.*;

import javax.ws.rs.Path;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

import static com.maxdemarzi.Properties.*;
import static com.maxdemarzi.Time.getLatestTime;
import static com.maxdemarzi.Time.utc;
import static com.maxdemarzi.posts.Posts.getAuthor;
import static com.maxdemarzi.posts.Posts.userRepostedPost;
import static com.maxdemarzi.users.Users.getPost;
import static java.util.Collections.reverseOrder;

@Path("/users/{username}/likes")
public class Likes {

    private final GraphDatabaseService db;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public Likes(@Context DatabaseManagementService dbms ) {
        this.db = dbms.database( "neo4j" );;
    }

    @GET
    public Response getLikes(@PathParam("username") final String username,
                             @QueryParam("limit") @DefaultValue("25") final Integer limit,
                             @QueryParam("since") final Long since,
                             @QueryParam("username2") final String username2) throws IOException {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        Long latest = getLatestTime(since);

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, tx);
            Node user2 = null;
            if (username2 != null) {
                user2 = Users.findUser(username2, tx);
            }
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.LIKES)) {
                Node post = r1.getEndNode();
                Map<String, Object> properties = post.getAllProperties();
                Long time = (Long)r1.getProperty("time");
                if(time < latest) {
                    Node author = getAuthor(post, (Long)properties.get(TIME));
                    properties.put(LIKED_TIME, time);
                    properties.put(USERNAME, author.getProperty(USERNAME));
                    properties.put(NAME, author.getProperty(NAME));
                    properties.put(HASH, author.getProperty(HASH));
                    properties.put(LIKES, post.getDegree(RelationshipTypes.LIKES));
                    properties.put(REPOSTS, post.getDegree() - 1 - post.getDegree(RelationshipTypes.LIKES));
                    if (user2 != null) {
                        properties.put(LIKED, userLikesPost(user2, post));
                        properties.put(REPOSTED, userRepostedPost(user2, post));
                    }
                    results.add(properties);
                }
            }
            tx.commit();
        }

        results.sort(Comparator.comparing(m -> (Long) m.get(LIKED_TIME), reverseOrder()));

        return Response.ok().entity(objectMapper.writeValueAsString(
                results.subList(0, Math.min(results.size(), limit))))
                .build();
    }

    @POST
    @Path("/{username2}/{time}")
    public Response createLike(@PathParam("username") final String username,
                               @PathParam("username2") final String username2,
                               @PathParam("time") final Long time) throws IOException {
        Map<String, Object> results;

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, tx);
            Node user2 = Users.findUser(username2, tx);
            Node post = getPost(user2, time);

            if (userLikesPost(user, post)) {
                throw LikeExceptions.alreadyLikesPost();
            }

            Relationship like = user.createRelationshipTo(post, RelationshipTypes.LIKES);
            LocalDateTime dateTime = LocalDateTime.now(utc);
            like.setProperty(TIME, dateTime.toEpochSecond(ZoneOffset.UTC));
            results = post.getAllProperties();
            results.put(USERNAME, user2.getProperty(USERNAME));
            results.put(NAME, user2.getProperty(NAME));
            results.put(LIKES, post.getDegree(RelationshipTypes.LIKES));
            results.put(REPOSTS, post.getDegree(Direction.INCOMING)
                    - 1 // for the Posted Relationship Type
                    - post.getDegree(RelationshipTypes.LIKES)
                    - post.getDegree(RelationshipTypes.REPLIED_TO));
            results.put(LIKED, true);
            results.put(REPOSTED, userRepostedPost(user, post));
            tx.commit();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @DELETE
    @Path("/{username2}/{time}")
    public Response removeLike(@PathParam("username") final String username,
                               @PathParam("username2") final String username2,
                               @PathParam("time") final Long time) throws IOException {
        boolean liked = false;
        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, tx);
            Node user2 = Users.findUser(username2, tx);
            Node post = getPost(user2, time);

            if (user.getDegree(RelationshipTypes.LIKES, Direction.OUTGOING)
                    < post.getDegree(RelationshipTypes.LIKES, Direction.INCOMING) ) {
                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.LIKES)) {
                    if (r1.getEndNode().equals(post)) {
                        r1.delete();
                        liked = true;
                        break;
                    }
                }
            } else {
                for (Relationship r1 : post.getRelationships(Direction.INCOMING, RelationshipTypes.LIKES)) {
                    if (r1.getStartNode().equals(user)) {
                        r1.delete();
                        liked = true;
                        break;
                    }
                }
            }
            tx.commit();
        }

        if(!liked) {
            throw LikeExceptions.notLikingPost();
        }

        return Response.noContent().build();
    }

    public static boolean userLikesPost(Node user, Node post) {
        boolean alreadyLiked = false;
        if (user.getDegree(RelationshipTypes.LIKES, Direction.OUTGOING)
                < post.getDegree(RelationshipTypes.LIKES, Direction.INCOMING) ) {
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.LIKES)) {
                if (r1.getEndNode().equals(post)) {
                    alreadyLiked = true;
                    break;
                }
            }
        } else {
            for (Relationship r1 : post.getRelationships(Direction.INCOMING, RelationshipTypes.LIKES)) {
                if (r1.getStartNode().equals(user)) {
                    alreadyLiked = true;
                    break;
                }
            }
        }
        return alreadyLiked;
    }
}
