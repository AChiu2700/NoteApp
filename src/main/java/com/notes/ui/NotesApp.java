package com.notes.ui;

import java.util.List;

import com.notes.app.AppController;
import com.notes.model.Note;
import com.notes.repo.NoteRepository;
import com.notes.repo.Trash;
import com.notes.search.SearchIndex;
import com.notes.sort.SortOrder;
import com.notes.sort.SortPreference;
import com.notes.storage.InMemoryLocalStorage;
import com.notes.storage.LocalStorage;
import com.notes.util.Clock;
import com.notes.util.SystemClock;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class NotesApp extends Application {

    private AppController controller;

    private ListView<Note> notesListView;
    private TextField titleField;
    private TextArea bodyArea;
    private TextField searchField;
    private ComboBox<SortOrder> sortBox;

    private Button newButton;
    private Button saveButton;
    private Button deleteButton;
    private Button restoreButton;
    private Button searchButton;
    private Button trashButton;

    private Label modeLabel;
    private Label sortLabel;

    private Note currentNote;
    private boolean showingTrash = false;

    // used to avoid autosaving when we programmatically change selection (e.g. right after New)
    private boolean suppressAutoSaveOnSelection = false;

    @Override
    public void start(Stage stage) {
        // 1) Backend
        LocalStorage storage = new InMemoryLocalStorage();
        Clock clock = new SystemClock();
        Trash trash = new Trash(30, clock);
        NoteRepository repo = new NoteRepository(storage, clock);
        SearchIndex index = new SearchIndex();
        SortPreference sortPref = new SortPreference();

        controller = new AppController(repo, trash, index, sortPref);

        // 2) UI controls
        notesListView = new ListView<>();
        notesListView.setPrefWidth(220);

        titleField = new TextField();
        titleField.setPromptText("Title");

        bodyArea = new TextArea();
        bodyArea.setPromptText("Write your note here...");

        searchField = new TextField();
        searchField.setPromptText("Search...");
        searchButton = new Button("Search");

        sortBox = new ComboBox<>();
        sortBox.getItems().addAll(SortOrder.values());
        sortBox.setValue(SortOrder.LastModified);

        newButton = new Button("New");
        saveButton = new Button("Save");
        deleteButton = new Button("Delete");
        restoreButton = new Button("Restore");
        trashButton = new Button("Trash");

        modeLabel = new Label("Notes");
        sortLabel = new Label("Sort:");

        // 3) Layout
        HBox topBar = new HBox(
                8,
                modeLabel,
                searchField,
                searchButton,
                sortLabel,
                sortBox,
                trashButton
        );
        topBar.setPadding(new Insets(8));

        VBox editorBox = new VBox(8, titleField, bodyArea);
        editorBox.setPadding(new Insets(8));

        HBox bottomBar = new HBox(8, newButton, saveButton, deleteButton, restoreButton);
        bottomBar.setPadding(new Insets(8));

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setLeft(notesListView);
        root.setCenter(editorBox);
        root.setBottom(bottomBar);

        // 4) Wire actions
        refreshNotesList(controller.getListOfNotes());
        updateModeUI();

        notesListView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            // auto-save current note when user switches selection
            if (!suppressAutoSaveOnSelection) {
                autoSaveIfNeeded();
            }

            if (selected != null) {
                Note reloaded = controller.openNote(selected.getId());
                currentNote = (reloaded != null) ? reloaded : selected;
                titleField.setText(currentNote.getTitle());
                bodyArea.setText(currentNote.getContent());
            } else {
                currentNote = null;
                titleField.clear();
                bodyArea.clear();
            }
        });

        newButton.setOnAction(e -> {
            if (showingTrash) {
                return;
            }

            // save current note before creating a new one
            autoSaveIfNeeded();

            currentNote = controller.newNote();

            // avoid autosaving the new note using old field contents when we select it
            suppressAutoSaveOnSelection = true;
            refreshNotesList(controller.getListOfNotes());
            notesListView.getSelectionModel().select(currentNote);
            suppressAutoSaveOnSelection = false;

            // show the (blank) new note
            titleField.setText(currentNote.getTitle());
            bodyArea.setText(currentNote.getContent());
        });

        saveButton.setOnAction(e -> {
            if (showingTrash) {
                return;
            }
            if (currentNote != null) {
                controller.editNote(
                        currentNote.getId(),
                        titleField.getText(),
                        bodyArea.getText()
                );
                refreshNotesList(controller.getListOfNotes());
            }
        });

        deleteButton.setOnAction(e -> {
            Note selected = notesListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }

            if (!showingTrash) {
                autoSaveIfNeeded();
            }

            if (showingTrash) {
                controller.emptyTrash(List.of(selected.getId()));
                refreshNotesList(controller.getDeletedNotes());
            } else {
                controller.deleteNote(selected.getId());
                refreshNotesList(controller.getListOfNotes());
            }

            currentNote = null;
            titleField.clear();
            bodyArea.clear();
        });

        restoreButton.setOnAction(e -> {
            if (!showingTrash) {
                return;
            }

            Note selected = notesListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }

            controller.restoreNote(selected.getId());
            refreshNotesList(controller.getDeletedNotes());

            currentNote = null;
            titleField.clear();
            bodyArea.clear();
        });

        sortBox.setOnAction(e -> {
            SortOrder order = sortBox.getValue();
            controller.setSortOrder(order);
            if (!showingTrash) {
                refreshNotesList(controller.getListOfNotes());
            } else {
                refreshNotesList(controller.getDeletedNotes());
            }
        });

        searchButton.setOnAction(e -> {
            if (showingTrash) {
                return;
            }

            autoSaveIfNeeded();

            String query = searchField.getText();
            List<Note> results = controller.search(query);
            refreshNotesList(results);

            currentNote = null;
            titleField.clear();
            bodyArea.clear();
        });

        searchField.setOnAction(e -> {
            if (showingTrash) {
                return;
            }

            autoSaveIfNeeded();

            String query = searchField.getText();
            List<Note> results = controller.search(query);
            refreshNotesList(results);

            currentNote = null;
            titleField.clear();
            bodyArea.clear();
        });

        trashButton.setOnAction(e -> {
            autoSaveIfNeeded();

            showingTrash = !showingTrash;

            if (showingTrash) {
                refreshNotesList(controller.getDeletedNotes());
            } else {
                refreshNotesList(controller.getListOfNotes());
            }

            currentNote = null;
            titleField.clear();
            bodyArea.clear();

            updateModeUI();
        });

        // 5) Show stage
        stage.setTitle("Notes App");
        stage.setScene(new Scene(root, 900, 600));
        stage.show();
    }

    private void refreshNotesList(List<Note> notes) {
        notesListView.setItems(FXCollections.observableArrayList(notes));
    }

    // Save current note if we’re in Notes (not Trash) and a note is selected
    private void autoSaveIfNeeded() {
        if (showingTrash) {
            return;
        }
        if (currentNote == null) {
            return;
        }

        controller.editNote(
                currentNote.getId(),
                titleField.getText(),
                bodyArea.getText()
        );
    }

    // Toggle visibility / text / editability based on mode
    private void updateModeUI() {
        if (showingTrash) {
            modeLabel.setText("Trash (Deleted Notes)");
            trashButton.setText("Back to Notes");

            // hide controls not needed in Trash
            newButton.setVisible(false);
            newButton.setManaged(false);

            saveButton.setVisible(false);
            saveButton.setManaged(false);

            searchField.setVisible(false);
            searchField.setManaged(false);

            searchButton.setVisible(false);
            searchButton.setManaged(false);

            sortLabel.setVisible(false);
            sortLabel.setManaged(false);

            sortBox.setVisible(false);
            sortBox.setManaged(false);

            // show restore
            restoreButton.setVisible(true);
            restoreButton.setManaged(true);

            deleteButton.setText("Delete Permanently");

            // read-only editor
            titleField.setEditable(false);
            bodyArea.setEditable(false);

        } else {
            modeLabel.setText("Notes");
            trashButton.setText("Trash");

            // show normal controls
            newButton.setVisible(true);
            newButton.setManaged(true);

            saveButton.setVisible(true);
            saveButton.setManaged(true);

            searchField.setVisible(true);
            searchField.setManaged(true);

            searchButton.setVisible(true);
            searchButton.setManaged(true);

            sortLabel.setVisible(true);
            sortLabel.setManaged(true);

            sortBox.setVisible(true);
            sortBox.setManaged(true);

            // hide restore in Notes mode
            restoreButton.setVisible(false);
            restoreButton.setManaged(false);

            deleteButton.setText("Delete");

            // editable editor
            titleField.setEditable(true);
            bodyArea.setEditable(true);

            searchField.setPromptText("Search...");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
