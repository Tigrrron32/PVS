package org.database;

import java.sql.*;
import java.util.concurrent.*;

public class CounterUpdate {
    private static final String URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASS = "yuliia";

    public static void main(String[] args) {
        try {
            ensureTableExists();

            ExecutorService executor;

            // Lost-Update
            resetDatabase();
            executor = Executors.newFixedThreadPool(10);
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < 10; i++) {
                executor.submit(() -> lostUpdate());
            }
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);
            System.out.println("Lost-Update Time: " + (System.currentTimeMillis() - startTime) + " ms");
            describeDatabase();

            // In-place Update
            resetDatabase();
            executor = Executors.newFixedThreadPool(10);
            startTime = System.currentTimeMillis();
            for (int i = 0; i < 10; i++) {
                executor.submit(() -> inPlaceUpdate());
            }
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);
            System.out.println("In-place Update Time: " + (System.currentTimeMillis() - startTime) + " ms");
            describeDatabase();

            // Row-level Locking
            resetDatabase();
            executor = Executors.newFixedThreadPool(10);
            startTime = System.currentTimeMillis();
            for (int i = 0; i < 10; i++) {
                executor.submit(() -> rowLevelLocking());
            }
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);
            System.out.println("Row-level Locking Time: " + (System.currentTimeMillis() - startTime) + " ms");
            describeDatabase();

            // Optimistic Concurrency Control
            resetDatabase();
            executor = Executors.newFixedThreadPool(10);
            startTime = System.currentTimeMillis();
            for (int i = 0; i < 10; i++) {
                executor.submit(() -> optimisticConcurrencyControl());
            }
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);
            System.out.println("Optimistic Concurrency Control Time: " + (System.currentTimeMillis() - startTime) + " ms");
            describeDatabase();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetDatabase() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement()) {

            conn.setAutoCommit(false);

            stmt.executeUpdate("DELETE FROM user_counter WHERE user_id = 1");
            stmt.executeUpdate("INSERT INTO user_counter (user_id, counter, version) VALUES (1, 0, 0)");
            conn.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void lostUpdate() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement()) {

            conn.setAutoCommit(false);

            for (int i = 0; i < 10000; i++) {
                ResultSet rs = stmt.executeQuery("SELECT counter FROM user_counter WHERE user_id = 1");
                if (rs.next()) {
                    int counter = rs.getInt(1);
                    stmt.executeUpdate("UPDATE user_counter SET counter = " + (counter + 1) + " WHERE user_id = 1");
                }
                conn.commit();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void inPlaceUpdate() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement()) {

            conn.setAutoCommit(false);

            for (int i = 0; i < 10000; i++) {
                stmt.executeUpdate("UPDATE user_counter SET counter = counter + 1 WHERE user_id = 1");
                conn.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void rowLevelLocking() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement()) {

            conn.setAutoCommit(false);

            for (int i = 0; i < 10000; i++) {
                ResultSet rs = stmt.executeQuery("SELECT counter FROM user_counter WHERE user_id = 1 FOR UPDATE");
                if (rs.next()) {
                    int counter = rs.getInt(1);
                    stmt.executeUpdate("UPDATE user_counter SET counter = " + (counter + 1) + " WHERE user_id = 1");
                }
                conn.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void optimisticConcurrencyControl() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement()) {

            conn.setAutoCommit(false);

            for (int i = 0; i < 10000; i++) {
                while (true) {
                    ResultSet rs = stmt.executeQuery("SELECT counter, version FROM user_counter WHERE user_id = 1");
                    if (rs.next()) {
                        int counter = rs.getInt(1);
                        int version = rs.getInt(2);
                        int rowsUpdated = stmt.executeUpdate("UPDATE user_counter SET counter = " + (counter + 1) + ", version = " + (version + 1) + " WHERE user_id = 1 AND version = " + version);
                        conn.commit();
                        if (rowsUpdated > 0) break;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void describeDatabase() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM user_counter WHERE user_id = 1");
            while(rs.next()) {
                System.out.println("USER_ID: " + rs.getInt("USER_ID") + ", Counter: " + rs.getInt("Counter") + ", Version: " + rs.getInt("Version"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void ensureTableExists() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement()) {

            conn.setAutoCommit(false);

            String createTableSQL = "CREATE TABLE IF NOT EXISTS user_counter (" +
                    "user_id SERIAL PRIMARY KEY," +
                    "counter INT NOT NULL DEFAULT 0," +
                    "version INT NOT NULL DEFAULT 0" +
                    ")";
            stmt.execute(createTableSQL);
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
