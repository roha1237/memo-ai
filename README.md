# MemoAI — Smart Notes

MemoAI is a modern, lightweight, AI-ready notes application built natively for Android using Kotlin and Jetpack Compose. It combines frictionless real-time note-taking, local SQLite persistence, smart categorization, and a pluggable architecture for Google Gemini AI features.

## Key Features

1. **Core Note-Taking & Editing**
   - Instant debounced auto-save with visual indicator ("Saved", "Saving...").
   - Rich note attributes: title, body, category badge, and multiple `#tags`.
   - Card color accents (Indigo, Emerald, Amber, Rose, Violet, Cyan).
   - Word and character count tracking in real time.

2. **Organization & Discovery**
   - Multi-category sorting: General, Work, Personal, Ideas, Study, To-Do.
   - Live full-text search filtering across title, content, tags, and categories.
   - Pinned notes visually organized in a top shelf.
   - Starred favorites with dedicated tab.
   - Staggered grid vs. Linear list toggle.
   - Sorting by Last Modified (newest/oldest), Created Date, and Title (A–Z).

3. **Trash & Safety**
   - Soft-delete system prevents accidental data loss.
   - Interactive Snackbar with one-tap "Undo" after note deletion.
   - Dedicated Trash screen with single note restore or permanent removal.
   - Confirmation dialogs for all destructive operations.

4. **Data Portability & Offline Persistence**
   - **Local Storage**: Built on Room SQLite database (`Flow<List<NoteEntity>>`) for reliable offline operation.
   - **Export**: Generates standard JSON backups with one-tap copy and Android Share Sheet integration.
   - **Import**: Robust JSON parser with schema validation, malformed input error handling, and safe merging.

5. **AI-Ready Architecture (Google Gemini)**
   - Pluggable AI Assistant supporting:
     - **Summarize**: 2–3 concise bullet points with key takeaways.
     - **Polish & Rewrite**: Professional, clear prose keeping facts intact.
     - **Extract Action Points**: Generates checklist tasks (`- [ ] item`).
   - Clean transparent status: When `GEMINI_API_KEY` is configured in the AI Studio Secrets panel, live API calls are dispatched. If unconfigured, a clear explanatory card informs the user rather than simulating fake data.
   - One-tap "Append to Note" or "Replace Note" workflows.

6. **Adaptive & Responsive UX**
   - **Phone**: Compact bottom navigation bar with fluid screen transitions.
   - **Tablet / Large Screens**: Navigation Rail on the left and dual-pane list-detail layout.
   - **Dark Mode by Default**: Designed with deep slate surfaces and high-contrast typography, with light and system mode options.

## Project Structure

```
app/src/main/java/com/example/
├── MainActivity.kt                      # Main Activity hosting MemoApp and theme
├── data/
│   ├── local/
│   │   ├── NoteEntity.kt               # Room database table entity
│   │   ├── NoteDao.kt                  # Reactive queries and database operations
│   │   ├── AppDatabase.kt              # Room Database singleton with seed notes
│   │   └── NoteRepository.kt           # Repository abstraction layer
│   ├── preferences/
│   │   └── UserPreferences.kt          # Theme, layout, and sort preferences
│   ├── ai/
│   │   └── AiService.kt                # Gemini API client & task orchestrator
│   └── backup/
│       └── BackupManager.kt            # JSON export and import validator
├── ui/
│   ├── theme/
│   │   ├── Color.kt                    # M3 dark/light palettes & note tint tokens
│   │   ├── Theme.kt                    # MemoTheme setup with dark mode default
│   │   └── Type.kt                     # Typography configuration
│   ├── model/
│   │   └── NoteUiModel.kt              # Preset categories, colors, tag utilities
│   ├── components/
│   │   ├── SaveStatusIndicator.kt      # Animated save pill
│   │   ├── NoteCard.kt                 # Interactive note card
│   │   ├── SearchBar.kt                # Search input with layout & sort menus
│   │   ├── CategoryChips.kt            # Horizontal category & tag filters
│   │   ├── EmptyStateView.kt           # Empty state with visual illustration
│   │   ├── ConfirmationDialog.kt       # Destructive action confirmation dialog
│   │   └── AiBottomSheet.kt            # AI Assistant bottom sheet
│   ├── screens/
│   │   ├── NotesDashboardScreen.kt     # Main notes feed
│   │   ├── NoteEditorScreen.kt         # Full auto-saving note editor
│   │   ├── FavoritesScreen.kt          # Priority starred notes
│   │   ├── TrashScreen.kt              # Soft-deleted notes & restoration
│   │   └── SettingsScreen.kt           # Theme, JSON backup, AI config, stats
│   └── navigation/
│       ├── NavDestination.kt           # Navigation destinations
│       └── MemoApp.kt                  # Root adaptive layout shell
```

## Build and Run

To compile and verify the Android application:

```bash
# Verify compilation
gradle :app:assembleDebug

# Run unit and Robolectric tests
gradle :app:testDebugUnitTest
```
