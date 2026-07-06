package Simulation;

/**
 *
 * @author Ruth Rudolph, Sonali Naidoo and Byron Clarke
 */
public class EPS extends Particle {

    //Simulation parameters
    public static double EPSElasticModulus;
    public static double size;
    public double getLength() {return size;}
    public double getd0() {return size;}
    
    //Primary EPS attributes
    private int id; public int getId() {return id;}
    public static int totalEPS = 0; //Used for giving ids

    public EPS(double xpos, double ypos, Medium media)
    {
        super(xpos, ypos);
        id = totalEPS;
        totalEPS++;
        this.media = media;
    }
    
    /**
     * Calculate the force currently acting on this EPS
     */
    public ForceVector calculateFNet() {
        synchronized (this) {
            FNet = FNet.add(Particle.randomForce());    //Random force
        }
        ForceVector repulsiveForce = calculateRepulsiveForces();
        synchronized (this) {
            FNet = FNet.add(repulsiveForce);//Repulsive forces (Overlapping particles push each other away to avoid more overlap)
        }
        return FNet;
    }
    
    /**
     * @return the correct elastic modulus for the current repulsive interaction
     */
    public double getElasticModulus(Particle other) {
        if (other instanceof Bacteria) {
            return Bacteria.EPSElasticModulus;
        }
        return EPSElasticModulus;
    }
    
    //DEPRECATED CODE I THINK - WILL REMOVE LATER MAYBE IF UNNEEDED
    /**
     * Calculates the attraction to all nearby EPS particles
     * each nearby EPS exerts an attractive force on this Bacteria
     */
    private ForceVector calculateAttractiveForces()
    {
        Particle[] nearbyParticles = media.getNearbyParticles(10, this);

        ForceVector attractiveForce = ForceVector.zeroVector();
        for (Particle nearbyParticle : nearbyParticles) {
            if (nearbyParticle instanceof Bacteria) {
                Bacteria nearbyBacteria = (Bacteria) nearbyParticle;
                attractiveForce = attractiveForce.add(calculateAttractiveForces(nearbyBacteria));
            }
        }

        return attractiveForce;
    }
    
    //DEPRECATED CODE I THINK - WILL REMOVE LATER MAYBE IF UNNEEDED
    /**
     * Calculates an attractive force to a specific eps nearby
     */
    private ForceVector calculateAttractiveForces(Bacteria bacteria)
    {
        ForceVector attractiveVector = new ForceVector(bacteria.xPosition-xPosition, bacteria.yPosition-yPosition);
        attractiveVector = attractiveVector.toUnitVector().multiply(Bacteria.EPS_ATTRACTION_CONSTANT);
        return attractiveVector;
    }
    
    /**
     * Used for printing the bacteria details to the output file
     */
    public String toString() {
        return id + ", " + getxPosition() + ", " + getyPosition() + ", "  + clusterID + "\n";
    }

    //Code for clustering -------------------------

    /**
     * Returns true or false in relation to whether typeIsBacteria is false
     * If 'typeIsBacteria' is false, then this method says true since yes this isn't a bacteria
     * (ie there's a match between the parameter not referring to bacteria, and this not being bacteria)
     */
    public boolean isType(boolean typeIsBacteria) {
        return !typeIsBacteria;
    }
}