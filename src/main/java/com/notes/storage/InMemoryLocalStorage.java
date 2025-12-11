package com.notes.storage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Adapter Pattern: LocalStorage.java + InMemoryStorage.java
public class InMemoryLocalStorage implements LocalStorage {

    private final Map<String, Object> storage = new ConcurrentHashMap<>();
    private final Path filePath;

    public InMemoryLocalStorage() {
        this(Path.of("notes.dat"));
    }

    public InMemoryLocalStorage(Path filePath) {
        this.filePath = filePath;
        loadFromDisk();
    }

    public InMemoryLocalStorage(String filename) {
        this(Path.of(filename));
    }

    @SuppressWarnings("unchecked")
    private synchronized void loadFromDisk() {
        if (!Files.exists(filePath)) {
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(filePath))) {
            Object raw = ois.readObject();
            if (raw instanceof Map<?, ?> map) {
                storage.clear();
                storage.putAll((Map<String, Object>) map);
            }
        } catch (IOException | ClassNotFoundException e) {
            // If something is corrupted, clear AND delete the bad file
            storage.clear();
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException ignore) {
                // best-effort only
            }
            // Log so we see what went wrong
            System.err.println("[LocalStorage] Failed to load from " + filePath + ": " + e);
            e.printStackTrace();
        }
    }

    private synchronized void saveToDisk() {
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(filePath))) {
                oos.writeObject(storage);
            }
        } catch (IOException e) {
            // IMPORTANT: log this so we see NotSerializableException etc.
            System.err.println("[LocalStorage] Failed to save to " + filePath + ": " + e);
            e.printStackTrace();
        }
    }

    @Override
    public synchronized Object read(String key) {
        return storage.get(key);
    }

    @Override
    public synchronized void write(String key, Object value) {
        storage.put(key, value);
        saveToDisk(); // persist every change
    }

    @Override
    public synchronized void delete(String key) {
        storage.remove(key);
        saveToDisk(); // update file after delete
    }
}
