package com.maxdemarzi;

import org.neo4j.graphdb.RelationshipType;

public enum  RelationshipTypes implements RelationshipType {
    BLOCKS,
    FOLLOWS,
    MUTES,
    LIKES,
    REPLIED_TO
}