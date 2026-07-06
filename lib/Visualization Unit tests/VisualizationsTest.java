package testing.unittestsvisualization;

import src.Visualization;
import org.junit.jupiter.api.Test;
import java.awt.Color;
import java.awt.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VisualizationsTest {

    private Visualization vis;

    @BeforeEach
    void setUp() {
        vis = new Visualization();
        vis.simulationRadius = 100;
        vis.artificial_size_increase = 1.0;
        vis.cellSizePx = 2.0;
    }

    @Test
    void parseColorValidAndInvalid() {
        assertEquals(Color.RED, vis.parseColor("red"));
        assertEquals(Color.BLUE, vis.parseColor("Blue")); // case-insensitive
        // Invalid -> default CYAN
        assertEquals(Color.CYAN, vis.parseColor("notAColor"));
    }

    @Test
    void generateColorHueWithinRange() {
        for (int i = 0; i < 20; i++) {
            Color c = vis.generateColor(i);
            float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
            assertTrue(hsb[0] >= 0 && hsb[0] <= 1);
            assertTrue(hsb[1] >= 0.5 && hsb[1] <= 1);
            assertTrue(hsb[2] >= 0.5 && hsb[2] <= 1);
        }
    }

    @Test
    void togglePauseAndResumePlayback() throws InterruptedException {
        assertFalse(vis.paused);
        vis.togglePause();
        assertTrue(vis.paused);
        vis.togglePause();
        assertFalse(vis.paused);
        // resumePlayback should also set paused=false
        vis.paused = true;
        vis.resumePlayback();
        assertFalse(vis.paused);
    }
}
