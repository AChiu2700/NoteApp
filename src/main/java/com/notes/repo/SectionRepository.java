package com.notes.repo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.notes.model.Section;
import com.notes.storage.LocalStorage;
import com.notes.util.Clock;

public class SectionRepository {

    private static final String KEY = "sections";

    private final LocalStorage storage;
    private final Clock clock;

    public SectionRepository(LocalStorage storage, Clock clock) {
        this.storage = storage;
        this.clock = clock;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Section> load() {
        Object raw = storage.read(KEY);
        if (raw instanceof Map<?, ?> map) {
            try {
                return (Map<String, Section>) map;
            } catch (ClassCastException ex) {
                return new HashMap<>();
            }
        }
        return new HashMap<>();
    }

    private void saveAll(Map<String, Section> sections) {
        storage.write(KEY, sections);
    }

    public Section createSection(String name) {
        Map<String, Section> sections = load();
        Section section = new Section(name, clock.now());
        sections.put(section.getId(), section);
        saveAll(sections);
        return section;
    }

    public Section getSection(String id) {
        if (id == null) return null;
        Map<String, Section> sections = load();
        return sections.get(id);
    }

    public List<Section> listSections() {
        Map<String, Section> sections = load();
        return new ArrayList<>(sections.values());
    }

    public void renameSection(String id, String newName) {
        if (id == null) return;
        Map<String, Section> sections = load();
        Section section = sections.get(id);
        if (section == null) return;
        section.setName(newName);
        saveAll(sections);
    }

    public void deleteSection(String id) {
        if (id == null) return;
        Map<String, Section> sections = load();
        sections.remove(id);
        saveAll(sections);
    }
}
