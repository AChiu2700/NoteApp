package com.notes.app;

import java.util.List;

import com.notes.model.Note;
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
    private SortPreference sortPreference;

    public AppController(NoteRepository noteRepository, Trash trash, SearchIndex searchIndex, SortPreference sortPreference) {
        this.noteRepository = noteRepository;
        this.trash = trash;
        this.searchIndex = searchIndex;
        this.sortPreference = sortPreference;
    }

    public Note newNote(){
        Note note = noteRepository.createNote("", "");
        return note;
    }

    public Note openNote(String id){
        return noteRepository.getNoteById(id);
    }

    public Note editNote(String id, String newTitle, String newContent){
        Note note = noteRepository.getNoteById(id);
        if (note == null) throw new IllegalArgumentException("Note not found");
        if (newTitle != null) note.updateTitle(newTitle);
        if (newContent != null) note.updateContent(newContent);
        note = noteRepository.save(note);
        return note;
    }

    public void deleteNote(String id){
        noteRepository.moveToTrash(id);
        Note note = noteRepository.getNoteById(id);
        if (note != null) {
            trash.add(note);
        }
        trash.autoPurge();
    }

    public void restoreNote(String id){
        noteRepository.restoreFromTrash(id);

        Note note = noteRepository.getNoteById(id);
        if (note != null) {
            trash.remove(note);
        }
    }

    public void emptyTrash(List<String> ids){
        for (String id : ids) {
            Note note = noteRepository.getNoteById(id);
            if (note != null) {
                trash.remove(note);
            }
            noteRepository.purgeDeletedNotes(id);
        }
    }

    public void setSortOrder(SortOrder sortOrder){
        sortPreference.setSortOrder(sortOrder);
    }

    public List<Note> getListOfNotes(){
        return sortPreference.apply(noteRepository.listNotes());
    }

    public List<Note> getDeletedNotes() {
        return noteRepository.listDeleted();
    }

    public List<Note> search(String query) {
        // If query is empty → just return the normal sorted list
        if (query == null || query.isBlank()) {
            return getListOfNotes();
        }

        // Index current notes
        var allNotes = noteRepository.listNotes();
        searchIndex.index(allNotes);

        // Do the search and apply sorting
        List<Note> results = searchIndex.search(query);
        return sortPreference.apply(results);
    }
}