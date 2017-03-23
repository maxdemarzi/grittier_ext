# grittier_ext
A Neo4j Based Twitter Clone Backend

[![Codeship Status for maxdemarzi/grittier_ext](https://app.codeship.com/projects/50014300-f166-0134-82f1-1a55004cd4f7/status?branch=master)](https://app.codeship.com/projects/209391)

[![Coverage Status](https://coveralls.io/repos/github/maxdemarzi/grittier_ext/badge.svg?branch=master)](https://coveralls.io/github/maxdemarzi/grittier_ext?branch=master)

# Instructions

1. Build it:

        mvn clean package

2. Copy target/gritter-1.0-SNAPSHOT.jar to the plugins/ directory of your Neo4j server.

3. Configure Neo4j by adding a line to conf/neo4j.conf:

        dbms.unmanaged_extension_classes=com.maxdemarzi/v1

4. Start Neo4j server.

5. Create the Schema:

        :POST /v1/schema/create