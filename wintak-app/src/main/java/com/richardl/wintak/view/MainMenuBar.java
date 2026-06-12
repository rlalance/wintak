package com.richardl.wintak.view;

import com.richardl.wintak.view.theme.Theme;
import com.richardl.wintak.view.theme.ThemeManager;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCombination;

/**
 * The application menu bar. Pure structure - every item carries a stable id; behaviour is
 * wired by the controller (FEAT-11), enablement by state bindings (FEAT-13).
 */
public class MainMenuBar extends MenuBar {

    private final ToggleGroup themeGroup = new ToggleGroup();

    public MainMenuBar() {
        setId(RootLayout.MENU_AREA);
        getMenus().addAll(buildFile(), buildEdit(), buildView(), buildHelp());
    }

    private Menu buildFile() {
        Menu file = new Menu("File");
        file.getItems().addAll(
                item("menu-file-new", "New", "Shortcut+N"),
                item("menu-file-open", "Open\u2026", "Shortcut+O"),
                new SeparatorMenuItem(),
                item("menu-file-save", "Save", "Shortcut+S"),
                item("menu-file-save-as", "Save As\u2026", "Shortcut+Shift+S"),
                new SeparatorMenuItem(),
                item("menu-file-exit", "Exit", null));
        return file;
    }

    private Menu buildEdit() {
        Menu edit = new Menu("Edit");
        edit.getItems().addAll(
                item("menu-edit-undo", "Undo", "Shortcut+Z"),
                item("menu-edit-redo", "Redo", "Shortcut+Y"),
                new SeparatorMenuItem(),
                item("menu-edit-add-task", "Add Task", null),
                item("menu-edit-delete-task", "Delete Task", "Delete"));
        return edit;
    }

    private Menu buildView() {
        Menu view = new Menu("View");
        Menu themes = new Menu("Theme");
        for (Theme theme : Theme.values()) {
            RadioMenuItem radio = new RadioMenuItem(themeLabel(theme));
            radio.setId("menu-view-theme-" + theme.name().toLowerCase(java.util.Locale.ROOT));
            radio.setToggleGroup(themeGroup);
            radio.setUserData(theme);
            themes.getItems().add(radio);
        }
        CheckMenuItem gantt = new CheckMenuItem("Gantt Editor");
        gantt.setId("menu-view-gantt");
        CheckMenuItem componentSheet = new CheckMenuItem("Component Sheet");
        componentSheet.setId("menu-view-component-sheet");
        view.getItems().addAll(
                item("menu-view-zoom-in", "Zoom In", "Shortcut+Plus"),
                item("menu-view-zoom-out", "Zoom Out", "Shortcut+Minus"),
                item("menu-view-zoom-fit", "Zoom to Fit", null),
                new SeparatorMenuItem(),
                themes,
                new SeparatorMenuItem(),
                gantt,
                componentSheet);
        return view;
    }

    private static String themeLabel(Theme theme) {
        String name = theme.name().toLowerCase(java.util.Locale.ROOT);
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    /** Two-way sync between the Theme radios and the active theme (FEAT-36). */
    public void bindThemeRadios(ThemeManager manager) {
        for (Toggle toggle : themeGroup.getToggles()) {
            if (toggle.getUserData() == manager.themeProperty().get()) {
                toggle.setSelected(true);
            }
        }
        themeGroup.selectedToggleProperty().addListener((obs, old, next) -> {
            if (next != null) {
                manager.themeProperty().set((Theme) next.getUserData());
            } else if (old != null) {
                old.setSelected(true); // a theme is always active
            }
        });
        manager.themeProperty().addListener((obs, old, next) ->
                themeGroup.getToggles().stream()
                        .filter(t -> t.getUserData() == next)
                        .findFirst()
                        .ifPresent(t -> t.setSelected(true)));
    }

    private Menu buildHelp() {
        Menu help = new Menu("Help");
        help.getItems().add(item("menu-help-about", "About", null));
        return help;
    }

    /** Finds a command item anywhere in the bar (submenus included) by its stable id. */
    public MenuItem item(String id) {
        return getMenus().stream()
                .flatMap(MainMenuBar::flatten)
                .filter(i -> id.equals(i.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("no menu item " + id));
    }

    private static java.util.stream.Stream<MenuItem> flatten(MenuItem item) {
        if (item instanceof Menu menu) {
            return menu.getItems().stream().flatMap(MainMenuBar::flatten);
        }
        return java.util.stream.Stream.of(item);
    }

    private static MenuItem item(String id, String text, String accelerator) {
        MenuItem item = new MenuItem(text);
        item.setId(id);
        if (accelerator != null) {
            item.setAccelerator(KeyCombination.valueOf(accelerator));
        }
        return item;
    }
}
