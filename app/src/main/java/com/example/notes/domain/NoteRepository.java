package com.example.notes.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.notes.data.Clock;
import com.example.notes.data.LocalStorage;
import com.example.notes.model.Note;

public class NoteRepository {
    private static final String KEY = "notes";

    private final LocalStorage storage;
    private final Clock clock;

    public NoteRepository(LocalStorage storage, Clock clock) {
        this.storage = storage;
        this.clock = clock;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Note> load() {
        Object raw = storage.read(KEY);
        if (raw instanceof Map<?, ?> map) {
            try {
                return (Map<String, Note>) map;
            } catch (ClassCastException ex) {
                return new HashMap<>();
            }
        }
        return new HashMap<>();
    }

    private void saveAll(Map<String, Note> notes) {
        storage.write(KEY, notes);
    }

    public Note createNote(String title, String content) {
        Map<String, Note> notes = load();
        Note note = new Note(title, content, clock.now());
        notes.put(note.getId(), note);
        saveAll(notes);
        return note;
    }

    public Note getNoteById(String id) {
        if (id == null) return null;
        Map<String, Note> notes = load();
        return notes.get(id);
    }

    public List<Note> listNotes() {
        Map<String, Note> notes = load();
        return notes.values().stream()
                .filter(n -> !n.isDeleted())
                .collect(Collectors.toList());
    }

    public List<Note> listDeleted() {
        Map<String, Note> notes = load();
        return notes.values().stream()
                .filter(Note::isDeleted)
                .collect(Collectors.toList());
    }

    public List<Note> listBySection(String sectionId) {
        Map<String, Note> notes = load();
        return notes.values().stream()
                .filter(n -> !n.isDeleted())
                .filter(n -> {
                    if (sectionId == null) {
                        return n.getSectionId() == null;
                    }
                    return sectionId.equals(n.getSectionId());
                })
                .collect(Collectors.toList());
    }

    public void save(Note note) {
        if (note == null) return;
        Map<String, Note> notes = load();
        notes.put(note.getId(), note);
        saveAll(notes);
    }

    public void moveToTrash(String noteId) {
        Map<String, Note> notes = load();
        Note note = notes.get(noteId);
        if (note == null) return;
        note.markDeleted(clock.now());
        notes.put(note.getId(), note);
        saveAll(notes);
    }

    public void restoreFromTrash(String noteId) {
        Map<String, Note> notes = load();
        Note note = notes.get(noteId);
        if (note == null) return;
        note.clearDelete();
        notes.put(note.getId(), note);
        saveAll(notes);
    }

    public void purgeDeletedNotes(String noteId) {
        Map<String, Note> notes = load();
        notes.remove(noteId);
        saveAll(notes);
    }
}
