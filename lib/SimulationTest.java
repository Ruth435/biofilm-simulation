package lib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import Simulation.Bacteria;
import Simulation.Medium;

public class SimulationTest {
    @Test
    public void testReadInputFileWithWrongTypes() {
        Bacteria result = Simulation.Simulation.readInputFile("SimulationInputTest1.txt");
        assertEquals(null, result);
    }

    @Test
    public void testReadInputFileWithWrongTypes2() {
        Bacteria result = Simulation.Simulation.readInputFile("SimulationInputTest2.txt");
        assertEquals(null, result);
    }

    @Test
    public void testReadInputFileWithBadNegatives() {
        Bacteria result = Simulation.Simulation.readInputFile("SimulationInputTest3.txt");
        assertEquals(null, result);
    }

    @Test
    public void testReadInputFileWithCorrectTypes() {
        Bacteria result = Simulation.Simulation.readInputFile("SimulationConfig.txt");
        Assert.assertNotEquals(null, result);
    }

}