package com.notes.repo;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.notes.model.Note;
import com.notes.storage.LocalStorage;
import com.notes.util.Clock;

public class NoteRepository{
    private static final String KEY = "notes";
    private final LocalStorage storage;
    private final Clock clock;

    @SuppressWarnings("unchecked")
    private Map<String, Note> load() {
        Object v = storage.read(KEY);
        return (v instanceof Map) ? (Map<String, Note>) v : new HashMap<>();
    }

    private void saveAll(Map<String, Note> notes) {
        storage.write(KEY, notes);
    }

    public NoteRepository(LocalStorage storage, Clock clock){
        this.storage = storage;
        this.clock = clock;
        saveAll (load()) ;
    }

    public Note createNote(String title, String content){
        Map<String, Note> notes = load();
        Note note = new Note(title, content, clock.now());
        notes.put(note.getId(), note);
        saveAll(notes);
        return note;
    }

    public Note getNoteById(String id){
        Map<String, Note> notes = load();
        return notes.get(id);
    }

    public List<Note> listNotes() {
        return load().values().stream()
                .filter(n -> n.getDeletedAt() == null)
                .sorted(Comparator.comparing(Note::getUpdatedAt).reversed())
                .collect(Collectors.toList());  
    }

    public List<Note> listDeleted() {
        return load().values().stream()
                .filter(n -> n.getDeletedAt() != null)
                .collect(Collectors.toList());
    }

    public Note save(Note note){
        Map<String, Note> notes = load();
        Note current = notes.get(note.getId());
        if (current != null) {
            current = new Note(
                    current.getId(),
                    note.getTitle(),
                    note.getContent(),
                    current.getCreatedAt(),
                    clock.now(),
                    current.getDeletedAt()
            );
            notes.put(current.getId(), current);
            saveAll(notes);
            return current;
        }
        notes.put(note.getId(), note);
        saveAll(notes);
        return note;
    }

    public void moveToTrash(String noteID){
        Map<String,Note> notes = load();
        Note note = notes.get(noteID);
        if (note == null) {return;}
        note.markDelete(clock.now());
        notes.put(note.getId(), note);
        saveAll(notes);
    }

    public void restoreFromTrash(String noteID){
        Map<String,Note> notes = load();
        Note note = notes.get(noteID);
        if (note == null) {return;}
        note.clearDelete();
        notes.put(note.getId(), note);
        saveAll(notes);
    }

    public void purgeDeletedNotes(String noteID){
        Map<String,Note> notes = load();
        notes.remove(noteID);
        saveAll(notes);
    }
}