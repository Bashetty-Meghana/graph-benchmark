package com.benchmark.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    private final Properties properties;

    public Config() {
        properties = new Properties();

        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (input == null) {
                throw new RuntimeException("config.properties not found");
            }

            properties.load(input);

        } catch (IOException e) {
            throw new RuntimeException("Unable to load config.properties", e);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}