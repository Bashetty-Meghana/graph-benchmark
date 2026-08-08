package com.benchmark.loader;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatasetLoader {

    private final Driver driver;
    private static final int BATCH_SIZE = 5000;

    public DatasetLoader(Driver driver) {
        this.driver = driver;
    }

    public long loadDataset(String filePath) {
        System.out.println("Loading dataset into Cypher database via UNWIND batching...");
        long start = System.currentTimeMillis();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int count = 0;
            List<Map<String, String>> batch = new ArrayList<>(BATCH_SIZE);

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }

                String[] data = line.split("\\s+");
                if (data.length < 2) continue;

                Map<String, String> rel = new HashMap<>();
                rel.put("from", data[0]);
                rel.put("to", data[1]);
                batch.add(rel);
                count++;

                if (batch.size() >= BATCH_SIZE) {
                    flushBatch(batch);
                    batch.clear();
                    if (count % 50000 == 0) {
                        System.out.println("Ingested " + count + " relationships...");
                    }
                }
            }

            if (!batch.isEmpty()) {
                flushBatch(batch);
                batch.clear();
            }

            long end = System.currentTimeMillis();
            long totalTimeMs = end - start;
            System.out.println("Cypher Dataset Ingest Complete: " + count + " relationships in " + totalTimeMs + " ms");
            return totalTimeMs;

        } catch (Exception e) {
            System.err.println("Error loading dataset: " + e.getMessage());
            return -1;
        }
    }

    private void flushBatch(List<Map<String, String>> batch) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(
                    "UNWIND $batch AS row " +
                    "MERGE (u1:User {id: row.from}) " +
                    "MERGE (u2:User {id: row.to}) " +
                    "MERGE (u1)-[:TRUSTS]->(u2)",
                    Values.parameters("batch", batch)
                );
                return null;
            });
        }
    }
}