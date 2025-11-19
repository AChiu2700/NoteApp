package com.notes.sort;

import java.util.Comparator;
import java.util.List;

import com.notes.model.Note;

// Strategy Design Pattern SortOrder.java + SortPreference.java
public class SortPreference {
    private SortOrder sortOrder = SortOrder.LastModified;

    public SortPreference() {
        this.sortOrder = sortOrder;
    }

    public SortOrder getSortOrder() { return sortOrder;}
    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public List<Note> apply(List<Note> notes) {
        return switch (sortOrder){
            case LastModified -> notes.stream()
                    .sorted(Comparator.comparing(Note::getUpdatedAt).reversed())
                    .toList();
            case CreatedDate -> notes.stream()
                    .sorted(Comparator.comparing(Note::getCreatedAt).reversed())
                    .toList();
            case TitleAZ -> notes.stream()
                    .sorted(Comparator.comparing(Note::getTitle))
                    .toList();
        };
    }
}