package com.notes.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Note implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;
    private String title;
    private String content;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public Note(String title, String content, Instant now){
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.content = content;
        this.createdAt = now;
        this.updatedAt = this.createdAt;
        this.deletedAt = null;
    }

    public Note(String id, String title, String content, Instant createdAt, Instant updatedAt, Instant deletedAt){
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public String getId() {return id;}
    public String getTitle() {return title;}
    public String getContent() {return content;}
    public Instant getCreatedAt() {return createdAt;}
    public Instant getUpdatedAt() {return updatedAt;}
    public Instant getDeletedAt() {return deletedAt;}

    public void updateTitle(String title) {
        this.title = title;
        this.updatedAt = Instant.now();
    }

    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = Instant.now();
    }

    public void markDelete(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public void clearDelete() {
        this.deletedAt = null;
    }

    @Override public boolean equals(Object o) {
        return (o instanceof Note n) && Objects.equals(id, n.id);
    }

    @Override public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        if (title == null || title.isBlank()) {
            return "(Untitled)";
        }
        return title;
    }
}