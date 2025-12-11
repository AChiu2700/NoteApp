package com.notes.repo;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.notes.model.Note;
import com.notes.model.NoteMemento;
import com.notes.util.Clock;

// Memento Pattern: Caretaker
public class Trash {

    private final int retentionDays;
    private final Clock clock;

    private final List<Note> deleted = new ArrayList<>();
    private final Map<String, NoteMemento> snapshots = new HashMap<>();

    public Trash(int retentionDays, Clock clock) {
        this.retentionDays = retentionDays;
        this.clock = clock;
    }

    public void add(Note note) {
        if (note == null || !note.isDeleted()) {
            return;
        }
        if (!deleted.contains(note)) {
            deleted.add(note);
        }
        snapshots.put(note.getId(), note.createMemento());
    }

    public void remove(Note note) {
        if (note == null) {
            return;
        }
        deleted.remove(note);
        snapshots.remove(note.getId());
    }

    public List<Note> listDeleted() {
        return new ArrayList<>(deleted);
    }

    public NoteMemento getSnapshot(String noteId) {
        return snapshots.get(noteId);
    }

    public void purgeExpired() {
        Instant now = clock.now();
        Iterator<Note> iterator = deleted.iterator();
        while (iterator.hasNext()) {
            Note note = iterator.next();
            Instant deletedAt = note.getDeletedAt();
            if (deletedAt != null) {
                long days = Duration.between(deletedAt, now).toDays();
                if (days >= retentionDays) {
                    iterator.remove();
                    snapshots.remove(note.getId());
                }
            }
        }
    }

    public int getRetentionDays() {
        return retentionDays;
    }

    public Clock getClock() {
        return clock;
    }
}
