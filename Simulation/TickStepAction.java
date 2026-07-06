package Simulation;

import java.util.concurrent.RecursiveAction;

/**
 * This class is used to implement multithreading within the tick steps of the simulation
 * @author Byron Clarke
 */
public class TickStepAction extends RecursiveAction{

    //These are used to make selecting the correct method for this tickstepaction easier.
    public static final int CALCULATE_MOVEMENTS = 0;
    public static final int CLEAR_COORDS = 1;
    public static final int MOVE_PARTICLES = 2;
    public static final int PRODUCE_EPS = 4;

    //Helps prevent too much splitting and causes a better serial cutoff
    public static final int THRESHOLD_SIZE = 32;

    //Alternate path to stop too many splits
    public static int maxSplits = (int) Math.ceil(Math.log(Runtime.getRuntime().availableProcessors())/Math.log(2));

    public Medium media;

    //The start and end of the range to process of the array
    //and then the method that is being run, and the number of splits so far for sequential cutoff purposes
    public int start, end, method, splits;

    public TickStepAction(Medium media, int start, int end, int method, int splits) {
        this.media = media;
        this.start = start;
        this.end = end;
        this.method = method;
        this.splits = splits;
    }

    /**
     * The main processing method for this class
     * It splits the array until it's small enough, then processes it in parallel
     * once it is processing the sequential part it selects which method to run based on the variable 'method'
     */
    public void compute() {

        //Decide if we need to fork some more or start processing:
        if (splits < maxSplits && (end-start) > THRESHOLD_SIZE) {

            int mid = (start + end)/2;

            TickStepAction left = new TickStepAction(media, start, mid, method, splits+1);
            TickStepAction right = new TickStepAction(media, mid, end, method, splits+1);

            left.fork();
            right.compute();
            left.join();
            
        } else {

        //Once we actually are just doing sequential code:
        switch (method) {
            case 0:
                //Calculate movements of all particles
                for (int i = start; i < end; i++) {
                    media.particles[i].calculateFNet();
                }
                break;
            case 1:
                //Clear coords of all mediaCoords
                for (int i = start; i < end; i++) {
                    int x = media.convertToArrayIndex(media.particles[i].getxPosition());
                    int y = media.convertToArrayIndex(media.particles[i].getyPosition());

                    media.grid[y][x].removeParticlesHere();
                }
                break;
            case 2:
                //Move all particles and reassign them to mediacoords
                for (int i = start; i < end; i++) {
                    media.particles[i].move();
                }
                break;
            case 4:
                //All bacteria check if they need to produce EPS, and if they need to then they do
                for (int i = start; i < end; i++) {
                    if (media.particles[i] instanceof Bacteria) {
                        Bacteria b = (Bacteria) media.particles[i];
                        b.produceEPS();
                    }
                }
            }
        }
    }
}
