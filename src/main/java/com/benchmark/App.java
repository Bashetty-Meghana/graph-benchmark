package com.benchmark;

import com.benchmark.BenchmarkUtils.BenchmarkResult;
import com.benchmark.BenchmarkUtils.MixedWorkloadResult;
import com.benchmark.arangodb.ArangoDBService;
import com.benchmark.cognodb.CognoDBService;
import com.benchmark.database.GraphDatabaseService;
import com.benchmark.falkordb.FalkorDBService;
import com.benchmark.memgraph.MemgraphService;
import com.benchmark.neo4j.Neo4jService;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class App {

    private static final String DATASET_PATH = "dataset/soc-Epinions1.txt";
    private static final int NODE_COUNT = 75879;
    private static final int REL_COUNT = 508837;

    public static void main(String[] args) {
        System.out.println("=========================================================================");
        System.out.println("      GRAPH DATABASE CLOUD BENCHMARK SUITE - WEXA AI TAKE-HOME           ");
        System.out.println("=========================================================================");
        System.out.println("Dataset: SNAP soc-Epinions1 (" + NODE_COUNT + " Nodes, " + REL_COUNT + " Relationships)");
        System.out.println("Resource Limit: 0.5 vCPU, 256MB-512MB RAM parity across all 5 databases");
        System.out.println("-------------------------------------------------------------------------\n");

        List<String> sampleNodes = BenchmarkUtils.loadSampleNodeIds(DATASET_PATH, 100);
        System.out.println("Sampled " + sampleNodes.size() + " starting nodes for randomized traversal/lookup testing.\n");

        List<GraphDatabaseService> services = new ArrayList<>();
        services.add(new CognoDBService());
        services.add(new Neo4jService());
        services.add(new FalkorDBService());
        services.add(new ArangoDBService());
        services.add(new MemgraphService());

        StringBuilder markdownTable = new StringBuilder();
        markdownTable.append("# Benchmark Results Matrix\n\n");
        markdownTable.append("| Platform | Ingest Wall Time (ms) | Ingest Throughput (rels/s) | Point Lookup p50/p95 (ms) | Indexed Lookup p50/p95 (ms) | 1-Hop Traversal p50/p95 (ms) | 2-Hop Traversal p50/p95 (ms) | 3-Hop Traversal p50/p95 (ms) | Aggregation p50/p95 (ms) | Mixed QPS (1/10/40 clients) |\n");
        markdownTable.append("| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |\n");

        for (GraphDatabaseService service : services) {
            System.out.println(">>> Running Benchmark for: " + service.getDatabaseName());
            service.connect();
            service.createIndexes();

            // 1. Ingest
            long ingestTimeMs = service.loadDataset(DATASET_PATH);
            double ingestRelsSec = ingestTimeMs > 0 ? (REL_COUNT * 1000.0 / ingestTimeMs) : 0;

            // 2. Lookups
            BenchmarkResult pointLookup = service.benchmarkPointLookup(sampleNodes);
            BenchmarkResult indexedLookup = service.benchmarkIndexedLookup(sampleNodes);

            // 3. Traversals
            BenchmarkResult trav1 = service.benchmarkTraversal(sampleNodes, 1);
            BenchmarkResult trav2 = service.benchmarkTraversal(sampleNodes, 2);
            BenchmarkResult trav3 = service.benchmarkTraversal(sampleNodes, 3);

            // 4. Aggregations
            BenchmarkResult agg = service.benchmarkAggregation();

            // 5. Mixed Concurrency
            MixedWorkloadResult conc1 = service.benchmarkMixedWorkload(1, 3, sampleNodes);
            MixedWorkloadResult conc10 = service.benchmarkMixedWorkload(10, 3, sampleNodes);
            MixedWorkloadResult conc40 = service.benchmarkMixedWorkload(40, 3, sampleNodes);

            service.disconnect();

            String row = String.format("| **%s** | %,d | %,.0f | %.2f / %.2f | %.2f / %.2f | %.2f / %.2f | %.2f / %.2f | %.2f / %.2f | %.2f / %.2f | %.0f / %.0f / %.0f |\n",
                    service.getDatabaseName(),
                    ingestTimeMs,
                    ingestRelsSec,
                    pointLookup.getP50Millis(), pointLookup.getP95Millis(),
                    indexedLookup.getP50Millis(), indexedLookup.getP95Millis(),
                    trav1.getP50Millis(), trav1.getP95Millis(),
                    trav2.getP50Millis(), trav2.getP95Millis(),
                    trav3.getP50Millis(), trav3.getP95Millis(),
                    agg.getP50Millis(), agg.getP95Millis(),
                    conc1.getQps(), conc10.getQps(), conc40.getQps()
            );

            markdownTable.append(row);
            System.out.println(row);
            System.out.println("-------------------------------------------------------------------------\n");
        }

        System.out.println("\n========== FULL RESULTS MATRIX SUMMARY ==========\n");
        System.out.println(markdownTable.toString());

        saveResultsToFile(markdownTable.toString());
    }

    private static void saveResultsToFile(String content) {
        try {
            File dir = new File("results");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            try (PrintWriter writer = new PrintWriter(new FileWriter("results/benchmark_matrix.md"))) {
                writer.print(content);
            }
            System.out.println("Results matrix successfully saved to results/benchmark_matrix.md");
        } catch (Exception e) {
            System.err.println("Failed to write results file: " + e.getMessage());
        }
    }
}