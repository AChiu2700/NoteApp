package com.notes.repo;

import com.notes.model.Note;
import com.notes.util.Clock;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Trash {
    private final int retentionDays;
    private final Clock clock;
    private final List<Note> deleted = new ArrayList<>();

    public Trash(int retentionDays, Clock clock) {
        this.retentionDays = retentionDays;
        this.clock = clock;
    }

    public void add(Note note) {
        if (note.getDeletedAt() != null) {
            deleted.add(note);
        }
    }

    public boolean remove(Note note) {
        return deleted.remove(note);
    }

    public List<Note> listDeletedNotes() {
        return new ArrayList<>(deleted);
    }

    public void autoPurge() {
        purgeExpired(clock.now().minus(Duration.ofDays(30)));
    }

    public void purgeExpired(Instant now) {
        Iterator<Note> iterator = deleted.iterator();
        while (iterator.hasNext()) {
            Note note = iterator.next();
            Instant deletedAt = note.getDeletedAt();
            if (deletedAt != null) {
                long days = Duration.between(deletedAt, now).toDays();
                if (days >= retentionDays) {
                    iterator.remove();
                }
            }
        }
    }

    public int getRetentionDays() { return retentionDays; }
    public Clock getClock() { return clock; }
}
