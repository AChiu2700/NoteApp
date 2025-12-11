Feature 2 (Arpit Jaysukhbhai Vora) – Move notes between sections & Delete Sections & Sort Sections
-	New Classes: MoveNoteToSection

New Relationships:
-	MoveNoteToSection → NoteRepository
-	MoveNoteToSection → SectionRepository
-	AppController → MoveNoteToSection (integration w/ facade)
Logic update: AppController.moveNoteToSection(noteId, targetSectionId) delegates to MoveNoteToSection, which validates the target section, updates the note’s sectionId, and persists the change so the note appears under the new section.
