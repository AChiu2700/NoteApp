package com.notes.app;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.notes.model.Note;
import com.notes.repo.NoteRepository;
import com.notes.repo.SectionRepository;
import com.notes.repo.Trash;
import com.notes.search.SearchIndex;
import com.notes.sort.SortOrder;
import com.notes.sort.SortPreference;
import com.notes.storage.InMemoryLocalStorage;
import com.notes.storage.LocalStorage;
import com.notes.util.Clock;

class AppControllerTest {

    private AppController controller;
    private NoteRepository repo;
    private SearchIndex index;

    @BeforeEach
    void setUp() {
        LocalStorage storage = new InMemoryLocalStorage();
        Clock clock = () -> Instant.parse("2025-01-01T00:00:00Z");
        repo = new NoteRepository(storage, clock);
        SectionRepository sectionRepo = new SectionRepository(storage, clock);
        Trash trash = new Trash(30, clock);
        index = SearchIndex.getInstance();
        index.index(new ArrayList<>());
        SortPreference sortPref = new SortPreference();
        controller = new AppController(repo, trash, index, sortPref, sectionRepo);
    }

    @Test
    void newNote_createsEmptyNoteInList() {
        int initialSize = controller.getListOfNotes().size();

        Note created = controller.newNote();
        List<Note> notes = controller.getListOfNotes();

        assertEquals(initialSize + 1, notes.size());

        Note found = notes.stream()
                .filter(n -> n.getId().equals(created.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals("", found.getTitle());
        assertEquals("", found.getContent());
    }

    @Test
    void editNote_updatesTitleAndBody() {
        Note created = controller.newNote();
        controller.editNote(created.getId(), "Title A", "Body A");

        Note loaded = controller.openNote(created.getId());
        assertEquals("Title A", loaded.getTitle());
        assertEquals("Body A", loaded.getContent());
    }

    @Test
    void deleteNote_movesNoteOutOfActiveList() {
        int initialSize = controller.getListOfNotes().size();

        Note created = controller.newNote();
        int afterCreate = controller.getListOfNotes().size();
        assertEquals(initialSize + 1, afterCreate);

        controller.deleteNote(created.getId());

        List<Note> notesAfterDelete = controller.getListOfNotes();
        assertEquals(initialSize, notesAfterDelete.size());

        boolean stillPresent = notesAfterDelete.stream()
                .anyMatch(n -> n.getId().equals(created.getId()));
        assertTrue(!stillPresent);
    }

    @Test
    void deleteAndRestoreNote_roundTrip() {
        int initialSize = controller.getListOfNotes().size();

        Note created = controller.newNote();
        controller.editNote(created.getId(), "Keep Me", "Body");

        int afterCreate = controller.getListOfNotes().size();
        assertEquals(initialSize + 1, afterCreate);

        controller.deleteNote(created.getId());
        int afterDelete = controller.getListOfNotes().size();
        assertEquals(initialSize, afterDelete);

        controller.restoreNote(created.getId());
        List<Note> notesAfterRestore = controller.getListOfNotes();
        int afterRestoreSize = notesAfterRestore.size();
        assertEquals(initialSize + 1, afterRestoreSize);

        Note restored = notesAfterRestore.stream()
                .filter(n -> n.getId().equals(created.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals("Keep Me", restored.getTitle());
        assertEquals("Body", restored.getContent());
    }

    @Test
    void setSortOrder_changesOrderingByTitle() {

        Note noteWithB = controller.newNote();
        controller.editNote(noteWithB.getId(), "B", "");

        Note noteWithA = controller.newNote();
        controller.editNote(noteWithA.getId(), "A", "");

        controller.setSortOrder(SortOrder.TitleAZ);
        List<Note> notes = controller.getListOfNotes();

        int indexOfA = -1;
        int indexOfB = -1;

        for (int i = 0; i < notes.size(); i++) {
            Note n = notes.get(i);
            if (n.getId().equals(noteWithA.getId())) {
                indexOfA = i;
            } else if (n.getId().equals(noteWithB.getId())) {
                indexOfB = i;
            }
        }

        assertTrue(indexOfA != -1 && indexOfB != -1);

        assertTrue(indexOfA < indexOfB);
    }
}
