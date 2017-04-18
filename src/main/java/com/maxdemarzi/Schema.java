package com.maxdemarzi;

import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;

import static com.maxdemarzi.Properties.NAME;
import static com.maxdemarzi.Properties.STATUS;
import static com.maxdemarzi.Properties.USERNAME;

@Path("/schema")
public class Schema {

        private static final ObjectMapper objectMapper = new ObjectMapper();

        @POST
        @Path("/create")
        public Response create(@Context GraphDatabaseService db) throws IOException {
            ArrayList<String> results = new ArrayList<>();

            try (Transaction tx = db.beginTx()) {
                org.neo4j.graphdb.schema.Schema schema = db.schema();
                if (!schema.getConstraints(Labels.User).iterator().hasNext()) {
                    schema.constraintFor(Labels.User)
                            .assertPropertyIsUnique(USERNAME)
                            .create();
                    tx.success();
                    results.add("(:User {username}) constraint created");
                }
            }

            try (Transaction tx = db.beginTx()) {
                org.neo4j.graphdb.schema.Schema schema = db.schema();
                if (!schema.getConstraints(Labels.Tag).iterator().hasNext()) {
                    schema.constraintFor(Labels.Tag)
                            .assertPropertyIsUnique(NAME)
                            .create();
                    tx.success();
                    results.add("(:Tag {name}) constraint created");
                }
            }

            try (Transaction tx = db.beginTx()) {
                org.neo4j.graphdb.schema.Schema schema = db.schema();
                if(!schema.getIndexes(Labels.Post).iterator().hasNext()) {
                    schema.indexFor(Labels.Post)
                            .on(STATUS)
                            .create();
                    tx.success();
                    results.add("(:Post {status}) index created");
                }
            }

            results.add("Schema Created");
            return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
        }
}
