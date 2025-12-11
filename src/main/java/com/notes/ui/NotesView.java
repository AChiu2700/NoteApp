package com.notes.ui;

import java.util.Comparator;
import java.util.List;

import com.notes.app.AppController;
import com.notes.model.Note;
import com.notes.model.Section;
import com.notes.sort.SortOrder;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class NotesView {

    private final AppController controller;

    private final BorderPane root;

    // Sections UI (tabs)
    private final TabPane sectionTabs;
    private final TextField newSectionField;
    private final Button addSectionButton;
    private final Button sortSectionsButton;
    private final Label sectionsLabel;
    private boolean sectionSortAscending = true;

    // Notes UI
    private final ListView<Note> notesListView;
    private final TextField titleField;
    private final TextArea bodyArea;
    private final Label notesLabel;

    // Search / sort / mode
    private final TextField searchField;
    private final Button searchButton;
    private final ComboBox<SortOrder> sortBox;
    private final Button trashButton;
    private final Button newButton;
    private final Button saveButton;
    private final Button deleteButton;
    private final Button restoreButton;
    private final Label modeLabel;
    private final Label sortLabel;

    private Note currentNote;
    private boolean showingTrash = false;
    private boolean suppressAutoSaveOnSelection = false;

    public NotesView(AppController controller) {
        this.controller = controller;

        sectionTabs = new TabPane();
        sectionTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        newSectionField = new TextField();
        newSectionField.setPromptText("New section name");
        addSectionButton = new Button("Add");
        sortSectionsButton = new Button("Sort A-Z");

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

        sectionsLabel = new Label("Sections");
        notesLabel = new Label("Notes");

        root = buildLayout();
        wireEvents();
        refreshSectionsList();
        refreshNotesList(controller.getListOfNotes());
        updateModeUI();
    }

    public BorderPane getRoot() {
        return root;
    }

    private BorderPane buildLayout() {
        // Top bar
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
        HBox.setHgrow(searchField, Priority.ALWAYS);

        // Sections row
        HBox addSectionRow = new HBox(6, newSectionField, addSectionButton, sortSectionsButton);
        addSectionRow.setPadding(new Insets(4, 0, 4, 0));
        HBox.setHgrow(newSectionField, Priority.ALWAYS);

        VBox leftBox = new VBox(
                6,
                sectionsLabel,
                addSectionRow,
                sectionTabs,
                notesLabel,
                notesListView
        );
        leftBox.setPadding(new Insets(8));
        leftBox.setPrefWidth(280);
        VBox.setVgrow(sectionTabs, Priority.NEVER);
        VBox.setVgrow(notesListView, Priority.ALWAYS);

        // Center editor
        VBox editorBox = new VBox(8, titleField, bodyArea);
        editorBox.setPadding(new Insets(8));
        VBox.setVgrow(bodyArea, Priority.ALWAYS);

        // Bottom bar
        HBox bottomBar = new HBox(8, newButton, saveButton, deleteButton, restoreButton);
        bottomBar.setPadding(new Insets(8));

        BorderPane pane = new BorderPane();
        pane.setTop(topBar);
        pane.setLeft(leftBox);
        pane.setCenter(editorBox);
        pane.setBottom(bottomBar);

        return pane;
    }

    private void wireEvents() {
        // Section tab selection → set active section + refresh notes
        sectionTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && newTab.getUserData() instanceof Section s) {
                controller.setActiveSection(s.getId());
            } else {
                controller.setActiveSection(null);
            }
            currentNote = null;
            titleField.clear();
            bodyArea.clear();
            if (!showingTrash) {
                refreshNotesList(controller.getListOfNotes());
            }
        });

        addSectionButton.setOnAction(e -> {
            String name = newSectionField.getText();
            if (name == null || name.isBlank()) {
                return;
            }
            Section created = controller.createSection(name);
            controller.setActiveSection(created.getId());
            newSectionField.clear();
            refreshSectionsList();
        });

        // Toggle sort A-Z / Z-A for sections
        sortSectionsButton.setOnAction(e -> {
            List<Section> sections = controller.listSections();
            if (sections.isEmpty()) {
                return;
            }

            Section active = controller.getActiveSection();

            if (sectionSortAscending) {
                // A → Z
                sections.sort(Comparator.comparing(
                        (Section s) -> s.getName() == null ? "" : s.getName(),
                        String.CASE_INSENSITIVE_ORDER
                ));
                sortSectionsButton.setText("Sort Z-A");
            } else {
                // Z → A
                sections.sort(Comparator.comparing(
                        (Section s) -> s.getName() == null ? "" : s.getName(),
                        String.CASE_INSENSITIVE_ORDER.reversed()
                ));
                sortSectionsButton.setText("Sort A-Z");
            }

            reloadTabsWithOrder(sections, active);
            sectionSortAscending = !sectionSortAscending;
        });

        // Notes selection
        notesListView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
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

            autoSaveIfNeeded();

            currentNote = controller.newNote();

            suppressAutoSaveOnSelection = true;
            refreshNotesList(controller.getListOfNotes());
            notesListView.getSelectionModel().select(currentNote);
            suppressAutoSaveOnSelection = false;

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

        searchButton.setOnAction(e -> performSearch());
        searchField.setOnAction(e -> performSearch());

        trashButton.setOnAction(e -> {
            autoSaveIfNeeded();

            showingTrash = !showingTrash;

            if (showingTrash) {
                // show ALL deleted notes, regardless of section
                refreshNotesList(controller.getDeletedNotes());
            } else {
                refreshNotesList(controller.getListOfNotes());
            }

            currentNote = null;
            titleField.clear();
            bodyArea.clear();

            updateModeUI();
        });
    }

    private void performSearch() {
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
    }

    private void refreshSectionsList() {
        List<Section> sections = controller.listSections();
        sections.sort(Comparator.comparing(
                (Section s) -> s.getName() == null ? "" : s.getName(),
                String.CASE_INSENSITIVE_ORDER
        ));

        // If no sections exist yet, create a default one
        if (sections.isEmpty()) {
            Section created = controller.createSection("General");
            controller.setActiveSection(created.getId());
            sections = controller.listSections();
            sections.sort(Comparator.comparing(
                    (Section s) -> s.getName() == null ? "" : s.getName(),
                    String.CASE_INSENSITIVE_ORDER
            ));
        }

        Section active = controller.getActiveSection();
        reloadTabsWithOrder(sections, active);
    }

    private void reloadTabsWithOrder(List<Section> sections, Section active) {
        sectionTabs.getTabs().clear();

        for (Section s : sections) {
            String tabTitle = (s.getName() == null || s.getName().isBlank())
                    ? "(Untitled Section)"
                    : s.getName();
            Tab tab = new Tab(tabTitle);
            tab.setUserData(s);
            sectionTabs.getTabs().add(tab);
        }

        if (!sectionTabs.getTabs().isEmpty()) {
            if (active != null) {
                for (Tab tab : sectionTabs.getTabs()) {
                    Section s = (Section) tab.getUserData();
                    if (s != null && s.getId().equals(active.getId())) {
                        sectionTabs.getSelectionModel().select(tab);
                        return;
                    }
                }
            }
            // If no active section could be matched, select first
            Tab first = sectionTabs.getTabs().get(0);
            sectionTabs.getSelectionModel().select(first);
            if (first.getUserData() instanceof Section s) {
                controller.setActiveSection(s.getId());
            }
        }
    }

    private void refreshNotesList(List<Note> notes) {
        notesListView.setItems(FXCollections.observableArrayList(notes));
    }

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

    private void updateModeUI() {
        if (showingTrash) {
            modeLabel.setText("Trash (Deleted Notes)");
            trashButton.setText("Back to Notes");

            // Hide section UI in trash mode
            setNodeVisibleManaged(sectionsLabel, false);
            setNodeVisibleManaged(sectionTabs, false);
            setNodeVisibleManaged(newSectionField, false);
            setNodeVisibleManaged(addSectionButton, false);
            setNodeVisibleManaged(sortSectionsButton, false);

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

            restoreButton.setVisible(true);
            restoreButton.setManaged(true);

            deleteButton.setText("Delete Permanently");

            titleField.setEditable(false);
            bodyArea.setEditable(false);

        } else {
            modeLabel.setText("Notes");
            trashButton.setText("Trash");

            // Show section UI in normal mode
            setNodeVisibleManaged(sectionsLabel, true);
            setNodeVisibleManaged(sectionTabs, true);
            setNodeVisibleManaged(newSectionField, true);
            setNodeVisibleManaged(addSectionButton, true);
            setNodeVisibleManaged(sortSectionsButton, true);

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

            restoreButton.setVisible(false);
            restoreButton.setManaged(false);

            deleteButton.setText("Delete");

            titleField.setEditable(true);
            bodyArea.setEditable(true);

            searchField.setPromptText("Search...");
        }
    }

    private void setNodeVisibleManaged(javafx.scene.Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }
}
