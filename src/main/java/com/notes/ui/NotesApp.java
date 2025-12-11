package com.notes.ui;

import com.notes.app.AppController;
import com.notes.repo.NoteRepository;
import com.notes.repo.SectionRepository;
import com.notes.repo.Trash;
import com.notes.search.SearchIndex;
import com.notes.sort.SortPreference;
import com.notes.storage.InMemoryLocalStorage;
import com.notes.storage.LocalStorage;
import com.notes.util.Clock;
import com.notes.util.SystemClock;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NotesApp extends Application {

    private AppController controller;

    @Override
    public void start(Stage stage) {
        // Backend
        LocalStorage storage = new InMemoryLocalStorage();
        Clock clock = new SystemClock();
        Trash trash = new Trash(30, clock);
        NoteRepository repo = new NoteRepository(storage, clock);
        SearchIndex index = SearchIndex.getInstance();
        SortPreference sortPref = new SortPreference();
        SectionRepository sectionRepo = new SectionRepository(storage, clock);

        controller = new AppController(repo, trash, index, sortPref, sectionRepo);

        // UI
        NotesView view = new NotesView(controller);
        Scene scene = new Scene(view.getRoot(), 1000, 650);

        stage.setTitle("Notes App");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
