package com.benchmark.loader;

import com.benchmark.database.DatabaseManager;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DatasetLoader {

    private final DatabaseManager databaseManager;

    public DatasetLoader(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void loadDataset() {

    System.out.println("Loading dataset...");

    long start = System.currentTimeMillis();

    try (
            BufferedReader reader =
                    new BufferedReader(new FileReader("dataset/soc-Epinions1.txt"));
            Session session = databaseManager.getDriver().session()
    ) {

        System.out.println("File opened successfully.");

        String line;
        int count = 0;

        while ((line = reader.readLine()) != null) {

            if (line.startsWith("#") || line.trim().isEmpty()) {
                continue;
            }

            String[] data = line.split("\\s+");

            String from = data[0];
            String to = data[1];

            session.run(
                    "MERGE (u1:User {id:$from}) " +
                    "MERGE (u2:User {id:$to}) " +
                    "MERGE (u1)-[:TRUSTS]->(u2)",
                    Values.parameters("from", from, "to", to)
            );

            count++;

            if (count % 10000 == 0) {
                System.out.println("Loaded " + count + " relationships...");
            }
        }

        long end = System.currentTimeMillis();

        System.out.println("Dataset Loaded Successfully");
        System.out.println("Total Relationships: " + count);
        System.out.println("Time Taken: " + (end - start) + " ms");

    } catch (Exception e) {
        e.printStackTrace();
    }
}
    
}