package com.richardl.wintak.app;

/**
 * Plain (non-Application) entry point so {@code java -jar} and IDE runs work from the
 * classpath: the JVM's "JavaFX runtime components are missing" check only fires when the
 * launched class itself extends Application. The shaded jar's manifest points here.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        WintakApp.main(args);
    }
}
