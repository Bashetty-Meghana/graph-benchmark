package com.benchmark;

import com.benchmark.arangodb.ArangoDBBenchmarkRunner;
import com.benchmark.arangodb.ArangoDBManager;

public class App {

    public static void main(String[] args) {

        ArangoDBManager manager = new ArangoDBManager();

        manager.connect();

        ArangoDBBenchmarkRunner benchmark =
                new ArangoDBBenchmarkRunner(manager.getDatabase());

        benchmark.runAllBenchmarks();

        manager.disconnect();
    }
}