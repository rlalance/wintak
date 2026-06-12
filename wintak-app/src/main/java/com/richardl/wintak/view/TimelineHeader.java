package com.richardl.wintak.view;

import com.richardl.wintak.model.TimeScale;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Date ruler above the bar canvas: week labels (Mondays) on top, day-of-month labels beneath
 * once a day cell is wide enough to carry them. Re-rendered whenever scale or range change.
 */
public class TimelineHeader extends Pane {

    /** Below this zoom a day cell is too narrow for a label. */
    public static final double DAY_LABEL_MIN_PX = 12;

    public static final double WEEK_ROW_Y = 2;
    public static final double DAY_ROW_Y = 20;

    /** Pinned to English - pattern-only formatters silently follow the OS locale. */
    private static final DateTimeFormatter WEEK_FORMAT =
            DateTimeFormatter.ofPattern("MMM d", java.util.Locale.ENGLISH);

    public TimelineHeader() {
        setId("timeline-header");
        getStyleClass().add("timeline-header");
        setMinHeight(38);
        setPrefHeight(38);
    }

    /** Rebuilds the ruler for the given scale and visible date range. */
    public void render(TimeScale scale, LocalDate from, LocalDate to) {
        getChildren().clear();

        for (LocalDate tick : scale.ticks(TimeScale.TickUnit.WEEK, from, to)) {
            Label label = ruled(WEEK_FORMAT.format(tick), scale.xOf(tick), WEEK_ROW_Y, "week-label");
            getChildren().add(label);
        }

        if (scale.pxPerDay() >= DAY_LABEL_MIN_PX) {
            for (LocalDate tick : scale.ticks(TimeScale.TickUnit.DAY, from, to)) {
                Label label = ruled(String.valueOf(tick.getDayOfMonth()),
                        scale.xOf(tick), DAY_ROW_Y, "day-label");
                getChildren().add(label);
            }
        }
    }

    private static Label ruled(String text, double x, double y, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().addAll(styleClass, "wintak-mono");
        label.setLayoutX(x);
        label.setLayoutY(y);
        return label;
    }

    public List<Label> weekLabels() {
        return labels("week-label");
    }

    public List<Label> dayLabels() {
        return labels("day-label");
    }

    private List<Label> labels(String styleClass) {
        return getChildren().stream()
                .filter(n -> n.getStyleClass().contains(styleClass))
                .map(Label.class::cast)
                .toList();
    }
}
