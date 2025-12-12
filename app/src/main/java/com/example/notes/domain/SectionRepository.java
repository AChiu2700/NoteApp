package com.example.notes.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.notes.data.Clock;
import com.example.notes.data.LocalStorage;
import com.example.notes.model.Section;

public class SectionRepository {

    private static final String STORAGE_KEY = "sections";

    private final LocalStorage storage;
    private final Clock clock;

    public SectionRepository(LocalStorage storage, Clock clock) {
        this.storage = storage;
        this.clock = clock;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Section> loadAll() {
        Object raw = storage.read(STORAGE_KEY);
        if (raw instanceof Map<?, ?> map) {
            return (Map<String, Section>) map;
        }
        return new LinkedHashMap<>();
    }

    private void saveAll(Map<String, Section> data) {
        storage.write(STORAGE_KEY, data);
    }

    public Section createSection(String name) {
        Map<String, Section> all = loadAll();
        Section s = new Section(name, clock.now());
        all.put(s.getId(), s);
        saveAll(all);
        return s;
    }

    public Section getSection(String id) {
        if (id == null) return null;
        Map<String, Section> all = loadAll();
        return all.get(id);
    }

    public List<Section> listSections() {
        Map<String, Section> all = loadAll();
        return all.values().stream()
                .filter(s -> !s.isDeleted())
                .toList();
    }

    public List<Section> listDeletedSections() {
        Map<String, Section> all = loadAll();
        return all.values().stream()
                .filter(Section::isDeleted)
                .toList();
    }

    public void renameSection(String id, String newName) {
        Map<String, Section> all = loadAll();
        Section s = all.get(id);
        if (s == null) return;
        s.setName(newName);
        saveAll(all);
    }

    // soft delete (goes to section trash)
    public void deleteSection(String id) {
        Map<String, Section> all = loadAll();
        Section s = all.get(id);
        if (s == null) return;
        s.markDeleted(clock.now());
        saveAll(all);
    }

    public void restoreSection(String id) {
        Map<String, Section> all = loadAll();
        Section s = all.get(id);
        if (s == null) return;
        s.clearDeleted();
        saveAll(all);
    }

    // permanent remove from storage
    public void purgeSection(String id) {
        Map<String, Section> all = loadAll();
        if (all.remove(id) != null) {
            saveAll(all);
        }
    }
}
