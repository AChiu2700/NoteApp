package com.notes.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class Section implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private String name;
    private final Instant createdAt;

    public Section(String name, Instant createdAt) {
        this(UUID.randomUUID().toString(), name, createdAt);
    }

    public Section(String id, String name, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return name == null || name.isBlank() ? "(Untitled Section)" : name;
    }
}
