package com.richardl.wintak.view;

import com.richardl.wintak.controller.MainController;
import com.richardl.wintak.controller.ToolMode;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.materialdesign2.MaterialDesignL;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class SideToolBar extends ToolBar {

    private static final int ICON_SIZE = 20;

    private static final Map<ToolMode, Ikon> ICONS = new EnumMap<>(Map.of(
            ToolMode.SELECT,        MaterialDesignC.CURSOR_DEFAULT,
            ToolMode.ADD_TASK,      MaterialDesignC.CALENDAR_PLUS,
            ToolMode.ADD_MILESTONE, MaterialDesignD.DIAMOND_OUTLINE,
            ToolMode.LINK,          MaterialDesignL.LINK_VARIANT,
            ToolMode.PAN,           MaterialDesignC.CURSOR_MOVE));

    private final ToggleGroup group = new ToggleGroup();
    private final Map<ToolMode, ToggleButton> toggles = new EnumMap<>(ToolMode.class);

    public SideToolBar(MainController controller) {
        setId(RootLayout.TOOLBAR_AREA);
        setOrientation(Orientation.VERTICAL);

        for (ToolMode mode : ToolMode.values()) {
            ToggleButton toggle = new ToggleButton();
            toggle.setGraphic(FontIcon.of(ICONS.get(mode), ICON_SIZE));
            toggle.setId("tool-" + mode.name().toLowerCase(java.util.Locale.ROOT).replace('_', '-'));
            toggle.setTooltip(new Tooltip(mode.label() + " \u2014 " + mode.tooltip()));
            toggle.setToggleGroup(group);
            toggle.setUserData(mode);
            toggles.put(mode, toggle);
            getItems().add(toggle);
        }

        toggles.get(controller.toolModeProperty().get()).setSelected(true);

        group.selectedToggleProperty().addListener((obs, old, next) -> {
            if (next == null) {
                old.setSelected(true);
            } else {
                controller.toolModeProperty().set((ToolMode) next.getUserData());
            }
        });
        controller.toolModeProperty().addListener((obs, old, next) -> toggles.get(next).setSelected(true));

        getItems().add(new Separator());
        getItems().add(zoomButton("toolbar-zoom-in",  MaterialDesignM.MAGNIFY_PLUS,     "Zoom In",     controller::zoomIn));
        getItems().add(zoomButton("toolbar-zoom-out", MaterialDesignM.MAGNIFY_MINUS,    "Zoom Out",    controller::zoomOut));
        getItems().add(zoomButton("toolbar-zoom-fit", MaterialDesignA.ARROW_EXPAND_ALL, "Zoom to Fit", controller::zoomToFit));
    }

    private Button zoomButton(String id, Ikon icon, String name, Runnable command) {
        Button button = new Button();
        button.setGraphic(FontIcon.of(icon, ICON_SIZE));
        button.setId(id);
        button.setTooltip(new Tooltip(name));
        button.setOnAction(e -> command.run());
        return button;
    }

    public List<ToggleButton> toolToggles() {
        return List.copyOf(toggles.values());
    }

    public ToggleButton toggleFor(ToolMode mode) {
        return toggles.get(mode);
    }
}
