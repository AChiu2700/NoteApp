package com.example.notes.app;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.notes.data.Clock;
import com.example.notes.data.InMemoryLocalStorage;
import com.example.notes.data.LocalStorage;
import com.example.notes.data.Trash;
import com.example.notes.domain.NoteRepository;
import com.example.notes.domain.SearchIndex;
import com.example.notes.domain.SectionRepository;
import com.example.notes.domain.SortOrder;
import com.example.notes.domain.SortPreference;
import com.example.notes.model.Note;

class CreateAndAutoSaveIT {

    private SearchIndex index;

    @BeforeEach
    void resetIndex() {
        index = SearchIndex.getInstance();
        index.index(new ArrayList<>());
    }

    @Test
    void create_edit_then_list_showsUpdatedNote() {
        LocalStorage storage = new InMemoryLocalStorage();
        Clock clock = () -> Instant.parse("2025-01-01T00:00:00Z");
        NoteRepository repo = new NoteRepository(storage, clock);
        SectionRepository sectionRepo = new SectionRepository(storage, clock);
        Trash trash = new Trash(30, clock);
        SortPreference sortPref = new SortPreference(storage);
        sortPref.setSortOrder(SortOrder.LastModified);

        AppController ctrl = new AppController(repo, trash, index, sortPref, sectionRepo);

        Note note = ctrl.newNote();
        ctrl.editNote(note.getId(), "Title A", "Body");

        List<String> titles = ctrl.getListOfNotes().stream()
                .map(Note::getTitle)
                .toList();

        assertTrue(titles.contains("Title A"));
    }
}
