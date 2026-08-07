package com.benchmark.arangodb;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;

public class ArangoDBManager {

    private ArangoDB arangoDB;
    private ArangoDatabase database;

    public void connect() {

        arangoDB = new ArangoDB.Builder()
                .host("localhost", 8529)
                .user("root")
                .password("root")
                .build();

        // Connect to _system database
        ArangoDatabase systemDB = arangoDB.db("_system");

        // Create benchmark database if it doesn't exist
        if (!systemDB.getAccessibleDatabases().contains("benchmark")) {
            arangoDB.createDatabase("benchmark");
            System.out.println("Database 'benchmark' created.");
        }

        // Connect to benchmark database
        database = arangoDB.db("benchmark");

        // Create users collection if it doesn't exist
        if (!database.collection("users").exists()) {
            database.createCollection("users");
            System.out.println("Created collection: users");
        }

        // Create trusts collection if it doesn't exist
        if (!database.collection("trusts").exists()) {
            database.createCollection("trusts");
            System.out.println("Created collection: trusts");
        }

        System.out.println("Connected Successfully to ArangoDB!");
    }

    public void disconnect() {

        if (arangoDB != null) {
            arangoDB.shutdown();
        }

        System.out.println("Connection Closed!");
    }

    public ArangoDatabase getDatabase() {
        return database;
    }
}