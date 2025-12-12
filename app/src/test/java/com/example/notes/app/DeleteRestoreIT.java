package com.example.notes.app;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.notes.app.AppController;
import com.example.notes.data.InMemoryLocalStorage;
import com.example.notes.data.LocalStorage;
import com.example.notes.domain.NoteRepository;
import com.example.notes.domain.SectionRepository;
import com.example.notes.data.Trash;
import com.example.notes.model.Note;
import com.example.notes.domain.SearchIndex;
import com.example.notes.domain.SortOrder;
import com.example.notes.domain.SortPreference;
import com.example.notes.data.Clock;

class DeleteRestoreIT {

    private SearchIndex index;

    @BeforeEach
    void resetIndex() {
        index = SearchIndex.getInstance();
        index.index(new ArrayList<>());
    }

    private AppController newController() {

        LocalStorage storage = new InMemoryLocalStorage();
        Clock clock = () -> Instant.parse("2025-01-01T00:00:00Z");
        NoteRepository repo = new NoteRepository(storage, clock);
        SectionRepository sectionRepo = new SectionRepository(storage, clock);
        Trash trash = new Trash(30, clock);

        SortPreference sortPref = new SortPreference(storage);
        sortPref.setSortOrder(SortOrder.LastModified);

        return new AppController(repo, trash, index, sortPref, sectionRepo);
    }


    @Test
    void delete_movesToTrash_and_reducesActiveCountByOne() {
        AppController ctrl = newController();

        int initialSize = ctrl.getListOfNotes().size();

        Note note = ctrl.newNote();
        ctrl.editNote(note.getId(), "Temp", "Body");

        int afterCreate = ctrl.getListOfNotes().size();
        assertEquals(initialSize + 1, afterCreate);

        ctrl.deleteNote(note.getId());

        int afterDelete = ctrl.getListOfNotes().size();
        assertEquals(initialSize, afterDelete);
    }

    @Test
    void delete_then_restore_restoresNoteAndActiveCount() {
        AppController ctrl = newController();

        int initialSize = ctrl.getListOfNotes().size();

        Note note = ctrl.newNote();
        ctrl.editNote(note.getId(), "Temp", "Body");

        int afterCreate = ctrl.getListOfNotes().size();
        assertEquals(initialSize + 1, afterCreate);

        ctrl.deleteNote(note.getId());

        int afterDelete = ctrl.getListOfNotes().size();
        assertEquals(initialSize, afterDelete);

        ctrl.restoreNote(note.getId());

        List<Note> notesAfterRestore = ctrl.getListOfNotes();
        int afterRestoreSize = notesAfterRestore.size();
        assertEquals(initialSize + 1, afterRestoreSize);

        Note restored = notesAfterRestore.stream()
                .filter(n -> n.getId().equals(note.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals("Temp", restored.getTitle());
        assertEquals("Body", restored.getContent());
    }

    @Test
    void delete_twoNotes_reducesActiveCountByTwo() {
        AppController ctrl = newController();

        int initialSize = ctrl.getListOfNotes().size();

        Note first = ctrl.newNote();
        ctrl.editNote(first.getId(), "First", "Body 1");

        Note second = ctrl.newNote();
        ctrl.editNote(second.getId(), "Second", "Body 2");

        int afterCreateTwo = ctrl.getListOfNotes().size();
        assertEquals(initialSize + 2, afterCreateTwo);

        ctrl.deleteNote(first.getId());
        ctrl.deleteNote(second.getId());

        int afterDeleteTwo = ctrl.getListOfNotes().size();
        assertEquals(initialSize, afterDeleteTwo);
    }
}
