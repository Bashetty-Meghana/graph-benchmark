package com.benchmark.database;

import com.benchmark.config.Config;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public class DatabaseManager implements GraphDatabaseService {

    private Driver driver;
    private Config config;

    public DatabaseManager() {
        config = new Config();
    }

    @Override
    public void connect() {

        String database = config.getProperty("database");

        String uri = config.getProperty(database + ".uri");
        String username = config.getProperty(database + ".username");
        String password = config.getProperty(database + ".password");

        if (username == null) {
            username = "";
        }

        if (password == null) {
            password = "";
        }

        if (username.isEmpty() && password.isEmpty()) {
            driver = GraphDatabase.driver(uri, AuthTokens.none());
        } else {
            driver = GraphDatabase.driver(
                    uri,
                    AuthTokens.basic(username, password)
            );
        }

        System.out.println("Connected Successfully to " + database + "!");
    }

    @Override
    public void disconnect() {

        if (driver != null) {
            driver.close();
        }

        System.out.println("Connection Closed!");
    }

    public Driver getDriver() {
        return driver;
    }
}