package com.richardl.wintak.view;

import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import com.richardl.wintak.view.theme.Theme;
import com.richardl.wintak.view.theme.ThemeManager;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Pagination;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FxToolkitExtension.class)
class ComponentSheetTest {

    private static final List<Class<? extends Node>> EVERY_CONTROL = List.of(
            Label.class, Button.class, ToggleButton.class, RadioButton.class, CheckBox.class,
            Hyperlink.class, TextField.class, PasswordField.class, TextArea.class,
            ComboBox.class, ChoiceBox.class, Spinner.class, Slider.class,
            ProgressBar.class, ProgressIndicator.class, DatePicker.class, ColorPicker.class,
            MenuButton.class, SplitMenuButton.class, ListView.class, TableView.class,
            TreeView.class, TabPane.class, TitledPane.class, Accordion.class,
            Pagination.class, ScrollBar.class, Separator.class, ToolBar.class);

    @Test
    void rendersEveryJavaFxControl() throws Exception {
        FxThread.run(() -> {
            ComponentSheet sheet = new ComponentSheet(new ThemeManager());
            for (Class<? extends Node> control : EVERY_CONTROL) {
                assertTrue(anyMatch(sheet, control::isInstance),
                        "component sheet is missing " + control.getSimpleName());
            }
        });
    }

    @Test
    void showsDisabledAndSelectedStates() throws Exception {
        FxThread.run(() -> {
            ComponentSheet sheet = new ComponentSheet(new ThemeManager());
            assertTrue(anyMatch(sheet, n -> n instanceof Button && n.isDisabled()),
                    "needs a disabled Button specimen");
            assertTrue(anyMatch(sheet, n -> n instanceof ToggleButton t && t.isSelected()),
                    "needs a selected ToggleButton specimen");
            assertTrue(anyMatch(sheet, n -> n instanceof CheckBox c && c.isIndeterminate()),
                    "needs an indeterminate CheckBox specimen");
        });
    }

    @Test
    void specimensCarryStyleReferences() throws Exception {
        FxThread.run(() -> {
            ComponentSheet sheet = new ComponentSheet(new ThemeManager());
            AtomicInteger refs = new AtomicInteger();
            anyMatch(sheet, n -> {
                if (n.getProperties().get(ComponentSheet.STYLE_REF_KEY) != null) {
                    refs.incrementAndGet();
                }
                return false;
            });
            assertTrue(refs.get() >= 60,
                    "every specimen needs a style-reference tooltip; found " + refs.get());
            assertTrue(anyMatch(sheet, n -> ".button:hover".equals(
                            n.getProperties().get(ComponentSheet.STYLE_REF_KEY))),
                    "button hover cell must reference .button:hover");
        });
    }

    @Test
    void solidThemedBackground() {
        String css = baseCss();
        int sheet = css.indexOf(".component-sheet");
        assertTrue(sheet >= 0 && css.indexOf("-wintak-bg", sheet) > sheet,
                "the overlay needs a solid themed background (-wintak-bg)");
    }

    @Test
    void themeToggleFollowsTheThemeManager() throws Exception {
        FxThread.run(() -> {
            ThemeManager themes = new ThemeManager();
            ComponentSheet sheet = new ComponentSheet(themes);

            themes.themeProperty().set(Theme.LIGHT);
            assertTrue(anyMatch(sheet, n -> n instanceof ToggleButton t
                            && "Light".equals(t.getText()) && t.isSelected()),
                    "the Light toggle tracks the theme property");

            themes.themeProperty().set(Theme.DARK);
            assertTrue(anyMatch(sheet, n -> n instanceof ToggleButton t
                            && "Dark".equals(t.getText()) && t.isSelected()),
                    "the Dark toggle tracks the theme property");
        });
    }

    static boolean anyMatch(Node node, java.util.function.Predicate<Node> test) {
        if (test.test(node)) {
            return true;
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (anyMatch(child, test)) {
                    return true;
                }
            }
            if (node instanceof javafx.scene.control.ScrollPane scroll && scroll.getContent() != null) {
                return anyMatch(scroll.getContent(), test);
            }
        }
        return false;
    }

    private static String baseCss() {
        try (InputStream in = Theme.class.getResourceAsStream("wintak-base.css")) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
