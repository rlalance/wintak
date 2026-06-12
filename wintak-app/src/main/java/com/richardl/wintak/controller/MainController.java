package com.richardl.wintak.controller;

import com.richardl.wintak.model.Dependency;
import com.richardl.wintak.model.GanttDocument;
import com.richardl.wintak.model.GanttTask;
import com.richardl.wintak.model.persistence.GanttDocumentStore;
import com.richardl.wintak.view.theme.ThemeManager;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.application.Platform;
import javafx.beans.value.ObservableBooleanValue;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * Command layer for the application chrome. Views call commands; state flows back through
 * observable properties. Window-level effects (closing, file dialogs, prompts) go through
 * injectable seams so the controller stays testable without a Stage.
 */
public class MainController {

    private final ObjectProperty<GanttDocument> document =
            new SimpleObjectProperty<>(this, "document", new GanttDocument());
    private final ObjectProperty<Path> currentFile =
            new SimpleObjectProperty<>(this, "currentFile", null);
    private final GanttDocumentStore store = new GanttDocumentStore();
    private final ThemeManager themeManager = new ThemeManager();
    private final ObjectProperty<GanttTask> selectedTask =
            new SimpleObjectProperty<>(this, "selectedTask", null);
    private final ObjectProperty<Dependency> selectedDependency =
            new SimpleObjectProperty<>(this, "selectedDependency", null);
    private final ObjectProperty<ToolMode> toolMode =
            new SimpleObjectProperty<>(this, "toolMode", ToolMode.SELECT) {
                @Override
                public void set(ToolMode value) {
                    super.set(java.util.Objects.requireNonNull(value, "toolMode"));
                }
            };
    /* Enablement state. saveAvailable follows the *current* document's dirty flag even across
       document swaps (explicit re-hook - lazy select-bindings go stale). */
    private final BooleanProperty saveAvailable = new SimpleBooleanProperty(this, "saveAvailable", false);
    private final UndoStack undoStack = new UndoStack();
    private final BooleanBinding deleteAvailable =
            selectedTask.isNotNull().or(selectedDependency.isNotNull());
    private AutoSaveCoalescer autoSaveCoalescer = AutoSaveCoalescer.NONE;
    private final InvalidationListener dirtyRelay = obs -> {
        saveAvailable.set(getDocument().isDirty());
        refreshTitle();
        if (getDocument().isDirty() && currentFile.get() != null) {
            autoSaveCoalescer.notifyChanged();
        }
    };
    private final IntegerProperty taskCount = new SimpleIntegerProperty(this, "taskCount", 0);
    private final InvalidationListener countRelay = obs -> taskCount.set(getDocument().getTasks().size());
    /* Timeline zoom: horizontal scale in pixels per day, geometric steps, clamped. */
    public static final double MIN_PX_PER_DAY = 2;
    public static final double MAX_PX_PER_DAY = 200;
    public static final double DEFAULT_PX_PER_DAY = 20;
    private static final double ZOOM_STEP = 1.25;

    private final DoubleProperty zoomPxPerDay =
            new SimpleDoubleProperty(this, "zoomPxPerDay", DEFAULT_PX_PER_DAY);
    /* Transient status line: setStatus shows a message and schedules its expiry; a stale
       expiry is detected by identity so it never wipes a newer message. */
    public static final Duration STATUS_LINGER = Duration.ofSeconds(5);
    private final StringProperty statusMessage = new SimpleStringProperty(this, "statusMessage", "");
    private long statusToken = 0;

    private final StringProperty windowTitle =
            new SimpleStringProperty(this, "windowTitle", "");

    private Runnable onCloseRequest = () -> { };
    private Runnable onZoomToFit = () -> { };
    private FileDialogs fileDialogs = FileDialogs.NONE;
    private UserPrompts userPrompts = UserPrompts.NONE;
    private DelayedRunner delayedRunner = DelayedRunner.NONE;

    public MainController() {
        selectedTask.addListener((obs, old, next) -> {
            if (next != null) selectedDependency.set(null);
        });
        selectedDependency.addListener((obs, old, next) -> {
            if (next != null) selectedTask.set(null);
        });
        getDocument().dirtyProperty().addListener(dirtyRelay);
        getDocument().getTasks().addListener(countRelay);
        document.addListener((obs, old, next) -> {
            old.dirtyProperty().removeListener(dirtyRelay);
            old.getTasks().removeListener(countRelay);
            next.dirtyProperty().addListener(dirtyRelay);
            next.getTasks().addListener(countRelay);
            saveAvailable.set(next.isDirty());
            taskCount.set(next.getTasks().size());
            undoStack.clear(); // a replaced document starts with no history
            selectedTask.set(null);
            selectedDependency.set(null);
            refreshTitle();
        });
        currentFile.addListener((obs, old, next) -> refreshTitle());
        refreshTitle();
    }

    /** Number of tasks in the current document, tracked across document swaps. */
    public ReadOnlyIntegerProperty taskCountProperty() {
        return taskCount;
    }

    private void refreshTitle() {
        Path file = currentFile.get();
        String name = (file != null) ? file.getFileName().toString() : "Untitled";
        String marker = getDocument().isDirty() ? " \u2022" : "";
        windowTitle.set("Wintak \u2014 " + name + marker);
    }

    /** Live window title: Wintak &mdash; &lt;file|Untitled&gt; plus &bull; when dirty. */
    public ReadOnlyStringProperty windowTitleProperty() {
        return windowTitle;
    }

    public GanttDocument getDocument() {
        return document.get();
    }

    /** The single open document; views re-bind when it is replaced. */
    public ReadOnlyObjectProperty<GanttDocument> documentProperty() {
        return document;
    }

    /** The file backing the document; null until first save / open. */
    public Path getCurrentFile() {
        return currentFile.get();
    }

    public ReadOnlyObjectProperty<Path> currentFileProperty() {
        return currentFile;
    }

    private final BooleanProperty componentSheetVisible =
            new SimpleBooleanProperty(this, "componentSheetVisible", false);

    /** View -> Component Sheet: the in-app specimen overlay (FEAT-38). */
    public BooleanProperty componentSheetVisibleProperty() {
        return componentSheetVisible;
    }

    private final BooleanProperty ganttOverlayVisible =
            new SimpleBooleanProperty(this, "ganttOverlayVisible", false);

    /** Whether the Gantt chart overlay is shown above the map. */
    public BooleanProperty ganttOverlayVisibleProperty() {
        return ganttOverlayVisible;
    }

    /** Owns the active theme; the app attaches it to the scene, the View menu binds to it. */
    public ThemeManager getThemeManager() {
        return themeManager;
    }

    /** The active editing tool; never null, defaults to SELECT. */
    public ObjectProperty<ToolMode> toolModeProperty() {
        return toolMode;
    }

    /** The task the user is working on (chart/table selection); null when nothing selected. */
    public ObjectProperty<GanttTask> selectedTaskProperty() {
        return selectedTask;
    }

    /** The dependency the user has selected; mutually exclusive with {@link #selectedTaskProperty()}. */
    public ObjectProperty<Dependency> selectedDependencyProperty() {
        return selectedDependency;
    }

    public ObservableBooleanValue saveAvailableProperty() {
        return saveAvailable;
    }

    public ObservableBooleanValue undoAvailableProperty() {
        return undoStack.canUndoProperty();
    }

    public ObservableBooleanValue redoAvailableProperty() {
        return undoStack.canRedoProperty();
    }

    /** Default shape of a freshly created task. */
    public static final String NEW_TASK_NAME = "New Task";
    public static final int NEW_TASK_DAYS = 5;

    /** ADD_TASK tool / Edit menu: create a task starting at {@code date}, select it. */
    public void addTaskAt(java.time.LocalDate date) {
        GanttTask task = new GanttTask(NEW_TASK_NAME, date, NEW_TASK_DAYS);
        execute(new AddTaskCommand(getDocument(), task));
        selectedTask.set(task);
    }

    /** ADD_MILESTONE tool: create a locked 1-day milestone at {@code date}, select it. */
    public void addMilestoneAt(java.time.LocalDate date) {
        GanttTask task = new GanttTask("Milestone", date, 1);
        task.setMilestone(true);
        execute(new AddTaskCommand(getDocument(), task));
        selectedTask.set(task);
    }

    /** Edit -> Add Task: append where the project currently ends (today when empty). */
    public void addTask() {
        addTaskAt(getDocument().getTasks().stream()
                .map(GanttTask::getEndExclusive)
                .max(java.time.LocalDate::compareTo)
                .orElse(java.time.LocalDate.now()));
    }

    /** Edit -> Delete Task / Delete key: removes the selected task or dependency, undoable. */
    public void deleteSelectedTask() {
        GanttTask task = selectedTask.get();
        if (task != null) {
            execute(new RemoveTaskCommand(getDocument(), task));
            selectedTask.set(null);
            return;
        }
        Dependency link = selectedDependency.get();
        if (link != null) {
            execute(new RemoveDependencyCommand(getDocument(), link));
            selectedDependency.set(null);
        }
    }

    /** LINK tool: connect two bars finish-to-start; cycles/duplicates are refused politely. */
    public void linkTasks(GanttTask predecessor, GanttTask successor) {
        if (predecessor == null || successor == null || predecessor == successor) {
            return;
        }
        try {
            execute(new AddDependencyCommand(getDocument(), predecessor, successor));
        } catch (IllegalArgumentException refused) {
            setStatus("Cannot link: " + refused.getMessage());
        }
    }

    /** Progress edit (table cell / handle): clamped to 0..100, undoable, no-op when unchanged. */
    public void setTaskProgress(GanttTask task, int percent) {
        int clamped = Math.max(0, Math.min(100, percent));
        int before = task.getPercentComplete();
        if (clamped == before) {
            return;
        }
        execute(new UndoableCommand() {
            @Override
            public void execute() {
                task.setPercentComplete(clamped);
            }

            @Override
            public void undo() {
                task.setPercentComplete(before);
            }

            @Override
            public String name() {
                return "Set Progress";
            }
        });
    }

    /** Runs a reversible edit through the undo history and announces it. */
    public void execute(UndoableCommand command) {
        undoStack.execute(command);
        setStatus(command.name());
    }

    public void undo() {
        undoStack.undo();
    }

    public void redo() {
        undoStack.redo();
    }

    public ObservableBooleanValue deleteAvailableProperty() {
        return deleteAvailable;
    }

    public ReadOnlyStringProperty statusMessageProperty() {
        return statusMessage;
    }

    /** Shows a transient message in the status line; it clears itself after a few seconds. */
    public void setStatus(String message) {
        statusMessage.set(message);
        long token = ++statusToken;
        delayedRunner.runLater(STATUS_LINGER, () -> {
            if (token == statusToken) {
                statusMessage.set("");
            }
        });
    }

    public void setDelayedRunner(DelayedRunner runner) {
        this.delayedRunner = runner;
    }

    public DoubleProperty zoomPxPerDayProperty() {
        return zoomPxPerDay;
    }

    public void zoomIn() {
        zoomPxPerDay.set(Math.min(MAX_PX_PER_DAY, zoomPxPerDay.get() * ZOOM_STEP));
    }

    public void zoomOut() {
        zoomPxPerDay.set(Math.max(MIN_PX_PER_DAY, zoomPxPerDay.get() / ZOOM_STEP));
    }

    /** View -> Zoom to Fit: the chart owns the viewport math, so it registers the handler. */
    public void zoomToFit() {
        onZoomToFit.run();
    }

    public void setOnZoomToFit(Runnable handler) {
        this.onZoomToFit = handler;
    }

    /** File -> New: replace the document with a clean untitled one. */
    public void newDocument() {
        if (!okToDiscard()) {
            return;
        }
        document.set(new GanttDocument());
        currentFile.set(null);
        setStatus("New project");
    }

    /** File -> Open: load a document chosen by the user. */
    public void open() {
        if (!okToDiscard()) {
            return;
        }
        fileDialogs.chooseOpen().ifPresent(file -> {
            try {
                Optional<GanttDocument> loaded = store.load(file);
                if (loaded.isEmpty()) {
                    userPrompts.showError("Could not read " + file);
                    return;
                }
                document.set(loaded.get());
                currentFile.set(file);
                setStatus("Opened " + file.getFileName());
            } catch (IOException e) {
                userPrompts.showError(e.getMessage());
            }
        });
    }

    /** File -> Save. Returns true when the document ended up on disk. */
    public boolean save() {
        if (getCurrentFile() == null) {
            return saveAs();
        }
        return writeTo(getCurrentFile());
    }

    /** File -> Save As. Returns true when the document ended up on disk. */
    public boolean saveAs() {
        Optional<Path> target = fileDialogs.chooseSaveTarget();
        if (target.isEmpty()) {
            return false;
        }
        if (writeTo(target.get())) {
            currentFile.set(target.get());
            return true;
        }
        return false;
    }

    /** File -> Exit: ask the window to close (seam - the app wires the real Stage). */
    public void exit() {
        autoSaveCoalescer.flush();
        if (!okToDiscard()) {
            return;
        }
        onCloseRequest.run();
    }

    /** Help -> About: show app version and build info. */
    public void about() {
        userPrompts.showAbout();
    }

    /** Dirty gate for New/Open/Exit: offer to save; false means the user cancelled. */
    private boolean okToDiscard() {
        if (!getDocument().isDirty()) {
            return true;
        }
        return switch (userPrompts.confirmUnsaved()) {
            case SAVE -> save();
            case DISCARD -> true;
            case CANCEL -> false;
        };
    }

    private boolean writeTo(Path target) {
        try {
            store.save(getDocument(), target);
            setStatus("Saved " + target.getFileName());
            return true;
        } catch (IOException e) {
            userPrompts.showError("Could not save " + target + ": " + e.getMessage());
            return false;
        }
    }

    public void setOnCloseRequest(Runnable handler) {
        this.onCloseRequest = handler;
    }

    public void setFileDialogs(FileDialogs dialogs) {
        this.fileDialogs = dialogs;
    }

    public void setUserPrompts(UserPrompts prompts) {
        this.userPrompts = prompts;
    }

    public void setAutoSaveCoalescer(AutoSaveCoalescer coalescer) {
        this.autoSaveCoalescer = Objects.requireNonNull(coalescer, "autoSaveCoalescer");
    }

    /**
     * Silently saves the current file if one is set and the document is dirty. Snapshots the
     * document on the calling (FX) thread, writes to disk on a background thread, then marks
     * the document saved back on the FX thread. IOExceptions are swallowed; manual Save will
     * surface errors if the path stays bad.
     *
     * @return a future that completes after the disk write (before the FX markSaved callback fires)
     */
    public CompletableFuture<Void> autoSave() {
        Path file = currentFile.get();
        if (file == null || !getDocument().isDirty()) return CompletableFuture.completedFuture(null);
        GanttDocument doc = getDocument();
        Callable<Void> write = store.beginSave(doc, file);
        return CompletableFuture.runAsync(() -> {
            try {
                write.call();
            } catch (Exception ignored) {
                // silent -- a user-initiated Save will surface the error if the path stays bad
                return;
            }
            Platform.runLater(doc::markSaved);
        });
    }

    /** Test seam: set the current file path without going through file-dialog flow. */
    void setCurrentFileForTest(Path file) {
        currentFile.set(file);
    }
}
