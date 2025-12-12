package com.example.notes.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class Section implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private String name;
    private final Instant createdAt;
    private Instant deletedAt;

    public Section(String name, Instant createdAt) {
        this(UUID.randomUUID().toString(), name, createdAt, null);
    }

    public Section(String id, String name, Instant createdAt, Instant deletedAt) {
        this.id = id;
        this.name = (name == null) ? "" : name;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = (name == null) ? "" : name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void markDeleted(Instant now) {
        this.deletedAt = now;
    }

    public void clearDeleted() {
        this.deletedAt = null;
    }

    @Override
    public String toString() {
        if (name == null || name.isBlank()) {
            return "(Untitled Section)";
        }
        return name;
    }
}
