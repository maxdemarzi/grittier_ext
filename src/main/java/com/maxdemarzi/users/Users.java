package com.maxdemarzi.users;

import com.maxdemarzi.Labels;
import com.maxdemarzi.RelationshipTypes;
import com.maxdemarzi.posts.PostExceptions;
import org.apache.shiro.crypto.hash.Md5Hash;
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
import java.util.*;

import static com.maxdemarzi.Properties.*;
import static com.maxdemarzi.Time.dateFormatter;
import static com.maxdemarzi.Time.utc;
import static java.util.Collections.reverseOrder;

@Path("/users")
public class Users {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GET
    @Path("/{username}")
    public Response getUser(@PathParam("username") final String username, @Context GraphDatabaseService db) throws IOException {
        Map<String, Object> results;
        try (Transaction tx = db.beginTx()) {
            Node user = findUser(username, db);
            results = user.getAllProperties();
            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @GET
    @Path("/{username}/profile")
    public Response getProfile(@PathParam("username") final String username,
                               @QueryParam("username2") final String username2,
                               @Context GraphDatabaseService db) throws IOException {
        Map<String, Object> results;
        try (Transaction tx = db.beginTx()) {
            Node user = findUser(username, db);
            results = getUserAttributes(user);

            if (username2 != null && !username.equals(username2)) {
                Node user2 = findUser(username2, db);
                HashSet<Node> followed = new HashSet<>();
                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.FOLLOWS)) {
                    followed.add(r1.getEndNode());
                }
                HashSet<Node> followed2 = new HashSet<>();
                for (Relationship r1 : user2.getRelationships(Direction.OUTGOING, RelationshipTypes.FOLLOWS)) {
                    followed2.add(r1.getEndNode());
                }

                boolean follows_me = followed.contains(user2);
                boolean i_follow = followed2.contains(user);
                results.put(I_FOLLOW, i_follow);
                results.put(FOLLOWS_ME, follows_me);

                followed.retainAll(followed2);

                results.put(FOLLOWERS_YOU_KNOW_COUNT, followed.size());
                ArrayList<Map<String, Object>> followers_sample = new ArrayList<>();
                int count = 0;
                for (Node follower : followed) {
                    count++;
                    Map<String, Object> properties = follower.getAllProperties();
                    properties.remove(PASSWORD);
                    properties.remove(EMAIL);
                    followers_sample.add(properties);
                    if (count > 10) { break; };
                }

                results.put(FOLLOWERS_YOU_KNOW, followers_sample);

            }

            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @POST
    public Response createUser(String body, @Context GraphDatabaseService db) throws IOException {
        HashMap parameters = UserValidator.validate(body);
        Map<String, Object> results;
        try (Transaction tx = db.beginTx()) {
            Node user = db.findNode(Labels.User, USERNAME, parameters.get(USERNAME));
            if (user == null) {
                user = db.findNode(Labels.User, EMAIL, parameters.get(EMAIL));
                if (user == null) {
                    user = db.createNode(Labels.User);
                    user.setProperty(EMAIL, parameters.get(EMAIL));
                    user.setProperty(NAME, parameters.get(NAME));
                    user.setProperty(USERNAME, parameters.get(USERNAME));
                    user.setProperty(PASSWORD, parameters.get(PASSWORD));
                    user.setProperty(HASH, new Md5Hash(((String)parameters.get(EMAIL)).toLowerCase()).toString());

                    LocalDateTime dateTime = LocalDateTime.now(utc);
                    user.setProperty(TIME, dateTime.truncatedTo(ChronoUnit.DAYS).toEpochSecond(ZoneOffset.UTC));

                    results = user.getAllProperties();
                } else {
                    throw UserExceptions.existingEmailParameter;
                }
            } else {
                throw UserExceptions.existingUsernameParameter;
            }
            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @GET
    @Path("/{username}/followers")
    public Response getFollowers(@PathParam("username") final String username,
                                 @QueryParam("limit") @DefaultValue("25") final Integer limit,
                                 @QueryParam("since") final Long since,
                                 @Context GraphDatabaseService db) throws IOException {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        // TODO: 4/3/17 Add Recent Array for Users with > 100k Followers
        LocalDateTime dateTime;
        if (since == null) {
            dateTime = LocalDateTime.now(utc);
        } else {
            dateTime = LocalDateTime.ofEpochSecond(since, 0, ZoneOffset.UTC);
        }
        Long latest = dateTime.toEpochSecond(ZoneOffset.UTC);

        try (Transaction tx = db.beginTx()) {
            Node user = findUser(username, db);
            for (Relationship r1: user.getRelationships(Direction.INCOMING, RelationshipTypes.FOLLOWS)) {
                Long time = (Long)r1.getProperty(TIME);
                if(time < latest) {
                    Node follower = r1.getStartNode();
                    Map<String, Object> result = getUserAttributes(follower);
                    result.put(TIME, time);
                    results.add(result);
                }
            }
            tx.success();
        }
        results.sort(Comparator.comparing(m -> (Long) m.get(TIME), reverseOrder()));

        return Response.ok().entity(objectMapper.writeValueAsString(
                results.subList(0, Math.min(results.size(), limit))))
                .build();
    }

    @GET
    @Path("/{username}/following")
    public Response getFollowing(@PathParam("username") final String username,
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
            Node user = findUser(username, db);
            for (Relationship r1: user.getRelationships(Direction.OUTGOING, RelationshipTypes.FOLLOWS)) {
                Long time = (Long)r1.getProperty(TIME);
                if(time < latest) {
                    Node following = r1.getEndNode();
                    Map<String, Object> result = getUserAttributes(following);
                    result.put(TIME, time);
                    results.add(result);
                }
            }
            tx.success();
        }
        results.sort(Comparator.comparing(m -> (Long) m.get(TIME), reverseOrder()));

        return Response.ok().entity(objectMapper.writeValueAsString(
                results.subList(0, Math.min(results.size(), limit))))
                .build();
    }

    @POST
    @Path("/{username}/follows/{username2}")
    public Response createFollows(@PathParam("username") final String username,
                                 @PathParam("username2") final String username2,
                                 @Context GraphDatabaseService db) throws IOException {
        Map<String, Object> results;
        try (Transaction tx = db.beginTx()) {
            Node user = findUser(username, db);
            Node user2 = findUser(username2, db);
            if (user.equals(user2)) {
                throw UserExceptions.userSame;
            } else {
                HashSet<Node> blocked = new HashSet<>();
                for (Relationship r1 : user2.getRelationships(Direction.OUTGOING, RelationshipTypes.BLOCKS)) {
                    blocked.add(r1.getEndNode());
                }

                if (blocked.contains(user)) {
                    throw UserExceptions.userBlocked;
                }

                Relationship follows = user.createRelationshipTo(user2, RelationshipTypes.FOLLOWS);
                LocalDateTime dateTime = LocalDateTime.now(utc);
                follows.setProperty(TIME, dateTime.toEpochSecond(ZoneOffset.UTC));
                results = user2.getAllProperties();
                results.remove(EMAIL);
                results.remove(PASSWORD);
                tx.success();
            }
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @DELETE
    @Path("/{username}/follows/{username2}")
    public Response removeFollows(@PathParam("username") final String username,
                                  @PathParam("username2") final String username2,
                                  @Context GraphDatabaseService db) throws IOException {
        Map<String, Object> results;
        try (Transaction tx = db.beginTx()) {
            Node user = findUser(username, db);
            Node user2 = findUser(username2, db);

            if (user.getDegree(RelationshipTypes.FOLLOWS, Direction.OUTGOING)
                    < user2.getDegree(RelationshipTypes.FOLLOWS, Direction.INCOMING)) {
                for (Relationship r1: user.getRelationships(Direction.OUTGOING, RelationshipTypes.FOLLOWS) ) {
                    if (r1.getEndNode().equals(user2)) {
                        r1.delete();
                    }
                }
            } else {
                for (Relationship r1 : user2.getRelationships(Direction.INCOMING, RelationshipTypes.FOLLOWS)) {
                    if (r1.getStartNode().equals(user)) {
                        r1.delete();
                    }
                }
            }

            tx.success();
        }
        return Response.noContent().build();
    }

    public static Node findUser(String username, @Context GraphDatabaseService db) {
        if (username == null) { return null; }
        Node user = db.findNode(Labels.User, USERNAME, username);
        if (user == null) { throw UserExceptions.userNotFound; }
        return user;
    }

    public static Map<String, Object> getUserAttributes(Node user) {
        Map<String, Object> results;
        results = user.getAllProperties();
        results.remove(EMAIL);
        results.remove(PASSWORD);
        Integer following = user.getDegree(RelationshipTypes.FOLLOWS, Direction.OUTGOING);
        Integer followers = user.getDegree(RelationshipTypes.FOLLOWS, Direction.INCOMING);
        Integer likes = user.getDegree(RelationshipTypes.LIKES, Direction.OUTGOING);
        Integer posts = user.getDegree(Direction.OUTGOING) - following - likes;
        results.put("following", following);
        results.put("followers", followers);
        results.put("likes", likes);
        results.put("posts", posts);
        return results;
    }

    public static Node getPost(Node author, Long time) {
        LocalDateTime postedDateTime = LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.UTC);
        RelationshipType original = RelationshipType.withName("POSTED_ON_" +
                postedDateTime.format(dateFormatter));
        Node post = null;
        for(Relationship r1 : author.getRelationships(Direction.OUTGOING, original)) {
            Node potential = r1.getEndNode();
            if (time.equals(potential.getProperty(TIME))) {
                post = potential;
                break;
            }
        }
        if(post == null) { throw PostExceptions.postNotFound; };

        return post;
    }


}