package com.richardl.wintak.view;

import com.richardl.wintak.app.WintakApp;
import com.richardl.wintak.controller.MainController;
import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import com.richardl.wintak.view.theme.Theme;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * UX-review capture harness (CLAUDE.md "UX REVIEW" step). Renders the app scene
 * off-screen via {@code Scene.snapshot} - the app window region only, never the
 * desktop - and writes PNGs to {@code Design/captures/} (git-ignored).
 *
 * <p>Deliberately NOT named {@code *Test} so surefire skips it in normal runs;
 * invoke on demand with {@code mvn test -Dtest=ComponentSheetCaptures}.
 */
@ExtendWith(FxToolkitExtension.class)
class ComponentSheetCaptures {

    private static final Path OUT_DIR = Path.of("Design", "captures");

    /** Scroll stops down the specimen sheet, named for what they land on at 1280x800. */
    private static final double[][] STOPS = {
            {0.00}, {0.18}, {0.30}, {0.45}, {0.62}, {0.80}, {0.95}};
    private static final String[] STOP_NAMES = {
            "top", "foundations", "buttons", "inputs", "toggles-tabs", "data-feedback", "tail"};

    @Test
    void capture() throws Exception {
        Files.createDirectories(OUT_DIR);
        FxThread.run(() -> {
            try {
                MainController controller = new MainController();
                Scene scene = WintakApp.buildScene(controller);

                render(scene);
                shoot(scene, "app-dark.png");

                controller.componentSheetVisibleProperty().set(true);
                render(scene);
                ScrollPane scroll = (ScrollPane) scene.getRoot()
                        .lookup("#component-sheet .scroll-pane");
                for (int i = 0; i < STOPS.length; i++) {
                    scroll.setVvalue(STOPS[i][0]);
                    shoot(scene, "sheet-dark-" + STOP_NAMES[i] + ".png");
                }

                controller.getThemeManager().themeProperty().set(Theme.LIGHT);
                render(scene);
                scroll.setVvalue(0);
                shoot(scene, "sheet-light-top.png");
                scroll.setVvalue(0.30);
                shoot(scene, "sheet-light-buttons.png");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void render(Scene scene) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
    }

    private static void shoot(Scene scene, String name) throws IOException {
        writePng(scene.snapshot(null), OUT_DIR.resolve(name).toString());
    }

    /* Minimal PNG writer - no javafx-swing on the classpath (see maven playbook). */
    private static void writePng(WritableImage img, String file) throws IOException {
        int w = (int) img.getWidth();
        int h = (int) img.getHeight();
        PixelReader px = img.getPixelReader();
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        for (int y = 0; y < h; y++) {
            raw.write(0);
            for (int x = 0; x < w; x++) {
                int argb = px.getArgb(x, y);
                raw.write((argb >> 16) & 0xFF);
                raw.write((argb >> 8) & 0xFF);
                raw.write(argb & 0xFF);
            }
        }
        ByteArrayOutputStream zipped = new ByteArrayOutputStream();
        try (DeflaterOutputStream dos = new DeflaterOutputStream(zipped,
                new Deflater(Deflater.BEST_SPEED))) {
            raw.writeTo(dos);
        }
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
            out.write(new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'});
            ByteArrayOutputStream ihdr = new ByteArrayOutputStream();
            DataOutputStream d = new DataOutputStream(ihdr);
            d.writeInt(w);
            d.writeInt(h);
            d.write(new byte[]{8, 2, 0, 0, 0});
            chunk(out, "IHDR", ihdr.toByteArray());
            chunk(out, "IDAT", zipped.toByteArray());
            chunk(out, "IEND", new byte[0]);
        }
    }

    private static void chunk(DataOutputStream out, String type, byte[] data) throws IOException {
        out.writeInt(data.length);
        byte[] t = type.getBytes(StandardCharsets.US_ASCII);
        out.write(t);
        out.write(data);
        CRC32 crc = new CRC32();
        crc.update(t);
        crc.update(data);
        out.writeInt((int) crc.getValue());
    }
}
