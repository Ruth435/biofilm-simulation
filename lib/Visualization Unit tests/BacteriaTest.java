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
import src.Bacteria;

/**
 *
 * @author ruth2
 */
public class BacteriaTest {
    
    @Test
    void constructorAndGettersWork() {
        Bacteria b = new Bacteria(5, 1.0, 2.0,
                0.3, 0.7, 4.0, 1.0, 9, Color.YELLOW);
        assertEquals(0.3, b.getXFacing());
        assertEquals(0.7, b.getYFacing());
        assertEquals(4.0, b.getLength());
        assertEquals(1.0, b.getWidth());
    }
    
}
