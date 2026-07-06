/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Simulation;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ruth Rudolph, Sonali Naidoo and Byron Clarke
 */
public class Simulation {

    //Instrumentation things:
    long calcFnetTime = 0, clearcoordstime = 0, movetime = 0, growtime = 0, dividetime = 0, diffusetime = 0, prodepstime = 0;

    //args[0], and args[1] are the user selected input and output files otherwise
    public static final String DEFAULT_INPUT_FILE = "simulationConfig.txt";
    public static final String DEFAULT_OUTPUT_FILE = "simulation_output.txt";

    public static int totalRuntime = 500; //Number of timepoints in output
    private static final double GRID_SPACING = 1.0; //distance between grid points

    public static int maxAgents;

    //This value is measured in in-simulation-hours. 
    //When getting input from the user it will ask for a value in in-simulation-minutes and convert it.
    private static double outputInterval;

    /*
     * Most units are measured in hours (as that is reasonable for real bacteria)
     * Here, each tick will be 10 seconds, and therefore 360 ticks per hour.
     * As for our perception of this time, it will run as fast as possible, therefore likely more than an hour per second of real time. 
     */
    private int ticksSinceStart, timePoint;
    public static final int TICKS_PER_HOUR = 360;
    private static final double TIME_STEP = 0.05; //Alternatively use 1/TICKS_PER_HOUR for slower growth

    private Medium media;
    private FileWriter outputWriter;
    public Bacteria colonyStart;
    
    public Simulation(String configFile, String outputFile)
    {
        colonyStart = readInputFile(configFile);
        //Initialize the media and place first bacteria
        media = new Medium(maxAgents, colonyStart);
        //Set up timing
        ticksSinceStart = 0;
        timePoint = 0;

        try {
            outputWriter = new FileWriter(outputFile);
        } catch (IOException e) {
            System.out.println("The filewriter couldn't be created");
            e.printStackTrace();
        }
    }
    
    /**
     * This method makes the whole simulation run, with multithreading to process each time-step
     * and intermediate output as fast as possible
     */
    public void startSimulation() {
        try {
            //Write the input parameters to the output file
            outputWriter.write(
                maxAgents+"\n"
                +outputInterval+"\n\n"

                +Medium.simulationBoundary+"\n"
                +Medium.nutrientConcentration+"\n"
                +Medium.nutrientConsumptionRate+"\n"
                +Medium.nutrientDiffusionRate+"\n\n"

                +EPS.size+"\n"
                +EPS.EPSElasticModulus+"\n\n"

                +Bacteria.lmax+"\n"
                +Bacteria.d0+"\n"
                +Bacteria.motility+"\n"
                +Bacteria.growthRate+"\n"
                +Bacteria.reproductionRate+"\n"
                +Bacteria.EPSproductionRate+"\n"
                +Bacteria.EPSElasticModulus+"\n"
                +Bacteria.BacteriaElasticModulus+"\n"
                +Particle.FrictionCoefficientPascalsPerhour+"\n"
                +Bacteria.EPSProductionMinCellDensity+"\n"
                +Bacteria.EPSProductionsMaxEPSDensity+"\n\n"
                );

        } catch (IOException e) {
            System.out.println("Outputwriter couldn't write the input parameters");
            e.printStackTrace();
        }

        //add first bacteria to the media
        media.addBacteriaToArray(colonyStart);
        colonyStart.setMedium(media);

        //Start running actual simulation
        intermediateOutput();
        // long justAfterOutput = System.nanoTime(); //Instrumentation
        long startTime = System.nanoTime(); //Instrumentation
        timePoint++;
        while (timePoint < totalRuntime) {
            // long preTimeStep = System.nanoTime(); //Instrumentation
            timeStep();
            // System.out.println("TimeStepTime: " + (System.nanoTime()-preTimeStep)/1000 + "us");
            ticksSinceStart++;
            if (ticksSinceStart%(TICKS_PER_HOUR*outputInterval) == 0) {
                // long justBeforeOutput = System.nanoTime(); //Instrumentation
                intermediateOutput();
                // long justAfterIntermediateOutput = System.nanoTime();//Instrumentation
                // System.out.println(
                //         "time between outputs: " + (justAfterIntermediateOutput - justAfterOutput) / 1000000 + " ms"
                //         + ", time steps: " + (justBeforeOutput - justAfterOutput) / 1000000 + " ms"
                //         + ", intermediate output: "
                //         + (justAfterIntermediateOutput - justBeforeOutput) / 1000 + " us");//Instrumentation
                // justAfterOutput = System.nanoTime();//Instrumentation
                timePoint++;
            }
        }
        System.out.println("total time: " + (System.nanoTime() - startTime)/1000000 + "ms");
        // System.out.println(calcFnetTime/1000000 + " " + clearcoordstime/1000000 + " " + movetime/1000000 + " " + growtime/1000000
        //     + " " + dividetime/1000000 + " " + diffusetime/1000000 + " " + prodepstime/1000000);
        try {
            outputWriter.close();
        } catch (IOException e) {
            System.out.println("Could not close outputWriter");
            e.printStackTrace();
        }
    }
    
    //In each time step, particles move, and bacteria grow or produce EPS
    private void timeStep()
    {
        long start = System.nanoTime();
        media.calculateMovements();
        long aftercalc = System.nanoTime();
        media.clearCoordinates(); 
        long afterclear = System.nanoTime();
        media.moveParticles();
        long aftermove = System.nanoTime();
        growBacteria();
        long aftergrow = System.nanoTime();
        divideBacteria();
        long afterdivide = System.nanoTime();
        media.diffuseAndConsume(TIME_STEP, GRID_SPACING);
        long afterdiffuse = System.nanoTime();
        media.produceEPS();
        long afterEps = System.nanoTime();

        //Instrumentation
        // calcFnetTime += (aftercalc-start);
        // clearcoordstime += (afterclear-aftercalc);
        // movetime += (aftermove-afterclear);
        // growtime += (aftergrow-aftermove);
        // dividetime += (afterdivide-aftergrow);
        // diffusetime += (afterdiffuse-afterdivide);
        // prodepstime += (afterEps-afterdiffuse);
        // System.out.println("CalcFNet: " + (aftercalc-start)/1000 + "us"
        //         + ", ClearCoords: " + (afterclear-aftercalc)/1000 + "us"
        //         + ", Move: " + (aftermove-afterclear)/1000 + "us"
        //         + ", Grow: " + (aftergrow-aftermove)/1000 + "us"
        //         + ", Divide: " + (afterdivide-aftergrow)/1000 + "us"
        //         + ", Diffuse: " + (afterdiffuse-afterdivide)/1000 + "us"
        //         + ", ProdEPS: " + (afterEps-afterdiffuse)/1000 + "us");
    }

    private double computeColonySize() {
        double sumLength = 0.0;
        List<Bacteria> bacteria = getBacteria();
        for (Bacteria b : bacteria) {
            sumLength += b.getLength();
    }
    return sumLength / bacteria.size();
    }

    /**
     * compute avg area of all current cells
     */
    private double computeAbar() 
    {
        //Abar=πr0^2​+2/3​r0​lc -> lc is cylindrical length -> compute avg area of all current cells
        double totalArea = 0.0;
        int count = 0;
        double r0 = Bacteria.getRadius();

        // for (int i = 0; i < media.grid.length; i++) {
        //     for (int j = 0; j < media.grid[i].length; j++) {
        //         MediaCoordinate cell = media.grid[i][j];
        //         if (cell == null) continue;

        //         for (Bacteria b : cell.getBacteria()) {
        //             double li = b.getLength();  // cylindrical length
        //             if (Double.isNaN(li) || li <= 0) li = 0.1; //safeguard
        //             double Ai = Math.PI * r0 * r0 + 2 * r0 * li;
        //             totalArea += Ai;
        //             count++;
        //         }
        //     }
        // }

        for (int i = 0; i < media.particleCounter; i++) {
            if (media.particles[i] instanceof Bacteria) {
                Bacteria b = (Bacteria) media.particles[i];
                double li = b.getLength();  // cylindrical length
                if (Double.isNaN(li) || li <= 0) li = 0.1; //safeguard
                double Ai = Math.PI * r0 * r0 + 2 * r0 * li;
                totalArea += Ai;
                count++;
            }
        }
        return totalArea/count; 
    }
    
    private void growBacteria()
    {
        double phi = 3.5; //µm/h -> constant parameter
        double Abar = computeAbar();

        // for (int i = 0; i < media.grid.length; i++) {
        //     for (int j = 0; j < media.grid[i].length; j++) {
        //         MediaCoordinate cell = media.grid[i][j];
        //         if (cell == null) continue;

        //         for (Bacteria b : cell.getBacteria()) {
        //             double localC = cell.getNutrients();   //local nutrient concentration
        //             b.grow(TIME_STEP, localC, Abar);
        //          }
        //     }
        // }

        for (int i = 0; i < media.particleCounter; i++) {
            if (media.particles[i] instanceof Bacteria) {
                Bacteria b = (Bacteria) media.particles[i]; //downcast
                double localC = media.grid[media.convertToArrayIndex(b.getyPosition())]
                            [media.convertToArrayIndex(b.getxPosition())].getNutrients();   //local nutrient concentration
                    b.grow(TIME_STEP, localC, Abar);
            }
        }
    }
    

    /**
     * Divide bacteria that should divide
     */
    private void divideBacteria()
    {
        // for (int i = 0; i < media.grid.length; i++) {
        //     for (int j = 0; j < media.grid[i].length; j++) {
        //         MediaCoordinate cell = media.grid[i][j];
        //         if (cell == null) continue;

        //         ArrayList<Bacteria> newCells = new ArrayList<>();
        //         for (Bacteria b : cell.getBacteria()) {
        //             if (b.shouldDivide(TIME_STEP)) {
        //                 Bacteria[] daughters = b.divide();
        //                 daughters[1].setMedium(media);
        //                 newCells.add(daughters[1]); //keep one daughter new
        //             }
        //         }
        //         //add daughters to the same grid location
        //         for (Bacteria newB : newCells) {
        //             cell.addBacteria(newB);
        //             media.addBacteriaToArray(newB);
        //         }
        //     }
        // }
        for (int i = 0; i < media.particleCounter; i++) {
            if (media.particles[i] instanceof Bacteria) {
                Bacteria b = (Bacteria) media.particles[i]; //downcast
                if (b.shouldDivide(TIME_STEP)) {
                        Bacteria[] daughters = b.divide();
                        daughters[1].setMedium(media);
                        media.addBacteriaToArray(daughters[1]);
                        media.placeParticle(daughters[1]);
                    }
            }
        }

    }

    /**
     * @return the sparseness of the colony
     */
    private double computeSparseness() {
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int occupied = 0;

        for (int i = 0; i < media.getParticleCounter(); i++) {
            if (media.getParticles()[i] instanceof Bacteria) {
                Bacteria b = (Bacteria) media.getParticles()[i];
            int x = b.getxPosition();
            int y = b.getyPosition();
            occupied++;
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
            }
        }

        if (occupied == 0) return 1.0;

        int boundingArea = (maxX - minX + 1) * (maxY - minY + 1);
        return 1.0 - (occupied/(double)boundingArea);
    }

    /**
     * @return the roughness of the colony
     */
    private double computeRoughness() {
        double cx = getColonyCentre()[0];
        double cy = getColonyCentre()[1];
        List<Bacteria> bacteria = getBacteria();
        List<Double> radii = new ArrayList<>();
        for (Bacteria b : bacteria) {
            double dx = b.getxPosition() - cx;
            double dy = b.getyPosition() - cy;
            radii.add(Math.sqrt(dx*dx + dy*dy));
        }

        double meanR = radii.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        if (meanR == 0) return 0.0;
        double variance = radii.stream().mapToDouble(r -> (r - meanR) * (r - meanR)).average().orElse(0.0);
        return Math.sqrt(variance) / meanR;
    }

    private List<Bacteria> getBacteria() {
        List<Bacteria> bacteria = new ArrayList<>();
        for (int i = 0; i < media.getParticleCounter(); i++) {
            if (media.getParticles()[i] instanceof Bacteria) {
                bacteria.add((Bacteria) media.getParticles()[i]);
            }
        }

        if (bacteria.isEmpty()) return null;
        else return bacteria;
    }
    
    /**
     * @return the centre coords of the colony
     */
    private double[] getColonyCentre() {
        List<Bacteria> bacteria = getBacteria();
        double cx = bacteria.stream().mapToDouble(Bacteria::getxPosition).average().orElse(0.0);
        double cy = bacteria.stream().mapToDouble(Bacteria::getyPosition).average().orElse(0.0);

        double[] centre = new double[2];
        centre[0] = cx;
        centre[1] = cy;

        return centre;
    }

    /**
     * @return a string containing the colony centre
     */
    private String printCentre() {
        double[] colonyCentre = getColonyCentre();
         double x = colonyCentre[0];
         double y = colonyCentre[1];
         return (x + ", " + y + "\n");
    }

    /**
     * @return a string containing the sparseness and roughness
     */
    private String graphingData() {
        double roughness = computeRoughness();
        double sparseness = computeSparseness();
        
        return roughness + "\n" + sparseness + "\n";
    }

    //Writes the latest simulation state to the output file
    private void intermediateOutput()
    {
        //Open a fileWriter
        try {   
            System.out.println("Timepoint: " + timePoint);
            //Time and colony details
            outputWriter.write(timePoint + "\n" + ticksSinceStart + "\n");
            outputWriter.write(printCentre()); //Colony centre.
            outputWriter.write(computeColonySize()+"\n");
            
            //display info for graphing
            outputWriter.write(graphingData());

            //its vital to calculate cluster info before displaying the Bacteria and EPS details - otherwise cluster details are
            //from mismatched timePoints
            String clusters = media.detectClusters(); //Calculate cluster info
            outputWriter.write(media.toString()); //Display all bacteria and EPS
            outputWriter.write(clusters); //Display all clusters

        } catch (IOException e) {
            System.out.println("Yeah so something is wrong with the fileWriter");
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) { //MAIN====================================================
        //get the input and output files
        String outputFile = DEFAULT_OUTPUT_FILE;
        String inputFile = DEFAULT_INPUT_FILE;
        if (args.length == 2) {
            inputFile = args[0]; //1st argument is input
            outputFile = args[1]; //2nd argument is output
        }
        System.out.println("Running simulation with:\nInput File = " + inputFile + "\nOutput File = " + outputFile);

        //Clear the output file
        try {
            FileWriter clearFile = new FileWriter(outputFile);
            clearFile.write("");
            clearFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Actually do the simuation
        Simulation simulation = new Simulation(inputFile, outputFile);
        simulation.startSimulation();
    }

    //GENERATED BY CHATGPT, with some edits by Byron and Sonali
    //Reads config file and sets all simulation parameters. Returned bacteria is the start of the colony
    public static Bacteria readInputFile(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            // Line 1: initial bacteria placement (x, y)
            String[] coords = requireLine(br, "initial bacteria coordinates").split(",");
            double initialX = parseDoubleSafe(coords[0], "initial X coordinate");
            double initialY = parseDoubleSafe(coords[1], "initial Y coordinate");
            br.readLine(); // blank line

            // local simulation parameters
            maxAgents = parseIntSafe(requireLine(br, "maxAgents"), "maxAgents");
            preventNegative(maxAgents, "maxAgents");
            outputInterval = parseDoubleSafe(requireLine(br, "outputInterval"), "outputInterval");
            preventNegative(outputInterval, "outputInterval");
            br.readLine(); // blank line

            // medium values
            Medium.simulationBoundary = parseIntSafe(requireLine(br, "simulationBoundary"), "simulationBoundary");
            preventNegative(Medium.simulationBoundary, "simulationBoundary");
            Medium.nutrientConcentration = parseDoubleSafe(requireLine(br, "nutrientConcentration"),
                    "nutrientConcentration");
            preventNegative(Medium.nutrientConcentration, "nutrientConcentration");
            Medium.nutrientConsumptionRate = parseDoubleSafe(requireLine(br, "nutrientConsumptionRate"),
                    "nutrientConsumptionRate");
            preventNegative(Medium.nutrientConsumptionRate, "nutrientConsumptionRate");
            Medium.nutrientDiffusionRate = parseDoubleSafe(requireLine(br, "nutrientDiffusionRate"),
                    "nutrientDiffusionRate");
            preventNegative(Medium.nutrientDiffusionRate, "nutrientDiffusionRate");
            br.readLine();

            // EPS values
            EPS.size = parseDoubleSafe(requireLine(br, "EPS size"), "EPS size");
            preventNegative(EPS.size, "EPS size");
            EPS.EPSElasticModulus = parseDoubleSafe(requireLine(br, "EPS-EPSElasticModulus"), "EPS-EPSElasticModulus");
            preventNegative(EPS.EPSElasticModulus, "EPS-EPSElasticModulus");
            br.readLine();

            // bacteria values
            Bacteria.lmax = parseDoubleSafe(requireLine(br, "bacteria lmax"), "Bacteria lmax");
            preventNegative(Bacteria.lmax, "Bacteria lmax");
            Bacteria.d0 = parseDoubleSafe(requireLine(br, "bacteria d0"), "Bacteria d0");
            preventNegative(Bacteria.d0, "Bacteria d0");
            Bacteria.motility = parseDoubleSafe(requireLine(br, "Bacteria motility"), "Bacteria motility");
            preventNegative(Bacteria.motility, "Bacteria motility");
            Bacteria.growthRate = parseDoubleSafe(requireLine(br, "Bacteria growth rate"), "Bacteria growth rate");
            preventNegative(Bacteria.growthRate, "Bacteria growth rate");
            Bacteria.reproductionRate = parseDoubleSafe(requireLine(br, "Bacteria reproduction rate"),
                    "Bacteria reproduction rate");
            preventNegative(Bacteria.reproductionRate, "Bacteria reproduction rate");
            Bacteria.EPSproductionRate = TICKS_PER_HOUR
                    / parseDoubleSafe(requireLine(br, "EPS production rate"), "EPS production rate"); //Input file has EPS/hr -> convert to Ticks/EPS (Ticks/hr * hr/EPS = Ticks/EPS)
            preventNegative(Bacteria.EPSproductionRate, "EPS production rate");
            Bacteria.EPSElasticModulus = parseDoubleSafe(requireLine(br, "bacteria-EPS elastic modulus"),
                    "Bacteria-EPSElasticModulus");
            preventNegative(Bacteria.EPSElasticModulus, "Bacteria-EPSElasticModulus");
            Bacteria.BacteriaElasticModulus = parseDoubleSafe(requireLine(br, "Bacteria-BacteriaElasticModulus"),
                    "Bacteria-BacteriaElasticModulus");
            preventNegative(Bacteria.BacteriaElasticModulus, "Bacteria-BacteriaElasticModulus");
            Particle.FrictionCoefficientPascalsPerhour = parseDoubleSafe(
                    requireLine(br, "FrictionCoefficient"), "FrictionCoefficient");
            preventNegative(Particle.FrictionCoefficientPascalsPerhour, "FrictionCoefficient");
            Bacteria.EPSProductionMinCellDensity = parseDoubleSafe(requireLine(br, "EPS production min cell density"),
                    "EPSProductionMinCellDensity");
            preventNegative(Bacteria.EPSProductionMinCellDensity, "EPSProductionMinCellDensity");
            Bacteria.EPSProductionsMaxEPSDensity = parseDoubleSafe(requireLine(br, "EPS production max density"),
                    "EPSProductionsMaxEPSDensity");
            preventNegative(Bacteria.EPSProductionsMaxEPSDensity, "EPSProductionsMaxEPSDensity");
            br.readLine();

            System.out.println("Data Loading Complete");
            return new Bacteria(initialY, initialX, 1);

        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error reading file: File not found");
            System.exit(0);
            return null;
        }
    }


    /**
     * Helper method for file parsing validation
     * @param s string to extract double from
     * @param fieldName field that this method is trying to assign the value to
     * @return the extracted double
     * @throws IllegalArgumentException if the input string isn't a double
     */
    private static double parseDoubleSafe(String s, String fieldName) throws IllegalArgumentException {
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid value for " + fieldName + ": '" + s + "'\nThis should be a float\nProgram terminating");
            System.exit(0);
        }
        return 0;
    }

    /**
     * Helper method for file parsing validation
     * @param s string to extract int from
     * @param fieldName field that this method is trying to assign the value to
     * @return the extracted int
     * @throws IllegalArgumentException if the input string isn't a int
     */
    private static int parseIntSafe(String s, String fieldName) throws IllegalArgumentException {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid value for " + fieldName + ": '" + s + "'\nThis should be an integer\nProgram terminating");
            System.exit(0);
        }
        return 0;
    }

    public static void preventNegative(double s, String fieldName) throws IllegalArgumentException {
        if (s < 0) {
           System.out.println("Invalid value for " + fieldName + ": '" + s + "'\nThis number should be positive\nProgram terminating");
           System.exit(0);
        }
    }

    /**
     * Helper method for file parsing validation
     * @param br buffered reader to extract from
     * @param description of what the line should be
     * @return the extracted string
     * @throws IOException if there isn't a string here
     */
    private static String requireLine(BufferedReader br, String description) throws IOException {
        String line = br.readLine();
        if (line == null) {
            System.out.println("Missing line: expected " + description + "\nProgram terminating");
            System.exit(0);
        }
        return line;
    }


}