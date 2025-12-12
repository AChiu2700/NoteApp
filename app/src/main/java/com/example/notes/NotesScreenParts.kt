package com.example.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.notes.app.AppController
import com.example.notes.model.Note
import com.example.notes.model.Section

@Composable
internal fun rememberFilteredSortedNotes(
    notes: List<Note>,
    searchQuery: String,
    sortOption: SortOption
): List<Note> {
    return androidx.compose.runtime.remember(notes, searchQuery, sortOption) {
        var filtered = notes
        if (searchQuery.isNotBlank()) {
            val query = searchQuery.lowercase()
            filtered = filtered.filter { note ->
                (note.getTitle() ?: "").lowercase().contains(query) ||
                        (note.getContent() ?: "").lowercase().contains(query)
            }
        }

        when (sortOption) {
            SortOption.LastModified ->
                filtered.sortedByDescending { it.getUpdatedAt() }
            SortOption.CreatedDate ->
                filtered.sortedByDescending { it.getCreatedAt() }
            SortOption.TitleAZ ->
                filtered.sortedBy { (it.getTitle() ?: "").lowercase() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotesScreenContent(
    padding: PaddingValues,
    mode: NotesMode,
    isInTrash: Boolean,
    notes: List<Note>,
    searchQuery: String,
    sortOption: SortOption,
    sortMenuExpanded: Boolean,
    onSearchChange: (String) -> Unit,
    onSortClick: () -> Unit,
    onSortMenuDismiss: () -> Unit,
    onSortSelected: (SortOption) -> Unit,
    onOpenNote: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onMoveNote: (Note) -> Unit,
    title: String,
    body: String,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onBackFromEditor: () -> Unit,
    onSaveFromEditor: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
    ) {
        if (mode != NotesMode.EDIT) {
            SearchAndSortRow(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchChange,
                sortOption = sortOption,
                onSortClick = onSortClick
            )

            DropdownMenu(
                expanded = sortMenuExpanded,
                onDismissRequest = onSortMenuDismiss
            ) {
                DropdownMenuItem(
                    text = { Text("Last Modified") },
                    onClick = { onSortSelected(SortOption.LastModified) }
                )
                DropdownMenuItem(
                    text = { Text("Created Date") },
                    onClick = { onSortSelected(SortOption.CreatedDate) }
                )
                DropdownMenuItem(
                    text = { Text("Title A–Z") },
                    onClick = { onSortSelected(SortOption.TitleAZ) }
                )
            }
        }

        when (mode) {
            NotesMode.LIST, NotesMode.TRASH -> {
                NotesList(
                    notes = notes,
                    isTrash = isInTrash,
                    onClick = onOpenNote,
                    onDelete = onDeleteNote,
                    onMove = onMoveNote
                )
            }
            NotesMode.EDIT -> {
                NoteEditorScreen(
                    title = title,
                    body = body,
                    onTitleChange = onTitleChange,
                    onBodyChange = onBodyChange,
                    onBack = onBackFromEditor,
                    onSave = onSaveFromEditor
                )
            }
        }
    }
}

@Composable
internal fun SearchAndSortRow(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    sortOption: SortOption,
    onSortClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Search…") },
            singleLine = true
        )

        IconButton(onClick = onSortClick) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "Sort options"
            )
        }
    }
}

@Composable
internal fun NotesList(
    notes: List<Note>,
    isTrash: Boolean,
    onClick: (Note) -> Unit,
    onDelete: (Note) -> Unit,
    onMove: (Note) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(notes) { note ->
            val title = (note.getTitle() ?: "").ifBlank { "Untitled" }
            val content = note.getContent() ?: ""
            val preview = content
                .lineSequence()
                .map { it.trim() }
                .firstOrNull { it.isNotBlank() }
                .orEmpty()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(note) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (preview.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = preview,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                    if (isTrash) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Tap to restore",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (!isTrash) {
                    IconButton(onClick = { onMove(note) }) {
                        Icon(
                            imageVector = Icons.Filled.SwapHoriz,
                            contentDescription = "Move"
                        )
                    }
                }

                IconButton(onClick = { onDelete(note) }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = if (isTrash) {
                            "Delete permanently"
                        } else {
                            "Delete"
                        }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NoteEditorScreen(
    title: String,
    body: String,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = { Text("") },
                actions = {
                    TextButton(onClick = onSave) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            TextField(
                value = title,
                onValueChange = onTitleChange,
                placeholder = { Text("Title") },
                textStyle = MaterialTheme.typography.headlineSmall,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    disabledContainerColor = MaterialTheme.colorScheme.background
                )
            )

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            TextField(
                value = body,
                onValueChange = onBodyChange,
                placeholder = { Text("Start writing…") },
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxSize(),
                maxLines = Int.MAX_VALUE,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    disabledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SectionSheet(
    sections: List<Section>,
    activeSection: Section?,
    isTrash: Boolean,
    onDismiss: () -> Unit,
    onSectionSelected: (Section) -> Unit,
    onTrashSelected: () -> Unit,
    onAddSection: () -> Unit,
    onRenameSection: (Section) -> Unit,
    onDeleteSection: (Section) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTrash) "Deleted Sections" else "Sections",
                    style = MaterialTheme.typography.titleMedium
                )
                if (!isTrash) {
                    IconButton(onClick = onAddSection) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add section"
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            sections.forEach { section ->
                val name = (section.getName() ?: "").ifBlank { "General" }
                val isActive = activeSection?.getId() == section.getId()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSectionSelected(section) }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (isActive && !isTrash) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Active",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (isTrash) {
                        TextButton(onClick = { onSectionSelected(section) }) {
                            Text("Restore")
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onRenameSection(section) }) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Rename"
                                )
                            }
                            IconButton(onClick = { onDeleteSection(section) }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete"
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTrashSelected() }
                    .padding(vertical = 10.dp)
            ) {
                Text(
                    text = if (isTrash) "Back to Notes" else "Trash",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isTrash) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
internal fun RestoreNoteDialog(
    note: Note,
    sections: List<Section>,
    onDismiss: () -> Unit,
    onRestoreIntoSection: (String) -> Unit
) {
    val title = (note.getTitle() ?: "").ifBlank { "Untitled" }
    val content = note.getContent() ?: ""
    val snippet = content
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("\n")
        .ifBlank { "(No content)" }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restore note") },
        text = {
            Column {
                Text("Preview", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = snippet,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )

                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                Text("Tap a section to restore into:")
                Spacer(Modifier.height(8.dp))

                sections.forEach { section ->
                    val id = section.getId()
                    val name = (section.getName() ?: "").ifBlank { "General" }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (id.isNotBlank()) {
                                    onRestoreIntoSection(id)
                                    onDismiss()
                                }
                            }
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
internal fun NewSectionDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    val (name, setName) = remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New section") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = setName,
                placeholder = { Text("Section name (default: General)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onCreate(name) }) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
internal fun RenameSectionDialog(
    section: Section,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    val (name, setName) = remember { mutableStateOf(section.getName() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename section") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = setName,
                placeholder = { Text("Section name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onRename(name) }) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
internal fun SectionDeleteDialog(
    section: Section,
    onDismiss: () -> Unit,
    onConfirmDelete: (Section) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete section") },
        text = {
            Text(
                "Delete the entire section \"${section.getName() ?: "General"}\" and move its notes to trash?"
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirmDelete(section) }) {
                Text("Delete section")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
internal fun MoveNoteDialog(
    note: Note,
    sections: List<Section>,
    onDismiss: () -> Unit,
    onMoveToSection: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Move note") },
        text = {
            Column {
                Text("Tap a section to move this note into:")
                Spacer(Modifier.height(8.dp))
                sections.forEach { section ->
                    val id = section.getId()
                    val name = (section.getName() ?: "").ifBlank { "General" }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (id.isNotBlank()) {
                                    onMoveToSection(id)
                                    onDismiss()
                                }
                            }
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
internal fun NoteDeleteDialog(
    isInTrash: Boolean,
    noteToDelete: Note,
    onDismiss: () -> Unit,
    onDeleted: (String) -> Unit,
    controller: AppController
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isInTrash) "Delete permanently" else "Delete note")
        },
        text = {
            Text(
                if (isInTrash) {
                    "Delete this note permanently from trash?"
                } else {
                    "Move this note to trash?"
                }
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (!isInTrash) {
                    controller.deleteNote(noteToDelete.getId())
                } else {
                    controller.emptyTrash(listOf(noteToDelete.getId()))
                }
                onDeleted(noteToDelete.getId())
                onDismiss()
            }) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
