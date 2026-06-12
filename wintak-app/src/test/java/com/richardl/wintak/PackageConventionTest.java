package com.richardl.wintak;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** The MVC layers live in fixed packages; each is documented by a package-info.java. */
class PackageConventionTest {

    private static final Path MAIN_ROOT = Path.of("src", "main", "java", "com", "richardl", "wintak");

    @ParameterizedTest
    @ValueSource(strings = {"model", "view", "controller", "app"})
    void mvcPackageExistsAndIsDocumented(String pkg) {
        Path packageInfo = MAIN_ROOT.resolve(pkg).resolve("package-info.java");
        assertTrue(Files.isRegularFile(packageInfo), "missing " + packageInfo);
    }
}
