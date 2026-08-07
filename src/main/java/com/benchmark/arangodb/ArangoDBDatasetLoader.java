package com.benchmark.arangodb;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class ArangoDBDatasetLoader {

    private final ArangoDatabase database;

    public ArangoDBDatasetLoader(ArangoDatabase database) {
        this.database = database;
    }

    public void loadDataset() {

        System.out.println("Loading dataset into ArangoDB...");

        long start = System.currentTimeMillis();

        ArangoCollection users = database.collection("users");
        ArangoCollection trusts = database.collection("trusts");

        int count = 0;

        try (BufferedReader reader =
                     new BufferedReader(new FileReader("dataset/soc-Epinions1.txt"))) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }

                String[] data = line.split("\\s+");

                String from = data[0];
                String to = data[1];

                Map<String, Object> user1 = new HashMap<>();
                user1.put("_key", from);

                Map<String, Object> user2 = new HashMap<>();
                user2.put("_key", to);

                Map<String, Object> edge = new HashMap<>();
                edge.put("_key", from + "_" + to);
                edge.put("_from", "users/" + from);
                edge.put("_to", "users/" + to);

                try {
                    if (!users.documentExists(from)) {
                        users.insertDocument(user1);
                    }

                    if (!users.documentExists(to)) {
                        users.insertDocument(user2);
                    }

                    if (!trusts.documentExists(from + "_" + to)) {
                        trusts.insertDocument(edge);
                    }
                } catch (Exception ignored) {
                }

                count++;

                if (count % 10000 == 0) {
                    System.out.println("Loaded " + count + " relationships...");
                }

                
            }

            long end = System.currentTimeMillis();

            System.out.println("\nDataset Loaded Successfully!");
            System.out.println("Relationships Loaded : " + count);
            System.out.println("Time : " + (end - start) + " ms");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}