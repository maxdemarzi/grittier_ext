# grittier_ext
A Neo4j Based Twitter Clone Backend

# Instructions

1. Build it:

        mvn clean package

2. Copy target/gritter-1.0-SNAPSHOT.jar to the plugins/ directory of your Neo4j server.

3. Configure Neo4j by adding a line to conf/neo4j.conf:

        dbms.unmanaged_extension_classes=com.maxdemarzi/v1

4. Start Neo4j server.

5. Create the Schema:

        :POST /v1/schema/create