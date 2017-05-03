package com.maxdemarzi.recommendations;

import com.maxdemarzi.RelationshipTypes;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.*;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;

import static com.maxdemarzi.Properties.EMAIL;
import static com.maxdemarzi.Properties.I_FOLLOW;
import static com.maxdemarzi.Properties.PASSWORD;
import static com.maxdemarzi.users.Users.findUser;
import static java.util.Collections.reverseOrder;

@Path("/users/{username}/recommendations")
public class Recommendations {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GET
    @Path("/friends")
    public Response recommendFriends(@PathParam("username") final String username,
                                     @QueryParam("limit") @DefaultValue("25") final Integer limit,
                                     @Context GraphDatabaseService db) throws IOException {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        try (Transaction tx = db.beginTx()) {
            Node user = findUser(username, db);
            HashSet<Long> following = new HashSet<>();
            for (Relationship r1: user.getRelationships(Direction.OUTGOING, RelationshipTypes.FOLLOWS)) {
                following.add(r1.getEndNode().getId());
            }

            for (Relationship r1 : user.getRelationships(Direction.INCOMING, RelationshipTypes.FOLLOWS)) {
                Node follower = r1.getStartNode();
                if (!following.contains(follower.getId())) {
                    Map<String, Object> properties = r1.getStartNode().getAllProperties();
                    properties.remove(PASSWORD);
                    properties.remove(EMAIL);
                    results.add(properties);
                }
            }
            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(
                results.subList(0, Math.min(results.size(), limit))))
                .build();
    }

    @GET
    @Path("/follows")
    public Response recommendFollows(@PathParam("username") final String username,
                                     @QueryParam("limit") @DefaultValue("25") final Integer limit,
                                     @Context GraphDatabaseService db) throws IOException {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        try (Transaction tx = db.beginTx()) {
            Node user = findUser(username, db);
            HashSet<Node> following = new HashSet<>();
            for (Relationship r1: user.getRelationships(Direction.OUTGOING, RelationshipTypes.FOLLOWS)) {
                following.add(r1.getEndNode());
            }

            HashMap<Node, LongAdder> fofs = new HashMap<>();

            for (Node user2 : following) {
                for (Relationship r1 : user2.getRelationships(Direction.OUTGOING, RelationshipTypes.FOLLOWS)) {
                    Node fof = r1.getEndNode();
                    if(fofs.containsKey(fof)) {
                        fofs.get(fof).increment();
                    } else {
                        LongAdder counter = new LongAdder();
                        counter.increment();
                        fofs.put(fof, counter);
                    }
                }
            }
            fofs.remove(user);
            for (Node user2 : following) {
                fofs.remove(user2);
            }

            ArrayList<Map.Entry<Node, LongAdder>> fofList = new ArrayList<>(fofs.entrySet());
            fofList.sort(Comparator.comparing(m -> (Long) m.getValue().longValue(), reverseOrder()));
            for (Map.Entry<Node, LongAdder> entry : fofList.subList(0, Math.min(fofList.size(), limit))) {
                Map<String, Object> properties = entry.getKey().getAllProperties();
                properties.remove(PASSWORD);
                properties.remove(EMAIL);
                properties.put(I_FOLLOW, false);
                results.add(properties);
            }

            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }
}
