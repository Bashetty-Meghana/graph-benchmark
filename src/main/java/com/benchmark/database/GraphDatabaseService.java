package com.benchmark.database;

import com.benchmark.BenchmarkUtils.BenchmarkResult;
import com.benchmark.BenchmarkUtils.MixedWorkloadResult;
import java.util.List;

public interface GraphDatabaseService {

    String getDatabaseName();

    void connect();

    void disconnect();

    boolean isAvailable();

    void createIndexes();

    long loadDataset(String datasetFilePath);

    BenchmarkResult benchmarkPointLookup(List<String> nodeIds);

    BenchmarkResult benchmarkIndexedLookup(List<String> nodeIds);

    BenchmarkResult benchmarkTraversal(List<String> startNodes, int hops);

    BenchmarkResult benchmarkAggregation();

    MixedWorkloadResult benchmarkMixedWorkload(int concurrency, int durationSeconds, List<String> sampleNodeIds);

    String getFootprintInfo();
}