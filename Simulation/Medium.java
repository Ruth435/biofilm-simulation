package Simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Ruth Rudolph, Sonali Naidoo and Byron Clarke
 */
public class Medium {

    //simulation parameters
    public static int simulationBoundary;
    public static double nutrientConcentration; //C0
    public static double nutrientConsumptionRate; //k
    public static double nutrientDiffusionRate; //D

    //for cluster calculations
    public static final double DISTANCE_FOR_CLUSTERS = 5;

    //particle tracking
    public Particle[] particles;
    public int particleCounter;
    public MediaCoordinate[][] grid; // grid[y][x] refers to (x;y) as the coordinate (sorry for the weirdness, but it makes looping nice)
    private double[][] nutrients; //nutrient concentration at each grid cell
    private double[][] nutrientsNext; //nutrient concentration at next time step
    public List<MediaCoordinate> occupiedCells = new ArrayList<>();

    public Medium(int maximumAgents, Bacteria startingBacteria)
    {

        //Create the grid: centre co-ord is (0,0) the board goes out simulationBoundary grid blocks in each direction.
        grid = new MediaCoordinate[simulationBoundary*2+1][simulationBoundary*2+1];

        //Create the particle array and put the first bacterium in it
        particles = new Particle[maximumAgents];
        particleCounter = 1;
        particles[0] = startingBacteria;

        nutrients = new double[simulationBoundary * 2 + 1][simulationBoundary * 2 + 1];
        nutrientsNext = new double[simulationBoundary * 2 + 1][simulationBoundary * 2 + 1];

        //initialise nutrients
        for (int y = 0; y <= simulationBoundary * 2; y++) {
            for (int x = 0; x <= simulationBoundary * 2; x++) {
                nutrients[y][x] = nutrientConcentration;
                nutrientsNext[y][x] = nutrientConcentration;
            }
        }

        //initialise entire grid
        for (int y = -simulationBoundary; y<= simulationBoundary; y++) {
            for (int x = -simulationBoundary; x <= simulationBoundary; x++) {
                grid[convertToArrayIndex(y)][convertToArrayIndex(x)] = 
                new MediaCoordinate(x, y, nutrientConcentration);
            }
        }

        //Place the starting bacteria, and create the mediacoordinate that it is on.
        int bacteriaX = (int) Math.round(startingBacteria.getxPosition());
        int bacteriaY = (int) Math.round(startingBacteria.getyPosition());
        grid[convertToArrayIndex(bacteriaY)][convertToArrayIndex(bacteriaX)] 
            = new MediaCoordinate(bacteriaX, bacteriaY, nutrientConcentration);

        grid[convertToArrayIndex(bacteriaY)][convertToArrayIndex(bacteriaX)].addBacteria(startingBacteria);
        markOccupied(grid[convertToArrayIndex(bacteriaY)][convertToArrayIndex(bacteriaX)]);
    }

    //Adding and removing particles-------------------------------------------------

    /**
     * Add a bacteria to the particle list if there's still space
     * If maxAgents is reached, then the simulation must stop
     */
    public synchronized void addBacteriaToArray(Bacteria bacteria) {
        if (particleCounter < Simulation.maxAgents) {
            particles[particleCounter] = bacteria;
            particleCounter++;
        } else {
            Simulation.totalRuntime = 0; //Stop the simulation
        }
    }

    /**
     * Add an EPS to the particle list if there's still space
     * If maxAgents is reached, then the simulation must stop
     */
    public synchronized void addEPSToArray(EPS eps) {
        if (particleCounter < Simulation.maxAgents) {
            particles[particleCounter] = eps;
            particleCounter++;
        } else {
            Simulation.totalRuntime = 0; //Stop the simulation
        }
    }

    public int getParticleCounter() {
        return particleCounter;
    }

    public Particle[] getParticles() {
        return particles;
    }

    //Simulation processing methods------------------------------------------------------------------

    /**
     * Creates a list containing all the Bacteria and EPS within a (2*distance+1) width square centred on the coordinate of the calling particle
     * This is used for Particles calculating repulsive forces, and clusters.
     * This list has space for the maximumAgents and so will definitely have null values
     * @param callingParticle the particle who's x and y coords will eb used for this. This particle IS NOT included in the returned list
     * @param distance The distance away from the reference coord to check for particles
     * @return A list of all particles in the area checked, excluding the callingParticle
     */
    public Particle[] getNearbyParticles(int distance, Particle callingParticle) {
        int x = callingParticle.getxPosition();
        int y = callingParticle.getyPosition();

        //Initialize the list to fill
        ArrayList<Particle> nearbyParticles = new ArrayList<>();
        int nearbyParticleCounter = 0;

        //Swap from coordinates to array indices for the media grid:
        x = convertToArrayIndex(x);
        y = convertToArrayIndex(y);

        int nx = grid.length;
        int ny = grid[0].length;

        int xStart = Math.max(0, x - distance);
        int xEnd   = Math.min(nx - 1, x + distance);
        int yStart = Math.max(0, y - distance);
        int yEnd   = Math.min(ny - 1, y + distance);

        //Start a loop for each axis starting 'distance' distance from the reference coord
        //and going up to 'distance' away from the reference coord
        for (int yLoopCounter = yStart; yLoopCounter <= yEnd; yLoopCounter++) {
            for (int xLoopCounter = xStart; xLoopCounter <= xEnd; xLoopCounter++) {

                //Checks whether there is a mediacoordinate in this spot. If so print it's details
                if (grid[yLoopCounter][xLoopCounter] != null) {
                    // System.out.println(grid[yLoopCounter][xLoopCounter]);

                    //Add every particle from this coordinate to the nearbyParticles list
                    //Ignore the calling particle as that will only waste time
                    for (Particle particle : grid[yLoopCounter][xLoopCounter].getParticles()) {
                        if (particle != callingParticle) {
                            nearbyParticles.add(particle);
                            nearbyParticleCounter++;
                        }
                    }

                } else {
                    // System.out.println("(" + convertToCoord(xLoopCounter) + ";" + convertToCoord(yLoopCounter) 
                    // + ") Nutrients: " + startingNutrients + " NoCoordInitializedHere");
                }
            }
        }

        //Turns out downcasting arrays is very weird. Here it is
        Object[] particleArray = new Particle[nearbyParticleCounter];
        particleArray = nearbyParticles.toArray(particleArray);
         if (particleArray instanceof Particle[]) {
            return (Particle[]) particleArray;
        } else {
            return null;
        }
    }

    /**
     * Creates a list containing all the Bacteria and EPS within a (2*distance+1) width square centred on the coordinate of the calling particle
     * This is used for Particles calculating EPS production limits
     * This list has space for the maximumAgents and so will definitely have null values
     * @param callingParticle the particle who's x and y coords will eb used for this. This particle IS included in the count
     * @param distance The distance away from the reference coord to check for particles
     * @return A list of all particles in the area checked
     */
    public int countNearbyParticles(int distance, Particle callingParticle) {
        int nearbyParticleCounter = 0;

        int x = callingParticle.getxPosition();
        int y = callingParticle.getyPosition();

        //Swap from coordinates to array indices for the media grid:
        x = convertToArrayIndex(x);
        y = convertToArrayIndex(y);

        int nx = grid[0].length;
        int ny = grid.length;

        int yStart = Math.max(0, y - distance);
        int yEnd = Math.min(ny - 1, y + distance);
        int xStart = Math.max(0, x - distance);
        int xEnd = Math.min(nx - 1, x + distance);

        //Start a loop for each axis starting 'distance' distance from the reference coord
        //and going up to 'distance' away from the reference coord
        for (int yLoopCounter = yStart; yLoopCounter <= yEnd; yLoopCounter++) {
            for (int xLoopCounter = xStart; xLoopCounter <= xEnd; xLoopCounter++) {

                //Checks whether there is a mediacoordinate in this spot. If so print it's details
                if (grid[yLoopCounter][xLoopCounter] != null) {

                    //Add every particle from this coordinate to the nearbyParticles list
                    for (Particle particle : grid[yLoopCounter][xLoopCounter].getParticles()) {
                        nearbyParticleCounter++;
                    }
                } 
            }
        }

        return nearbyParticleCounter;
    }

    /**
     * Adds particle to the correct coord
     * if that coord doesn't exist yet, then initialize it.
     */
    public void placeParticle(Particle particle) {
        int x = convertToArrayIndex(particle.getxPosition());
        int y = convertToArrayIndex(particle.getyPosition());

        //clamp to grid boundaries
        x = Math.max(0, Math.min(grid[0].length - 1, x));
        y = Math.max(0, Math.min(grid.length - 1, y));

        // First check without locking (fast path for already initialized cells)
        if (grid[y][x] != null) {
            grid[y][x].addParticle(particle, this);
            return;
        } else {
            System.out.println("The grid was not initialised properly. This code should never run");
        }
    }

    /**
     * Calculate the movements of all the particles in the medium, but don't move them yet.
     */
    public void calculateMovements() {
        //Create forkjoinpool and first task
		ForkJoinPool fjp = ForkJoinPool.commonPool();
        TickStepAction tsa = new TickStepAction(this, 0, particleCounter, TickStepAction.CALCULATE_MOVEMENTS, 0);
        fjp.invoke(tsa);
    }

    /**
     * Clears all media coordinates of particles so the particles can be moved to their new coords
     * For each particle
     * find its location, and remove all particles there
     */
    public void clearCoordinates() {
        ForkJoinPool fjp = ForkJoinPool.commonPool();
        TickStepAction tsa = new TickStepAction(this, 0, particleCounter, TickStepAction.CLEAR_COORDS, 0);
        fjp.invoke(tsa);
    }

    /**
     * Move all the particles and place them in new grid cells
     */
    public void moveParticles() {
        ForkJoinPool fjp = ForkJoinPool.commonPool();
        TickStepAction tsa = new TickStepAction(this, 0, particleCounter, TickStepAction.MOVE_PARTICLES, 0);
        fjp.invoke(tsa);
    }

    /**
     * Checks which particles are bacteria and then updates their EPS production
     */
    public void produceEPS() {
        ForkJoinPool fjp = ForkJoinPool.commonPool();
        TickStepAction tsa = new TickStepAction(this, 0, particleCounter, TickStepAction.PRODUCE_EPS, 0);
        fjp.invoke(tsa);
    }

    /**
     * This detects all bacterial and EPS clusters and returns an output String fit for an intermediate output File
     */
    public String detectClusters() {
        //Clear all clusterIDs before recalculating them for this output.
        for (int i = 0; i < particleCounter; i++) {
            particles[i].clusterID = -1;
        }

        //Detect Bacteria and EPS clusters
        AtomicInteger[] bacteriaClusters = detectParticleClusters(true);
        AtomicInteger[] epsClusters = detectParticleClusters(false);

        String result = "Bacterial Clusters:\n";

        for (int i = 0; i < bacteriaClusters.length; i++) {
            result += i + ", " + bacteriaClusters[i] + "\n";
        }

        result += "EPS Clusters:\n";

        for (int i = 0; i < epsClusters.length; i++) {
            result += i + ", " + epsClusters[i] + "\n";
        }
        return result+"\n";
    }

    

     /** Returns an array of atomicIntegers corresponding to the number of particles in each cluster
     */
    private AtomicInteger[] detectParticleClusters(boolean typeIsBacteria) {
        int currentClusterID = 0;
        ArrayList<AtomicInteger> particleCounts = new ArrayList<>();

        for (int i = 0; i < particleCounter; i++) {
            if (particles[i].isType(typeIsBacteria)) {
                if (particles[i].clusterID == -1) {
                    //System.out.println("Cluster: " + currentClusterID);
                    particleCounts.add(detectParticleCluster(typeIsBacteria, particles[i], currentClusterID));
                    currentClusterID++;
                } else {//Cluster number already given
                    //Code to note that these 2 clusters are equal
                }
            } 
        }

        //Yay downcasting arrays :) I sure do love the toArray() method.
        Object[] output = new AtomicInteger[0];
        output = particleCounts.toArray(output);
         if (output instanceof AtomicInteger[]) {
            return (AtomicInteger[]) output;
        } else {
            return null;
        }
    }

    /**
     * Calculates all particles that are part of the same cluster as the one given, and assigns them the clusterID
     * 
     * also returns the total number of particles in that cluster
     */
    private AtomicInteger detectParticleCluster(boolean typeIsBacteria, Particle particle, int clusterID) {
        particle.clusterID = clusterID;

        // Queue for breadth-first search
        Particle[] particlesInCluster = new Particle[particleCounter];
        int particlesInClusterCounter = 1;
        particlesInCluster[0] = particle;
        int clusterTraverse = 0;
        int searchRadius = (int) Math.ceil(DISTANCE_FOR_CLUSTERS) + 1;

        while (clusterTraverse < particlesInClusterCounter && particlesInCluster[clusterTraverse] != null) {
            Particle currentParticle = particlesInCluster[clusterTraverse];
            for (Particle nearbyParticle : getNearbyParticles(searchRadius, currentParticle)) {
                
                //For this particle to be newly added to the cluster it needs to:
                //1: Not be in a cluster already 2: Be the correct type 3: Be close enough
                if (nearbyParticle.clusterID == -1 && nearbyParticle.isType(typeIsBacteria) && currentParticle.distanceToCentre(nearbyParticle) <= DISTANCE_FOR_CLUSTERS) {
                    //System.out.println("ClusterID:" + clusterID);
                    nearbyParticle.clusterID = clusterID; //Add this particle to the cluster
                    particlesInCluster[particlesInClusterCounter] = nearbyParticle; //Add this particle to the queue
                    particlesInClusterCounter++; //move queue pointer
                }
            }
            clusterTraverse++;
        }
        return new AtomicInteger(particlesInClusterCounter);
    }

    /**
     * Creates a string of all the details for all of the particles in the simulation
     * formatted for the output file
     */
    public String toString() {
        String toString = "Bacteria:\n";
        String EPSstring = "EPS:\n"; //Sorry yes this is clunky. its the nature of the particles being in 1 array. I think it's worth it (hopefully)
        for (int i = 0; i < particleCounter; i++) {
            if (particles[i] instanceof Bacteria) {
                toString += particles[i].toString();
            } else {
                EPSstring += particles[i].toString();
            }
        }
        toString += EPSstring;
        return toString;
    }

    public void markOccupied(MediaCoordinate mc) {
        if (mc !=null && !occupiedCells.contains(mc))
            occupiedCells.add(mc);
    }

    /**
     * Bacteria consume nutrients and then diffusion of nutrients happens
     */
    public void diffuseAndConsume(double timestep, double dx) {
        int nx = grid.length;
        int ny = grid[0].length;

        ForkJoinPool.commonPool().invoke(
            new DiffuseTask(nutrients, nutrientsNext, 0, nx, dx, timestep)
        );

        double[][] temp = nutrients;
        nutrients = nutrientsNext;
        nutrientsNext = temp;

        //consume nutrients only where bacteria are present
        for (int i = 0; i < particleCounter; i++) {
            if (particles[i] instanceof Bacteria) {
                Bacteria b = (Bacteria) particles[i];
                MediaCoordinate mc = grid[convertToArrayIndex(b.getyPosition())][convertToArrayIndex(b.getxPosition())];

                double C_old = mc.getNutrients();
                double C_new = C_old;
                double Ai = b.getArea();
                double fC = C_old / (1.0 + C_old); // Monod
                C_new -= nutrientConsumptionRate * Ai * fC * timestep;
            
            mc.setNutrients(Math.max(0, C_new));
            }
        }
    }

    //-------Inner Diffuse Task Class-------//
    private class DiffuseTask extends RecursiveAction {
        private static final int THRESHOLD = 40;  // tune for best performance
        private final double[][] oldC, newC;
        private final int startRow, endRow;
        private final double dx, timestep;

        DiffuseTask(double[][] oldC, double[][] newC, int startRow, int endRow, double dx, double timestep) {
            this.oldC = oldC;
            this.newC = newC;
            this.startRow = startRow;
            this.endRow = endRow;
            this.dx = dx;
            this.timestep = timestep;
        }

        @Override
        protected void compute() {
            if (endRow - startRow <= THRESHOLD) {
                computeDirectly();
            } else {
                int mid = (startRow + endRow) / 2;
                invokeAll(
                    new DiffuseTask(oldC, newC, startRow, mid, dx, timestep),
                    new DiffuseTask(oldC, newC, mid, endRow, dx, timestep)
                );
            }
        }

        private void computeDirectly() {
            int nx = oldC.length;
            int ny = oldC[0].length;

            for (int i = startRow; i < endRow; i++) {
                for (int j = 0; j < ny; j++) {
                    double C_old = oldC[i][j];
                    double C_new;

                    //diffusion (skip edges)
                    if (i > 0 && i < nx - 1 && j > 0 && j < ny - 1) {
                        double lap = (oldC[i + 1][j] + oldC[i - 1][j]
                                    + oldC[i][j + 1] + oldC[i][j - 1]
                                    - 4 * C_old) / (dx * dx);
                        C_new = C_old + nutrientDiffusionRate * timestep * lap;
                    } else {
                        C_new = C_old; // edge: fixed
                    }
                }
            }
        }
    }

    /**
     * converts a co-ordinate on the grid to an array index so 0 becomes simulationBoundary+1
     */
    public int convertToArrayIndex(int x) {
        int index = x + simulationBoundary;  // shift negative coordinates
        if (index < 0) index = 0;
        if (index >= grid.length) index = grid.length - 1;
        return index;
    }

    /**
     * @return the corresponding coordinate for the inputted grid array index
     */
    public int convertToCoord(int x) {
        return x-simulationBoundary;
    }
}