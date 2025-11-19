package com.notes.repo;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.notes.model.Note;
import com.notes.storage.InMemoryLocalStorage;
import com.notes.storage.LocalStorage;
import com.notes.util.Clock;

class NoteRepositoryTest {

    private NoteRepository repo;

    @BeforeEach
    void setUp() {
        LocalStorage storage = new InMemoryLocalStorage();
        Clock clock = () -> Instant.parse("2025-01-01T00:00:00Z");
        repo = new NoteRepository(storage, clock);
        clearAllNotes();
    }

    private void clearAllNotes() {
        for (Note note : repo.listNotes()) {
            repo.moveToTrash(note.getId());
        }
        for (Note note : repo.listDeleted()) {
            repo.purgeDeletedNotes(note.getId());
        }
    }

    @Test
    void createNote_persistsNote() {
        Note n = repo.createNote("Title", "Body");
        assertNotNull(n.getId());

        List<Note> notes = repo.listNotes();
        assertEquals(1, notes.size());
        assertEquals("Title", notes.get(0).getTitle());
    }

    @Test
    void listNotes_excludesDeletedNotes() {
        Note keep = repo.createNote("Keep", "");
        Note delete = repo.createNote("Delete", "");

        repo.moveToTrash(delete.getId());

        List<Note> active = repo.listNotes();
        List<Note> deleted = repo.listDeleted();

        assertEquals(1, active.size());
        assertEquals("Keep", active.get(0).getTitle());
        assertEquals(1, deleted.size());
        assertEquals("Delete", deleted.get(0).getTitle());
    }

    @Test
    void moveToTrash_setsDeletedAt() {
        Note n = repo.createNote("X", "");
        assertNull(repo.getNoteById(n.getId()).getDeletedAt());

        repo.moveToTrash(n.getId());
        Note trashed = repo.getNoteById(n.getId());

        assertNotNull(trashed.getDeletedAt());
    }

    @Test
    void restoreFromTrash_clearsDeletedAt_andReturnsToActiveList() {
        Note n = repo.createNote("X", "");
        repo.moveToTrash(n.getId());
        assertEquals(0, repo.listNotes().size());
        assertEquals(1, repo.listDeleted().size());

        repo.restoreFromTrash(n.getId());

        assertEquals(1, repo.listNotes().size());
        assertEquals(0, repo.listDeleted().size());
        assertNull(repo.getNoteById(n.getId()).getDeletedAt());
    }

    @Test
    void purgeDeletedNotes_permanentlyRemovesNote() {
        Note n = repo.createNote("X", "");
        repo.moveToTrash(n.getId());

        repo.purgeDeletedNotes(n.getId());

        assertNull(repo.getNoteById(n.getId()));
        assertTrue(repo.listNotes().isEmpty());
        assertTrue(repo.listDeleted().isEmpty());
    }
}
