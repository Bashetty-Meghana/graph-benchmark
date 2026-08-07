# Graph Database Benchmark

## Project Overview

This project benchmarks the performance of multiple graph databases using the same dataset and workload. The benchmark compares data loading performance and query execution time across different graph database systems.

## Graph Databases Used

- CognoDB (Neo4j)
- Memgraph
- FalkorDB
- ArangoDB

## Technologies

- Java
- Maven
- Neo4j Java Driver
- FalkorDB Java Driver
- ArangoDB Java Driver
- Docker

## Dataset

Dataset used:

- soc-Epinions1.txt

The dataset contains trust relationships between users and is used to benchmark graph database performance.

## Features

- Database Connection
- Dataset Loading
- Node Lookup Benchmark
- Degree Count Benchmark
- Traversal Benchmark
- Aggregation Benchmark
- Mixed Workload Benchmark

## Project Structure

```
graph-benchmark
│
├── dataset
├── results
├── src
│   ├── arangodb
│   ├── falkordb
│   ├── database
│   ├── loader
│   └── benchmark
├── pom.xml
└── README.md
```

## Prerequisites

- Java 17 or later
- Maven
- Docker Desktop

## Running the Project

### Compile

```bash
mvn clean compile
```

### Run

```bash
mvn exec:java -Dexec.mainClass=com.benchmark.App
```

## Benchmark Results

### CognoDB (Neo4j)

| Operation | Time |
|----------|------|
| Node Lookup | 2349 ms |
| Degree Count | 591 ms |
| Traversal | 554 ms |
| Aggregation | 562 ms |
| Mixed Workload | 2620 ms |

### Memgraph

| Operation | Time |
|----------|------|
| Node Lookup | 267 ms |
| Degree Count | 19 ms |
| Traversal | 11 ms |
| Aggregation | 6 ms |
| Mixed Workload | 20 ms |

### FalkorDB

| Operation | Time |
|----------|------|
| Node Lookup | 343 ms |
| Degree Count | 151 ms |
| Traversal | 24 ms |
| Aggregation | 137 ms |
| Mixed Workload | 152 ms |

### ArangoDB

| Operation | Time |
|----------|------|
| Node Lookup | 78 ms |
| Degree Count | 339 ms |
| Traversal | 84 ms |
| Aggregation | 6 ms |
| Mixed Workload | 94 ms |

## Dataset Loading

| Database | Relationships | Time |
|----------|---------------|------|
| FalkorDB | 508,837 | 67 minutes |
| ArangoDB | 508,837 | 37 minutes 41 seconds |

## Author

**Bashetty Meghana**

B.Tech Information Technology

Malla Reddy University
