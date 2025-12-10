package com.notes.sort;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import org.junit.jupiter.api.Test;

import com.notes.model.Note;

class SortPreferenceTest {

    private static Note n(String id, String title, String body, String created, String updated) {
        return new Note(id, title, body,
                Instant.parse(created), Instant.parse(updated), null);
    }

    @Test
    void apply_lastModified_sortsByUpdatedDescending() {
        var a = n("1","A","", "2025-01-01T00:00:00Z","2025-01-01T00:00:00Z");
        var b = n("2","B","", "2025-01-01T00:00:00Z","2025-01-02T00:00:00Z");
        var pref = new SortPreference();
        pref.setSortOrder(SortOrder.LastModified);

        var out = pref.apply(List.of(a,b));
        var titles = out.stream().map(Note::getTitle).toList();

        assertEquals(List.of("B","A"), titles);
    }

    @Test
    void apply_createdDate_sortsByCreatedDescending() {
        var a = n("1","A","", "2025-01-01T00:00:00Z","2025-01-02T00:00:00Z");
        var b = n("2","B","", "2025-01-03T00:00:00Z","2025-01-03T00:00:00Z");
        var pref = new SortPreference();
        pref.setSortOrder(SortOrder.CreatedDate);

        var out = pref.apply(List.of(a,b));
        var titles = out.stream().map(Note::getTitle).toList();

        assertEquals(List.of("B","A"), titles);
    }

    @Test
    void apply_titleAZ_sortsAlphabetically() {
        var a = n("1","B","", "2025-01-01T00:00:00Z","2025-01-01T00:00:00Z");
        var b = n("2","A","", "2025-01-01T00:00:00Z","2025-01-01T00:00:00Z");

        var pref = new SortPreference();
        pref.setSortOrder(SortOrder.TitleAZ);
        var out = pref.apply(List.of(a,b));
        var titles = out.stream().map(Note::getTitle).toList();

        assertEquals(List.of("A","B"), titles);
    }

    @Test
    void apply_doesNotMutateInput_returnsNewList() {
        var a = n("1","B","", "2025-01-01T00:00:00Z","2025-01-01T00:00:00Z");
        var b = n("2","A","", "2025-01-01T00:00:00Z","2025-01-01T00:00:00Z");
        var list = List.of(a,b);

        var pref = new SortPreference();
        pref.setSortOrder(SortOrder.TitleAZ);
        var out = pref.apply(list);

        assertNotSame(list, out);
        assertEquals(List.of("B","A"), list.stream().map(Note::getTitle).toList());
    }
}
