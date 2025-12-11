package com.notes.app;

import com.notes.model.Note;
import com.notes.model.Section;
import com.notes.repo.NoteRepository;
import com.notes.repo.SectionRepository;

public class MoveNoteToSection {

    private final NoteRepository noteRepository;
    private final SectionRepository sectionRepository;

    public MoveNoteToSection(NoteRepository noteRepository, SectionRepository sectionRepository) {
        this.noteRepository = noteRepository;
        this.sectionRepository = sectionRepository;
    }

    public void move(String noteId, String targetSectionId) {
        if (noteId == null || targetSectionId == null) {
            return;
        }

        Note note = noteRepository.getNoteById(noteId);
        if (note == null) {
            return;
        }

        Section target = sectionRepository.getSection(targetSectionId);
        if (target == null) {
            return;
        }

        note.setSectionId(targetSectionId);
        noteRepository.save(note);
    }
}
