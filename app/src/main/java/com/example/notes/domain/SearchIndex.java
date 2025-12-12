package com.example.notes.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.notes.model.Note;

// Singleton Pattern: SearchIndex
public class SearchIndex {

    private static final SearchIndex INSTANCE = new SearchIndex();
    private List<Note> snapshot = new ArrayList<>();
    private SearchIndex() {
    }

    public static SearchIndex getInstance() {
        return INSTANCE;
    }

    public synchronized void index(List<Note> notes) {
        if (notes == null) {
            this.snapshot = new ArrayList<>();
        } else {
            this.snapshot = new ArrayList<>(notes);
        }
    }

    public synchronized List<Note> search(String query) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>(snapshot);
        }

        String q = query.toLowerCase(Locale.ROOT);

        return snapshot.stream()
                .filter(n -> {
                    String t = n.getTitle() == null ? "" : n.getTitle().toLowerCase(Locale.ROOT);
                    String c = n.getContent() == null ? "" : n.getContent().toLowerCase(Locale.ROOT);
                    return t.contains(q) || c.contains(q);
                })
                .toList();
    }
}
