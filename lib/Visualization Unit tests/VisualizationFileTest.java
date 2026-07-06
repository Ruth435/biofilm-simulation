package tests;

import src.Visualization;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

public class VisualizationFileTest {

    private Visualization vis;
    private Path tempFile;

    @BeforeEach
    void setUp() throws IOException {
        vis = new Visualization();
        tempFile = Files.createTempFile("simtest", ".txt");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    void readValidSimulationFilePopulatesData() throws Exception {
        // Minimal valid file with headers + one frame
        String content = """
                #Settings
                radius:10
                #Frame
                BACTERIA 1 1 0 1 1 0 1 red
                EPS 2 2 0 1 blue
                """;
        Files.writeString(tempFile, content);
        vis.readSimulationFile(tempFile.toString());

        assertFalse(vis.bacteria.isEmpty());
        assertFalse(vis.epsList.isEmpty());
        assertEquals("red", vis.bacteria.get(0).colour.toString().toLowerCase());
    }

    @Test
    void readEmptyFileDoesNotCrash() throws Exception {
        Files.writeString(tempFile, "");
        vis.readSimulationFile(tempFile.toString());
        assertTrue(vis.bacteria.isEmpty());
        assertTrue(vis.epsList.isEmpty());
    }

    @Test
    void readCorruptFileThrowsControlledException() throws Exception {
        Files.writeString(tempFile, "BAD DATA");
        assertThrows(Exception.class,
                () -> vis.readSimulationFile(tempFile.toString()));
    }
}
