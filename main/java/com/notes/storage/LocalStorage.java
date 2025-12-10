package com.notes.storage;

public interface LocalStorage{
    Object read(String key);
    void write(String key, Object value);
    void delete(String key);
}