package com.example.notes.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Adapter Pattern: LocalStorage.java + InMemoryStorage.java
public class InMemoryLocalStorage implements LocalStorage, Serializable {

    private final Map<String, Object> storage = new ConcurrentHashMap<>();
    private final Path filePath;

    // Use this only on desktop, not Android
    public InMemoryLocalStorage() {
        this(Path.of("notes.dat"));
    }

    public InMemoryLocalStorage(Path filePath) {
        this.filePath = filePath;
        loadFromDisk();
    }

    @SuppressWarnings("unchecked")
    private synchronized void loadFromDisk() {
        if (!Files.exists(filePath)) return;
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(filePath))) {
            Object raw = ois.readObject();
            if (raw instanceof Map<?, ?> map) {
                storage.clear();
                storage.putAll((Map<String, Object>) map);
            }
        } catch (IOException | ClassNotFoundException e) {
            storage.clear();
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException ignore) { }
            System.err.println("LocalStorage: Failed to load from " + filePath);
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
            System.err.println("LocalStorage: Failed to save to " + filePath);
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
        saveToDisk();
    }

    @Override
    public synchronized void delete(String key) {
        storage.remove(key);
        saveToDisk();
    }
}