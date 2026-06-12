package com.richardl.wintak.view;

import com.richardl.wintak.controller.MainController;
import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(FxToolkitExtension.class)
class MainMenuBarTest {

    private static MainMenuBar menuBar;

    @BeforeAll
    static void build() throws Exception {
        menuBar = FxThread.call(MainMenuBar::new);
    }

    @Test
    void hasTheFourTopLevelMenus() {
        assertEquals(List.of("File", "Edit", "View", "Help"),
                menuBar.getMenus().stream().map(Menu::getText).toList());
    }

    @ParameterizedTest
    @CsvSource({
            "menu-file-new,      New",
            "menu-file-open,     Open\u2026",
            "menu-file-save,     Save",
            "menu-file-save-as,  Save As\u2026",
            "menu-file-exit,     Exit",
            "menu-edit-undo,     Undo",
            "menu-edit-redo,     Redo",
            "menu-edit-add-task, Add Task",
            "menu-edit-delete-task, Delete Task",
            "menu-view-zoom-in,  Zoom In",
            "menu-view-zoom-out, Zoom Out",
            "menu-view-zoom-fit, Zoom to Fit",
            "menu-help-about,    About",
    })
    void everyCommandItemExistsById(String id, String text) {
        MenuItem item = item(id);
        assertEquals(text, item.getText());
    }

    @ParameterizedTest
    @CsvSource({
            "menu-file-new,     Shortcut+N",
            "menu-file-open,    Shortcut+O",
            "menu-file-save,    Shortcut+S",
            "menu-file-save-as, Shortcut+Shift+S",
            "menu-edit-undo,    Shortcut+Z",
            "menu-edit-redo,    Shortcut+Y",
            "menu-view-zoom-in, Shortcut+Plus",
            "menu-view-zoom-out, Shortcut+Minus",
    })
    void standardAcceleratorsAreBound(String id, String combo) {
        assertEquals(KeyCombination.valueOf(combo), item(id).getAccelerator());
    }

    @Test
    void rootLayoutMountsTheMenuBarInTheMenuArea() throws Exception {
        RootLayout root = FxThread.call(() -> new RootLayout(new MainController()));
        assertInstanceOf(MainMenuBar.class, root.getTop());
        assertNotNull(root.lookup("#" + RootLayout.MENU_AREA), "menu bar must keep the region id");
    }

    private static MenuItem item(String id) {
        return menuBar.getMenus().stream()
                .flatMap(m -> m.getItems().stream())
                .filter(i -> Objects.equals(id, i.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no menu item with id " + id));
    }
}
