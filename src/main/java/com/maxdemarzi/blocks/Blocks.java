package com.maxdemarzi.blocks;

import com.maxdemarzi.RelationshipTypes;
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

import static com.maxdemarzi.Properties.EMAIL;
import static com.maxdemarzi.Properties.PASSWORD;
import static com.maxdemarzi.Properties.TIME;
import static com.maxdemarzi.Time.utc;
import static com.maxdemarzi.users.Users.findUser;
import static com.maxdemarzi.users.Users.getUserAttributes;
import static java.util.Collections.reverseOrder;

@Path("/users/{username}/blocks")
public class Blocks {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GET
    public Response getBlocks(@PathParam("username") final String username, @Context GraphDatabaseService db) throws IOException {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        try (Transaction tx = db.beginTx()) {
            Node user = findUser(username, db);
            for (Relationship r1: user.getRelationships(Direction.OUTGOING, RelationshipTypes.BLOCKS)) {
                Node blocked = r1.getEndNode();
                Long time = (Long)r1.getProperty(TIME);
                Map<String, Object> result = getUserAttributes(blocked);
                result.put(TIME, time);
                results.add(result);
            }
            tx.success();
        }
        results.sort(Comparator.comparing(m -> (Long) m.get(TIME), reverseOrder()));
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @POST
    @Path("/{username2}")
    public Response createBlocks(@PathParam("username") final String username,
                                  @PathParam("username2") final String username2,
                                  @Context GraphDatabaseService db) throws IOException {
        Map<String, Object> results;
        try (Transaction tx = db.beginTx()) {
            Node user = findUser(username, db);
            Node user2 = findUser(username2, db);

            if (user.getDegree(RelationshipTypes.BLOCKS, Direction.OUTGOING)
                    < user2.getDegree(RelationshipTypes.BLOCKS, Direction.INCOMING) ) {
                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.BLOCKS) ) {
                    if (r1.getEndNode().equals(user2)) {
                        throw BlockExceptions.alreadyBlockingUser;
                    }
                }
            } else {
                for (Relationship r1 : user2.getRelationships(Direction.INCOMING, RelationshipTypes.BLOCKS)) {
                    if (r1.getStartNode().equals(user)) {
                        throw BlockExceptions.alreadyBlockingUser;
                    }
                }
           }
            
            Relationship blocks = user.createRelationshipTo(user2, RelationshipTypes.BLOCKS);
            LocalDateTime dateTime = LocalDateTime.now(utc);
            blocks.setProperty(TIME, dateTime.toEpochSecond(ZoneOffset.UTC));

            if (user.getDegree(RelationshipTypes.FOLLOWS, Direction.OUTGOING)
                    < user2.getDegree(RelationshipTypes.FOLLOWS, Direction.INCOMING)) {
                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.FOLLOWS) ) {
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
            results = user2.getAllProperties();
            results.remove(PASSWORD);
            results.remove(EMAIL);

            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @DELETE
    @Path("/{username2}")
    public Response removeBlocks(@PathParam("username") final String username,
                                 @PathParam("username2") final String username2,
                                 @Context GraphDatabaseService db) throws IOException {
        boolean deleted = false;
        try (Transaction tx = db.beginTx()) {
            Node user = findUser(username, db);
            Node user2 = findUser(username2, db);

            if (user.getDegree(RelationshipTypes.BLOCKS, Direction.OUTGOING)
                    < user2.getDegree(RelationshipTypes.BLOCKS, Direction.INCOMING) ) {
                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.BLOCKS) ) {
                    if (r1.getEndNode().equals(user2)) {
                        r1.delete();
                        deleted = true;
                        break;
                    }
                }
            } else {
                for (Relationship r1 : user2.getRelationships(Direction.INCOMING, RelationshipTypes.BLOCKS)) {
                    if (r1.getStartNode().equals(user)) {
                        r1.delete();
                        deleted = true;
                        break;
                    }
                }
            }

            tx.success();
        }

        if (deleted) {
            return Response.noContent().build();
        } else {
            throw BlockExceptions.notBlockingUser;
        }
    }
}
