## Feature 1 (Viet Nguyen) – Sections (Organizing Notes by Category)

**New Classes**
- `Section`
- `SectionRepository`

**New Relationships**
- `SectionRepository` ↔ `LocalStorage`
- `AppController` → `SectionRepository` (integration with facade)

**New AppController Methods**
- `listSections`
- `createSection`
- `renameSection`
- `deleteSection`
- `setActiveSection`
- `getActiveSection`
- `moveNoteToSection`

**Note Update**
- Added `sectionId` field to link each note to its assigned section.

---

## Feature 2 (Arpit Jaysukhbhai Vora) – Move Notes Between Sections, Delete Sections, Sort Sections

**New Classes**
- `MoveNoteToSection`

**New Relationships**
- `MoveNoteToSection` → `NoteRepository`
- `MoveNoteToSection` → `SectionRepository`
- `AppController` → `MoveNoteToSection` (integration with facade)

**Logic Update**
- `AppController.moveNoteToSection(noteId, targetSectionId)` delegates to `MoveNoteToSection`, which validates the target section, updates the note’s `sectionId`, and persists the change so the note appears under the new section.
