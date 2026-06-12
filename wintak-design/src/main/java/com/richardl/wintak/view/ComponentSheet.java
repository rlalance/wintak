package com.richardl.wintak.view;

import com.richardl.wintak.view.theme.Theme;
import com.richardl.wintak.view.theme.ThemeManager;
import javafx.css.PseudoClass;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
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
import javafx.scene.control.MenuItem;
import javafx.scene.control.Pagination;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

import java.time.LocalDate;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * FEAT-45: the in-app specimen sheet as a fullscreen overlay mirroring
 * Design/WinTAK Specimen Sheet (standalone).html - foundations (tokens, type,
 * spacing, radius, borders, elevation) plus every control in interaction-state
 * matrices, styled purely by the active theme's token sheets. Hovering any
 * specimen shows the selector/token it demonstrates (also exposed under
 * {@link #STYLE_REF_KEY} in the node's properties). Toggled from
 * View -> Component Sheet; Esc closes it; the topbar switches themes live.
 */
public class ComponentSheet extends BorderPane {

    /** Node-properties key carrying a specimen's style reference (the tooltip text). */
    public static final String STYLE_REF_KEY = "wintak.styleRef";

    private static final double CONTENT_MAX_WIDTH = 1180;

    /* stroke icons from the specimen sheet, as SVG path data in a 24px box */
    private static final String I_TARGET =
            "M5.5 12a6.5 6.5 0 1 0 13 0a6.5 6.5 0 1 0 -13 0M12 1.5V5M12 19v3.5M1.5 12H5M19 12h3.5";
    private static final String I_LAYERS = "M12 3 21 8 12 13 3 8ZM3 12l9 5 9-5M3 16l9 5 9-5";
    private static final String I_GLOBE =
            "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0M8 12a4 9 0 1 0 8 0a4 9 0 1 0 -8 0M3 12h18";
    private static final String I_RULER = "M2.5 8h19v8h-19ZM6.5 8v3M10.5 8v4M14.5 8v3M18 8v4";
    private static final String I_ROUTE = "M2.8 6a2.2 2.2 0 1 0 4.4 0a2.2 2.2 0 1 0 -4.4 0"
            + "M16.8 18a2.2 2.2 0 1 0 4.4 0a2.2 2.2 0 1 0 -4.4 0M5 8.5V12a3 3 0 0 0 3 3h6a3 3 0 0 1 3 3v-2.5";
    private static final String I_SEARCH = "M4 10.5a6.5 6.5 0 1 0 13 0a6.5 6.5 0 1 0 -13 0M15.5 15.5 21 21";
    private static final String I_CHECK = "M4 12l5 6L20 5";
    private static final String I_CLOSE = "M5 5l14 14M19 5L5 19";
    private static final String I_PIN = "M12 22s7-7.5 7-12.5A7 7 0 0 0 5 9.5C5 14.5 12 22 12 22Z"
            + "M9.7 9.5a2.3 2.3 0 1 0 4.6 0a2.3 2.3 0 1 0 -4.6 0";
    private static final String I_COPY = "M8 8h12v12H8ZM4 16V4h12";
    private static final String I_EDIT = "M4 20v-4L16 4l4 4L8 20ZM14 6l4 4";
    private static final String I_GEAR = "M8.8 12a3.2 3.2 0 1 0 6.4 0a3.2 3.2 0 1 0 -6.4 0"
            + "M12 2v3M12 19v3M2 12h3M19 12h3M4.9 4.9l2.1 2.1M16.9 16.9l2.1 2.1M19.1 4.9l-2.1 2.1M7 16.9l-2.1 2.1";
    private static final String I_TRASH = "M4 6h16M9 6V4h6v2M6 6l1 14h10l1-14";
    private static final String I_INFO = "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0M12 11v6M12 7.5v0.01";
    private static final String I_WARN = "M12 3 22 20H2ZM12 10v4M12 17.5v0.01";
    private static final String I_ERRX = "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0M9 9l6 6M15 9l-6 6";

    private final ScrollPane scroll;
    private final VBox wrap;
    private final Map<String, Node> sections = new LinkedHashMap<>();

    public ComponentSheet(ThemeManager themes) {
        setId("component-sheet");
        getStyleClass().add("component-sheet");

        wrap = new VBox(46);
        wrap.getStyleClass().add("sheet-wrap");
        wrap.setPadding(new Insets(34, 22, 120, 22));
        wrap.setMaxWidth(CONTENT_MAX_WIDTH);

        Label lede = new Label("Every control in the WinTAK component library, laid out in "
                + "interaction-state matrices, with the complete token reference. Neutral surfaces "
                + "invert between Dark and Light; accent, semantic, and structural specs are shared "
                + "across themes. Toggle the theme top-right - the whole sheet restyles live. "
                + "Hover any specimen for the selector or token to reference in code.");
        lede.getStyleClass().add("sheet-lede");
        lede.setWrapText(true);
        wrap.getChildren().add(lede);

        wrap.getChildren().addAll(
                colorSection(), typographySection(), spacingSection(), radiusSection(),
                bordersSection(), dividersSection(), elevationSection(), focusSection(),
                buttonsSection(), inputsSection(), selectSection(), togglesSection(),
                tabsSection(), rangeSection(), chipsSection(), linksSection(),
                overlaysSection(), dataSection(), feedbackSection(), progressSection(),
                scrollbarSection(), playgroundSection(), extrasSection());

        VBox centring = new VBox(wrap);
        centring.setAlignment(Pos.TOP_CENTER);
        scroll = new ScrollPane(centring);
        scroll.setFitToWidth(true);

        setTop(new VBox(topbar(themes), toc()));
        setCenter(scroll);

        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                setVisible(false);
                e.consume();
            }
        });
    }

    /* =====================================================================
       CHROME: maroon topbar + jump nav
       ===================================================================== */

    private Node topbar(ThemeManager themes) {
        Label brand = new Label("WinTAK");
        brand.getStyleClass().add("sheet-brand");
        Label sub = new Label("Component Specimen Sheet");
        sub.getStyleClass().add("sheet-brand-sub");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label meta = new Label("tokens \u00b7 WinTAK Design System \u00b7 Esc closes");
        meta.getStyleClass().add("sheet-meta");

        ToggleButton dark = new ToggleButton("Dark");
        ToggleButton light = new ToggleButton("Light");
        ToggleGroup group = new ToggleGroup();
        dark.setToggleGroup(group);
        light.setToggleGroup(group);
        dark.setOnAction(e -> themes.themeProperty().set(Theme.DARK));
        light.setOnAction(e -> themes.themeProperty().set(Theme.LIGHT));
        Runnable sync = () -> {
            dark.setSelected(themes.themeProperty().get() == Theme.DARK);
            light.setSelected(themes.themeProperty().get() == Theme.LIGHT);
        };
        themes.themeProperty().addListener((obs, old, next) -> sync.run());
        sync.run();
        HBox toggle = new HBox(dark, light);
        toggle.getStyleClass().add("sheet-theme-toggle");
        toggle.setAlignment(Pos.CENTER);

        HBox bar = new HBox(brand, sub, spacer, meta, toggle);
        bar.getStyleClass().add("sheet-topbar");
        return bar;
    }

    private Node toc() {
        FlowPane links = new FlowPane();
        links.getStyleClass().add("sheet-toc");
        for (Map.Entry<String, Node> entry : sections.entrySet()) {
            Hyperlink link = new Hyperlink(entry.getKey());
            link.setOnAction(e -> scrollTo(entry.getValue()));
            links.getChildren().add(link);
        }
        return links;
    }

    private void scrollTo(Node section) {
        double y = section.getBoundsInParent().getMinY() + wrap.getBoundsInParent().getMinY();
        double overflow = scroll.getContent().getLayoutBounds().getHeight()
                - scroll.getViewportBounds().getHeight();
        scroll.setVvalue(overflow > 0 ? Math.min(1, y / overflow) : 0);
    }

    /* =====================================================================
       FOUNDATIONS
       ===================================================================== */

    private Node colorSection() {
        return section("Color",
                "Neutral ramp (theme-dependent), the blue accent family, semantic feedback "
                        + "colors, and the WinTAK brand maroon.",
                block("Neutral ramp", "Backgrounds, surfaces, borders and text - the values that "
                        + "flip between themes.", swatches(new String[][]{
                        {"Background", "bg", "app canvas"}, {"Surface 1", "surface-1", "panels"},
                        {"Surface 2", "surface-2", "cards / controls"},
                        {"Surface 3", "surface-3", "headers / raised"},
                        {"Surface raised", "surface-raised", "popups"},
                        {"Field", "field-bg", "input wells"}, {"Border", "border", "1px structural"},
                        {"Border strong", "border-strong", "2px / hairline"},
                        {"Border neutral", "border-neutral", "combo stroke"},
                        {"Text", "text", "primary"}, {"Text dim", "text-dim", "secondary"},
                        {"Text mute", "text-mute", "tertiary"},
                        {"Disabled fill", "disabled-fill", "disabled bg"}})),
                block("Accent", "Interactive blue - focus, selection, primary actions and links; "
                        + "identical in both themes.", swatches(new String[][]{
                        {"Accent", "accent", "primary action"}, {"Accent 2", "accent-2", "alt links"},
                        {"Hover", "accent-hover", "hover stroke"},
                        {"Focus", "accent-focus", "focus ring"}, {"Check", "check", "check / dot"}})),
                block("Semantic", "Status & feedback. Solid fills sit under dark ink; the darker "
                        + "variants are text/iconography on dark surfaces.", swatches(new String[][]{
                        {"Success", "success", "text / icon"}, {"Success fill", "success-bg", "snackbar"},
                        {"Warning", "warning", "text / icon"}, {"Warning fill", "warning-bg", "snackbar"},
                        {"Error", "error", "field error"}, {"Error fill", "error-bg", "snackbar"}})),
                block("Brand", "Maroon window chrome and the signature title underline.",
                        swatches(new String[][]{
                                {"Maroon", "maroon", "window chrome"},
                                {"Maroon accent", "maroon-accent", "title underline"}})));
    }

    private Node typographySection() {
        VBox ramp = new VBox();
        ramp.getStyleClass().add("sheet-panel");
        String[][] rows = {
                {"Segoe UI bold \u00b7 30px", "wintak-display-1", "Situational Awareness"},
                {"Segoe UI \u00b7 18px", "wintak-display-2", "Overlay Manager"},
                {"Segoe UI \u00b7 14px", "wintak-title", "Window title bar text"},
                {"Segoe UI \u00b7 13px", "", "Default UI / body label text"},
                {"Segoe UI bold \u00b7 13px", "wintak-strong", "GroupBox & emphasis"},
                {"Segoe UI \u00b7 11px", "wintak-caption", "Textbox content / helper"},
                {"Segoe UI \u00b7 10px", "wintak-dense", "Listview rows / dense data"},
                {"Consolas \u00b7 13px", "wintak-mono", "45.4215\u00b0 N  75.6972\u00b0 W"},
        };
        for (int i = 0; i < rows.length; i++) {
            Label spec = new Label(rows[i][0]);
            spec.getStyleClass().add("sheet-cap");
            spec.setPrefWidth(150);
            Label sample = new Label(rows[i][2]);
            if (!rows[i][1].isEmpty()) {
                sample.getStyleClass().add(rows[i][1]);
            }
            HBox row = new HBox(18, spec, sample);
            row.setAlignment(Pos.BASELINE_LEFT);
            row.setPadding(new Insets(10, 0, 10, 0));
            tip(row, rows[i][1].isEmpty() ? ".root (base type)" : "." + rows[i][1]);
            ramp.getChildren().add(row);
            if (i < rows.length - 1) {
                ramp.getChildren().add(styled(new Region(), "wintak-divider-soft"));
            }
        }
        return section("Typography", "Segoe UI is the system UI face. Display roles render in "
                + "Segoe UI until a bundled face lands; data/coordinates are Consolas "
                + "(see wintak-base.css mono note).", ramp);
    }

    private Node spacingSection() {
        FlowPane scale = specRow();
        for (int v : new int[]{1, 2, 4, 6, 8, 10, 12, 16, 20, 24}) {
            Region box = styled(new Region(), "sheet-scale-accent");
            box.setMinSize(v, 22);
            box.setMaxSize(v, 22);
            scale.getChildren().add(specimen(box, String.valueOf(v), "spacing scale \u00b7 " + v + "px"));
        }
        FlowPane heights = specRow();
        for (Object[] h : new Object[][]{{"field", 20}, {"small", 28}, {"control", 30},
                {"icon", 34}, {"large", 42}}) {
            Region box = styled(new Region(), "sheet-scale-surface");
            box.setMinSize(46, (int) h[1]);
            box.setMaxSize(46, (int) h[1]);
            heights.getChildren().add(specimen(box, h[0] + " \u00b7 " + h[1],
                    "control height \u00b7 " + h[1] + "px"));
        }
        scale.setRowValignment(VPos.BOTTOM);
        heights.setRowValignment(VPos.BOTTOM);
        return section("Spacing & Sizing",
                "A tight desktop scale - the kit composes on 1 / 2 / 4 / 6 / 8 / 10 px increments.",
                panel(scale),
                block("Control heights", "Standard interactive target sizes.", panel(heights)));
    }

    private Node radiusSection() {
        FlowPane row = specRow();
        for (Object[] r : new Object[][]{{"sharp \u00b7 0px", "sheet-radius-0"},
                {"xs \u00b7 1px", "sheet-radius-1"}, {"sm \u00b7 3px", "sheet-radius-3"},
                {"md \u00b7 4px", "sheet-radius-4"}}) {
            Region box = styled(new Region(), (String) r[1]);
            box.setMinSize(54, 54);
            box.setMaxSize(54, 54);
            row.getChildren().add(specimen(box, (String) r[0], "." + r[1]));
        }
        return section("Radius",
                "Mostly sharp. Buttons take a 1px softening; cards/menus 3px; combos & snackbars 4px.",
                panel(row));
    }

    private Node bordersSection() {
        FlowPane widths = specRow();
        for (Object[] b : new Object[][]{{"hairline \u00b7 0.5px", "sheet-border-hairline"},
                {"structural \u00b7 1px", "sheet-border-structural"},
                {"interactive \u00b7 2px", "sheet-border-interactive"}}) {
            Region box = styled(new Region(), (String) b[1]);
            box.setMinSize(80, 40);
            box.setMaxSize(80, 40);
            widths.getChildren().add(specimen(box, (String) b[0], "." + b[1]));
        }
        Node strokes = swatches(new String[][]{
                {"Default", "border", "rest"}, {"Hover", "accent-hover", "pointer over"},
                {"Focus", "accent-focus", "keyboard focus"}, {"Error", "error", "invalid"},
                {"Disabled", "disabled-border", "inactive"}});
        return section("Borders & Strokes",
                "Border width carries meaning: 1px structural, 2px interactive, 0.5px hairlines. "
                        + "Color shifts by state.",
                block("Width scale", null, panel(widths)),
                block("Border color by state", "The interactive stroke ramp shared by fields, "
                        + "checkboxes, comboboxes and toggles.", strokes));
    }

    private Node dividersSection() {
        VBox demo = new VBox(22);
        demo.getStyleClass().add("sheet-panel");
        demo.getChildren().addAll(
                dividerDemo("strong \u00b7 1px border-strong", "wintak-divider", -1),
                dividerDemo("soft \u00b7 1px border", "wintak-divider-soft", -1),
                dividerDemo("title underline \u00b7 3px maroon + shadow", "wintak-divider-maroon", 200),
                dividerDemo("dotted drag rail", "wintak-dotted-rail", 200));
        Region vertical = styled(new Region(), "wintak-divider-v");
        vertical.setMinHeight(66);
        vertical.setMaxHeight(66);
        Label boxTitle = styled(new Label("Group"), "wintak-groupbox-title");
        Label boxBody = styled(new Label("notched container border"), "wintak-caption");
        VBox groupbox = styled(new VBox(boxBody), "wintak-groupbox");
        StackPane notched = new StackPane(groupbox, boxTitle);
        StackPane.setAlignment(boxTitle, Pos.TOP_LEFT);
        boxTitle.setTranslateX(8);
        boxTitle.setTranslateY(-9);
        HBox lastRow = new HBox(22,
                specimen(vertical, "vertical 66px", ".wintak-divider-v"),
                specimen(notched, "groupbox", ".wintak-groupbox / .wintak-groupbox-title"));
        lastRow.setAlignment(Pos.CENTER_LEFT);
        demo.getChildren().add(lastRow);
        return section("Dividers & Separators", "Line treatments for grouping and chrome.", demo);
    }

    private Node dividerDemo(String caption, String styleClass, double width) {
        Region line = styled(new Region(), styleClass);
        if (width > 0) {
            line.setMaxWidth(width);
        }
        Label cap = styled(new Label(caption), "sheet-cap");
        VBox box = new VBox(6, cap, line);
        tip(box, "." + styleClass);
        box.getProperties().put(STYLE_REF_KEY, "." + styleClass);
        return box;
    }

    private Node elevationSection() {
        FlowPane row = specRow();
        for (Object[] e : new Object[][]{{"Flat", "sheet-elev-flat", "flat"},
                {"Card", "sheet-elev-card", "card"}, {"Popover", "sheet-elev-popover", "menu / tooltip"},
                {"Window", "sheet-elev-window", "dialog"}}) {
            Label inner = styled(new Label((String) e[0]), "sheet-cap");
            StackPane box = styled(new StackPane(inner), (String) e[1]);
            box.setMinSize(96, 56);
            box.setMaxSize(96, 56);
            row.getChildren().add(specimen(box, (String) e[2], "." + e[1]));
        }
        Region underline = styled(new Region(), "wintak-divider-maroon");
        underline.setMinWidth(96);
        underline.setMaxWidth(96);
        row.getChildren().add(specimen(underline, "title rule", ".wintak-divider-maroon"));
        row.setRowValignment(VPos.BOTTOM);
        return section("Elevation & Shadow", "WinTAK is mostly flat; elevation appears on floating "
                + "windows, popovers and the maroon title underline.", panel(row));
    }

    private Node focusSection() {
        FlowPane row = specRow();
        row.getChildren().addAll(
                specimen(inState(new Button("Button"), "focused"), "outer ring + inset",
                        ".button:focused"),
                specimen(inState(new TextField("Field"), "focused"), "stroke \u2192 focus blue",
                        ".text-input:focused"),
                specimen(inState(checked(new CheckBox("Checkbox")), "focused"), "box ring",
                        ".check-box:focused:selected"),
                specimen(inState(combo("Combobox"), "focused"), "stroke \u2192 focus blue",
                        ".combo-box:focused"));
        return section("Focus Ring", "Keyboard focus is the -wintak-accent-focus stroke. Fields and "
                + "checkboxes flip their own stroke to the focus blue; buttons take the ring on the "
                + "outer plate.", panel(row));
    }

    /* =====================================================================
       CONTROLS
       ===================================================================== */

    private Node buttonsSection() {
        String[] states = {"DEFAULT", "HOVER", "FOCUS", "PRESSED", "DISABLED", "LOADING"};
        String[] pseudos = {null, "hover", "focused", "armed", "disabled", null};

        BiFunction<String, String, Node> command = (pseudo, ref) ->
                inState(new Button("Center", icon(I_TARGET, 15)), pseudo);
        Node mxCommand = matrix(150, states, new String[]{"Command"}, (r, c) -> {
            if (c == 5) {
                ProgressIndicator spin = new ProgressIndicator(-1);
                spin.setPrefSize(14, 14);
                return new Spec(inState(new Button(null, spin), null),
                        ".button (ProgressIndicator graphic)");
            }
            return new Spec(command.apply(pseudos[c], null), ref(".button", pseudos[c]));
        });

        Node mxPrimary = matrix(150, states, new String[]{"Primary"}, (r, c) -> {
            if (c == 5) {
                ProgressIndicator spin = new ProgressIndicator(-1);
                spin.setPrefSize(14, 14);
                Button b = new Button(null, spin);
                b.setDefaultButton(true);
                return new Spec(inState(b, null), ".button:default (ProgressIndicator graphic)");
            }
            Button b = new Button("Save Overlay");
            b.setDefaultButton(true);
            return new Spec(inState(b, pseudos[c]), ref(".button:default", pseudos[c]));
        });

        Node mxDestructive = matrix(150, new String[]{"DEFAULT", "HOVER", "PRESSED", "DISABLED"},
                new String[]{"Destructive"}, (r, c) -> {
                    String[] p = {null, "hover", "armed", "disabled"};
                    Button b = styled(new Button("Delete"), "destructive");
                    return new Spec(inState(b, p[c]), ref(".button.destructive", p[c]));
                });

        Node mxTertiary = matrix(150, new String[]{"DEFAULT", "HOVER", "FOCUS", "DISABLED"},
                new String[]{"Tertiary"}, (r, c) -> {
                    String[] p = {null, "hover", "focused", "disabled"};
                    Button b = styled(new Button("Cancel"), "tertiary");
                    return new Spec(inState(b, p[c]), ref(".button.tertiary", p[c]));
                });

        int[] sizes = {28, 34, 42};
        int[] glyphs = {15, 18, 22};
        Node mxIcon = matrix(150, new String[]{"DEFAULT", "HOVER", "PRESSED", "DISABLED"},
                new String[]{"Small \u00b7 28", "Normal \u00b7 34", "Large \u00b7 42"}, (r, c) -> {
                    String[] p = {null, "hover", "armed", "disabled"};
                    Button b = styled(new Button(null, icon(I_TARGET, glyphs[r])), "icon-button");
                    b.setMinSize(sizes[r], sizes[r]);
                    b.setMaxSize(sizes[r], sizes[r]);
                    return new Spec(inState(b, p[c]), ref(".button.icon-button", p[c]));
                });

        FlowPane ribbon = specRow();
        Object[][] ribbons = {{"Layers", I_LAYERS, false}, {"Overlays", I_LAYERS, true},
                {"Basemap", I_GLOBE, false}, {"Measure", I_RULER, false}, {"Route", I_ROUTE, false}};
        for (Object[] spec : ribbons) {
            ToggleButton b = styled(new ToggleButton((String) spec[0], icon((String) spec[1], 26)),
                    "ribbon-button");
            b.setMinSize(60, 64);
            b.setSelected((boolean) spec[2]);
            inState(b, null);
            ribbon.getChildren().add(specimen(b, (boolean) spec[2] ? "selected"
                            : ((String) spec[0]).toLowerCase(),
                    (boolean) spec[2] ? ".toggle-button.ribbon-button:selected"
                            : ".toggle-button.ribbon-button"));
        }

        return section("Buttons",
                "State matrices across default \u00b7 hover \u00b7 focus \u00b7 pressed \u00b7 "
                        + "disabled \u00b7 loading. Command is the workhorse; primary/destructive/"
                        + "tertiary are semantic variants; icon & ribbon are the chrome buttons.",
                block("Command (default)", null, panel(mxCommand)),
                block("Primary", null, panel(mxPrimary)),
                block("Destructive", null, panel(mxDestructive)),
                block("Tertiary / text", null, panel(mxTertiary)),
                block("Icon button - sizes & states", null, panel(mxIcon)),
                block("Ribbon command button", "Icon-over-label, used in the application ribbon. "
                        + "Includes the selected/toggled state.", panel(ribbon)));
    }

    private Node inputsSection() {
        Node mxField = matrix(150,
                new String[]{"DEFAULT", "HOVER", "FOCUS", "DISABLED", "ERROR", "READ-ONLY"},
                new String[]{"Textbox"}, (r, c) -> {
                    TextField f = new TextField("Grid North");
                    f.setPrefColumnCount(9);
                    switch (c) {
                        case 1 -> { return new Spec(inState(f, "hover"), ".text-input:hover"); }
                        case 2 -> { return new Spec(inState(f, "focused"), ".text-input:focused"); }
                        case 3 -> { return new Spec(inState(f, "disabled"), ".text-input:disabled"); }
                        case 4 -> {
                            return new Spec(inState(styled(f, "error"), null), ".text-input.error");
                        }
                        case 5 -> {
                            f.setEditable(false);
                            return new Spec(inState(f, null), ".text-input:readonly");
                        }
                        default -> { return new Spec(inState(f, null), ".text-input"); }
                    }
                });

        TextField bad = styled(new TextField("91.0000"), "error");
        bad.setPrefColumnCount(9);
        VBox errorPattern = new VBox(3, inState(bad, null),
                styled(new Label("Latitude out of range"), "wintak-field-msg"));
        TextField ok = new TextField("45.4215");
        ok.setPrefColumnCount(9);
        VBox hintPattern = new VBox(3, inState(ok, null),
                styled(new Label("Decimal degrees, WGS-84"), "wintak-field-hint"));
        FlowPane patterns = specRow();
        patterns.getChildren().addAll(
                specimen(errorPattern, "invalid + message", ".text-input.error + .wintak-field-msg"),
                specimen(hintPattern, "default + hint", ".text-input + .wintak-field-hint"));

        Node mxArea = matrix(150, new String[]{"DEFAULT", "FOCUS", "DISABLED", "ERROR"},
                new String[]{"Textarea"}, (r, c) -> {
                    TextArea a = new TextArea("CASEVAC at OBJ BRAVO. Request immediate extraction.");
                    a.setPrefRowCount(2);
                    a.setPrefColumnCount(12);
                    a.setWrapText(true);
                    String[] p = {null, "focused", "disabled", null};
                    if (c == 3) {
                        return new Spec(inState(styled(a, "error"), null), ".text-area.error");
                    }
                    return new Spec(inState(a, p[c]), ref(".text-area", p[c]));
                });

        Node mxSearch = matrix(150, new String[]{"DEFAULT", "FOCUS", "DISABLED"},
                new String[]{"Search"}, (r, c) -> {
                    TextField input = new TextField(c == 1 ? "ALPHA" : "");
                    input.setPromptText("Filter by callsign\u2026");
                    input.setPrefColumnCount(9);
                    HBox search = styled(new HBox(icon(I_SEARCH, 13, "sheet-icon-dim"), input),
                            "wintak-search");
                    String[] p = {null, "focus-within", null};
                    if (c == 2) {
                        search.getStyleClass().add("disabled");
                        input.setDisable(true);
                    }
                    return new Spec(inState(search, p[c]), ref(".wintak-search", p[c]));
                });

        return section("Text Inputs",
                "The signature underline-frame field: 1px top, 2px sides & bottom; stroke color "
                        + "carries the state. Includes textarea, search and the inline error pattern.",
                block("Textbox", null, panel(mxField)),
                block("Error message pattern", null, panel(patterns)),
                block("Textarea", null, panel(mxArea)),
                block("Search field", null, panel(mxSearch)));
    }

    private Node selectSection() {
        Node mxCombo = matrix(150, new String[]{"DEFAULT", "HOVER", "FOCUS", "ERROR", "DISABLED"},
                new String[]{"Combobox"}, (r, c) -> {
                    ComboBox<String> box = combo("MGRS");
                    String[] p = {null, "hover", "focused", null, "disabled"};
                    if (c == 3) {
                        return new Spec(inState(styled(box, "error"), null), ".combo-box.error");
                    }
                    return new Spec(inState(box, p[c]), ref(".combo-box", p[c]));
                });

        VBox menu = menuCard(190,
                menuOption("Decimal Degrees", null),
                menuOption("MGRS", "selected"),
                menuOption("DD\u00b0 MM' SS\"", "hover"),
                menuOption("UTM", null));
        FlowPane open = specRow();
        open.getChildren().add(specimen(menu, "option: default / selected / hover",
                ".combo-box-popup .list-view .list-cell"));

        return section("Select & Combobox",
                "Field-bg well, neutral stroke, 4px radius. The expanded menu is a 3px card with "
                        + "hover & selected options.",
                block("Combobox - states", null, panel(mxCombo)),
                block("Expanded menu", null, panel(open)));
    }

    private Node togglesSection() {
        String[] states = {"DEFAULT", "HOVER", "FOCUS", "DISABLED"};
        String[] pseudos = {null, "hover", "focused", "disabled"};

        Node mxCheck = matrix(120, states, new String[]{"Unchecked", "Checked"}, (r, c) -> {
            CheckBox box = new CheckBox();
            if (r == 1) {
                box.setSelected(true);
            }
            return new Spec(inState(box, pseudos[c]),
                    ref(r == 1 ? ".check-box:selected" : ".check-box", pseudos[c]));
        });
        Node mxRadio = matrix(120, states, new String[]{"Unselected", "Selected"}, (r, c) -> {
            RadioButton radio = new RadioButton();
            if (r == 1) {
                radio.setSelected(true);
            }
            return new Spec(inState(radio, pseudos[c]),
                    ref(r == 1 ? ".radio-button:selected" : ".radio-button", pseudos[c]));
        });
        Node mxToggle = matrix(120, states, new String[]{"Off", "On"}, (r, c) -> {
            ToggleButton toggle = new ToggleButton(r == 1 ? "On" : "Off");
            if (r == 1) {
                toggle.setSelected(true);
            }
            return new Spec(inState(toggle, pseudos[c]),
                    ref(r == 1 ? ".toggle-button:selected" : ".toggle-button", pseudos[c]));
        });

        CheckBox indeterminate = new CheckBox("Partial selection");
        indeterminate.setIndeterminate(true);
        FlowPane extra = specRow();
        extra.getChildren().add(specimen(inState(indeterminate, null), "indeterminate",
                ".check-box:indeterminate"));

        return section("Checkbox \u00b7 Radio \u00b7 Toggle",
                "Inset 2px strokes with the reserved cyan -wintak-check mark; the toggle button "
                        + "is JavaFX's switch.",
                block("Checkbox", null, panel(mxCheck)),
                block("Radio", null, panel(mxRadio)),
                block("Toggle button", null, panel(mxToggle)),
                block("Indeterminate", null, panel(extra)));
    }

    private Node tabsSection() {
        TabPane ribbon = new TabPane(tab("Home"), tab("Creation"), tab("Map"), tab("Plugins"));
        ribbon.getSelectionModel().select(2);
        ribbon.setPrefHeight(90);
        tipOnly(ribbon, ".tab-pane / .tab:selected");

        TabPane boxed = new TabPane(tab("Overlays"), tab("Contacts"), tab("Tracks"), tab("Logs"));
        boxed.getStyleClass().add("dockpane-tabs");
        boxed.setPrefHeight(90);
        tipOnly(boxed, ".dockpane-tabs .tab");

        FlowPane segRow = specRow();
        segRow.getChildren().addAll(
                specimen(segmented(0, "2D", "3D", "Globe"), "map mode",
                        ".wintak-seg .toggle-button:selected"),
                specimen(segmented(1, "Day", "Night"), "palette", ".wintak-seg .toggle-button"));

        return section("Tabs & Segmented",
                "Ribbon-style tabs (accent active) and boxed dockpane tabs; segmented control for "
                        + "compact mutually-exclusive choices.",
                block("Ribbon tabs", null, panel(ribbon)),
                block("Dockpane (boxed) tabs", null, panel(boxed)),
                block("Segmented control", null, panel(segRow)));
    }

    private Node rangeSection() {
        Node mxSlider = matrix(150, new String[]{"DEFAULT", "FOCUS", "DISABLED"},
                new String[]{"Slider"}, (r, c) -> {
                    Slider slider = new Slider(0, 100, 58);
                    slider.setPrefWidth(200);
                    String[] p = {null, "focused", "disabled"};
                    return new Spec(inState(slider, p[c]), ref(".slider", p[c]));
                });
        FlowPane steppers = specRow();
        Spinner<Integer> zoom = new Spinner<>(0, 30, 12);
        zoom.setEditable(true);
        zoom.setPrefWidth(110);
        Spinner<Integer> rings = new Spinner<>(0, 10, 3);
        rings.setPrefWidth(110);
        steppers.getChildren().addAll(
                specimen(zoom, "zoom level", ".spinner"),
                specimen(rings, "range rings", ".spinner"));
        return section("Slider & Stepper", "Continuous and incremental numeric entry.",
                block("Slider", null, panel(mxSlider)),
                block("Stepper", null, panel(steppers)));
    }

    private Node chipsSection() {
        FlowPane row = specRow();
        row.getChildren().addAll(
                specimen(styled(new Label("ALPHA-1  \u00d7"), "wintak-chip"), "tag", ".wintak-chip"),
                specimen(styled(new Label("Filter: Hostile  \u00d7"), "wintak-chip-accent"),
                        "active filter", ".wintak-chip-accent"),
                specimen(badge("Online", "success"), "badge", ".wintak-badge.success"),
                specimen(badge("Stale", "warning"), "badge", ".wintak-badge.warning"),
                specimen(badge("Offline", "error"), "badge", ".wintak-badge.error"),
                specimen(pill("HEALTHY", "success"), "pill", ".wintak-pill.success"),
                specimen(pill("EXTENDED", "warning"), "pill", ".wintak-pill.warning"),
                specimen(pill("EXCEEDED", "error"), "pill", ".wintak-pill.error"),
                specimen(pill("NEW", "info"), "pill", ".wintak-pill.info"),
                specimen(styled(new Label("7"), "wintak-counter"), "count", ".wintak-counter"));
        row.setRowValignment(VPos.CENTER);
        return section("Chips \u00b7 Tags \u00b7 Badges",
                "Removable chips, status badges (dot + label), solid status pills, count badges.",
                panel(row));
    }

    private Node linksSection() {
        Node mx = matrix(150, new String[]{"DEFAULT", "HOVER", "VISITED", "FOCUS", "DISABLED"},
                new String[]{"Text link"}, (r, c) -> {
                    Hyperlink link = new Hyperlink("Open manager");
                    switch (c) {
                        case 1 -> { return new Spec(inState(link, "hover"), ".hyperlink:hover"); }
                        case 2 -> {
                            link.setVisited(true);
                            return new Spec(inState(link, null), ".hyperlink:visited");
                        }
                        case 3 -> { return new Spec(inState(link, "focused"), ".hyperlink:focused"); }
                        case 4 -> { return new Spec(inState(link, "disabled"), ".hyperlink:disabled"); }
                        default -> { return new Spec(inState(link, null), ".hyperlink"); }
                    }
                });
        return section("Links", "Inline text links across states.", panel(mx));
    }

    private Node overlaysSection() {
        VBox context = menuCard(200,
                menuItem(I_PIN, "Drop Marker", "Ctrl+M", null),
                menuItem(I_COPY, "Copy Coordinates", "Ctrl+C", "hover"),
                menuItem(I_EDIT, "Rename", null, null),
                styled(new Region(), "sheet-menu-sep"),
                menuItem(I_GEAR, "Properties", null, null),
                menuItem(I_TRASH, "Delete", "Del", "disabled"));

        Label tooltipSample = styled(new Label("Lock map rotation to true north"),
                "sheet-tooltip-sample");

        VBox popover = menuCard(170,
                menuOption("Friendly", "selected"),
                menuOption("Hostile", null),
                menuOption("Neutral", null),
                menuOption("Unknown", null));

        FlowPane row = specRow();
        row.setHgap(34);
        row.getChildren().addAll(
                specimen(context, "context menu", ".context-menu / .menu-item (mock: .sheet-menu-card)"),
                specimen(tooltipSample, "tooltip", ".tooltip (mock: .sheet-tooltip-sample)"),
                specimen(popover, "dropdown popover", ".combo-box-popup .list-view"));
        return section("Menus \u00b7 Tooltips \u00b7 Popovers",
                "Floating surfaces: context menu (with shortcuts & dividers), tooltip, and the "
                        + "dropdown popover. Live popups can't embed, so these are styled mocks of "
                        + "the real selectors.", panel(row));
    }

    private Node dataSection() {
        FlowPane rows = specRow();
        rows.setHgap(34);
        rows.getChildren().addAll(
                specimen(listRow("RAVEN 6 - Team Lead", null), "default", ".list-cell"),
                specimen(listRow("ALPHA-1 - Rifleman", "hover"), "hover", ".list-cell:filled:hover"),
                specimen(listRow("OBS-1 - Observation Post", "selected"), "selected",
                        ".list-cell:filled:selected"));

        ListView<String> list = new ListView<>(FXCollections.observableArrayList(
                "RAVEN 6 - Team Lead", "ALPHA-1 - Rifleman", "OBS-1 - Observation Post"));
        list.setPrefSize(220, 92);
        list.getSelectionModel().select(2);
        tipOnly(list, ".list-view / .list-cell:odd");

        TreeItem<String> root = new TreeItem<>("Overlays");
        root.setExpanded(true);
        root.getChildren().addAll(new TreeItem<>("AO BRAVO"), new TreeItem<>("Route GOLD"));
        TreeView<String> tree = new TreeView<>(root);
        tree.setPrefSize(220, 92);
        tree.getSelectionModel().select(1);
        tipOnly(tree, ".tree-view / .tree-cell");

        TableView<String[]> grid = new TableView<>(FXCollections.observableArrayList(
                new String[]{"RAVEN 6", "Friendly", "18T VR 446 311", "now"},
                new String[]{"ALPHA-1", "Friendly", "18T VR 472 329", "0:12"},
                new String[]{"HOSTILE TRK", "Hostile", "18T VR 510 240", "0:03 (selected)"},
                new String[]{"UNK-204", "Unknown", "18T VR 461 289", "0:43"}));
        String[] headers = {"Callsign", "Affiliation", "Grid", "Updated"};
        for (int i = 0; i < headers.length; i++) {
            final int col = i;
            TableColumn<String[], String> column = new TableColumn<>(headers[i]);
            column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue()[col]));
            column.setPrefWidth(col == 2 ? 140 : 110);
            grid.getColumns().add(column);
        }
        grid.getSelectionModel().select(2);
        grid.setPrefSize(490, 160);
        tipOnly(grid, ".table-view / .column-header / .table-row-cell:filled:selected");

        FlowPane real = specRow();
        real.setHgap(34);
        real.getChildren().addAll(
                specimen(list, "listview", ".list-view"),
                specimen(tree, "treeview", ".tree-view"));

        return section("Lists & Tables",
                "Listview rows (default \u00b7 hover \u00b7 selected) and the datagrid with header, "
                        + "zebra rows, and selection.",
                block("Listview rows", null, panel(rows)),
                block("Live list & tree", null, panel(real)),
                block("Datagrid", null, panel(specimen(grid, "datagrid",
                        ".table-view / .table-row-cell:filled:selected"))));
    }

    private Node feedbackSection() {
        VBox snackbars = new VBox(10,
                snackbar("normal", I_INFO, "12 markers synced to TAK server."),
                snackbar("success", I_CHECK, "Overlay saved successfully."),
                snackbar("warning", I_WARN, "GPS signal degraded - using last known position."),
                snackbar("error", I_ERRX, "Failed to reach server 10.0.4.21."));
        snackbars.getStyleClass().add("sheet-panel");

        Label title = new Label("Delete Overlay");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox titleBar = styled(new HBox(title, spacer, icon(I_CLOSE, 14, "sheet-icon-brand")),
                "sheet-modal-title");
        Label body = styled(new Label("Remove \u201cAO BRAVO\u201d and its 4 child markers? "
                + "This cannot be undone."), "sheet-modal-body");
        body.setWrapText(true);
        HBox foot = styled(new HBox(styled(new Button("Cancel"), "tertiary"),
                styled(new Button("Delete"), "destructive")), "sheet-modal-foot");
        VBox modal = styled(new VBox(titleBar, body, foot), "sheet-modal");
        modal.setMaxWidth(340);

        return section("Snackbars & Modals",
                "Toast feedback in four severities, and the dialog shell with maroon title bar.",
                block("Snackbars", null, snackbars),
                block("Modal dialog", null, panel(specimen(modal, "modal",
                        ".sheet-modal (chrome: -wintak-maroon)"))));
    }

    private Node progressSection() {
        ProgressBar determinate = new ProgressBar(0.62);
        determinate.setPrefWidth(220);
        ProgressBar indeterminate = new ProgressBar(ProgressBar.INDETERMINATE_PROGRESS);
        indeterminate.setPrefWidth(220);
        ProgressIndicator spinner = new ProgressIndicator(-1);
        spinner.setMaxSize(28, 28);
        ProgressIndicator ring = new ProgressIndicator(0.75);
        ring.setMaxSize(34, 34);
        VBox column = new VBox(18,
                specimen(determinate, "determinate \u00b7 62%", ".progress-bar > .bar"),
                specimen(indeterminate, "indeterminate", ".progress-bar:indeterminate"),
                specimen(new HBox(18, spinner, ring), "spinner / ring", ".progress-indicator"));
        column.getStyleClass().add("sheet-panel");
        return section("Progress & Loading", "Determinate bar, indeterminate bar, and spinner.",
                column);
    }

    private Node scrollbarSection() {
        ScrollBar bar = new ScrollBar();
        bar.setPrefWidth(160);
        VBox log = new VBox(2);
        for (String line : new String[]{"0601Z waypoint set", "0604Z marker dropped",
                "0612Z contact gained", "0620Z route updated", "0633Z overlay synced",
                "0641Z range ring added", "0655Z chat received", "0701Z basemap switched"}) {
            log.getChildren().add(styled(new Label(line), "wintak-dense"));
        }
        ScrollPane demo = new ScrollPane(log);
        demo.setPrefSize(160, 90);
        FlowPane row = specRow();
        row.getChildren().addAll(
                specimen(bar, "scrollbar", ".scroll-bar / .scroll-bar .thumb"),
                specimen(demo, "scroll the box", ".scroll-pane"));
        return section("Scrollbar", "Windows-dark scrollbar on the token sheet.", panel(row));
    }

    private Node playgroundSection() {
        FlowPane row = specRow();
        row.setHgap(26);

        CheckBox labels = new CheckBox("Show labels");
        ToggleGroup dims = new ToggleGroup();
        VBox radios = new VBox(6);
        for (String mode : new String[]{"2D", "3D", "Globe"}) {
            RadioButton radio = new RadioButton(mode);
            radio.setToggleGroup(dims);
            radio.setSelected(mode.equals("2D"));
            radios.getChildren().add(radio);
        }
        Slider slider = new Slider(0, 100, 50);
        slider.setPrefWidth(180);
        Spinner<Integer> stepper = new Spinner<>(0, 99, 8);
        stepper.setEditable(true);
        stepper.setPrefWidth(110);

        row.getChildren().addAll(
                specimen(new Button("Command", icon(I_TARGET, 15)), "hover \u00b7 focus \u00b7 press",
                        ".button"),
                specimen(new TextField("Type here"), "field", ".text-input"),
                specimen(labels, "click to toggle", ".check-box"),
                specimen(radios, "radio group", ".radio-button"),
                specimen(combo("MGRS"), "click to open", ".combo-box"),
                specimen(slider, "drag", ".slider"),
                specimen(segmented(0, "Low", "Med", "High"), "segmented", ".wintak-seg"),
                specimen(stepper, "stepper", ".spinner"));
        return section("Live Playground",
                "Fully interactive instances - real hover, focus, click, toggle and theme response.",
                panel(row));
    }

    /** JavaFX controls beyond the HTML sheet - kept so the bench covers every family. */
    private Node extrasSection() {
        FlowPane row1 = specRow();
        PasswordField password = new PasswordField();
        password.setText("secret");
        ChoiceBox<String> choice = new ChoiceBox<>(
                FXCollections.observableArrayList("Choice A", "Choice B"));
        choice.getSelectionModel().selectFirst();
        MenuButton menu = new MenuButton("Menu button");
        menu.getItems().addAll(new MenuItem("First"), new MenuItem("Second"));
        SplitMenuButton split = new SplitMenuButton(new MenuItem("Action"));
        split.setText("Split menu");
        Label disabledLabel = new Label("Disabled label");
        disabledLabel.setDisable(true);
        row1.getChildren().addAll(
                specimen(password, "password", ".text-input (PasswordField)"),
                specimen(choice, "choice box", ".choice-box"),
                specimen(menu, "menu button", ".menu-button"),
                specimen(split, "split menu", ".split-menu-button"),
                specimen(new DatePicker(LocalDate.now()), "date picker", ".date-picker"),
                specimen(new ColorPicker(), "color picker", ".color-picker"),
                specimen(disabledLabel, "disabled text", ".label:disabled"));

        FlowPane row2 = specRow();
        TitledPane titled = new TitledPane("Titled pane", new Label("content"));
        titled.setPrefWidth(180);
        Accordion accordion = new Accordion(
                new TitledPane("Section A", new Label("a")),
                new TitledPane("Section B", new Label("b")));
        accordion.setPrefWidth(180);
        Pagination pagination = new Pagination(5, 0);
        pagination.setPrefHeight(60);
        ToolBar toolBar = new ToolBar(new Button("Tool"), new Separator(),
                new ToggleButton("Toggle"));
        row2.getChildren().addAll(
                specimen(titled, "titled pane", ".titled-pane > .title"),
                specimen(accordion, "accordion", ".accordion / .titled-pane"),
                specimen(pagination, "pagination", ".pagination .number-button:selected"),
                specimen(toolBar, "toolbar", ".tool-bar"),
                specimen(new Separator(), "separator", ".separator .line"));

        return section("JavaFX Extras",
                "Controls beyond the HTML specimen - styled by the same token sheets so no Modena "
                        + "default leaks through.",
                block("Inputs & pickers", null, panel(row1)),
                block("Containers & navigation", null, panel(row2)));
    }

    /* =====================================================================
       SPECIMEN HELPERS
       ===================================================================== */

    /** Section: uppercase rule-bottom heading + sub + body; registered for the jump nav. */
    private Node section(String title, String sub, Node... body) {
        Label heading = styled(new Label(title.toUpperCase(Locale.ENGLISH)), "sheet-h2");
        heading.setMaxWidth(Double.MAX_VALUE);
        VBox rule = styled(new VBox(heading), "sheet-rule");
        VBox box = new VBox(9, rule);
        if (sub != null) {
            Label subLabel = styled(new Label(sub), "sheet-sub");
            subLabel.setWrapText(true);
            box.getChildren().add(subLabel);
        }
        VBox blocks = new VBox(26);
        blocks.getChildren().addAll(body);
        box.getChildren().add(blocks);
        sections.put(title, box);
        return box;
    }

    private static Node block(String title, String desc, Node body) {
        VBox box = new VBox(3, styled(new Label(title), "sheet-h3"));
        if (desc != null) {
            Label descLabel = styled(new Label(desc), "sheet-desc");
            descLabel.setWrapText(true);
            box.getChildren().add(descLabel);
        }
        VBox.setMargin(body, new Insets(11, 0, 0, 0));
        box.getChildren().add(body);
        return box;
    }

    private static Node panel(Node content) {
        if (content.getStyleClass().contains("sheet-panel")) {
            return content;
        }
        VBox box = new VBox(content);
        box.getStyleClass().add("sheet-panel");
        return box;
    }

    /** A wrapping specimen row - the HTML sheet's flex-wrap, so nothing ever truncates. */
    private static FlowPane specRow() {
        FlowPane row = new FlowPane(18, 14);
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    /** A specimen with its mono caption and the style-reference hover tooltip. */
    private static Node specimen(Node node, String caption, String styleRef) {
        VBox box = new VBox(7, node);
        box.setAlignment(Pos.TOP_LEFT);
        box.setFillWidth(false);
        if (caption != null) {
            box.getChildren().add(styled(new Label(caption), "sheet-cap"));
        }
        tip(box, styleRef);
        return box;
    }

    /**
     * Locks a specimen into a display state: mouse-transparent (so a forced :hover/:focused
     * never gets cleared by real pointer traffic) with the pseudo-classes applied.
     */
    private static <T extends Node> T inState(T node, String pseudo) {
        node.setMouseTransparent(true);
        node.setFocusTraversable(false);
        if ("disabled".equals(pseudo)) {
            node.setDisable(true);
        } else if (pseudo != null) {
            node.pseudoClassStateChanged(PseudoClass.getPseudoClass(pseudo), true);
        }
        return node;
    }

    private static String ref(String selector, String pseudo) {
        return pseudo == null ? selector : selector + ":" + pseudo;
    }

    private static <T extends Node> T styled(T node, String styleClass) {
        node.getStyleClass().add(styleClass);
        return node;
    }

    private static void tip(Node node, String styleRef) {
        if (styleRef == null) {
            return;
        }
        Tooltip tooltip = new Tooltip(styleRef);
        tooltip.getStyleClass().add("style-ref-tip");
        tooltip.setShowDelay(Duration.millis(200));
        Tooltip.install(node, tooltip);
        node.getProperties().put(STYLE_REF_KEY, styleRef);
    }

    /** Tooltip directly on an interactive specimen (no caption wrapper). */
    private static void tipOnly(Node node, String styleRef) {
        tip(node, styleRef);
    }

    /**
     * State matrix mirroring the HTML sheet: corner + state headers across, one row per
     * variant, every cell tooltipped with the selector it demonstrates.
     */
    private static Node matrix(double labelWidth, String[] cols, String[] rows,
                               BiFunction<Integer, Integer, Spec> cells) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("sheet-matrix");
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(labelWidth);
        grid.getColumnConstraints().add(labelCol);
        for (int c = 0; c < cols.length; c++) {
            ColumnConstraints state = new ColumnConstraints();
            state.setHgrow(Priority.ALWAYS);
            state.setFillWidth(true);
            grid.getColumnConstraints().add(state);
        }

        grid.add(headerCell(""), 0, 0);
        for (int c = 0; c < cols.length; c++) {
            grid.add(headerCell(cols[c]), c + 1, 0);
        }
        for (int r = 0; r < rows.length; r++) {
            Label rowLabel = styled(new Label(rows[r]), "sheet-ml");
            rowLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            grid.add(rowLabel, 0, r + 1);
            for (int c = 0; c < cols.length; c++) {
                Spec spec = cells.apply(r, c);
                StackPane cell = styled(new StackPane(spec.node()), "sheet-mc");
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                tip(cell, spec.ref());
                grid.add(cell, c + 1, r + 1);
            }
        }
        return grid;
    }

    private static Label headerCell(String text) {
        Label label = styled(new Label(text), "sheet-mh");
        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return label;
    }

    private record Spec(Node node, String ref) {}

    /* component builders */

    private static ComboBox<String> combo(String value) {
        ComboBox<String> box = new ComboBox<>(FXCollections.observableArrayList(
                "MGRS", "Decimal Degrees", "UTM", value));
        box.setValue(value);
        box.setPrefWidth(190);
        return box;
    }

    private static CheckBox checked(CheckBox box) {
        box.setSelected(true);
        return box;
    }

    private static Tab tab(String title) {
        Tab tab = new Tab(title, new Label(title + " content"));
        tab.setClosable(false);
        return tab;
    }

    private static Node segmented(int selected, String... options) {
        HBox seg = new HBox();
        seg.getStyleClass().add("wintak-seg");
        ToggleGroup group = new ToggleGroup();
        for (int i = 0; i < options.length; i++) {
            ToggleButton button = new ToggleButton(options[i]);
            button.setToggleGroup(group);
            button.setSelected(i == selected);
            seg.getChildren().add(button);
        }
        return seg;
    }

    private static Label badge(String text, String severity) {
        Label label = new Label(text, styled(new Region(), "wintak-badge-dot"));
        label.getStyleClass().addAll("wintak-badge", severity);
        return label;
    }

    private static Label pill(String text, String severity) {
        Label label = new Label(text);
        label.getStyleClass().addAll("wintak-pill", severity);
        return label;
    }

    private static HBox snackbar(String severity, String iconPath, String text) {
        HBox bar = new HBox(icon(iconPath, 16, "sheet-icon-ink"), new Label(text));
        bar.getStyleClass().addAll("wintak-snackbar", severity);
        bar.setMaxWidth(324);
        tip(bar, ".wintak-snackbar." + severity);
        return bar;
    }

    private static VBox menuCard(double width, Node... rows) {
        VBox card = new VBox(rows);
        card.getStyleClass().add("sheet-menu-card");
        card.setMaxWidth(width);
        card.setMinWidth(width);
        return card;
    }

    private static Node menuOption(String text, String state) {
        HBox row = new HBox(new Label(text));
        row.getStyleClass().add("sheet-menu-row");
        if (state != null) {
            row.getStyleClass().add(state);
        }
        return row;
    }

    private static Node menuItem(String iconPath, String text, String shortcut, String state) {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(icon(iconPath, 14, "sheet-icon-dim"), new Label(text), spacer);
        if (shortcut != null) {
            row.getChildren().add(styled(new Label(shortcut), "sheet-menu-shortcut"));
        }
        row.getStyleClass().add("sheet-menu-row");
        if (state != null) {
            row.getStyleClass().add(state);
        }
        return row;
    }

    private static Label listRow(String text, String state) {
        Label row = styled(new Label(text), "sheet-list-row");
        if (state != null) {
            row.getStyleClass().add(state);
        }
        row.setMinWidth(220);
        return row;
    }

    private static Node icon(String path, double size) {
        return icon(path, size, null);
    }

    private static Node icon(String path, double size, String extraClass) {
        SVGPath glyph = new SVGPath();
        glyph.setContent(path);
        glyph.getStyleClass().add("sheet-icon");
        if (extraClass != null) {
            glyph.getStyleClass().add(extraClass);
        }
        double k = size / 24.0;
        glyph.getTransforms().add(new Scale(k, k));
        return new Group(glyph);
    }

    /** Token swatch grid: chip + name + token + role, tooltipped with the token. */
    private static Node swatches(String[][] entries) {
        FlowPane pane = new FlowPane(12, 12);
        for (String[] entry : entries) {
            Region chip = new Region();
            chip.getStyleClass().addAll("sheet-swatch-chip", "sw-" + entry[1]);
            VBox info = new VBox(1,
                    styled(new Label(entry[0]), "sheet-swatch-name"),
                    styled(new Label("-wintak-" + entry[1]), "sheet-swatch-token"),
                    styled(new Label(entry[2]), "sheet-swatch-role"));
            info.setPadding(new Insets(7, 9, 7, 9));
            VBox swatch = styled(new VBox(chip, info), "sheet-swatch");
            swatch.setPrefWidth(168);
            tip(swatch, "-wintak-" + entry[1]);
            pane.getChildren().add(swatch);
        }
        return pane;
    }
}
