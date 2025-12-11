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
    private final SectionRepository sectionRepository;

    private String activeSectionId;

    public AppController(NoteRepository noteRepository,
                         Trash trash,
                         SearchIndex searchIndex,
                         SortPreference sortPreference,
                         SectionRepository sectionRepository) {
        this.noteRepository = noteRepository;
        this.trash = trash;
        this.searchIndex = searchIndex;
        this.sortPreference = sortPreference;
        this.sectionRepository = sectionRepository;
        this.activeSectionId = null;
    }

    public Note newNote() {
        Note note = noteRepository.createNote("", "");
        if (activeSectionId != null) {
            note.setSectionId(activeSectionId);
            noteRepository.save(note);
        }
        return note;
    }

    public Note openNote(String id) {
        return noteRepository.getNoteById(id);
    }

    public void editNote(String id, String title, String body) {
        Note note = noteRepository.getNoteById(id);
        if (note == null) return;
        note.updateTitle(title);
        note.updateContent(body);
        noteRepository.save(note);
    }

    public void deleteNote(String id) {
        Note note = noteRepository.getNoteById(id);
        if (note == null) return;

        noteRepository.moveToTrash(id);
        if (note.getDeletedAt() != null) {
            trash.add(note);
        }
    }

    public void restoreNote(String id) {
        Note note = noteRepository.getNoteById(id);
        if (note == null) return;

        NoteMemento snapshot = trash.getSnapshot(id);
        noteRepository.restoreFromTrash(id);

        if (snapshot != null) {
            note.restore(snapshot);
            noteRepository.save(note);
        }

        trash.remove(note);
    }

    public void emptyTrash(List<String> ids) {
        if (ids == null) return;

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
        if (query == null || query.isBlank()) {
            return getListOfNotes();
        }

        List<Note> allNotes = getListOfNotes();
        searchIndex.index(allNotes);

        List<Note> results = searchIndex.search(query);
        return sortPreference.apply(results);
    }

    // Sections API

    public List<Section> listSections() {
        return sectionRepository.listSections();
    }

    public Section createSection(String name) {
        Section section = sectionRepository.createSection(name);
        if (activeSectionId == null) {
            activeSectionId = section.getId();
        }
        return section;
    }

    public void renameSection(String id, String newName) {
        sectionRepository.renameSection(id, newName);
    }

    public void deleteSection(String id) {
        if (id == null) return;
        sectionRepository.deleteSection(id);
        if (id.equals(activeSectionId)) {
            activeSectionId = null;
        }
    }

    public void setActiveSection(String sectionId) {
        this.activeSectionId = sectionId;
    }

    public Section getActiveSection() {
        if (activeSectionId == null) return null;
        return sectionRepository.getSection(activeSectionId);
    }

    public void moveNoteToSection(String noteId, String sectionId) {
        Note note = noteRepository.getNoteById(noteId);
        if (note == null) return;
        note.setSectionId(sectionId);
        noteRepository.save(note);
    }
}
