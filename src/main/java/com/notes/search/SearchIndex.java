package com.notes.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.notes.model.Note;

public class SearchIndex {
  private List<Note> snapshot = new ArrayList<>();

  public void index(List<Note> notes) {
    this.snapshot = new ArrayList<>(notes);
  }

  public List<Note> search(String query) {
    if (query == null || query.isBlank()) return new ArrayList<>(snapshot);

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
