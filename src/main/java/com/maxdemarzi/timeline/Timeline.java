package com.maxdemarzi.timeline;

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
import java.util.HashSet;
import java.util.Map;

import static com.maxdemarzi.Properties.*;
import static com.maxdemarzi.Time.dateFormatter;
import static com.maxdemarzi.Time.utc;
import static com.maxdemarzi.likes.Likes.userLikesPost;
import static com.maxdemarzi.posts.Posts.getAuthor;
import static com.maxdemarzi.posts.Posts.userRepostedPost;
import static java.util.Collections.reverseOrder;

@Path("/users/{username}/timeline")
public class Timeline {

    private final GraphDatabaseService db;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public Timeline(@Context DatabaseManagementService dbms ) {
        this.db = dbms.database( "neo4j" );;
    }

    @GET
    public Response getTimeline(@PathParam("username") final String username,
                             @QueryParam("limit") @DefaultValue("100") final Integer limit,
                             @QueryParam("since") final Long since) throws IOException {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        LocalDateTime dateTime;
        if (since == null) {
            dateTime = LocalDateTime.now(utc);
        } else {
            dateTime = LocalDateTime.ofEpochSecond(since, 0, ZoneOffset.UTC);
        }
        Long latest = dateTime.toEpochSecond(ZoneOffset.UTC);

        try (Transaction tx = db.beginTx()) {
            Node user = Users.findUser(username, tx);
            HashSet<Long> seen = new HashSet<>();
            ArrayList<Node> follows = new ArrayList<>();
            follows.add(user); // Adding user to see their posts on timeline as well
            for (Relationship r : user.getRelationships(Direction.OUTGOING, RelationshipTypes.FOLLOWS)) {
                follows.add(r.getEndNode());
            }

            LocalDateTime earliest = LocalDateTime.ofEpochSecond((Long)user.getProperty(TIME), 0, ZoneOffset.UTC);

            while (seen.size() < limit && (dateTime.isAfter(earliest))) {
                RelationshipType posted = RelationshipType.withName("POSTED_ON_" +
                        dateTime.format(dateFormatter));
                RelationshipType reposted = RelationshipType.withName("REPOSTED_ON_" +
                        dateTime.format(dateFormatter));

                for (Node follow : follows) {
                    Map followProperties = follow.getAllProperties();

                    for (Relationship r1 : follow.getRelationships(Direction.OUTGOING, posted)) {
                        Node post = r1.getEndNode();
                        if(seen.add(post.getId())) {
                            Long time = (Long)r1.getProperty("time");
                            Map<String, Object> properties = r1.getEndNode().getAllProperties();
                            if (time < latest) {
                                properties.put(TIME, time);
                                properties.put(USERNAME, followProperties.get(USERNAME));
                                properties.put(NAME, followProperties.get(NAME));
                                properties.put(HASH, followProperties.get(HASH));
                                properties.put(LIKES, post.getDegree(RelationshipTypes.LIKES));
                                properties.put(REPOSTS, post.getDegree(Direction.INCOMING)
                                        - 1 // for the Posted Relationship Type
                                        - post.getDegree(RelationshipTypes.LIKES)
                                        - post.getDegree(RelationshipTypes.REPLIED_TO));
                                properties.put(LIKED, userLikesPost(user, post));
                                properties.put(REPOSTED, userRepostedPost(user, post));
                                results.add(properties);
                            }
                        }
                    }

                    for (Relationship r1 : follow.getRelationships(Direction.OUTGOING, reposted)) {
                        Node post = r1.getEndNode();
                        if(seen.add(post.getId())) {
                            Map<String, Object> properties = r1.getEndNode().getAllProperties();
                            Long reposted_time = (Long)r1.getProperty(TIME);
                            if (reposted_time < latest) {
                                properties.put(REPOSTED_TIME, reposted_time);
                                properties.put(REPOSTER_USERNAME, followProperties.get(USERNAME));
                                properties.put(REPOSTER_NAME, followProperties.get(NAME));
                                properties.put(HASH, followProperties.get(HASH));
                                properties.put(LIKES, post.getDegree(RelationshipTypes.LIKES));
                                properties.put(REPOSTS, post.getDegree(Direction.INCOMING)
                                        - 1 // for the Posted Relationship Type
                                        - post.getDegree(RelationshipTypes.LIKES)
                                        - post.getDegree(RelationshipTypes.REPLIED_TO));
                                properties.put(LIKED, userLikesPost(user, post));
                                properties.put(REPOSTED, userRepostedPost(user, post));

                                Node author = getAuthor(post, (Long)properties.get(TIME));
                                properties.put(USERNAME, author.getProperty(USERNAME));
                                properties.put(NAME, author.getProperty(NAME));
                                results.add(properties);
                            }
                        }
                    }
                }
                dateTime = dateTime.minusDays(1);
            }
            tx.commit();
        }

        results.sort(Comparator.comparing(m -> (Long) m.get("time"), reverseOrder()));

        return Response.ok().entity(objectMapper.writeValueAsString(
                results.subList(0, Math.min(results.size(), limit))))
                .build();
    }

}
