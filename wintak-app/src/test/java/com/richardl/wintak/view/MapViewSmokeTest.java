package com.richardl.wintak.view;

import com.sothawo.mapjfx.MapView;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MapViewSmokeTest {

    @Test
    void mapjfxClassOnClasspath() {
        assertNotNull(MapView.class.getName());
    }
}
