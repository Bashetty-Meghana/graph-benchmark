package com.benchmark;

import com.benchmark.database.DatabaseManager;
import com.benchmark.loader.DatasetLoader;
import org.neo4j.driver.Session;

public class BenchmarkRunner {

    private final DatabaseManager databaseManager;

    public BenchmarkRunner(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    // Run this only when importing a fresh database
    public void loadDataset() {
        DatasetLoader loader = new DatasetLoader(databaseManager);
        loader.loadDataset();
    }

    // Runs every benchmark
    public void runAllBenchmarks() {

        System.out.println("\n========== BENCHMARK START ==========\n");

        benchmarkNodeLookup();

        benchmarkDegreeCount();

        benchmarkTraversal();

        benchmarkAggregation();

        benchmarkMixedWorkload();

        System.out.println("\n========== BENCHMARK END ==========\n");
    }

    // 1. Node Lookup
    public void benchmarkNodeLookup() {

        long start = System.currentTimeMillis();

        try (Session session = databaseManager.getDriver().session()) {

            session.run(
                    "MATCH (u:User {id:'0'}) RETURN u"
            ).consume();

        }

        long end = System.currentTimeMillis();

        System.out.println("Node Lookup : " + (end - start) + " ms");
    }

    // 2. Degree Count
    public void benchmarkDegreeCount() {

        long start = System.currentTimeMillis();

        try (Session session = databaseManager.getDriver().session()) {

            session.run(
                    "MATCH (u:User {id:'0'})-[r]-() RETURN COUNT(r) AS degree"
            ).consume();

        }

        long end = System.currentTimeMillis();

        System.out.println("Degree Count : " + (end - start) + " ms");
    }

    // 3. Traversal
    public void benchmarkTraversal() {

        long start = System.currentTimeMillis();

        try (Session session = databaseManager.getDriver().session()) {

            session.run(
                    "MATCH (u:User {id:'0'})-[:TRUSTS]->(f) RETURN count(f)"
            ).consume();

        }

        long end = System.currentTimeMillis();

        System.out.println("Traversal : " + (end - start) + " ms");
    }

    // 4. Aggregation
    public void benchmarkAggregation() {

        long start = System.currentTimeMillis();

        try (Session session = databaseManager.getDriver().session()) {

            session.run(
                    "MATCH (u)-[:TRUSTS]->() RETURN count(*)"
            ).consume();

        }

        long end = System.currentTimeMillis();

        System.out.println("Aggregation : " + (end - start) + " ms");
    }

    // 5. Mixed Workload
    public void benchmarkMixedWorkload() {

        long start = System.currentTimeMillis();

        try (Session session = databaseManager.getDriver().session()) {

            session.run(
                    "MATCH (u:User {id:'0'}) RETURN u"
            ).consume();

            session.run(
                    "MATCH (u:User {id:'0'})-[:TRUSTS]->(f) RETURN count(f)"
            ).consume();

            session.run(
                    "MATCH (u)-[:TRUSTS]->() RETURN count(*)"
            ).consume();

        }

        long end = System.currentTimeMillis();

        System.out.println("Mixed Workload : " + (end - start) + " ms");
    }
}