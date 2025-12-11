package com.notes.it;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.notes.app.AppController;
import com.notes.model.Note;
import com.notes.model.Section;
import com.notes.repo.NoteRepository;
import com.notes.repo.SectionRepository;
import com.notes.repo.Trash;
import com.notes.search.SearchIndex;
import com.notes.sort.SortOrder;
import com.notes.sort.SortPreference;
import com.notes.storage.InMemoryLocalStorage;
import com.notes.storage.LocalStorage;
import com.notes.util.Clock;

class SectionCascadeIT {

    private SearchIndex index;

    @BeforeEach
    void resetIndex() {
        index = SearchIndex.getInstance();
        index.index(new ArrayList<>());
    }

    private AppController newController() {
        LocalStorage storage = new InMemoryLocalStorage();
        Clock clock = () -> Instant.parse("2025-01-01T00:00:00Z");
        NoteRepository noteRepo = new NoteRepository(storage, clock);
        SectionRepository sectionRepo = new SectionRepository(storage, clock);
        Trash trash = new Trash(30, clock);
        SortPreference sortPref = new SortPreference();
        sortPref.setSortOrder(SortOrder.LastModified);
        return new AppController(noteRepo, trash, index, sortPref, sectionRepo);
    }

    @Test
    void deleteSection_softDeletesNotesInThatSection() {
        AppController ctrl = newController();

        Section s = ctrl.createSection("Projects");
        ctrl.setActiveSection(s.getId());

        Note n1 = ctrl.newNote();
        ctrl.editNote(n1.getId(), "N1", "body1");
        Note n2 = ctrl.newNote();
        ctrl.editNote(n2.getId(), "N2", "body2");

        List<Note> activeBeforeDelete = ctrl.getListOfNotes();
        assertEquals(2, activeBeforeDelete.size());

        ctrl.deleteSection(s.getId());

        List<Section> activeSections = ctrl.listSections();
        List<Section> deletedSections = ctrl.listDeletedSections();
        assertTrue(activeSections.stream().noneMatch(sec -> sec.getId().equals(s.getId())));
        assertTrue(deletedSections.stream().anyMatch(sec -> sec.getId().equals(s.getId())));

        List<Note> activeAfterDelete = ctrl.getListOfNotes();
        List<Note> deletedNotes = ctrl.getDeletedNotes();

        assertTrue(activeAfterDelete.stream().noneMatch(n -> n.getId().equals(n1.getId())));
        assertTrue(activeAfterDelete.stream().noneMatch(n -> n.getId().equals(n2.getId())));

        assertTrue(deletedNotes.stream().anyMatch(n -> n.getId().equals(n1.getId())));
        assertTrue(deletedNotes.stream().anyMatch(n -> n.getId().equals(n2.getId())));
    }

    @Test
    void restoreSection_restoresNotesInThatSection() {
        AppController ctrl = newController();

        Section s = ctrl.createSection("Work");
        ctrl.setActiveSection(s.getId());

        Note n1 = ctrl.newNote();
        ctrl.editNote(n1.getId(), "N1", "body1");
        Note n2 = ctrl.newNote();
        ctrl.editNote(n2.getId(), "N2", "body2");

        ctrl.deleteSection(s.getId());

        List<Note> deletedBeforeRestore = ctrl.getDeletedNotes();
        assertTrue(deletedBeforeRestore.stream().anyMatch(n -> n.getId().equals(n1.getId())));
        assertTrue(deletedBeforeRestore.stream().anyMatch(n -> n.getId().equals(n2.getId())));

        ctrl.restoreSection(s.getId());

        List<Section> activeSections = ctrl.listSections();
        List<Section> deletedSections = ctrl.listDeletedSections();
        assertTrue(activeSections.stream().anyMatch(sec -> sec.getId().equals(s.getId())));
        assertFalse(deletedSections.stream().anyMatch(sec -> sec.getId().equals(s.getId())));

        List<Note> deletedAfterRestore = ctrl.getDeletedNotes();
        assertFalse(deletedAfterRestore.stream().anyMatch(n -> n.getId().equals(n1.getId())));
        assertFalse(deletedAfterRestore.stream().anyMatch(n -> n.getId().equals(n2.getId())));

        List<Note> activeAfterRestore = ctrl.getListOfNotes();
        assertTrue(activeAfterRestore.stream().anyMatch(n -> n.getId().equals(n1.getId())));
        assertTrue(activeAfterRestore.stream().anyMatch(n -> n.getId().equals(n2.getId())));
    }
}
