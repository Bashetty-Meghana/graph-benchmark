package com.benchmark.falkordb;

import com.falkordb.Graph;

import java.io.BufferedReader;
import java.io.FileReader;

public class FalkorDBDatasetLoader {

    private final Graph graph;

    public FalkorDBDatasetLoader(Graph graph) {
        this.graph = graph;
    }

    public void loadDataset() {

        System.out.println("Loading dataset into FalkorDB...");

        long start = System.currentTimeMillis();

        int count = 0;

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader("dataset/soc-Epinions1.txt"))) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }

                String[] data = line.split("\\s+");

                String from = data[0];
                String to = data[1];

                String query =
                        "MERGE (u1:User {id:'" + from + "'}) " +
                        "MERGE (u2:User {id:'" + to + "'}) " +
                        "MERGE (u1)-[:TRUSTS]->(u2)";

                graph.query(query);

                count++;

                if (count % 10000 == 0) {
                    System.out.println("Loaded " + count + " relationships...");
                }

                
            }

            long end = System.currentTimeMillis();

            System.out.println("Dataset Load Successful");
            System.out.println("Relationships Loaded : " + count);
            System.out.println("Time : " + (end - start) + " ms");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}