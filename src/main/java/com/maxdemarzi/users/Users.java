package com.maxdemarzi.users;

import com.maxdemarzi.Labels;
import com.maxdemarzi.RelationshipTypes;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.*;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.maxdemarzi.Properties.*;

@Path("/users")
public class Users {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GET
    @Path("/{username}")
    public Response getUser(@PathParam("username") final String username, @Context GraphDatabaseService db) throws IOException {
        Map<String, Object> results;
        try (Transaction tx = db.beginTx()) {
            Node user = db.findNode(Labels.User, USERNAME, username);
            results = user.getAllProperties();
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
    public Response getFollowers(@PathParam("username") final String username, @Context GraphDatabaseService db) throws IOException {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        try (Transaction tx = db.beginTx()) {
            Node user = db.findNode(Labels.User, USERNAME, username);
            for (Relationship r1: user.getRelationships(Direction.INCOMING, RelationshipTypes.FOLLOWS)) {
                Map<String, Object> follower = r1.getStartNode().getAllProperties();
                follower.remove(PASSWORD);
                results.add(follower);
            }
            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @GET
    @Path("/{username}/following")
    public Response getFollowing(@PathParam("username") final String username, @Context GraphDatabaseService db) throws IOException {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        try (Transaction tx = db.beginTx()) {
            Node user = db.findNode(Labels.User, USERNAME, username);
            for (Relationship r1: user.getRelationships(Direction.OUTGOING, RelationshipTypes.FOLLOWS)) {
                Map<String, Object> follower = r1.getEndNode().getAllProperties();
                follower.remove(PASSWORD);
                results.add(follower);
            }
            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @POST
    @Path("/{username}/follow/{username2}")
    public Response createFollow(@PathParam("username") final String username,
                                 @PathParam("username2") final String username2,
                                 @Context GraphDatabaseService db) throws IOException {
        Map<String, Object> results;
        try (Transaction tx = db.beginTx()) {
            Node user = db.findNode(Labels.User, USERNAME, username);
            Node user2 = db.findNode(Labels.User, USERNAME, username2);
            user.createRelationshipTo(user2, RelationshipTypes.FOLLOWS);
            results = user2.getAllProperties();
            results.remove(PASSWORD);
            tx.success();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

}