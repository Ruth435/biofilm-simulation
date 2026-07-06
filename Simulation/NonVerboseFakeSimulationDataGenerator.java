package Simulation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class NonVerboseFakeSimulationDataGenerator {

    private static final Random rand = new Random();

    public static void main(String[] args) {
        String fileName = "fake_simulation_output.txt";
        int timePoints = 5;        // number of time snapshots
        int bacteriaCount = 10;    // number of bacteria per time point
        int epsCount = 5;          // number of EPS per time point

        try (FileWriter writer = new FileWriter(fileName)) {
            // ---- Simulation parameters ----
            int x = rand.nextInt(100);
            int y = rand.nextInt(100);
            double fmot = rand.nextDouble();
            double kdiv = rand.nextDouble();
            double nut = 10 + rand.nextDouble() * 90;
            double kEPS = rand.nextDouble();
            double EPSnutMIN = 5 + rand.nextDouble() * 5;
            double EPSsize = 1 + rand.nextDouble() * 4;
            double EPScellAttractionRange = 5 + rand.nextDouble() * 10;
            double CoeffFriction = rand.nextDouble();
            int maxAgents = 1000;
            int ticksBetweenOutput = 50;
            int simulationBoundary = 200;

            writer.write(x + ", " + y + "\n");
            writer.write(fmot + "\n");
            writer.write(kdiv + "\n");
            writer.write(nut + "\n");
            writer.write(kEPS + "\n");
            writer.write(EPSnutMIN + "\n");
            writer.write(EPSsize + "\n");
            writer.write(EPScellAttractionRange + "\n");
            writer.write(CoeffFriction + "\n");
            writer.write(maxAgents + "\n");
            writer.write(ticksBetweenOutput + "\n");
            writer.write(simulationBoundary + "\n\n");

            // ---- Time points ----
            for (int t = 1; t <= timePoints; t++) {
                int ticksSinceStart = t * ticksBetweenOutput;
                int colonyX = rand.nextInt(simulationBoundary);
                int colonyY = rand.nextInt(simulationBoundary);
                double diameter = 5 + rand.nextDouble() * 20;

                writer.write(t + "\n");
                writer.write(ticksSinceStart + "\n");
                writer.write(colonyX + ", " + colonyY + "\n");
                writer.write(diameter + "\n");

                // ---- Bacteria ----
                writer.write("Bacteria:\n");
                for (int b = 0; b < bacteriaCount; b++) {
                    int id = b;
                    int bx = rand.nextInt(simulationBoundary);
                    int by = rand.nextInt(simulationBoundary);
                    double xFacing = rand.nextDouble() * 2 - 1;
                    double yFacing = rand.nextDouble() * 2 - 1;
                    double size = 0.5 + rand.nextDouble() * 1.5;
                    int cluster = rand.nextInt(3);
                    writer.write(id + ", " + bx + ", " + by + ", " +
                            xFacing + ", " + yFacing + ", " + size + ", " + cluster + "\n");
                }

                // ---- EPS ----
                writer.write("EPS:\n");
                for (int e = 0; e < epsCount; e++) {
                    int id = e;
                    int ex = rand.nextInt(simulationBoundary);
                    int ey = rand.nextInt(simulationBoundary);
                    int cluster = rand.nextInt(2);
                    writer.write(id + ", " + ex + ", " + ey + ", " + cluster + "\n");
                }

                // ---- Bacterial clusters ----
                writer.write("Bacterial Clusters:\n");
                for (int c = 0; c < 3; c++) {
                    int particles = rand.nextInt(bacteriaCount / 2) + 1;
                    writer.write(c + ", " + particles + "\n");
                }

                // ---- EPS clusters ----
                writer.write("EPS Clusters:\n");
                for (int c = 0; c < 2; c++) {
                    int particles = rand.nextInt(epsCount / 2) + 1;
                    writer.write(c + ", " + particles + "\n");
                }

                writer.write("\n");
            }

            System.out.println("Fake simulation data written to " + fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

