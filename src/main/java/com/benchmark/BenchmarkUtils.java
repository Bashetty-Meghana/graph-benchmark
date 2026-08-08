package com.benchmark;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BenchmarkUtils {

    public static final int DEFAULT_WARMUP_RUNS = 10;
    public static final int DEFAULT_MEASURED_RUNS = 100;

    public static BenchmarkResult measure(Runnable operation) {
        return measure(operation, DEFAULT_WARMUP_RUNS, DEFAULT_MEASURED_RUNS);
    }

    public static BenchmarkResult measure(Runnable operation, int warmupRuns, int measuredRuns) {
        // Warm-up
        for (int i = 0; i < warmupRuns; i++) {
            try {
                operation.run();
            } catch (Exception ignored) {}
        }

        // Measurement
        List<Long> timesMicros = new ArrayList<>(measuredRuns);
        for (int i = 0; i < measuredRuns; i++) {
            long start = System.nanoTime();
            try {
                operation.run();
            } catch (Exception ignored) {}
            long end = System.nanoTime();
            timesMicros.add((end - start) / 1_000);
        }

        Collections.sort(timesMicros);
        long p50 = percentile(timesMicros, 50);
        long p95 = percentile(timesMicros, 95);

        return new BenchmarkResult(p50, p95);
    }

    public static <T> BenchmarkResult measureParameterized(Consumer<T> operation, List<T> inputs, int warmupRuns, int measuredRuns) {
        if (inputs == null || inputs.isEmpty()) {
            return new BenchmarkResult(0, 0);
        }

        Random rand = new Random(42);

        // Warm-up
        for (int i = 0; i < warmupRuns; i++) {
            T input = inputs.get(rand.nextInt(inputs.size()));
            try {
                operation.accept(input);
            } catch (Exception ignored) {}
        }

        // Measurement
        List<Long> timesMicros = new ArrayList<>(measuredRuns);
        for (int i = 0; i < measuredRuns; i++) {
            T input = inputs.get(rand.nextInt(inputs.size()));
            long start = System.nanoTime();
            try {
                operation.accept(input);
            } catch (Exception ignored) {}
            long end = System.nanoTime();
            timesMicros.add((end - start) / 1_000);
        }

        Collections.sort(timesMicros);
        long p50 = percentile(timesMicros, 50);
        long p95 = percentile(timesMicros, 95);

        return new BenchmarkResult(p50, p95);
    }

    public static MixedWorkloadResult measureConcurrency(Runnable task, int concurrency, int durationSeconds) {
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        AtomicBoolean running = new AtomicBoolean(true);
        AtomicLong opsCounter = new AtomicLong(0);
        List<Long> latenciesMicros = Collections.synchronizedList(new ArrayList<>());

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < concurrency; i++) {
            executor.submit(() -> {
                while (running.get()) {
                    long start = System.nanoTime();
                    try {
                        task.run();
                        opsCounter.incrementAndGet();
                    } catch (Exception ignored) {}
                    long end = System.nanoTime();
                    if (latenciesMicros.size() < 10000) {
                        latenciesMicros.add((end - start) / 1_000);
                    }
                }
            });
        }

        try {
            Thread.sleep(durationSeconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        running.set(false);
        executor.shutdown();
        try {
            executor.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}

        long endTime = System.currentTimeMillis();
        double actualDurationSec = (endTime - startTime) / 1000.0;
        long totalOps = opsCounter.get();
        double qps = actualDurationSec > 0 ? totalOps / actualDurationSec : 0;

        List<Long> sorted;
        synchronized (latenciesMicros) {
            sorted = new ArrayList<>(latenciesMicros);
        }
        Collections.sort(sorted);

        long p50 = percentile(sorted, 50);
        long p95 = percentile(sorted, 95);

        return new MixedWorkloadResult(concurrency, qps, p50, p95, totalOps);
    }

    public static List<String> loadSampleNodeIds(String filePath, int sampleSize) {
        Set<String> uniqueNodes = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    uniqueNodes.add(parts[0]);
                    uniqueNodes.add(parts[1]);
                }
                if (uniqueNodes.size() >= sampleSize * 5) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: Failed to load dataset sample: " + e.getMessage());
        }

        List<String> nodes = new ArrayList<>(uniqueNodes);
        Collections.shuffle(nodes, new Random(42));
        if (nodes.size() > sampleSize) {
            return nodes.subList(0, sampleSize);
        }
        if (nodes.isEmpty()) {
            nodes.add("0");
        }
        return nodes;
    }

    private static long percentile(List<Long> values, int percentile) {
        if (values == null || values.isEmpty()) return 0;
        int index = (int) Math.ceil(percentile / 100.0 * values.size()) - 1;
        if (index < 0) index = 0;
        if (index >= values.size()) index = values.size() - 1;
        return values.get(index);
    }

    public static class BenchmarkResult {
        private final long p50Micros;
        private final long p95Micros;

        public BenchmarkResult(long p50Micros, long p95Micros) {
            this.p50Micros = p50Micros;
            this.p95Micros = p95Micros;
        }

        public long getP50Micros() { return p50Micros; }
        public long getP95Micros() { return p95Micros; }
        public double getP50Millis() { return p50Micros / 1000.0; }
        public double getP95Millis() { return p95Micros / 1000.0; }
    }

    public static class MixedWorkloadResult {
        private final int concurrency;
        private final double qps;
        private final long p50Micros;
        private final long p95Micros;
        private final long totalOperations;

        public MixedWorkloadResult(int concurrency, double qps, long p50Micros, long p95Micros, long totalOperations) {
            this.concurrency = concurrency;
            this.qps = qps;
            this.p50Micros = p50Micros;
            this.p95Micros = p95Micros;
            this.totalOperations = totalOperations;
        }

        public int getConcurrency() { return concurrency; }
        public double getQps() { return qps; }
        public double getP50Millis() { return p50Micros / 1000.0; }
        public double getP95Millis() { return p95Micros / 1000.0; }
        public long getTotalOperations() { return totalOperations; }
    }
}