package com.benchmark.falkordb;

import com.falkordb.Graph;

public class FalkorDBBenchmarkRunner {

    private final Graph graph;

    public FalkorDBBenchmarkRunner(Graph graph) {
        this.graph = graph;
    }

    public void runAllBenchmarks() {

        System.out.println("\n========== FALKORDB BENCHMARK ==========\n");

        benchmarkNodeLookup();
        benchmarkDegreeCount();
        benchmarkTraversal();
        benchmarkAggregation();
        benchmarkMixedWorkload();

        System.out.println("\n========== BENCHMARK END ==========\n");
    }

    public void benchmarkNodeLookup() {

        long start = System.currentTimeMillis();

        graph.query("MATCH (u:User {id:'0'}) RETURN u");

        long end = System.currentTimeMillis();

        System.out.println("Node Lookup : " + (end - start) + " ms");
    }

    public void benchmarkDegreeCount() {

        long start = System.currentTimeMillis();

        graph.query("MATCH (u:User {id:'0'})-[r]-() RETURN COUNT(r)");

        long end = System.currentTimeMillis();

        System.out.println("Degree Count : " + (end - start) + " ms");
    }

    public void benchmarkTraversal() {

        long start = System.currentTimeMillis();

        graph.query("MATCH (u:User {id:'0'})-[:TRUSTS]->(f) RETURN COUNT(f)");

        long end = System.currentTimeMillis();

        System.out.println("Traversal : " + (end - start) + " ms");
    }

    public void benchmarkAggregation() {

        long start = System.currentTimeMillis();

        graph.query("MATCH (u)-[:TRUSTS]->() RETURN COUNT(*)");

        long end = System.currentTimeMillis();

        System.out.println("Aggregation : " + (end - start) + " ms");
    }

    public void benchmarkMixedWorkload() {

        long start = System.currentTimeMillis();

        graph.query("MATCH (u:User {id:'0'}) RETURN u");
        graph.query("MATCH (u:User {id:'0'})-[:TRUSTS]->(f) RETURN COUNT(f)");
        graph.query("MATCH (u)-[:TRUSTS]->() RETURN COUNT(*)");

        long end = System.currentTimeMillis();

        System.out.println("Mixed Workload : " + (end - start) + " ms");
    }
}
