<<<<<<< HEAD
=======
Source Code Refactoring:

- Strategy Pattern – SortPreference + SortOrder allow pluggable note-sorting behavior.
- Facade Pattern – AppController hides complexity of repository, trash, search, and sorting from the UI.
- Adapter Pattern – InMemoryLocalStorage adapts file-based persistence to the LocalStorage interface.
- Singleton Pattern – SearchIndex ensures only one search index instance is used across the app.
- Memento Pattern – Note (originator), NoteMemento (memento), and Trash (caretaker) capture and manage snapshots of deleted/previous note state.
>>>>>>> parent of 17425a7 (Update README.md)
