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
