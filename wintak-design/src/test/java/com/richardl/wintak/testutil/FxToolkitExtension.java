package com.richardl.wintak.testutil;

import javafx.application.Platform;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Boots the JavaFX toolkit once per JVM so view/controller tests can construct scene-graph
 * nodes without {@code Application.launch()} (which is one-shot and never test-safe).
 * Usage: {@code @ExtendWith(FxToolkitExtension.class)}; do FX work via {@link FxThread}.
 */
public final class FxToolkitExtension implements BeforeAllCallback {

    private static final AtomicBoolean STARTED = new AtomicBoolean();

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (!STARTED.compareAndSet(false, true)) {
            return;
        }
        CountDownLatch ready = new CountDownLatch(1);
        Platform.startup(ready::countDown);
        if (!ready.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("JavaFX toolkit did not start within 10s");
        }
        // Keep the toolkit alive across test classes; surefire runs them all in one JVM.
        Platform.setImplicitExit(false);
    }
}
