/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package testing.unittestsvisualization;

import java.awt.Color;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import src.VisualParticle;

/**
 *
 * @author ruth2
 */
public class VisualParticleTest {
    
    private VisualParticle vp;

    @BeforeEach
    void setup() {
        vp = new VisualParticle(1, 10.5, 20.5, 7, Color.RED);
    }

    @Test
    void constructorAndGettersWork() {
        assertEquals(Color.RED, vp.getColour());
        assertEquals(10.5, vp.getX());
        assertEquals(20.5, vp.getY());
        assertEquals(7, vp.getClusterID());
    }

    @Test
    void settersUpdateValues() {
        vp.setColour(Color.BLUE);
        vp.setX(42.0);
        vp.setY(-13.2);
        assertEquals(Color.BLUE, vp.getColour());
        assertEquals(42.0, vp.getX());
        assertEquals(-13.2, vp.getY());
    }
    
}
