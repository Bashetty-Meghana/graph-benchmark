package com.benchmark.database;

import com.benchmark.BenchmarkUtils;
import com.benchmark.BenchmarkUtils.BenchmarkResult;
import com.benchmark.BenchmarkUtils.MixedWorkloadResult;
import com.benchmark.config.Config;
import com.benchmark.loader.DatasetLoader;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

import java.util.List;

public class DatabaseManager implements GraphDatabaseService {

    private Driver driver;
    private Config config;

    public DatabaseManager() {
        config = new Config();
    }

    @Override
    public String getDatabaseName() {
        return "Generic Neo4j/Bolt Database";
    }

    @Override
    public void connect() {
        String database = config.getProperty("database");
        String uri = config.getProperty(database + ".uri");
        String username = config.getProperty(database + ".username");
        String password = config.getProperty(database + ".password");

        if (username == null) username = "";
        if (password == null) password = "";

        if (username.isEmpty() && password.isEmpty()) {
            driver = GraphDatabase.driver(uri, AuthTokens.none());
        } else {
            driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
        }

        System.out.println("Connected Successfully to " + database + "!");
    }

    @Override
    public void disconnect() {
        if (driver != null) {
            driver.close();
        }
        System.out.println("Connection Closed!");
    }

    @Override
    public boolean isAvailable() {
        return driver != null;
    }

    @Override
    public void createIndexes() {
        if (driver != null) {
            try (Session session = driver.session()) {
                session.run("CREATE INDEX user_id_idx IF NOT EXISTS FOR (u:User) ON (u.id)").consume();
            }
        }
    }

    @Override
    public long loadDataset(String datasetFilePath) {
        if (driver != null) {
            DatasetLoader loader = new DatasetLoader(driver);
            return loader.loadDataset(datasetFilePath);
        }
        return 0;
    }

    @Override
    public BenchmarkResult benchmarkPointLookup(List<String> nodeIds) {
        if (driver != null) {
            return BenchmarkUtils.measureParameterized(id -> {
                try (Session session = driver.session()) {
                    session.run("MATCH (u:User {id: $id}) RETURN u", org.neo4j.driver.Values.parameters("id", id)).consume();
                }
            }, nodeIds, 10, 100);
        }
        return new BenchmarkResult(0, 0);
    }

    @Override
    public BenchmarkResult benchmarkIndexedLookup(List<String> nodeIds) {
        if (driver != null) {
            return BenchmarkUtils.measureParameterized(id -> {
                try (Session session = driver.session()) {
                    session.run("MATCH (u:User) WHERE u.id = $id RETURN u", org.neo4j.driver.Values.parameters("id", id)).consume();
                }
            }, nodeIds, 10, 100);
        }
        return new BenchmarkResult(0, 0);
    }

    @Override
    public BenchmarkResult benchmarkTraversal(List<String> startNodes, int hops) {
        if (driver != null) {
            String cypher = hops == 1 ? "MATCH (u:User {id: $id})-[:TRUSTS]->(f) RETURN count(f)"
                    : hops == 2 ? "MATCH (u:User {id: $id})-[:TRUSTS*2]->(f) RETURN count(DISTINCT f)"
                    : "MATCH (u:User {id: $id})-[:TRUSTS*3]->(f) RETURN count(DISTINCT f)";

            return BenchmarkUtils.measureParameterized(id -> {
                try (Session session = driver.session()) {
                    session.run(cypher, org.neo4j.driver.Values.parameters("id", id)).consume();
                }
            }, startNodes, 10, 100);
        }
        return new BenchmarkResult(0, 0);
    }

    @Override
    public BenchmarkResult benchmarkAggregation() {
        if (driver != null) {
            return BenchmarkUtils.measure(() -> {
                try (Session session = driver.session()) {
                    session.run("MATCH (u:User)-[r:TRUSTS]->() RETURN u.id, count(r) AS deg ORDER BY deg DESC LIMIT 10").consume();
                }
            }, 5, 50);
        }
        return new BenchmarkResult(0, 0);
    }

    @Override
    public MixedWorkloadResult benchmarkMixedWorkload(int concurrency, int durationSeconds, List<String> sampleNodeIds) {
        if (driver != null) {
            return BenchmarkUtils.measureConcurrency(() -> {
                try (Session session = driver.session()) {
                    session.run("MATCH (u:User {id: '0'})-[:TRUSTS]->(f) RETURN count(f)").consume();
                }
            }, concurrency, durationSeconds);
        }
        return new MixedWorkloadResult(concurrency, 0, 0, 0, 0);
    }

    @Override
    public String getFootprintInfo() {
        return "Generic Neo4j Driver Connection";
    }

    public Driver getDriver() {
        return driver;
    }
}