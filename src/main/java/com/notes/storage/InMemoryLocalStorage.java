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
        this("notes-storage.ser");
    }

    public InMemoryLocalStorage(String filename) {
        this.filePath = Path.of(filename);
        loadFromDisk();
    }

    @SuppressWarnings("unchecked")
    private void loadFromDisk() {
        if (!Files.exists(filePath)) {
            return; // no existing file → start empty
        }

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(filePath))) {
            Object obj = ois.readObject();
            if (obj instanceof Map<?, ?> map) {
                storage.clear();
                storage.putAll((Map<String, Object>) map);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveToDisk() {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(filePath))) {
            oos.writeObject(storage);
        } catch (IOException e) {
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
