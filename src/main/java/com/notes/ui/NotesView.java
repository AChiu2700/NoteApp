package com.notes.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.notes.app.AppController;
import com.notes.model.Note;
import com.notes.model.Section;
import com.notes.sort.SortOrder;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

public class NotesView {

    private final AppController controller;

    private final BorderPane root;

    // Sections UI (normal mode)
    private final TabPane sectionTabs;
    private final TextField newSectionField;
    private final Button addSectionButton;
    private final Button sortSectionsButton;
    private final Button deleteSectionButton;
    private final Label sectionsLabel;
    private final HBox sectionHeaderRow;
    private final HBox addSectionRow;

    // Deleted sections UI (trash mode)
    private final Label deletedSectionsLabel;
    private final ListView<Section> deletedSectionsList;
    private final Button restoreSectionButton;
    private final Button purgeSectionButton;
    private final HBox deletedSectionButtonsRow;

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
    private final Button moveNotesButton;
    private final Button deleteButton;
    private final Button restoreButton;
    private final Label modeLabel;
    private final Label sortLabel;

    private boolean sectionSortAscending = true;

    private Note currentNote;
    private boolean showingTrash = false;
    private boolean suppressAutoSaveOnSelection = false;

    public NotesView(AppController controller) {
        this.controller = controller;

        // Sections (normal mode)
        sectionTabs = new TabPane();
        sectionTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        sectionsLabel = new Label("Sections");
        newSectionField = new TextField();
        newSectionField.setPromptText("New section name");
        addSectionButton = new Button("Add");
        sortSectionsButton = new Button("Sort A-Z");
        deleteSectionButton = new Button("Delete Section");

        sectionHeaderRow = new HBox(6, sectionsLabel, sortSectionsButton, deleteSectionButton);
        sectionHeaderRow.setPadding(new Insets(4, 0, 0, 0));

        addSectionRow = new HBox(6, newSectionField, addSectionButton);
        addSectionRow.setPadding(new Insets(4, 0, 4, 0));
        HBox.setHgrow(newSectionField, Priority.ALWAYS);

        // Deleted sections (trash mode)
        deletedSectionsLabel = new Label("Deleted Sections");
        deletedSectionsList = new ListView<>();
        restoreSectionButton = new Button("Restore Section");
        purgeSectionButton = new Button("Delete Section Permanently");
        deletedSectionButtonsRow = new HBox(6, restoreSectionButton, purgeSectionButton);

        // Notes
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
        moveNotesButton = new Button("Move Notes...");
        deleteButton = new Button("Delete");
        restoreButton = new Button("Restore");
        trashButton = new Button("Trash");

        modeLabel = new Label("Notes");
        sortLabel = new Label("Sort:");

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

        VBox leftBox = new VBox(
                6,
                sectionHeaderRow,
                addSectionRow,
                sectionTabs,
                deletedSectionsLabel,
                deletedSectionsList,
                deletedSectionButtonsRow,
                notesLabel,
                notesListView
        );
        leftBox.setPadding(new Insets(8));
        leftBox.setPrefWidth(320);
        VBox.setVgrow(sectionTabs, Priority.NEVER);
        VBox.setVgrow(deletedSectionsList, Priority.NEVER);
        VBox.setVgrow(notesListView, Priority.ALWAYS);

        VBox editorBox = new VBox(8, titleField, bodyArea);
        editorBox.setPadding(new Insets(8));
        VBox.setVgrow(bodyArea, Priority.ALWAYS);

        HBox bottomBar = new HBox(8, newButton, saveButton, moveNotesButton, deleteButton, restoreButton);
        bottomBar.setPadding(new Insets(8));

        BorderPane pane = new BorderPane();
        pane.setTop(topBar);
        pane.setLeft(leftBox);
        pane.setCenter(editorBox);
        pane.setBottom(bottomBar);

        return pane;
    }

    private void wireEvents() {
        // Section tab change (normal mode)
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

        // Toggle sections sort A-Z / Z-A
        sortSectionsButton.setOnAction(e -> {
            List<Section> sections = new ArrayList<>(controller.listSections());
            if (sections.isEmpty()) {
                return;
            }

            Section active = controller.getActiveSection();

            if (sectionSortAscending) {
                sections.sort(Comparator.comparing(
                        (Section s) -> s.getName() == null ? "" : s.getName(),
                        String.CASE_INSENSITIVE_ORDER
                ));
                sortSectionsButton.setText("Sort Z-A");
            } else {
                sections.sort(Comparator.comparing(
                        (Section s) -> s.getName() == null ? "" : s.getName(),
                        String.CASE_INSENSITIVE_ORDER.reversed()
                ));
                sortSectionsButton.setText("Sort A-Z");
            }

            reloadTabsWithOrder(sections, active);
            sectionSortAscending = !sectionSortAscending;
        });

        deleteSectionButton.setOnAction(e -> {
            Tab selectedTab = sectionTabs.getSelectionModel().getSelectedItem();
            if (selectedTab == null || !(selectedTab.getUserData() instanceof Section s)) {
                return;
            }
            controller.deleteSection(s.getId());
            refreshSectionsList();
            refreshNotesList(controller.getListOfNotes());
        });

        // Deleted sections actions
        restoreSectionButton.setOnAction(e -> {
            Section s = deletedSectionsList.getSelectionModel().getSelectedItem();
            if (s == null) return;
            controller.restoreSection(s.getId());
            refreshDeletedSectionsList();
            refreshSectionsList();
            refreshDeletedNotesForSelection();
        });

        purgeSectionButton.setOnAction(e -> {
            Section s = deletedSectionsList.getSelectionModel().getSelectedItem();
            if (s == null) return;
            controller.purgeSection(s.getId());
            refreshDeletedSectionsList();
            refreshDeletedNotesForSelection();
        });

        // When user clicks a deleted section, show that section's deleted notes
        deletedSectionsList.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (showingTrash) {
                refreshDeletedNotesForSelection();
            }
        });

        // Note selection
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

        moveNotesButton.setOnAction(e -> {
            if (showingTrash) {
                return;
            }
            if (root.getScene() == null) {
                return;
            }
            MoveNotesDialog dialog = new MoveNotesDialog(
                    controller,
                    root.getScene().getWindow(),
                    () -> {
                        refreshSectionsList();
                        refreshNotesList(controller.getListOfNotes());
                    }
            );
            dialog.show();
        });

        // Delete note
        deleteButton.setOnAction(e -> {
            Note selected = notesListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }

            if (!showingTrash) {
                autoSaveIfNeeded();
                controller.deleteNote(selected.getId());
                refreshNotesList(controller.getListOfNotes());
            } else {
                controller.emptyTrash(List.of(selected.getId()));
                refreshDeletedNotesForSelection();
            }

            currentNote = null;
            titleField.clear();
            bodyArea.clear();
        });

        // Restore note in trash – ALWAYS ask which section to use
        restoreButton.setOnAction(e -> {
            if (!showingTrash) {
                return;
            }

            Note selected = notesListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }

            String originalSectionName = null;
            if (selected.getSectionId() != null) {
                Section original = controller.findSectionById(selected.getSectionId());
                if (original != null) {
                    originalSectionName = original.getName();
                }
            }

            String targetSectionId = chooseSectionForRestore(originalSectionName);
            if (targetSectionId == null) {
                return; // user cancelled
            }

            controller.restoreNoteToSection(selected.getId(), targetSectionId);
            refreshDeletedNotesForSelection();

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
                refreshDeletedNotesForSelection();
            }
        });

        searchButton.setOnAction(e -> performSearch());
        searchField.setOnAction(e -> performSearch());

        trashButton.setOnAction(e -> {
            autoSaveIfNeeded();

            showingTrash = !showingTrash;

            if (showingTrash) {
                refreshDeletedSectionsList();
                refreshDeletedNotesForSelection();
            } else {
                refreshSectionsList();
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
        List<Section> sections = new ArrayList<>(controller.listSections());
        sections.sort(Comparator.comparing(
                (Section s) -> s.getName() == null ? "" : s.getName(),
                String.CASE_INSENSITIVE_ORDER
        ));

        if (sections.isEmpty()) {
            Section created = controller.createSection("General");
            controller.setActiveSection(created.getId());
            sections = new ArrayList<>(controller.listSections());
            sections.sort(Comparator.comparing(
                    (Section s) -> s.getName() == null ? "" : s.getName(),
                    String.CASE_INSENSITIVE_ORDER
            ));
        }

        Section active = controller.getActiveSection();
        reloadTabsWithOrder(sections, active);
    }

    private void refreshDeletedSectionsList() {
        List<Section> deleted = new ArrayList<>(controller.listDeletedSections());
        deleted.sort(Comparator.comparing(
                (Section s) -> s.getName() == null ? "" : s.getName(),
                String.CASE_INSENSITIVE_ORDER
        ));
        deletedSectionsList.setItems(FXCollections.observableArrayList(deleted));
    }

    // Show deleted notes belonging to selected deleted section, or all if none selected
    private void refreshDeletedNotesForSelection() {
        List<Note> allDeleted = controller.getDeletedNotes();
        Section selectedSection = deletedSectionsList.getSelectionModel().getSelectedItem();

        List<Note> filtered;
        if (selectedSection == null) {
            filtered = allDeleted;
        } else {
            String targetId = selectedSection.getId();
            filtered = allDeleted.stream()
                    .filter(n -> targetId.equals(n.getSectionId()))
                    .toList();
        }

        refreshNotesList(filtered);
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
            modeLabel.setText("Trash (Deleted Items)");
            trashButton.setText("Back to Notes");

            setNodeVisibleManaged(sectionHeaderRow, false);
            setNodeVisibleManaged(addSectionRow, false);
            setNodeVisibleManaged(sectionTabs, false);

            setNodeVisibleManaged(deletedSectionsLabel, true);
            setNodeVisibleManaged(deletedSectionsList, true);
            setNodeVisibleManaged(deletedSectionButtonsRow, true);

            notesLabel.setText("Deleted Notes");

            newButton.setVisible(false);
            newButton.setManaged(false);

            saveButton.setVisible(false);
            saveButton.setManaged(false);

            moveNotesButton.setVisible(false);
            moveNotesButton.setManaged(false);

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

            setNodeVisibleManaged(sectionHeaderRow, true);
            setNodeVisibleManaged(addSectionRow, true);
            setNodeVisibleManaged(sectionTabs, true);

            setNodeVisibleManaged(deletedSectionsLabel, false);
            setNodeVisibleManaged(deletedSectionsList, false);
            setNodeVisibleManaged(deletedSectionButtonsRow, false);

            notesLabel.setText("Notes");

            newButton.setVisible(true);
            newButton.setManaged(true);

            saveButton.setVisible(true);
            saveButton.setManaged(true);

            moveNotesButton.setVisible(true);
            moveNotesButton.setManaged(true);

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

    // Small dialog to choose a target section to restore into
    private String chooseSectionForRestore(String oldSectionName) {
        List<Section> sections = new ArrayList<>(controller.listSections());
        if (sections.isEmpty()) {
            Section created = controller.createSection("General");
            sections.add(created);
        }

        Stage dialog = new Stage();
        dialog.initOwner(root.getScene().getWindow());
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Choose Section for Restore");

        String oldName = (oldSectionName == null || oldSectionName.isBlank())
                ? "(deleted/missing section)"
                : oldSectionName;

        Label label = new Label(
                "This note previously lived in \"" + oldName + "\".\n" +
                "Choose a section to restore it under:"
        );

        ComboBox<Section> box = new ComboBox<>();
        box.setItems(FXCollections.observableArrayList(sections));
        box.getSelectionModel().select(0);

        Button ok = new Button("OK");
        Button cancel = new Button("Cancel");

        HBox buttonRow = new HBox(8, ok, cancel);
        buttonRow.setPadding(new Insets(10));
        buttonRow.setSpacing(10);

        VBox content = new VBox(10, label, box, buttonRow);
        content.setPadding(new Insets(15));

        BorderPane pane = new BorderPane(content);
        Scene scene = new Scene(pane, 420, 170);
        dialog.setScene(scene);

        final String[] resultHolder = new String[1];
        resultHolder[0] = null;

        ok.setOnAction(ev -> {
            Section chosen = box.getValue();
            if (chosen != null) {
                resultHolder[0] = chosen.getId();
            }
            dialog.close();
        });

        cancel.setOnAction(ev -> {
            resultHolder[0] = null;
            dialog.close();
        });

        dialog.showAndWait();
        return resultHolder[0];
    }
}
