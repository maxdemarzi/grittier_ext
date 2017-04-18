package com.maxdemarzi.search;

import com.maxdemarzi.Labels;
import com.maxdemarzi.RelationshipTypes;
import com.maxdemarzi.users.Users;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.collection.primitive.PrimitiveLongIterator;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.api.ReadOperations;
import org.neo4j.kernel.api.exceptions.index.IndexNotFoundKernelException;
import org.neo4j.kernel.api.exceptions.schema.SchemaRuleNotFoundException;
import org.neo4j.kernel.api.index.IndexDescriptor;
import org.neo4j.kernel.impl.api.KernelStatement;
import org.neo4j.kernel.impl.core.ThreadToStatementContextBridge;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.storageengine.api.schema.IndexReader;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
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
import static com.maxdemarzi.likes.Likes.userLikesPost;
import static com.maxdemarzi.posts.Posts.getAuthor;
import static com.maxdemarzi.posts.Posts.userRepostedPost;
import static java.util.Collections.reverseOrder;

@Path("/search")
public class Search {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static GraphDatabaseAPI dbapi;
    private static int postLabelId;
    private static int statusPropertyId;

    public Search(@Context GraphDatabaseService db) throws NoSuchMethodException {
        this.dbapi = (GraphDatabaseAPI) db;
        try (Transaction tx = db.beginTx()) {
            ThreadToStatementContextBridge ctx = dbapi.getDependencyResolver().resolveDependency(ThreadToStatementContextBridge.class);
            ReadOperations ops = ctx.get().readOperations();
            postLabelId = ops.labelGetForName(Labels.Post.name());
            statusPropertyId = ops.propertyKeyGetForName(STATUS);
            tx.success();
        }

//        Method method = IndexReader.class.getDeclaredMethod("getIndexSearcher");
//        method.setAccessible(true);
    }

    @GET
    public Response getSearch(@QueryParam("q") final String q,
                              @QueryParam("limit") @DefaultValue("25") final Integer limit,
                              @QueryParam("since") final Long since,
                              @QueryParam("username") final String username,
                              @Context GraphDatabaseService db) throws SchemaRuleNotFoundException, IndexNotFoundKernelException, NoSuchMethodException, IOException {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        LocalDateTime dateTime;
        if (since == null) {
            dateTime = LocalDateTime.now(utc);
        } else {
            dateTime = LocalDateTime.ofEpochSecond(since, 0, ZoneOffset.UTC);
        }
        Long latest = dateTime.toEpochSecond(ZoneOffset.UTC);

        try (Transaction tx = db.beginTx()) {
            Node user = null;
            if (username != null) {
                user = Users.findUser(username, db);
            }

            ThreadToStatementContextBridge ctx = dbapi.getDependencyResolver().resolveDependency(ThreadToStatementContextBridge.class);
            KernelStatement st = (KernelStatement)ctx.get();
            ReadOperations ops = st.readOperations();
            IndexDescriptor descriptor = ops.indexGetForLabelAndPropertyKey(postLabelId, statusPropertyId);
            IndexReader reader = st.getStoreStatement().getIndexReader(descriptor);
            PrimitiveLongIterator hits = reader.containsString(q);
            while(hits.hasNext()) {
                Node post = db.getNodeById(hits.next());
                Long time = (Long)post.getProperty("time");
                if(time < latest) {
                    Map<String, Object> properties = post.getAllProperties();
                    Node author = getAuthor(post, (Long) properties.get(TIME));
                    properties.put(USERNAME, author.getProperty(USERNAME));
                    properties.put(NAME, author.getProperty(NAME));
                    properties.put(HASH, author.getProperty(HASH));
                    properties.put(LIKES, post.getDegree(RelationshipTypes.LIKES));
                    properties.put(REPOSTS, post.getDegree() - 1 - post.getDegree(RelationshipTypes.LIKES));
                    if (user != null) {
                        properties.put(LIKED, userLikesPost(user, post));
                        properties.put(REPOSTED, userRepostedPost(user, post));
                    }
                    results.add(properties);
                }

            }

        }
        results.sort(Comparator.comparing(m -> (Long) m.get(TIME), reverseOrder()));

        return Response.ok().entity(objectMapper.writeValueAsString(
                results.subList(0, Math.min(results.size(), limit))))
                .build();
    }

}
