package com.notes.model;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class NoteTest {

    private static Instant ISO(String s){ return Instant.parse(s); }

    @Test
    void updateTitle_valid_updatesTitle_andBumpsModifiedAt() {
        var n = new Note("Old", "Body", ISO("2025-01-01T00:00:00Z"));
        var before = n.getUpdatedAt();
        n.updateTitle("New Title");
        assertEquals("New Title", n.getTitle());
        assertTrue(n.getUpdatedAt().isAfter(before));
    }

    @Test
    void updateTitle_null_doesNotThrow() {
        var n = new Note("Old", "Body", ISO("2025-01-01T00:00:00Z"));
        assertDoesNotThrow(() -> n.updateTitle(null));
    }

    @Test
    void updateTitle_blank_doesNotThrow() {
        var n = new Note("Old", "Body", ISO("2025-01-01T00:00:00Z"));
        assertDoesNotThrow(() -> n.updateTitle(""));
        assertDoesNotThrow(() -> n.updateTitle("   "));
    }

    @Test
    void updateBody_valid_updatesBody_andBumpsModifiedAt() {
        var n = new Note("T", "Old body", ISO("2025-01-01T00:00:00Z"));
        var before = n.getUpdatedAt();
        n.updateContent("New body");
        assertEquals("New body", n.getContent());
        assertTrue(n.getUpdatedAt().isAfter(before));
    }

    @Test
    void updateBody_null_doesNotThrow() {
        var n = new Note("T", "Body", ISO("2025-01-01T00:00:00Z"));
        assertDoesNotThrow(() -> n.updateContent(null));
    }

    @Test
    void updateBody_blank_allowsEmptyBody_andBumpsModifiedAt() {
        var n = new Note("T", "Something", ISO("2025-01-01T00:00:00Z"));
        var before = n.getUpdatedAt();
        n.updateContent("");
        assertEquals("", n.getContent());
        assertTrue(n.getUpdatedAt().isAfter(before));
    }

    @Test
    void markDeleted_setsDeletedAt_toNow() {
        var n = new Note("T", "C", ISO("2025-01-01T00:00:00Z"));
        assertNull(n.getDeletedAt());
        var now = ISO("2025-01-02T00:00:00Z");
        n.markDeleted(now);
        assertEquals(now, n.getDeletedAt());
    }

    @Test
    void markDeleted_whenAlreadyDeleted_overwritesDeletedAt() {
        var n = new Note("T", "C", ISO("2025-01-01T00:00:00Z"));
        var first = ISO("2025-01-02T00:00:00Z");
        n.markDeleted(first);

        var second = ISO("2025-01-03T00:00:00Z");
        n.markDeleted(second);

        assertEquals(second, n.getDeletedAt());
    }

    @Test
    void clearDeleted_whenDeleted_unsetsDeletedAt() {
        var n = new Note("T", "C", ISO("2025-01-01T00:00:00Z"));
        n.markDeleted(ISO("2025-01-02T00:00:00Z"));
        n.clearDelete();
        assertNull(n.getDeletedAt());
    }

    @Test
    void clearDeleted_whenNotDeleted_noop() {
        var n = new Note("T", "C", ISO("2025-01-01T00:00:00Z"));
        n.clearDelete();
        assertNull(n.getDeletedAt());
    }
}
