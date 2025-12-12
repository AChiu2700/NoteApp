package com.example.notes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow
import com.example.notes.app.AppController
import com.example.notes.model.Note
import com.example.notes.model.Section

enum class NotesMode {
    LIST,
    EDIT,
    TRASH
}

enum class SortOption {
    LastModified,
    CreatedDate,
    TitleAZ
}

private data class EditorState(
    val currentNote: Note?,
    val title: String,
    val body: String
)

private data class DialogState(
    val showSectionSheet: Boolean,
    val showRestoreDialog: Boolean,
    val noteToRestore: Note?,
    val showDeleteNoteDialog: Boolean,
    val noteToDelete: Note?,
    val showNewSectionDialog: Boolean,
    val showDeleteSectionDialog: Boolean,
    val sectionToDelete: Section?,
    val showMoveNoteDialog: Boolean,
    val noteToMove: Note?,
    val showRenameSectionDialog: Boolean,
    val sectionToRename: Section?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(controller: AppController) {
    var mode by remember { mutableStateOf(NotesMode.LIST) }
    var notes by remember { mutableStateOf(emptyList<Note>()) }
    var sections by remember { mutableStateOf(emptyList<Section>()) }
    var activeSection by remember { mutableStateOf<Section?>(null) }

    var editorState by remember {
        mutableStateOf(
            EditorState(
                currentNote = null,
                title = "",
                body = ""
            )
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(SortOption.LastModified) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    var dialogState by remember {
        mutableStateOf(
            DialogState(
                showSectionSheet = false,
                showRestoreDialog = false,
                noteToRestore = null,
                showDeleteNoteDialog = false,
                noteToDelete = null,
                showNewSectionDialog = false,
                showDeleteSectionDialog = false,
                sectionToDelete = null,
                showMoveNoteDialog = false,
                noteToMove = null,
                showRenameSectionDialog = false,
                sectionToRename = null
            )
        )
    }

    val isInTrash = mode == NotesMode.TRASH

    fun ensureDefaultSection() {
        val existingSections = controller.listSections()
        if (existingSections.isEmpty()) {
            val created = controller.createSection("General")
            controller.setActiveSection(created.id)
        } else if (controller.getActiveSection() == null) {
            controller.setActiveSection(existingSections.first().id)
        }
    }

    fun reloadSections() {
        if (mode != NotesMode.TRASH) {
            ensureDefaultSection()
            sections = controller.listSections()
            activeSection = controller.getActiveSection()
        } else {
            sections = controller.listDeletedSections()
            activeSection = null
        }
    }

    fun reloadNotes() {
        notes = when (mode) {
            NotesMode.LIST -> controller.getListOfNotes() as List<Note>
            NotesMode.TRASH -> controller.getDeletedNotes() as List<Note>
            NotesMode.EDIT -> notes
        }
    }

    fun autoSaveCurrentNoteIfPresent() {
        val note = editorState.currentNote ?: return
        controller.editNote(note.id, editorState.title, editorState.body)
        if (mode != NotesMode.TRASH) {
            notes = controller.getListOfNotes() as List<Note>
        }
    }

    fun openNoteForEditing(note: Note) {
        autoSaveCurrentNoteIfPresent()
        editorState = editorState.copy(
            currentNote = note,
            title = note.title ?: "",
            body = note.content ?: ""
        )
        mode = NotesMode.EDIT
    }

    fun clearEditorState() {
        editorState = editorState.copy(
            currentNote = null,
            title = "",
            body = ""
        )
    }

    LaunchedEffect(mode) {
        reloadSections()
        reloadNotes()
    }

    val filteredAndSortedNotes = rememberFilteredSortedNotes(
        notes = notes,
        searchQuery = searchQuery,
        sortOption = sortOption
    )

    val screenTitle = when (mode) {
        NotesMode.TRASH -> "Trash"
        NotesMode.EDIT -> activeSection?.name ?: "Notes"
        NotesMode.LIST -> activeSection?.name ?: "Notes"
    }

    Scaffold(
        topBar = {
            if (mode != NotesMode.EDIT) {
                NotesTopBar(
                    title = screenTitle,
                    isInTrash = isInTrash,
                    onAddNote = {
                        autoSaveCurrentNoteIfPresent()
                        val createdNote = controller.newNote()
                        reloadNotes()
                        openNoteForEditing(createdNote)
                    }
                )
            }
        },
        floatingActionButton = {
            if (mode != NotesMode.EDIT) {
                NotesFab(onClick = {
                    dialogState = dialogState.copy(showSectionSheet = true)
                })
            }
        }
    ) { padding ->
        NotesScreenContent(
            padding = padding,
            mode = mode,
            isInTrash = isInTrash,
            notes = filteredAndSortedNotes,
            searchQuery = searchQuery,
            sortOption = sortOption,
            sortMenuExpanded = sortMenuExpanded,
            onSearchChange = { searchQuery = it },
            onSortClick = { sortMenuExpanded = true },
            onSortMenuDismiss = { sortMenuExpanded = false },
            onSortSelected = {
                sortOption = it
                sortMenuExpanded = false
            },
            onOpenNote = { note ->
                if (isInTrash) {
                    dialogState = dialogState.copy(
                        noteToRestore = note,
                        showRestoreDialog = true
                    )
                } else {
                    openNoteForEditing(note)
                }
            },
            onDeleteNote = { note ->
                dialogState = dialogState.copy(
                    noteToDelete = note,
                    showDeleteNoteDialog = true
                )
            },
            onMoveNote = { note ->
                dialogState = dialogState.copy(
                    noteToMove = note,
                    showMoveNoteDialog = true
                )
            },
            title = editorState.title,
            body = editorState.body,
            onTitleChange = { newTitle ->
                editorState = editorState.copy(title = newTitle)
            },
            onBodyChange = { newBody ->
                editorState = editorState.copy(body = newBody)
            },
            onBackFromEditor = {
                autoSaveCurrentNoteIfPresent()
                mode = NotesMode.LIST
                reloadNotes()
                clearEditorState()
            },
            onSaveFromEditor = {
                autoSaveCurrentNoteIfPresent()
                mode = NotesMode.LIST
                reloadNotes()
                clearEditorState()
            }
        )

        if (dialogState.showSectionSheet) {
            SectionSheet(
                sections = sections,
                activeSection = activeSection,
                isTrash = isInTrash,
                onDismiss = {
                    dialogState = dialogState.copy(showSectionSheet = false)
                },
                onSectionSelected = { section ->
                    if (isInTrash) {
                        controller.restoreSection(section.id)
                        mode = NotesMode.LIST
                        controller.setActiveSection(section.id)
                        activeSection = controller.getActiveSection()
                    } else {
                        autoSaveCurrentNoteIfPresent()
                        controller.setActiveSection(section.id)
                        activeSection = controller.getActiveSection()
                    }
                    reloadSections()
                    reloadNotes()
                    clearEditorState()
                    dialogState = dialogState.copy(showSectionSheet = false)
                },
                onTrashSelected = {
                    autoSaveCurrentNoteIfPresent()
                    mode = if (isInTrash) NotesMode.LIST else NotesMode.TRASH
                    dialogState = dialogState.copy(showSectionSheet = false)
                    clearEditorState()
                    reloadSections()
                    reloadNotes()
                },
                onAddSection = {
                    dialogState = dialogState.copy(
                        showSectionSheet = false,
                        showNewSectionDialog = true
                    )
                },
                onRenameSection = { section ->
                    dialogState = dialogState.copy(
                        showSectionSheet = false,
                        showRenameSectionDialog = true,
                        sectionToRename = section
                    )
                },
                onDeleteSection = { section ->
                    dialogState = dialogState.copy(
                        showDeleteSectionDialog = true,
                        sectionToDelete = section
                    )
                }
            )
        }

        if (dialogState.showNewSectionDialog) {
            NewSectionDialog(
                onDismiss = {
                    dialogState = dialogState.copy(showNewSectionDialog = false)
                },
                onCreate = { name ->
                    val section = controller.createSection(name.ifBlank { "General" })
                    controller.setActiveSection(section.id)
                    dialogState = dialogState.copy(showNewSectionDialog = false)
                    reloadSections()
                    reloadNotes()
                }
            )
        }

        if (dialogState.showRenameSectionDialog && dialogState.sectionToRename != null) {
            RenameSectionDialog(
                section = dialogState.sectionToRename!!,
                onDismiss = {
                    dialogState = dialogState.copy(
                        showRenameSectionDialog = false,
                        sectionToRename = null
                    )
                },
                onRename = { newName ->
                    val section = dialogState.sectionToRename!!
                    controller.renameSection(section.id, newName.ifBlank { "General" })
                    dialogState = dialogState.copy(
                        showRenameSectionDialog = false,
                        sectionToRename = null
                    )
                    reloadSections()
                    reloadNotes()
                }
            )
        }

        if (dialogState.showDeleteSectionDialog && dialogState.sectionToDelete != null) {
            SectionDeleteDialog(
                section = dialogState.sectionToDelete!!,
                onDismiss = {
                    dialogState = dialogState.copy(
                        showDeleteSectionDialog = false,
                        sectionToDelete = null
                    )
                },
                onConfirmDelete = { section ->
                    controller.deleteSection(section.id)
                    if (activeSection?.id == section.id) {
                        controller.setActiveSection(null)
                    }
                    dialogState = dialogState.copy(
                        showDeleteSectionDialog = false,
                        sectionToDelete = null
                    )
                    reloadSections()
                    reloadNotes()
                }
            )
        }

        if (dialogState.showRestoreDialog && dialogState.noteToRestore != null) {
            RestoreNoteDialog(
                note = dialogState.noteToRestore!!,
                sections = controller.listSections(),
                onDismiss = {
                    dialogState = dialogState.copy(
                        showRestoreDialog = false,
                        noteToRestore = null
                    )
                },
                onRestoreIntoSection = { sectionId ->
                    controller.restoreNoteToSection(dialogState.noteToRestore!!.id, sectionId)
                    reloadNotes()
                    dialogState = dialogState.copy(
                        showRestoreDialog = false,
                        noteToRestore = null
                    )
                }
            )
        }

        if (dialogState.showDeleteNoteDialog && dialogState.noteToDelete != null) {
            NoteDeleteDialog(
                isInTrash = isInTrash,
                noteToDelete = dialogState.noteToDelete!!,
                onDismiss = {
                    dialogState = dialogState.copy(
                        showDeleteNoteDialog = false,
                        noteToDelete = null
                    )
                },
                onDeleted = { deletedId ->
                    if (editorState.currentNote?.id == deletedId) {
                        clearEditorState()
                    }
                    reloadNotes()
                },
                controller = controller
            )
        }

        if (dialogState.showMoveNoteDialog && dialogState.noteToMove != null) {
            MoveNoteDialog(
                note = dialogState.noteToMove!!,
                sections = controller.listSections(),
                onDismiss = {
                    dialogState = dialogState.copy(
                        showMoveNoteDialog = false,
                        noteToMove = null
                    )
                },
                onMoveToSection = { sectionId ->
                    controller.moveNoteToSection(dialogState.noteToMove!!.id, sectionId)
                    reloadNotes()
                    dialogState = dialogState.copy(
                        showMoveNoteDialog = false,
                        noteToMove = null
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesTopBar(
    title: String,
    isInTrash: Boolean,
    onAddNote: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actions = {
            if (!isInTrash) {
                IconButton(onClick = onAddNote) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "New note"
                    )
                }
            }
        }
    )
}

@Composable
private fun NotesFab(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = "Sections / Trash"
        )
    }
}
