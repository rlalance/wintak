package com.richardl.wintak.view;

import com.richardl.wintak.controller.MainController;
import com.sothawo.mapjfx.Coordinate;
import com.sothawo.mapjfx.MapView;
import javafx.beans.binding.Bindings;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

/**
 * Application shell: BorderPane with one named region per chrome area; each region keeps
 * its stable id so tests and CSS address it the same way. The component-sheet overlay
 * (FEAT-45) stacks above this whole shell in {@code WintakApp.buildScene}.
 */
public class RootLayout extends BorderPane {

    public static final String MENU_AREA = "menu-area";
    public static final String TOOLBAR_AREA = "toolbar-area";
    public static final String EDITOR_AREA = "editor-area";
    public static final String STATUS_AREA = "status-area";

    public RootLayout(MainController controller) {
        setTop(wireMenu(new MainMenuBar(), controller));
        SideToolBar toolBar = new SideToolBar(controller);
        toolBar.visibleProperty().bind(controller.ganttOverlayVisibleProperty());
        toolBar.managedProperty().bind(toolBar.visibleProperty());
        setLeft(toolBar);
        MapView mapView = new MapView();
        mapView.setId("map-layer");
        // Initialize lazily: sceneProperty fires when the node joins a live Stage, so headless
        // tests that never attach to a Scene skip WebView creation entirely.
        mapView.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && !mapView.getInitialized()) {
                mapView.initialize();
                mapView.initializedProperty().addListener((p, wasInit, isInit) -> {
                    if (isInit) {
                        mapView.setCenter(new Coordinate(45.4215, -75.6972));
                        mapView.setZoom(10.0);
                    }
                });
            }
        });
        GanttEditor ganttEditor = new GanttEditor(controller);
        ganttEditor.visibleProperty().bind(controller.ganttOverlayVisibleProperty());
        StackPane editorArea = new StackPane(mapView, ganttEditor);
        editorArea.setId(EDITOR_AREA);
        setCenter(editorArea);
        StatusBar statusBar = new StatusBar();
        statusBar.bindTo(controller);
        setBottom(statusBar);
    }

    private static MainMenuBar wireMenu(MainMenuBar menuBar, MainController controller) {
        menuBar.item("menu-file-new").setOnAction(e -> controller.newDocument());
        menuBar.item("menu-file-open").setOnAction(e -> controller.open());
        menuBar.item("menu-file-save").setOnAction(e -> controller.save());
        menuBar.item("menu-file-save-as").setOnAction(e -> controller.saveAs());
        menuBar.item("menu-file-exit").setOnAction(e -> controller.exit());
        menuBar.item("menu-edit-add-task").setOnAction(e -> controller.addTask());
        menuBar.item("menu-edit-delete-task").setOnAction(e -> controller.deleteSelectedTask());
        menuBar.item("menu-edit-undo").setOnAction(e -> controller.undo());
        menuBar.item("menu-edit-redo").setOnAction(e -> controller.redo());
        menuBar.item("menu-view-zoom-in").setOnAction(e -> controller.zoomIn());
        menuBar.item("menu-view-zoom-out").setOnAction(e -> controller.zoomOut());
        menuBar.item("menu-view-zoom-fit").setOnAction(e -> controller.zoomToFit());
        menuBar.bindThemeRadios(controller.getThemeManager());
        ((javafx.scene.control.CheckMenuItem) menuBar.item("menu-view-gantt"))
                .selectedProperty().bindBidirectional(controller.ganttOverlayVisibleProperty());
        ((javafx.scene.control.CheckMenuItem) menuBar.item("menu-view-component-sheet"))
                .selectedProperty().bindBidirectional(controller.componentSheetVisibleProperty());

        menuBar.item("menu-file-save").disableProperty()
                .bind(Bindings.not(controller.saveAvailableProperty()));
        menuBar.item("menu-edit-undo").disableProperty()
                .bind(Bindings.not(controller.undoAvailableProperty()));
        menuBar.item("menu-edit-redo").disableProperty()
                .bind(Bindings.not(controller.redoAvailableProperty()));
        menuBar.item("menu-edit-delete-task").disableProperty()
                .bind(Bindings.not(controller.deleteAvailableProperty()));
        menuBar.item("menu-help-about").setOnAction(e -> controller.about());
        return menuBar;
    }
}
