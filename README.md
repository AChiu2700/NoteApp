# Notes App (Android)

A clean, section-based notes application built with **Jetpack Compose** and a layered architecture.  
Designed with a minimal, Bear-inspired UI and a strong separation between UI, domain, and storage layers.

---

## ✨ Features

- Create, edit, and delete notes  
- Organize notes into sections (e.g., **General**)  
- Trash support:
  - Move notes to trash
  - Restore notes
  - Permanently delete notes  
- Search notes by title or content  
- Sort notes by:
  - Last modified
  - Created date
  - Title (A–Z)  
- Section management:
  - Create, rename, delete, and restore sections  

---

## 🧱 Architecture

### UI Layer (Kotlin / Jetpack Compose)

- **MainActivity.kt**  
  Android entry point. Sets the Compose content and provides the `AppController` to the UI.

- **NotesScreen.kt**  
  Owns screen-level state (list/edit/trash mode, active section, current note, search/sort state, dialog visibility) and coordinates all user actions with the controller.

- **NotesScreenParts.kt**  
  Reusable composables for:
  - Notes list
  - Search & sort row
  - Note editor
  - Section bottom sheet
  - Dialogs (move, delete, restore notes; create/rename/delete sections)

---

### Application / Domain Layer (Java)

- **AppController.java**  
  Acts as a **Facade**, exposing high-level operations to the UI while hiding repository and storage details.

- **Repositories & Services**
  - `NoteRepository.java`
  - `SectionRepository.java`
  - `Trash.java`
  - `SearchIndex.java`
  - `SortPreference.java`  
  Manage notes, sections, trash lifecycle, indexing, and sorting behavior.

- **MoveNoteToSection.java**  
  Service class responsible for moving notes between sections consistently.

---

### Model Layer (Java)

- **Core entities**
  - `Note.java`
  - `Section.java`
  - `NoteMemento.java` (used to snapshot note state for trash/restore)

- **Supporting types**
  - `SortOrder.java`
  - `Clock.java` / `SystemClock.java`

---

### Storage Layer (Java)

- **LocalStorage.java**  
  Simple key–value storage abstraction (read, write, delete).

- **InMemoryLocalStorage.java**  
  Thread-safe in-memory implementation that persists to a serialized file.

- **AndroidStorageFactory.java**  
  Creates a `LocalStorage` instance backed by `notes.dat` in the app’s internal storage directory.

---

## ✅ Prerequisites

- Android Studio (Giraffe or newer recommended)
- Android SDK 21+
- JDK 8+

---

## ▶️ Running the App

1. Open the project in Android Studio  
2. Let Gradle sync and indexing complete  
3. Select an emulator or connected device  
4. Click **Run** to build and deploy  

On first launch, the app automatically creates a default **General** section if none exist.

---

## 🧼 Clean Code & Design Patterns

- **Facade Pattern** via `AppController` to decouple UI from repositories and storage
- **Adapter-like abstraction** using `LocalStorage` to adapt persistence for Android
- Refactored using Clean Code principles:
  - Clear, intention-revealing names
  - Small, single-purpose functions and composables
  - Explicit types (`List<Note>`, `List<Section>`)
  - Minimal and focused public APIs

---

## 🚀 Future Improvements

- Add unit and instrumented tests for the domain layer  
- Revamp the UI  
- Add export/import support or cloud sync
