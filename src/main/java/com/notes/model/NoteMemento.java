package com.notes.model;

import java.io.Serializable;
import java.time.Instant;

public class NoteMemento implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String title;
    private final String content;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant deletedAt;
    private final String sectionId;

    public NoteMemento(String id,
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
}
