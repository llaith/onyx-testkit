# onyx-testkit

A junit rule to run docker containers, based on: https://github.com/geowarin/docker-junit-rule

[![Github License](https://img.shields.io/github/license/llaith/onyx-testkit.svg)](./LICENSE.txt)
[![Download](https://api.bintray.com/packages/llaith/onyx/onyx-testkit/images/download.svg) ](https://bintray.com/llaith/onyx/onyx-testkit/_latestVersion)

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/ec7ee50b4f1f48879513be2e07339dad)](https://www.codacy.com/project/llaith/onyx-testkit/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=llaith/onyx-testkit&amp;utm_campaign=Badge_Grade_Dashboard)
[![CircleCI](https://circleci.com/gh/llaith/onyx-testkit/tree/master.svg?style=shield&circle-token=16fbebf80d0c0cf291f9faa970f3ed92a4664c9f)](https://circleci.com/gh/llaith/onyx-testkit/tree/master)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/llaith/onyx-testkit.svg)
![Github file size](https://img.shields.io/github/repo-size/llaith/onyx-testkit.svg)

[![GitHub issues](https://img.shields.io/github/issues/llaith/onyx-testkit.svg)](https://github.com/llaith/onyx-testkit/issues)
[![Waffle.io](https://badge.waffle.io/llaith/onyx-testkit.svg?columns=all)](https://waffle.io/llaith/onyx-testkit)

## Usage

Example for rabbitMQ:

```java
import com.rabbitmq.client.ConnectionFactory;
import org.junit.ClassRule;
import org.junit.Test;

import static org.llaith.onyx.testkit.docker.junit.GenericWaitingStrategies.waitForLog;

public class RabbitIntegrationIT {

    @ClassRule
    public static GenericResource rabbitRule =
            GenericConfig.builder()
                         .image("rabbitmq:management")
                         .ports("5672")
                         .waitFor(60, 6, 10, (wait) -> wait.addStrategy(waitForLog("Server startup complete")))
                         .build();

    @Test
    public void testConnectsToDocker() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitRule.getContainerHost());
        factory.setPort(rabbitRule.getMappedPort("5672/tcp"));
        factory.newConnection();
    }

}

```

The library is designed to be extended with specific instances where it makes sense, such as databases, allowing for 
easily accessable jdbc URL derived from the container.

Use the shipped PostgreSQL resource like so:

```java

    // normal setup:

    @ClassRule
    public static PostgresResource pg =
            PostgresConfig.builder()
                          .image("postgres:9.6")
                          .postgresPort("5432/tcp")
                          .waitFor(60, 6, 10, (wait) -> {
                              wait.addStrategy(waitForPort("5432/tcp"));
                              wait.addStrategy(waitForSelect("SELECT 1"));
                          })
                          .build();

    // simpler setup:

    @ClassRule
    public static PostgresResource postgresSimpler =
            PostgresConfig.builderWithDefaults()
                          .databaseName("mydb")
                          .build();

    // simplest setup:

    @ClassRule
    public static PostgresResource defaultPg = PostgresResource.buildWithDefaults();

```

The use of the PostgreSQL specific container allows for easy integration with test code like the following:

```java

    // example of pulling values from container:

    final HikariConfig hikariConfig = new HikariConfig();
    
    hikariConfig.setJdbcUrl(pg.getJdbcUrl());
    hikariConfig.setUsername(pg.getUsername());
    hikariConfig.setPassword(pg.getPassword());

    HikariDataSource dataSource = new HikariDataSource(hikariConfig);

    // very easy use of embedded sql execution for test code: 
            
    pg.executeSQL(sql, statement -> logger.debug("Successful execution of sql: " + sql));
            
```

## Build

To build and run the tests, the following command must be run:

$> mvn clean install -DskipITs=false

**WARNING:** currently the library has an undiagnosed issue when running the tests from maven, the docker client
is being closed and the tests fail, despite the tests passing when run from the IDE. Until this is resolved, it will
remain an alpha release and should not be used for any production code.

## Installation

It can be added to your POM like so:

### Maven

Add the following to your `pom.xml`:

```xml
<repositories>
    <repository>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
        <id>jcenter</id>
        <name>bintray</name>
        <url>http://jcenter.bintray.com</url>
    </repository>
</repositories>

...

<dependency>
    <groupId>org.llaith.onyx</groupId>
    <artifactId>onyx-testkit</artifactId>
    <version>1.0</version>
    <scope>test</scope>
</dependency>
```

## Principle

Uses https://github.com/spotify/docker-client to connect to the docker daemon API. More instructions on what is possible,
and ideas on how to extend it further, can be found in the documentation to that project.

## Licence

MIT
