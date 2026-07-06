package Simulation;

import java.util.ArrayList;

/**
 *
 * @author Ruth Rudolph, Sonali Naidoo and Byron Clarke
 */
public class MediaCoordinate {

    //Lists of Bacteria and EPS
    private ArrayList<Bacteria> bacteria;
    private ArrayList<EPS> EPS;

    //info about this gridsquare
    public int xCoordinate;
    public int yCoordinate;
    private double nutrients;



    public MediaCoordinate(int x, int y, double startingNutrients) {
        this.yCoordinate = y;
        this.xCoordinate = x;
        this.nutrients = startingNutrients;

        bacteria = new ArrayList<>();
        EPS = new ArrayList<>();
    }

    /** creates and returns a list of all particles in this coordinate
     * @return a list of all the particles in this coordinate
     */
    public ArrayList<Particle> getParticles() {
        ArrayList<Particle> particles = new ArrayList<>();

        //Add all bacteria
        for (Particle particle : bacteria) {
            particles.add(particle);
        }
        //Add all eps
        for (Particle particle : EPS) {
            particles.add(particle);
        }

        return particles;
    }

    //Adding and removing bacteria from the lists --------------------------------------------------------

    /**
     * Checks whether the given particle is a bacteria or EPS and then adds it to the correct arrayList
     */
    public synchronized void addParticle(Particle particle, Medium media) {
        if (particle instanceof Bacteria) {
            Bacteria b = (Bacteria) particle;
            bacteria.add(b);
            media.markOccupied(this);
        } else {
            EPS e = (EPS) particle;
            EPS.add(e);
        }
    }

    public void addBacteria(Bacteria b) {
        bacteria.add(b);
    }

    public ArrayList<Bacteria> getBacteria() {
        return bacteria;
    }
    
    //Particle removal ------------------------

    //Removes all particles on the coordinate
    public synchronized void removeParticlesHere() {
        bacteria.clear();
        EPS.clear();
    }

    /**
     * @return the coordinate, and nutrients of this MediaCoordiante
     */
    public String toString() {
        return "(" + xCoordinate + ";" + yCoordinate + ") Nutrients: " + nutrients;
    }

    //nutrient methods
    public double getNutrients() {
        return nutrients;
    }

    public void setNutrients(double nutrients) {
        this.nutrients = nutrients;
    }

    public void addNutrients(double delta) {
        this.nutrients += delta;
        if (this.nutrients < 0) this.nutrients = 0;
    }
}
