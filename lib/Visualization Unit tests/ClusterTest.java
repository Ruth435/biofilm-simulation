/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package testing.unittestsvisualization;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import src.Cluster;

/**
 *
 * @author ruth2
 */
public class ClusterTest {
    
    @Test
    void constructorAndGettersWork() {
        Cluster c = new Cluster(12, 42);
        assertEquals(12, c.getId());
        assertEquals(42, c.getCount());
    }
    
}
