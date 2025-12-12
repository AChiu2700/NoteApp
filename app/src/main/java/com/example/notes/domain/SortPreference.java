package com.example.notes.domain;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.example.notes.data.LocalStorage;
import com.example.notes.model.Note;

// Strategy Design Pattern: SortOrder.java + SortPreference.java
public class SortPreference {

    private static final String KEY = "sortOrder";

    private final LocalStorage storage;
    private SortOrder sortOrder = SortOrder.LastModified;

    public SortPreference(LocalStorage storage) {
        this.storage = storage;
        Object raw = storage.read(KEY);
        if (raw instanceof SortOrder s) {
            this.sortOrder = s;
        }
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        if (sortOrder != null) {
            this.sortOrder = sortOrder;
            storage.write(KEY, sortOrder);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Note> apply(List notes) {
        if (notes == null) {
            return List.of();
        }

        return switch (sortOrder) {
            case LastModified -> (List<Note>) notes.stream()
                    .sorted(Comparator.comparing(Note::getUpdatedAt).reversed())
                    .collect(Collectors.toList());
            case CreatedDate -> (List<Note>) notes.stream()
                    .sorted(Comparator.comparing(Note::getCreatedAt).reversed())
                    .collect(Collectors.toList());
            case TitleAZ -> (List<Note>) notes.stream()
                    .sorted(Comparator.comparing(
                            Note::getTitle,
                            Comparator.nullsFirst(String::compareToIgnoreCase)))
                    .collect(Collectors.toList());
        };
    }
}