package com.notes.app;

import java.util.List;

import com.notes.model.Note;
import com.notes.model.NoteMemento;
import com.notes.repo.NoteRepository;
import com.notes.repo.Trash;
import com.notes.search.SearchIndex;
import com.notes.sort.SortOrder;
import com.notes.sort.SortPreference;

// Facade: AppController.java central point to cooridnate
public class AppController {
    private final NoteRepository noteRepository;
    private final Trash trash;
    private final SearchIndex searchIndex;
    private final SortPreference sortPreference;

    public AppController(NoteRepository noteRepository,
                         Trash trash,
                         SearchIndex searchIndex,
                         SortPreference sortPreference) {
        this.noteRepository = noteRepository;
        this.trash = trash;
        this.searchIndex = searchIndex;
        this.sortPreference = sortPreference;
    }

    public Note newNote() {
        return noteRepository.createNote("", "");
    }

    public Note openNote(String id) {
        return noteRepository.getNoteById(id);
    }

    public void editNote(String id, String title, String body) {
        Note note = noteRepository.getNoteById(id);
        if (note == null) {
            return;
        }
        note.updateTitle(title);
        note.updateContent(body);
        noteRepository.save(note);
    }

    public void deleteNote(String id) {
        Note note = noteRepository.getNoteById(id);
        if (note == null) {
            return;
        }
        noteRepository.moveToTrash(id);
        if (note.getDeletedAt() != null) {
            trash.add(note);
        }
    }

    public void restoreNote(String id) {
        Note note = noteRepository.getNoteById(id);
        if (note == null) {
            return;
        }
        NoteMemento snapshot = trash.getSnapshot(id);
        noteRepository.restoreFromTrash(id);
        if (snapshot != null) {
            note.restore(snapshot);
            noteRepository.save(note);
        }
        trash.remove(note);
    }

    public void emptyTrash(List<String> ids) {
        if (ids == null) {
            return;
        }
        for (String id : ids) {
            Note note = noteRepository.getNoteById(id);
            if (note != null) {
                trash.remove(note);
            }
            noteRepository.purgeDeletedNotes(id);
        }
    }

    public void setSortOrder(SortOrder sortOrder) {
        sortPreference.setSortOrder(sortOrder);
    }

    public List<Note> getListOfNotes() {
        return sortPreference.apply(noteRepository.listNotes());
    }

    public List<Note> getDeletedNotes() {
        return sortPreference.apply(noteRepository.listDeleted());
    }

    public List<Note> search(String query) {
        // If query is empty → just return the normal sorted list
        if (query == null || query.isBlank()) {
            return getListOfNotes();
        }

        // Index current notes
        List<Note> allNotes = noteRepository.listNotes();
        searchIndex.index(allNotes);

        // Do the search and apply sorting
        List<Note> results = searchIndex.search(query);
        return sortPreference.apply(results);
    }
}