package com.example.notes.data;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.notes.model.Note;

class TrashTest {

    private Trash trash;
    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = () -> Instant.parse("2025-01-31T00:00:00Z");
        trash = new Trash(30, clock);
    }

    @Test
    void add_onlyStoresNotesThatAreMarkedDeleted() {
        Note n1 = new Note("T1", "C1", clock.now());
        Note n2 = new Note("T2", "C2", clock.now());
        n2.markDeleted(clock.now());

        trash.add(n1);
        assertTrue(trash.listDeleted().isEmpty());

        trash.add(n2);
        assertEquals(1, trash.listDeleted().size());
        assertEquals("T2", trash.listDeleted().get(0).getTitle());
    }

    @Test
    void purgeExpired_removesNotesBeyondRetentionDays() {
        Note old = new Note("Old", "C", clock.now().minus(40, ChronoUnit.DAYS));
        old.markDeleted(old.getCreatedAt());
        trash.add(old);

        Note recent = new Note("New", "C", clock.now().minus(5, ChronoUnit.DAYS));
        recent.markDeleted(recent.getCreatedAt());
        trash.add(recent);

        // uses internal clock in Trash
        trash.purgeExpired();

        var remaining = trash.listDeleted();
        assertEquals(1, remaining.size());
        assertEquals("New", remaining.get(0).getTitle());
    }
}
