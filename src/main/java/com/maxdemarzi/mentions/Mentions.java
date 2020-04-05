package com.maxdemarzi.mentions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxdemarzi.Labels;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.maxdemarzi.Properties.*;
import static com.maxdemarzi.Time.dateFormatter;
import static com.maxdemarzi.Time.utc;
import static com.maxdemarzi.likes.Likes.userLikesPost;
import static com.maxdemarzi.posts.Posts.getAuthor;
import static com.maxdemarzi.posts.Posts.userRepostedPost;
import static java.util.Collections.reverseOrder;

@Path("/users/{username}/mentions")
public class Mentions {

    private static final Pattern mentionsPattern = Pattern.compile("@(\\S+)");

    private final GraphDatabaseService db;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public Mentions(@Context DatabaseManagementService dbms ) {
        this.db = dbms.database( "neo4j" );;
    }

    @GET
    public Response getMentions(@PathParam("username") final String username,
                                @QueryParam("limit") @DefaultValue("25") final Integer limit,
                                @QueryParam("since") final Long since,
                                @QueryParam("username2") final String username2) throws IOException {
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
            Node user2 = null;
            if (username2 != null) {
                user2 = Users.findUser(username2, tx);
            }

            HashSet<Node> blocked = new HashSet<>();
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.BLOCKS)) {
                blocked.add(r1.getEndNode());
            }
            LocalDateTime earliest = LocalDateTime.ofEpochSecond((Long)user.getProperty(TIME), 0, ZoneOffset.UTC);
            int count = 0;
            while (count < limit && (dateTime.isAfter(earliest))) {
                RelationshipType relType = RelationshipType.withName("MENTIONED_ON_" +
                        dateTime.format(dateFormatter));

                for (Relationship r1 : user.getRelationships(Direction.INCOMING, relType)) {
                    Node post = r1.getStartNode();
                    Map<String, Object> result = post.getAllProperties();
                    Long time = (Long)r1.getProperty("time");
                    if(time < latest) {
                        Node author = getAuthor(post, time);
                        if (!blocked.contains(author)) {
                            result.put(TIME, time);
                            result.put(USERNAME, author.getProperty(USERNAME));
                            result.put(NAME, author.getProperty(NAME));
                            result.put(HASH, author.getProperty(HASH));
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
                }
                dateTime = dateTime.minusDays(1);
            }
            tx.commit();
        }

        results.sort(Comparator.comparing(m -> (Long) m.get(TIME), reverseOrder()));

        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    public static void createMentions(Node post, HashMap<String, Object> input, LocalDateTime dateTime, Transaction tx) {
        Matcher mat = mentionsPattern.matcher(((String)input.get("status")).toLowerCase());

        for (Relationship r1 : post.getRelationships(Direction.OUTGOING, RelationshipType.withName("MENTIONED_ON_" +
                dateTime.format(dateFormatter)))) {
            r1.delete();
        }

        Set<Node> mentioned = new HashSet<>();
        while (mat.find()) {
            String username = mat.group(1);
            Node user = tx.findNode(Labels.User, USERNAME, username);
            if (user != null && !mentioned.contains(user)) {
                Relationship r1 = post.createRelationshipTo(user, RelationshipType.withName("MENTIONED_ON_" +
                        dateTime.format(dateFormatter)));
                r1.setProperty(TIME, dateTime.toEpochSecond(ZoneOffset.UTC));
                mentioned.add(user);
            }
        }
    }
}
