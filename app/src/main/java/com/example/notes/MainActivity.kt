package com.example.notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.example.notes.app.AppController
import com.example.notes.data.InMemoryLocalStorage
import com.example.notes.data.LocalStorage
import com.example.notes.data.SystemClock
import com.example.notes.data.Trash
import com.example.notes.domain.NoteRepository
import com.example.notes.domain.SearchIndex
import com.example.notes.domain.SectionRepository
import com.example.notes.domain.SortPreference
import java.io.File

class MainActivity : ComponentActivity() {

    private lateinit var controller: AppController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val notesPath = File(filesDir, "notes.dat").toPath()
        val storage: LocalStorage = InMemoryLocalStorage(notesPath)

        val clock = SystemClock()
        val trash = Trash(30, clock)
        val noteRepo = NoteRepository(storage, clock)
        val searchIndex = SearchIndex.getInstance()
        val sortPref = SortPreference(storage)
        val sectionRepo = SectionRepository(storage, clock)

        controller = AppController(
            noteRepo,
            trash,
            searchIndex,
            sortPref,
            sectionRepo
        )

        setContent {
            MaterialTheme {
                NotesScreen(controller = controller)
            }
        }
    }
}
