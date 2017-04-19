package com.maxdemarzi.tags;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
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
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.maxdemarzi.Properties.*;
import static com.maxdemarzi.Time.dateFormatter;
import static com.maxdemarzi.Time.utc;
import static com.maxdemarzi.likes.Likes.userLikesPost;
import static com.maxdemarzi.posts.Posts.getAuthor;
import static com.maxdemarzi.posts.Posts.userRepostedPost;
import static java.util.Collections.reverseOrder;

@Path("/tags")
public class Tags {

    private static final Pattern hashtagPattern = Pattern.compile("#(\\S+)");

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static GraphDatabaseService db;

    public Tags(@Context GraphDatabaseService graphDatabaseService) {
        db = graphDatabaseService;
    }

    // Cache
    private static LoadingCache<String, List<Map<String, Object>>> trends = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .refreshAfterWrite(5, TimeUnit.MINUTES)
            .build(Tags::getTrends);

    private static List<Map<String, Object>> getTrends(String key) {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        LocalDateTime dateTime = LocalDateTime.now(utc);
        RelationshipType tagged = RelationshipType.withName("TAGGED_ON_" +
                dateTime.format(dateFormatter));
        try (Transaction tx = db.beginTx()) {
            ResourceIterator<Node> tags = db.findNodes(Labels.Tag);
            while (tags.hasNext()) {
                Node tag = tags.next();
                int taggings = tag.getDegree(tagged, Direction.INCOMING);
                if ( taggings > 0) {
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(NAME, tag.getProperty(NAME));
                    result.put(COUNT, taggings);
                    results.add(result);
                }
            }
            tx.success();
        }

        results.sort(Comparator.comparing(m -> (Integer) m.get(COUNT), reverseOrder()));
        return results.subList(0, Math.min(results.size(), 10));
    }

    @GET
    @Path("/{hashtag}")
    public Response getTags(@PathParam("hashtag") final String hashtag,
                             @QueryParam("limit") @DefaultValue("25") final Integer limit,
                             @QueryParam("since") final Long since,
                             @QueryParam("username") final String username,
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
            Node user = null;
            if (username != null) {
                user = Users.findUser(username, db);
            }

            Node tag = db.findNode(Labels.Tag, NAME, hashtag.toLowerCase());
            if (tag != null) {
                LocalDateTime earliestTag = LocalDateTime.ofEpochSecond((Long) tag.getProperty(TIME), 0, ZoneOffset.UTC);

                int count = 0;
                while (count < limit && (dateTime.isAfter(earliestTag))) {
                    RelationshipType relType = RelationshipType.withName("TAGGED_ON_" +
                            dateTime.format(dateFormatter));

                    for (Relationship r1 : tag.getRelationships(Direction.INCOMING, relType)) {
                        Node post = r1.getStartNode();
                        Map<String, Object> result = post.getAllProperties();
                        Long time = (Long) result.get("time");

                        if (count < limit && time < latest) {
                            Node author = getAuthor(post, time);
                            Map userProperties = author.getAllProperties();
                            result.put(USERNAME, userProperties.get(USERNAME));
                            result.put(NAME, userProperties.get(NAME));
                            result.put(HASH, userProperties.get(HASH));
                            result.put(LIKES, post.getDegree(RelationshipTypes.LIKES));
                            result.put(REPOSTS, post.getDegree(Direction.INCOMING)
                                    - 1 // for the Posted Relationship Type
                                    - post.getDegree(RelationshipTypes.LIKES)
                                    - post.getDegree(RelationshipTypes.REPLIED_TO));
                            if (user != null) {
                                result.put(LIKED, userLikesPost(user, post));
                                result.put(REPOSTED, userRepostedPost(user, post));
                            }
                            results.add(result);
                            count++;
                        }
                    }
                    dateTime = dateTime.minusDays(1);
                }
                tx.success();
                results.sort(Comparator.comparing(m -> (Long) m.get(TIME), reverseOrder()));
            } else {
                throw TagExceptions.tagNotFound;
            }
        }
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    public static void createTags(Node post, HashMap<String, Object>  input, LocalDateTime dateTime, GraphDatabaseService db) {
        Matcher mat = hashtagPattern.matcher(((String)input.get("status")).toLowerCase());
        for (Relationship r1 : post.getRelationships(Direction.OUTGOING, RelationshipType.withName("TAGGED_ON_" +
                dateTime.format(dateFormatter)))) {
            r1.delete();
        }
        Set<Node> tagged = new HashSet<>();
        while (mat.find()) {
            String tag = mat.group(1);
            Node hashtag = db.findNode(Labels.Tag, NAME, tag);
            if (hashtag == null) {
                hashtag = db.createNode(Labels.Tag);
                hashtag.setProperty(NAME, tag);
                hashtag.setProperty(TIME, dateTime.truncatedTo(ChronoUnit.DAYS).toEpochSecond(ZoneOffset.UTC));
            }
            if (!tagged.contains(hashtag)) {
                post.createRelationshipTo(hashtag, RelationshipType.withName("TAGGED_ON_" +
                        dateTime.format(dateFormatter)));
                tagged.add(hashtag);
            }
        }
    }

    @GET
    public Response getTrends(@Context GraphDatabaseService db) throws IOException {
        List<Map<String, Object>> results;
        try (Transaction tx = db.beginTx()) {
            results = trends.get("trends");
        }

        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }
}
