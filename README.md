# Alfresco SOLR Index Checker

This is an *experimental* project to check the indexation coverage for an Alfresco Repository DB.

This project is not intended to be used in any *production* system (yet).

## Configuring

Alfresco JDBC and SOLR Endpoint can be configured in [src/main/resources/application.properties](src/main/resources/application.properties)

```
# Alfresco Database
spring.datasource.url=jdbc:postgresql://localhost/alfresco
spring.datasource.username=alfresco
spring.datasource.password=alfresco
spring.datasource.driver-class-name=org.postgresql.Driver

# Alfresco SOLR endpoint
solr.url=http://localhost:8983/solr
```

When the property `report.detailed` in this properties file is set to `true`, a list including the dbIds and the aclIds different in SOLR and Database is produced.

```
# Produce detail report on missing nodes or documents (true / false)
report.detailed=true
```

## Compiling

Java 11 is required.

```
$ mvn clean package
```

## Execution

```
$ java -jar target/index-checker-0.0.1-SNAPSHOT.jar

org.alfresco.indexchecker.App            : Count SOLR document = 815
org.alfresco.indexchecker.App            : Count DB nodes = 815
org.alfresco.indexchecker.App            : Count SOLR permissions = 57
org.alfresco.indexchecker.App            : Count DB permissions = 58
```

If all the validations are passing, no additional messages are logged.

If some of the types has a different count in SOLR than in DB, error messages like the following one are logged.

```
SOLR indexed 201 nodes more than the existing in database for {http://www.alfresco.org/model/content/1.0}content
SOLR indexed 393 nodes more than the existing in database for {http://www.alfresco.org/model/content/1.0}failedThumbnail
SOLR indexed 2 nodes more than the existing in database for {http://www.alfresco.org/model/content/1.0}folder

The database contains 1 acls more than the indexed in SOLR
```

When using `report.detailed` option, additional details are provided.

```
AclIds present in DB but missed in SOLR [15]
```
