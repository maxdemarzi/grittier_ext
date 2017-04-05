package com.maxdemarzi.tags;

import com.maxdemarzi.Labels;
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
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.maxdemarzi.Properties.*;
import static com.maxdemarzi.Time.dateFormatter;
import static com.maxdemarzi.Time.utc;
import static com.maxdemarzi.posts.Posts.getAuthor;
import static java.util.Collections.reverseOrder;

@Path("/tags")
public class Tags {

    private static final Pattern hashtagPattern = Pattern.compile("#(\\S+)");

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GET
    @Path("/{hashtag}")
    public Response getTags(@PathParam("hashtag") final String hashtag,
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
}
