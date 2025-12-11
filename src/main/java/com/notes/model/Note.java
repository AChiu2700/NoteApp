package com.notes.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

// Memento Pattern: Originator for Note.java + Trash.java + NoteMemento.java
public class Note implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private String title;
    private String content;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public Note(String title, String content, Instant now) {
        this(UUID.randomUUID().toString(), title, content, now, now, null);
    }

    public Note(String id,
                String title,
                String content,
                Instant createdAt,
                Instant updatedAt,
                Instant deletedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void updateTitle(String title) {
        this.title = title;
        this.updatedAt = Instant.now();
    }

    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = Instant.now();
    }

    public void markDeleted(Instant now) {
        this.deletedAt = now;
        this.updatedAt = now;
    }

    public void clearDelete() {
        this.deletedAt = null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    // Memento Pattern: create memento snapshot
    public NoteMemento createMemento() {
        return new NoteMemento(id, title, content, createdAt, updatedAt, deletedAt);
    }

    // Memento Pattern: restore from snapshot (but keep note active)
    public void restore(NoteMemento memento) {
        if (memento == null || !Objects.equals(this.id, memento.getId())) {
            return;
        }
        this.title = memento.getTitle();
        this.content = memento.getContent();
        this.updatedAt = memento.getUpdatedAt();
        // do NOT restore deletedAt; it stays cleared so note is active after restore
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Note other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
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
