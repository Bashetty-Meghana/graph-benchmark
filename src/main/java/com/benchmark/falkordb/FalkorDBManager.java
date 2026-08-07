package com.benchmark.falkordb;

import com.falkordb.Driver;
import com.falkordb.FalkorDB;
import com.falkordb.Graph;

public class FalkorDBManager {

    private Driver driver;
    private Graph graph;

    public void connect() {

        driver = FalkorDB.driver("localhost", 6379);

        graph = driver.graph("social");

        System.out.println("Connected Successfully to FalkorDB!");
    }

    public void disconnect() {

        if (graph != null) {
            graph.close();
        }

        if (driver != null) {
            //driver.close();
        }

        System.out.println("Connection Closed!");
    }

    public Graph getGraph() {
        return graph;
    }
}