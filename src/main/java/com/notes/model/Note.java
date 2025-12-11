package com.notes.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

// Memento Pattern: Note.java + NoteMemento.java + Trash.java
public class Note implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private String title;
    private String content;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private String sectionId;

    public Note(String title, String content, Instant now) {
        this(UUID.randomUUID().toString(), title, content, now, now, null, null);
    }

    public Note(String id,
                String title,
                String content,
                Instant createdAt,
                Instant updatedAt,
                Instant deletedAt) {
        this(id, title, content, createdAt, updatedAt, deletedAt, null);
    }

    public Note(String id,
                String title,
                String content,
                Instant createdAt,
                Instant updatedAt,
                Instant deletedAt,
                String sectionId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.sectionId = sectionId;
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

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
        this.updatedAt = Instant.now();
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

    public NoteMemento createMemento() {
        return new NoteMemento(id, title, content, createdAt, updatedAt, deletedAt, sectionId);
    }

    // Memento Pattern: restore from snapshot
    public void restore(NoteMemento memento) {
        if (memento == null || !Objects.equals(this.id, memento.getId())) {
            return;
        }
        this.title = memento.getTitle();
        this.content = memento.getContent();
        this.updatedAt = memento.getUpdatedAt();
        this.sectionId = memento.getSectionId();
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
