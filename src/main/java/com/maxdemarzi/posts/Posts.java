package com.maxdemarzi.posts;

import com.maxdemarzi.Labels;
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
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.maxdemarzi.Properties.*;
import static com.maxdemarzi.users.Users.getPost;
import static java.util.Collections.reverseOrder;

@Path("/users/{username}/posts")
public class Posts {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ZoneId utc = TimeZone.getTimeZone("UTC").toZoneId();
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter
            .ofPattern("yyyy_MM_dd")
            .withZone(utc);

    private static final LocalDateTime earliest = LocalDateTime.of(2017,3,20,0,0,0);

    @GET
    public Response getPosts(@PathParam("username") final String username,
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
            int count = 0;
            while (count < limit && (dateTime.isAfter(earliest))) {
                RelationshipType relType = RelationshipType.withName("POSTED_ON_" +
                        dateTime.format(dateFormatter));

                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, relType)) {
                    Map<String, Object> post = r1.getEndNode().getAllProperties();
                    if((Long)post.get("time") < latest) {
                        results.add(post);
                        count++;
                    }
                }
                dateTime = dateTime.minusDays(1);
            }
            tx.success();
        }

        results.sort(Comparator.comparing(m -> (Long) m.get("time"), reverseOrder()));

        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @POST
    public Response createPost(String body, @PathParam("username") final String username,
                               @Context GraphDatabaseService db) throws IOException {
        Map<String, Object> results;
        HashMap input = PostValidator.validate(body);

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, db);
            Node post = db.createNode(Labels.Post);
            post.setProperty(STATUS, input.get("status"));
            LocalDateTime dateTime = LocalDateTime.now(utc);
            post.setProperty(TIME, dateTime.toEpochSecond(ZoneOffset.UTC));

            user.createRelationshipTo(post, RelationshipType.withName("POSTED_ON_" +
                            dateTime.format(dateFormatter)));
            results = post.getAllProperties();
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
            
            user.createRelationshipTo(post, RelationshipType.withName("REPOSTED_ON_" +
                    dateTime.format(dateFormatter)));
            results = post.getAllProperties();
            results.put(USERNAME, user2.getProperty(USERNAME));
            results.put(NAME, user2.getProperty(NAME));
            results.put(LIKES, post.getDegree(RelationshipTypes.LIKES));
            results.put(REPOSTS, post.getDegree() - 1 - post.getDegree(RelationshipTypes.LIKES));

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
}
