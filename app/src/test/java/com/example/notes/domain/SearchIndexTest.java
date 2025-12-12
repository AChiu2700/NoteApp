package com.example.notes.domain;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.example.notes.model.Note;

class SearchIndexTest {
    private static Note n(String id, String title, String body) {
        return new Note(id, title, body,
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-01-01T00:00:00Z"),
                null);
    }

    @Test
    void index_buildsSearchTokens_forTitleAndBody() {
        var idx = SearchIndex.getInstance();
        var notes = List.of(n("1","Alpha","bravo data"), n("2","Misc","nope"));
        idx.index(notes);
        assertEquals(1, idx.search("Alpha").size());
        assertEquals(1, idx.search("bravo").size());
    }

    @Test
    void search_emptyQuery_returnsAllNotes() {
        var idx = SearchIndex.getInstance();
        idx.index(List.of(n("1","A",""), n("2","B","")));
        assertEquals(2, idx.search("").size());
    }

    @Test
    void search_singleKeyword_returnsMatches() {
        var idx = SearchIndex.getInstance();
        idx.index(List.of(n("1","Alpha","x"), n("2","Beta","alpha body")));
        assertEquals(2, idx.search("alpha").size());
    }

    @Test
    void search_caseInsensitive_matches() {
        var idx = SearchIndex.getInstance();
        idx.index(List.of(n("1","Project Plan",""), n("2","misc","PROJECT details")));
        assertEquals(2, idx.search("pRoJeCt").size());
    }

    @Test
    void search_noMatches_returnsEmptyList() {
        var idx = SearchIndex.getInstance();
        idx.index(List.of(n("1","A",""), n("2","B","")));
        assertTrue(idx.search("zzz").isEmpty());
    }
}
