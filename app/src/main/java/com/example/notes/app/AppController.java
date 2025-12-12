package com.example.notes.app;

import java.util.List;

import com.example.notes.data.Trash;
import com.example.notes.domain.NoteRepository;
import com.example.notes.domain.SearchIndex;
import com.example.notes.domain.SectionRepository;
import com.example.notes.domain.SortOrder;
import com.example.notes.domain.SortPreference;
import com.example.notes.model.Note;
import com.example.notes.model.NoteMemento;
import com.example.notes.model.Section;

// Facade Pattern: AppController hides backend complexity from UI
public class AppController {

    private final NoteRepository noteRepository;
    private final Trash trash;
    private final SearchIndex searchIndex;
    private final SortPreference sortPreference;
    private final SectionRepository sectionRepository;
    private final MoveNoteToSection moveNoteService;

    private String activeSectionId;

    public AppController(
            NoteRepository noteRepository,
            Trash trash,
            SearchIndex searchIndex,
            SortPreference sortPreference,
            SectionRepository sectionRepository
    ) {
        this.noteRepository = noteRepository;
        this.trash = trash;
        this.searchIndex = searchIndex;
        this.sortPreference = sortPreference;
        this.sectionRepository = sectionRepository;
        this.moveNoteService = new MoveNoteToSection(noteRepository, sectionRepository);
        this.activeSectionId = null;
    }

    public Note newNote() {
        Note n = noteRepository.createNote("", "");
        if (activeSectionId != null) {
            n.setSectionId(activeSectionId);
            noteRepository.save(n);
        }
        return n;
    }

    public Note openNote(String id) {
        return noteRepository.getNoteById(id);
    }

    public void editNote(String id, String title, String body) {
        Note n = noteRepository.getNoteById(id);
        if (n == null) return;
        n.updateTitle(title);
        n.updateContent(body);
        noteRepository.save(n);
    }

    public void deleteNote(String id) {
        Note n = noteRepository.getNoteById(id);
        if (n == null) return;

        noteRepository.moveToTrash(id);
        if (n.getDeletedAt() != null) {
            trash.add(n);
        }
    }

    public void restoreNote(String id) {
        Note n = noteRepository.getNoteById(id);
        if (n == null) return;

        NoteMemento snap = trash.getSnapshot(id);
        noteRepository.restoreFromTrash(id);

        if (snap != null) {
            n.restore(snap);
        }

        noteRepository.save(n);
        trash.remove(n);
    }

    public void restoreNoteToSection(String id, String targetSectionId) {
        Note n = noteRepository.getNoteById(id);
        if (n == null) return;

        NoteMemento snap = trash.getSnapshot(id);
        noteRepository.restoreFromTrash(id);

        if (snap != null) {
            n.restore(snap);
        }

        n.setSectionId(targetSectionId);
        noteRepository.save(n);
        trash.remove(n);
    }

    public void emptyTrash(List<String> ids) {
        if (ids == null) return;
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
        return sortPreference.apply(noteRepository.listDeleted());
    }

    public List<Note> search(String q) {
        if (q == null || q.isBlank()) {
            return getListOfNotes();
        }
        List<Note> notes = getListOfNotes();
        searchIndex.index(notes);
        return sortPreference.apply(searchIndex.search(q));
    }

    public List<Section> listSections() {
        return sectionRepository.listSections();
    }

    public Section createSection(String name) {
        Section s = sectionRepository.createSection(name);
        if (activeSectionId == null) {
            activeSectionId = s.getId();
        }
        return s;
    }

    public void renameSection(String id, String newName) {
        sectionRepository.renameSection(id, newName);
    }

    // UPDATED: When deleting a section, delete its notes too
    public void deleteSection(String id) {
        if (id == null) return;

        List<Note> notes = noteRepository.listBySection(id);
        for (Note n : notes) {
            if (n.getDeletedAt() == null) {
                noteRepository.moveToTrash(n.getId());
                if (n.getDeletedAt() != null) {
                    trash.add(n);
                }
            }
        }

        sectionRepository.deleteSection(id);

        if (id.equals(activeSectionId)) {
            activeSectionId = null;
        }
    }

    public List<Section> listDeletedSections() {
        return sectionRepository.listDeletedSections();
    }

    // UPDATED: When restoring a section, restore its notes too
    public void restoreSection(String id) {
        if (id == null) return;

        sectionRepository.restoreSection(id);

        List<Note> deleted = noteRepository.listDeleted();
        for (Note n : deleted) {
            if (id.equals(n.getSectionId())) {
                restoreNote(n.getId());
            }
        }

        if (activeSectionId == null) {
            activeSectionId = id;
        }
    }

    public void purgeSection(String id) {
        sectionRepository.purgeSection(id);
    }

    public void setActiveSection(String sectionId) {
        this.activeSectionId = sectionId;
    }

    public Section getActiveSection() {
        if (activeSectionId == null) return null;
        return sectionRepository.getSection(activeSectionId);
    }

    public Section findSectionById(String id) {
        if (id == null) return null;
        return sectionRepository.getSection(id);
    }

    public void moveNoteToSection(String noteId, String sectionId) {
        moveNoteService.move(noteId, sectionId);
    }

    public List<Note> getNotesForSection(String sectionId) {
        List<Note> notes = noteRepository.listBySection(sectionId);
        return sortPreference.apply(notes);
    }
}
