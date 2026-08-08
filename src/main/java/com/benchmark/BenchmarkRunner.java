package com.benchmark;

import com.benchmark.database.DatabaseManager;
import com.benchmark.loader.DatasetLoader;
import org.neo4j.driver.Session;

public class BenchmarkRunner {

    private final DatabaseManager databaseManager;

    public BenchmarkRunner(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void loadDataset() {
        if (databaseManager.getDriver() != null) {
            DatasetLoader loader = new DatasetLoader(databaseManager.getDriver());
            loader.loadDataset("dataset/soc-Epinions1.txt");
        }
    }

    public void runAllBenchmarks() {
        System.out.println("\n========== BENCHMARK START ==========\n");
        benchmarkNodeLookup();
        benchmarkDegreeCount();
        benchmarkTraversal();
        benchmarkAggregation();
        benchmarkMixedWorkload();
        System.out.println("\n========== BENCHMARK END ==========\n");
    }

    public void benchmarkNodeLookup() {
        long start = System.currentTimeMillis();
        try (Session session = databaseManager.getDriver().session()) {
            session.run("MATCH (u:User {id:'0'}) RETURN u").consume();
        }
        long end = System.currentTimeMillis();
        System.out.println("Node Lookup : " + (end - start) + " ms");
    }

    public void benchmarkDegreeCount() {
        long start = System.currentTimeMillis();
        try (Session session = databaseManager.getDriver().session()) {
            session.run("MATCH (u:User {id:'0'})-[r]-() RETURN COUNT(r) AS degree").consume();
        }
        long end = System.currentTimeMillis();
        System.out.println("Degree Count : " + (end - start) + " ms");
    }

    public void benchmarkTraversal() {
        long start = System.currentTimeMillis();
        try (Session session = databaseManager.getDriver().session()) {
            session.run("MATCH (u:User {id:'0'})-[:TRUSTS]->(f) RETURN count(f)").consume();
        }
        long end = System.currentTimeMillis();
        System.out.println("Traversal : " + (end - start) + " ms");
    }

    public void benchmarkAggregation() {
        long start = System.currentTimeMillis();
        try (Session session = databaseManager.getDriver().session()) {
            session.run("MATCH (u)-[:TRUSTS]->() RETURN count(*)").consume();
        }
        long end = System.currentTimeMillis();
        System.out.println("Aggregation : " + (end - start) + " ms");
    }

    public void benchmarkMixedWorkload() {
        long start = System.currentTimeMillis();
        try (Session session = databaseManager.getDriver().session()) {
            session.run("MATCH (u:User {id:'0'}) RETURN u").consume();
            session.run("MATCH (u:User {id:'0'})-[:TRUSTS]->(f) RETURN count(f)").consume();
            session.run("MATCH (u)-[:TRUSTS]->() RETURN count(*)").consume();
        }
        long end = System.currentTimeMillis();
        System.out.println("Mixed Workload : " + (end - start) + " ms");
    }
}