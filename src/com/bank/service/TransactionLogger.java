package com.bank.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TransactionLogger {
    private final Path logFile = Paths.get("data", "transactions.log");
    private final Queue<String> buffer = new ConcurrentLinkedQueue<>();

    public TransactionLogger() throws IOException {
        Files.createDirectories(logFile.getParent());
    }

    // synchronized so lines from different threads do not get mixed up
    public synchronized void log(String message) {
        String line = LocalDateTime.now() + " | " + message;
        buffer.add(line);
        try {
            Files.write(logFile, List.of(line),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Could not write log: " + e.getMessage());
        }
    }

    public List<String> readHistory() throws IOException {
        return Files.readAllLines(logFile);
    }

    public int linesLoggedThisRun() {
        return buffer.size();
    }
}
