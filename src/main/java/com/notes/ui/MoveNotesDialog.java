package com.notes.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.notes.app.AppController;
import com.notes.model.Note;
import com.notes.model.Section;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class MoveNotesDialog extends Stage {

    private final AppController controller;
    private final Runnable onSaveCallback;

    public MoveNotesDialog(AppController controller, Window owner, Runnable onSaveCallback) {
        this.controller = controller;
        this.onSaveCallback = onSaveCallback;

        setTitle("Move Notes Between Sections");
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);

        ComboBox<Section> fromSectionBox = new ComboBox<>();
        ComboBox<Section> toSectionBox = new ComboBox<>();
        fromSectionBox.setPromptText("From section");
        toSectionBox.setPromptText("To section");

        ListView<Note> fromNotesList = new ListView<>();
        ListView<Note> toNotesList = new ListView<>();

        List<Section> sections = new ArrayList<>(controller.listSections());
        sections.sort(Comparator.comparing(
                (Section s) -> s.getName() == null ? "" : s.getName(),
                String.CASE_INSENSITIVE_ORDER
        ));
        fromSectionBox.setItems(FXCollections.observableArrayList(sections));
        toSectionBox.setItems(FXCollections.observableArrayList(sections));

        if (!sections.isEmpty()) {
            Section active = controller.getActiveSection();
            if (active != null) {
                fromSectionBox.setValue(active);
            } else {
                fromSectionBox.setValue(sections.get(0));
            }
            if (sections.size() > 1) {
                toSectionBox.setValue(sections.get(1));
            }
        }

        Runnable refreshLists = () -> {
            Section from = fromSectionBox.getValue();
            Section to = toSectionBox.getValue();

            if (from != null) {
                List<Note> fromNotes = controller.getNotesForSection(from.getId());
                fromNotesList.setItems(FXCollections.observableArrayList(fromNotes));
            } else {
                fromNotesList.setItems(FXCollections.emptyObservableList());
            }

            if (to != null) {
                List<Note> toNotes = controller.getNotesForSection(to.getId());
                toNotesList.setItems(FXCollections.observableArrayList(toNotes));
            } else {
                toNotesList.setItems(FXCollections.emptyObservableList());
            }
        };

        fromSectionBox.setOnAction(e -> refreshLists.run());
        toSectionBox.setOnAction(e -> refreshLists.run());

        Button moveRightButton = new Button(">");
        Button moveLeftButton = new Button("<");

        moveRightButton.setOnAction(e -> {
            Note selected = fromNotesList.getSelectionModel().getSelectedItem();
            Section target = toSectionBox.getValue();
            if (selected == null || target == null) return;
            controller.moveNoteToSection(selected.getId(), target.getId());
            refreshLists.run();
        });

        moveLeftButton.setOnAction(e -> {
            Note selected = toNotesList.getSelectionModel().getSelectedItem();
            Section target = fromSectionBox.getValue();
            if (selected == null || target == null) return;
            controller.moveNoteToSection(selected.getId(), target.getId());
            refreshLists.run();
        });

        Button saveButton = new Button("Save");
        Button closeButton = new Button("Close");

        saveButton.setOnAction(e -> {
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            close();
        });

        closeButton.setOnAction(e -> close());

        HBox sectionSelectors = new HBox(8,
                new Label("From:"), fromSectionBox,
                new Label("To:"), toSectionBox
        );
        sectionSelectors.setPadding(new Insets(10));
        HBox.setHgrow(fromSectionBox, Priority.ALWAYS);
        HBox.setHgrow(toSectionBox, Priority.ALWAYS);

        VBox arrows = new VBox(10, moveRightButton, moveLeftButton);
        arrows.setAlignment(Pos.CENTER);

        VBox leftBox = new VBox(4, new Label("From Section Notes"), fromNotesList);
        VBox rightBox = new VBox(4, new Label("To Section Notes"), toNotesList);
        VBox.setVgrow(fromNotesList, Priority.ALWAYS);
        VBox.setVgrow(toNotesList, Priority.ALWAYS);

        HBox center = new HBox(10, leftBox, arrows, rightBox);
        center.setPadding(new Insets(10));
        HBox.setHgrow(leftBox, Priority.ALWAYS);
        HBox.setHgrow(rightBox, Priority.ALWAYS);

        HBox bottomBar = new HBox(10, saveButton, closeButton);
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(sectionSelectors);
        root.setCenter(center);
        root.setBottom(bottomBar);

        Scene scene = new Scene(root, 700, 400);
        setScene(scene);

        refreshLists.run();
    }
}
