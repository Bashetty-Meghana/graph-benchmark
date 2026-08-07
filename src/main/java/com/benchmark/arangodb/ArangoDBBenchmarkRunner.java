package com.benchmark.arangodb;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDatabase;

public class ArangoDBBenchmarkRunner {

    private final ArangoDatabase database;

    public ArangoDBBenchmarkRunner(ArangoDatabase database) {
        this.database = database;
    }

    public void runAllBenchmarks() {

        System.out.println("\n========== ARANGODB BENCHMARK ==========\n");

        benchmarkNodeLookup();
        benchmarkDegreeCount();
        benchmarkTraversal();
        benchmarkAggregation();
        benchmarkMixedWorkload();

        System.out.println("\n========== BENCHMARK END ==========\n");
    }

    private void benchmarkNodeLookup() {

        long start = System.currentTimeMillis();

        ArangoCursor<Object> cursor =
                database.query(
                        "FOR u IN users FILTER u._key == '0' RETURN u",
                        Object.class
                );

        while (cursor.hasNext()) {
            cursor.next();
        }

        long end = System.currentTimeMillis();

        System.out.println("Node Lookup : " + (end - start) + " ms");
    }

    private void benchmarkDegreeCount() {

        long start = System.currentTimeMillis();

        ArangoCursor<Object> cursor =
                database.query(
                        "RETURN LENGTH(FOR t IN trusts FILTER t._from == 'users/0' RETURN t)",
                        Object.class
                );

        while (cursor.hasNext()) {
            cursor.next();
        }

        long end = System.currentTimeMillis();

        System.out.println("Degree Count : " + (end - start) + " ms");
    }

    private void benchmarkTraversal() {

        long start = System.currentTimeMillis();

        ArangoCursor<Object> cursor =
                database.query(
                        "FOR t IN trusts FILTER t._from == 'users/0' RETURN t",
                        Object.class
                );

        while (cursor.hasNext()) {
            cursor.next();
        }

        long end = System.currentTimeMillis();

        System.out.println("Traversal : " + (end - start) + " ms");
    }

    private void benchmarkAggregation() {

        long start = System.currentTimeMillis();

        ArangoCursor<Object> cursor =
                database.query(
                        "RETURN LENGTH(trusts)",
                        Object.class
                );

        while (cursor.hasNext()) {
            cursor.next();
        }

        long end = System.currentTimeMillis();

        System.out.println("Aggregation : " + (end - start) + " ms");
    }

    private void benchmarkMixedWorkload() {

        long start = System.currentTimeMillis();

        database.query(
                "FOR u IN users FILTER u._key == '0' RETURN u",
                Object.class
        );

        database.query(
                "FOR t IN trusts FILTER t._from == 'users/0' RETURN t",
                Object.class
        );

        database.query(
                "RETURN LENGTH(trusts)",
                Object.class
        );

        long end = System.currentTimeMillis();

        System.out.println("Mixed Workload : " + (end - start) + " ms");
    }
}
