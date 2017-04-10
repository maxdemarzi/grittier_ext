package com.maxdemarzi.posts;

import com.maxdemarzi.Labels;
import com.maxdemarzi.RelationshipTypes;
import com.maxdemarzi.mentions.Mentions;
import com.maxdemarzi.tags.Tags;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static com.maxdemarzi.Properties.*;
import static com.maxdemarzi.Time.*;
import static com.maxdemarzi.likes.Likes.userLikesPost;
import static com.maxdemarzi.users.Users.getPost;
import static java.util.Collections.reverseOrder;

@Path("/users/{username}/posts")
public class Posts {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GET
    public Response getPosts(@PathParam("username") final String username,
                             @QueryParam("limit") @DefaultValue("25") final Integer limit,
                             @QueryParam("since") final Long since,
                             @QueryParam("username2") final String username2,
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
            Node user2 = null;
            if (username2 != null) {
                user2 = Users.findUser(username2, db);
            }

            Map userProperties = user.getAllProperties();
            LocalDateTime earliest = LocalDateTime.ofEpochSecond((Long)userProperties.get(TIME), 0, ZoneOffset.UTC);
            int count = 0;
            while (count < limit && (dateTime.isAfter(earliest))) {
                RelationshipType relType = RelationshipType.withName("POSTED_ON_" +
                        dateTime.format(dateFormatter));

                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, relType)) {
                    Node post = r1.getEndNode();
                    Map<String, Object> result = post.getAllProperties();
                    Long time = (Long)r1.getProperty("time");
                    if(time < latest) {
                        result.put(TIME, time);
                        result.put(USERNAME, username);
                        result.put(NAME, userProperties.get(NAME));
                        result.put(HASH, userProperties.get(HASH));
                        result.put(LIKES, post.getDegree(RelationshipTypes.LIKES));
                        result.put(REPOSTS, post.getDegree(Direction.INCOMING)
                                - 1 // for the Posted Relationship Type
                                - post.getDegree(RelationshipTypes.LIKES)
                                - post.getDegree(RelationshipTypes.REPLIED_TO));
                        if (user2 != null) {
                            result.put(LIKED, userLikesPost(user2, post));
                            result.put(REPOSTED, userRepostedPost(user2, post));
                        }
                        results.add(result);
                        count++;
                    }
                }
                dateTime = dateTime.minusDays(1);
            }
            tx.success();
        }

        results.sort(Comparator.comparing(m -> (Long) m.get(TIME), reverseOrder()));

        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @POST
    public Response createPost(String body, @PathParam("username") final String username,
                               @Context GraphDatabaseService db) throws IOException {
        Map<String, Object> results;
        HashMap<String, Object> input = PostValidator.validate(body);
        LocalDateTime dateTime = LocalDateTime.now(utc);

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);
            Node post = createPost(db, input, user, dateTime);
            results = post.getAllProperties();
            results.put(USERNAME, username);
            results.put(NAME, user.getProperty(NAME));
            results.put(HASH, user.getProperty(HASH));
            results.put(REPOSTS, 0);
            results.put(LIKES, 0);

            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    private Node createPost(@Context GraphDatabaseService db, HashMap input, Node user, LocalDateTime dateTime) {
        Node post = db.createNode(Labels.Post);
        post.setProperty(STATUS, input.get("status"));
        post.setProperty(TIME, dateTime.toEpochSecond(ZoneOffset.UTC));
        Relationship r1 = user.createRelationshipTo(post, RelationshipType.withName("POSTED_ON_" +
                        dateTime.format(dateFormatter)));
        r1.setProperty(TIME, dateTime.toEpochSecond(ZoneOffset.UTC));
        Tags.createTags(post, input, dateTime, db);
        Mentions.createMentions(post, input, dateTime, db);
        return post;
    }


    @PUT
    @Path("/{time}")
    public Response updatePost(String body,
                               @PathParam("username") final String username,
                               @PathParam("time") final Long time,
                               @Context GraphDatabaseService db) throws IOException {
        Map<String, Object> results;
        HashMap<String, Object> input = PostValidator.validate(body);

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);
            Node post = getPost(user, time);
            post.setProperty(STATUS, input.get(STATUS));
            LocalDateTime dateTime = LocalDateTime.ofEpochSecond((Long)post.getProperty(TIME), 0, ZoneOffset.UTC);
            Tags.createTags(post, input, dateTime, db);
            Mentions.createMentions(post, input, dateTime, db);
            results = post.getAllProperties();
            results.put(USERNAME, username);
            results.put(NAME, user.getProperty(NAME));
            results.put(LIKES, post.getDegree(RelationshipTypes.LIKES));
            results.put(REPOSTS, post.getDegree(Direction.INCOMING)
                    - 1 // for the Posted Relationship Type
                    - post.getDegree(RelationshipTypes.LIKES)
                    - post.getDegree(RelationshipTypes.REPLIED_TO));
            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @POST
    @Path("/{username2}/{time}/reply")
    public Response createReply(String body, @PathParam("username") final String username,
                                 @PathParam("username2") final String username2,
                                 @PathParam("time") final Long time,
                                 @Context GraphDatabaseService db) throws IOException {

        Map<String, Object> results;
        HashMap input = PostValidator.validate(body);
        LocalDateTime dateTime = LocalDateTime.now(utc);

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);
            Node post = createPost(db, input, user, dateTime);
            results = post.getAllProperties();
            results.put(TIME, dateTime.toEpochSecond(ZoneOffset.UTC));
            results.put(USERNAME, username);
            results.put(NAME, user.getProperty(NAME));
            results.put(REPOSTS, 0);
            results.put(LIKES, 0);

            Node user2 = Users.findUser(username2, db);
            Node post2 = getPost(user2, time);
            Relationship r2 = post.createRelationshipTo(post2, RelationshipTypes.REPLIED_TO);
            r2.setProperty(TIME, dateTime.toEpochSecond(ZoneOffset.UTC));

            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }


    @POST
    @Path("/{username2}/{time}")
    public Response createRepost(@PathParam("username") final String username,
                               @PathParam("username2") final String username2,
                               @PathParam("time") final Long time,
                               @Context GraphDatabaseService db) throws IOException {
        Map<String, Object> results;

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);
            Node user2 = Users.findUser(username2, db);
            Node post = getPost(user2, time);

            LocalDateTime dateTime = LocalDateTime.now(utc);
            if (userRepostedPost(user, post)) {
                throw PostExceptions.postAlreadyReposted;
            } else {
                Relationship r1 = user.createRelationshipTo(post, RelationshipType.withName("REPOSTED_ON_" +
                        dateTime.format(dateFormatter)));
                r1.setProperty(TIME, dateTime.toEpochSecond(ZoneOffset.UTC));
                results = post.getAllProperties();
                results.put(REPOSTED_TIME, dateTime.toEpochSecond(ZoneOffset.UTC));
                results.put(TIME, time);
                results.put(USERNAME, user2.getProperty(USERNAME));
                results.put(NAME, user2.getProperty(NAME));
                results.put(LIKES, post.getDegree(RelationshipTypes.LIKES));
                results.put(REPOSTS, post.getDegree(Direction.INCOMING)
                        - 1 // for the Posted Relationship Type
                        - post.getDegree(RelationshipTypes.LIKES)
                        - post.getDegree(RelationshipTypes.REPLIED_TO));
                results.put(LIKED, userLikesPost(user, post));
                results.put(REPOSTED, true);

            }
            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    public static Node getAuthor(Node post, Long time) {
        LocalDateTime postedDateTime = LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.UTC);
        RelationshipType original = RelationshipType.withName("POSTED_ON_" +
                postedDateTime.format(dateFormatter));
        return post.getSingleRelationship(original, Direction.INCOMING).getStartNode();
    }

    public static boolean userRepostedPost(Node user, Node post) {
        boolean alreadyReposted = false;

        if (post.getDegree(Direction.INCOMING) < 1000) {
            for (Relationship r1 : post.getRelationships(Direction.INCOMING)) {
                if (r1.getStartNode().equals(user) && r1.getType().name().startsWith("REPOSTED_ON_")) {
                    alreadyReposted = true;
                    break;
                }
            }
        }

        LocalDateTime now = LocalDateTime.now(utc);
        LocalDateTime dateTime = LocalDateTime.ofEpochSecond((Long)post.getProperty(TIME), 0, ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS);

        while (dateTime.isBefore(now) && !alreadyReposted) {
            RelationshipType repostedOn = RelationshipType.withName("REPOSTED_ON_" +
                    dateTime.format(dateFormatter));

            if (user.getDegree(repostedOn, Direction.OUTGOING)
                    < post.getDegree(repostedOn, Direction.INCOMING)) {
                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, repostedOn)) {
                    if (r1.getEndNode().equals(post)) {
                        alreadyReposted = true;
                        break;
                    }
                }
            } else {
                for (Relationship r1 : post.getRelationships(Direction.INCOMING, repostedOn)) {
                    if (r1.getStartNode().equals(user)) {
                        alreadyReposted = true;
                        break;
                    }
                }
            }
            dateTime = dateTime.plusDays(1);
        }
        return alreadyReposted;
    }
}
