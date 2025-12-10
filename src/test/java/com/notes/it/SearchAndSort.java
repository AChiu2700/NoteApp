package com.notes.it;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

class SearchAndSort {

    @Test
    void searchAndSort_projectsByTitleAscending() {
        LocalStorage storage = new InMemoryLocalStorage();
        Clock clock = () -> Instant.parse("2025-01-01T00:00:00Z");
        NoteRepository repo = new NoteRepository(storage, clock);
        Trash trash = new Trash(30, clock);
        SearchIndex index = SearchIndex.getInstance();
        var sortPref = new SortPreference();
        sortPref.setSortOrder(SortOrder.LastModified);

        AppController ctrl = new AppController(repo, trash, index, sortPref);

        Note n1 = ctrl.newNote();
        ctrl.editNote(n1.getId(), "Project B", "details");
        Note n2 = ctrl.newNote();
        ctrl.editNote(n2.getId(), "Project A", "more details");

        // build search index from current notes
        index.index(ctrl.getListOfNotes());
        List<Note> hits = index.search("project");

        var pref = new SortPreference();
        pref.setSortOrder(SortOrder.TitleAZ);
        List<String> sortedTitlesDistinct = pref.apply(hits).stream()
                .map(Note::getTitle)
                .distinct()
                .toList();

        // We only care that, after sorting, A comes before B
        assertEquals(List.of("Project A", "Project B"), sortedTitlesDistinct);
    }

    @Test
    void searchAndSort_byLastModifiedDescending() {
        LocalStorage storage = new InMemoryLocalStorage();

        // Clock with increasing timestamps
        Clock clock = new Clock() {
            private long calls = 0;

            @Override
            public Instant now() {
                return Instant.parse("2025-01-01T00:00:00Z").plusSeconds(calls++);
            }
        };

        NoteRepository repo = new NoteRepository(storage, clock);
        Trash trash = new Trash(30, clock);
        SearchIndex index = SearchIndex.getInstance();
        var sortPref = new SortPreference();
        sortPref.setSortOrder(SortOrder.LastModified);

        AppController ctrl = new AppController(repo, trash, index, sortPref);

        // First note: older updatedAt
        Note n1 = ctrl.newNote();
        ctrl.editNote(n1.getId(), "A", "first");

        // Second note: newer updatedAt
        Note n2 = ctrl.newNote();
        ctrl.editNote(n2.getId(), "B", "second");

        index.index(ctrl.getListOfNotes());
        // empty query → all notes
        List<Note> hits = index.search("");

        var pref = new SortPreference();
        pref.setSortOrder(SortOrder.LastModified);
        List<String> sortedTitles = pref.apply(hits).stream()
                .map(Note::getTitle)
                .toList();

        // Ensure both our notes exist
        assertTrue(sortedTitles.contains("A"));
        assertTrue(sortedTitles.contains("B"));

        int idxB = sortedTitles.indexOf("B");
        int idxA = sortedTitles.indexOf("A");

        // Newest modified first → B must come before A
        assertTrue(idxB >= 0 && idxA >= 0);
        assertTrue(idxB < idxA);
    }

    @Test
    void searchAndSort_byCreatedDateDescending() {
        LocalStorage storage = new InMemoryLocalStorage();

        Clock clock = new Clock() {
            private long calls = 0;

            @Override
            public Instant now() {
                return Instant.parse("2025-01-01T00:00:00Z").plusSeconds(calls++);
            }
        };

        NoteRepository repo = new NoteRepository(storage, clock);
        Trash trash = new Trash(30, clock);
        SearchIndex index = SearchIndex.getInstance();
        var sortPref = new SortPreference();
        sortPref.setSortOrder(SortOrder.CreatedDate);

        AppController ctrl = new AppController(repo, trash, index, sortPref);

        // Older note (created first)
        Note n1 = ctrl.newNote(); 
        ctrl.editNote(n1.getId(), "Older", "first");

        // Newer note (created second)
        Note n2 = ctrl.newNote();
        ctrl.editNote(n2.getId(), "Newer", "second");

        index.index(ctrl.getListOfNotes());
        List<Note> hits = index.search("");

        var pref = new SortPreference();
        pref.setSortOrder(SortOrder.CreatedDate);
        List<String> sortedTitles = pref.apply(hits).stream()
                .map(Note::getTitle)
                .toList();

        // Ensure both our notes exist
        assertTrue(sortedTitles.contains("Older"));
        assertTrue(sortedTitles.contains("Newer"));

        int idxNewer = sortedTitles.indexOf("Newer");
        int idxOlder = sortedTitles.indexOf("Older");

        // Newest created first → Newer must come before Older
        assertTrue(idxNewer >= 0 && idxOlder >= 0);
        assertTrue(idxNewer < idxOlder);
    }
}
