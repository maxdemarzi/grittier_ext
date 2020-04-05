package com.maxdemarzi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;

import static com.maxdemarzi.Properties.*;

@Path("/schema")
public class Schema {
        private final DatabaseManagementService dbms;
        private final GraphDatabaseService db;
        private static final ObjectMapper objectMapper = new ObjectMapper();

        public Schema(@Context DatabaseManagementService dbms ) {
            this.dbms = dbms;
            this.db = dbms.database( "neo4j" );;
        }

        @POST
        @Path("/create")
        public Response create() throws IOException {
            ArrayList<String> results = new ArrayList<>();

            try (Transaction tx = db.beginTx()) {
                org.neo4j.graphdb.schema.Schema schema = tx.schema();
                if (!schema.getConstraints(Labels.User).iterator().hasNext()) {
                    schema.constraintFor(Labels.User)
                            .assertPropertyIsUnique(USERNAME)
                            .create();
                    tx.commit();
                    results.add("(:User {username}) constraint created");
                }
            }

            try (Transaction tx = db.beginTx()) {
                org.neo4j.graphdb.schema.Schema schema = tx.schema();
                if (!schema.getConstraints(Labels.Tag).iterator().hasNext()) {
                    schema.constraintFor(Labels.Tag)
                            .assertPropertyIsUnique(NAME)
                            .create();
                    tx.commit();
                    results.add("(:Tag {name}) constraint created");
                }
            }

            try (Transaction tx = db.beginTx()) {
                org.neo4j.graphdb.schema.Schema schema = tx.schema();
                if(!schema.getIndexes(Labels.Post).iterator().hasNext()) {
                    schema.indexFor(Labels.Post)
                            .on(STATUS)
                            .create();
                    tx.commit();
                    results.add("(:Post {status}) index created");
                }
            }

            results.add("Schema Created");
            return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
        }
}
