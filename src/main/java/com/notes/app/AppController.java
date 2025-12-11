package com.notes.app;

import java.util.List;

import com.notes.model.Note;
import com.notes.model.NoteMemento;
import com.notes.model.Section;
import com.notes.repo.NoteRepository;
import com.notes.repo.SectionRepository;
import com.notes.repo.Trash;
import com.notes.search.SearchIndex;
import com.notes.sort.SortOrder;
import com.notes.sort.SortPreference;

// Facade Pattern: AppController hides backend complexity from UI
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
        if (n.getDeletedAt() != null) {
            trash.add(n);
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
            Note n = noteRepository.getNoteById(id);
            if (n != null) trash.remove(n);
            noteRepository.purgeDeletedNotes(id);
        }
    }

    public void setSortOrder(SortOrder order) {
        sortPreference.setSortOrder(order);
    }

    public List<Note> getListOfNotes() {
        List<Note> notes;
        if (activeSectionId == null) {
            notes = noteRepository.listNotes();
        } else {
            notes = noteRepository.listBySection(activeSectionId);
        }
        return sortPreference.apply(notes);
    }

    public List<Note> getDeletedNotes() {
        List<Note> notes = noteRepository.listDeleted();
        return sortPreference.apply(notes);
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