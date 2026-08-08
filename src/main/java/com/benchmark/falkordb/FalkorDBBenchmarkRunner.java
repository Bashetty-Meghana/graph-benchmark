package com.benchmark.falkordb;

import com.benchmark.BenchmarkUtils;
import com.benchmark.BenchmarkUtils.BenchmarkResult;
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
        BenchmarkResult result = BenchmarkUtils.measure(() ->
                graph.query("MATCH (u:User {id:'0'}) RETURN u")
        );
        System.out.println("Node Lookup : p50 = " + result.getP50Millis() + " ms, p95 = " + result.getP95Millis() + " ms");
    }

    public void benchmarkDegreeCount() {
        BenchmarkResult result = BenchmarkUtils.measure(() ->
                graph.query("MATCH (u:User {id:'0'})-[r]-() RETURN COUNT(r)")
        );
        System.out.println("Degree Count : p50 = " + result.getP50Millis() + " ms, p95 = " + result.getP95Millis() + " ms");
    }

    public void benchmarkTraversal() {
        BenchmarkResult result = BenchmarkUtils.measure(() ->
                graph.query("MATCH (u:User {id:'0'})-[:TRUSTS]->(f) RETURN COUNT(f)")
        );
        System.out.println("Traversal : p50 = " + result.getP50Millis() + " ms, p95 = " + result.getP95Millis() + " ms");
    }

    public void benchmarkAggregation() {
        BenchmarkResult result = BenchmarkUtils.measure(() ->
                graph.query("MATCH (u)-[:TRUSTS]->() RETURN COUNT(*)")
        );
        System.out.println("Aggregation : p50 = " + result.getP50Millis() + " ms, p95 = " + result.getP95Millis() + " ms");
    }

    public void benchmarkMixedWorkload() {
        BenchmarkResult result = BenchmarkUtils.measure(() -> {
            graph.query("MATCH (u:User {id:'0'}) RETURN u");
            graph.query("MATCH (u:User {id:'0'})-[:TRUSTS]->(f) RETURN COUNT(f)");
            graph.query("MATCH (u)-[:TRUSTS]->() RETURN COUNT(*)");
        });
        System.out.println("Mixed Workload : p50 = " + result.getP50Millis() + " ms, p95 = " + result.getP95Millis() + " ms");
    }
}