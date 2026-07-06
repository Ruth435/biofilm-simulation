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
import src.EPS;

/**
 *
 * @author ruth2
 */
public class EPSTest {
    
    @Test
    void sizeAccessorsWork() {
        EPS eps = new EPS(2, 1.0, 2.0, 5.5, 3, Color.GREEN);
        assertEquals(5.5, eps.getSize());
        eps.setSize(7.7);
        assertEquals(7.7, eps.getSize());
    }
    
}
