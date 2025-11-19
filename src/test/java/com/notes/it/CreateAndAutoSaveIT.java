package com.notes.it;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.notes.app.AppController;
import com.notes.model.Note;
import com.notes.repo.NoteRepository;
import com.notes.repo.Trash;
import com.notes.search.SearchIndex;
import com.notes.sort.SortOrder;
import com.notes.sort.SortPreference;
import com.notes.storage.InMemoryLocalStorage;
import com.notes.storage.LocalStorage;
import com.notes.util.Clock;

class CreateAndAutoSaveIT {

    @Test
    void create_edit_then_list_showsUpdatedNote() {
        LocalStorage storage = new InMemoryLocalStorage();
        Clock clock = () -> Instant.parse("2025-01-01T00:00:00Z");
        NoteRepository repo = new NoteRepository(storage, clock);
        Trash trash = new Trash(30, clock);
        SearchIndex index = new SearchIndex();
        var sortPref = new SortPreference();
        sortPref.setSortOrder(SortOrder.LastModified);

        AppController ctrl = new AppController(repo, trash, index, sortPref);

        Note note = ctrl.newNote();
        ctrl.editNote(note.getId(), "Title A", "Body");

        List<String> titles = ctrl.getListOfNotes().stream()
                .map(Note::getTitle)
                .toList();

        assertTrue(titles.contains("Title A"));
    }
}
