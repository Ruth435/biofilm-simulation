package Simulation;

import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author Ruth Rudolph, Sonali Naidoo and Byron Clarke
 */
public class Bacteria extends Particle {

    //Deprecated
    public static final double EPS_ATTRACTION_CONSTANT = 100;

    //Simulation parameters
    public static double lmax;
    public static double d0; public double getd0() {return d0;}
    public static double motility;
    public static double growthRate;
    public static double reproductionRate;
    public static double EPSproductionRate;
    public static double EPSElasticModulus;
    public static double BacteriaElasticModulus;
    public static double EPSProductionMinCellDensity;
    public static double EPSProductionsMaxEPSDensity;
    
    //For ids
    private static int totalBacteria = 0;

    //Primary bacterial properties---------
    private int id; public int getId() {return id;}
    private double length; public double getLength() {return length;}

    private ForceVector facing;
    public double getxFacing() {return facing.xDir;}
    public double getyFacing() {return facing.yDir;}

    //Properties for growth and eps production
    private int timeToNextEPS; public int getTimeToNextEPS() {return timeToNextEPS;} //this get is for unit testing only
    
    /**
     * Constructor for the first bacteria
     */
    public Bacteria(double xpos, double ypos, double l)
    {
        super(xpos, ypos);
        timeToNextEPS = (int) EPSproductionRate;
        id = totalBacteria;
        totalBacteria++;
        length = l;
        facing = Particle.generateFacingVector();
    }

    /**
     * Create a new bacteria from an existing one
     * @param motherCell the cell that is dividing
     */
    public Bacteria(Bacteria motherCell)
    {
        super(motherCell.xPosition, motherCell.yPosition);
        this.id = totalBacteria;
        totalBacteria++;
        this.length = motherCell.length;
        facing = Particle.generateFacingVector();
    }

    /**
     * @return the radius of the bacterium
     */
    public static double getRadius() {
        return d0/2.0;
    }

    /**
     * @return the area of the bacterium (its a spherocylinder)
     */
    public double getArea() {
        double r0 = getRadius();
        return Math.PI*r0*r0 + 2*r0*length;
    }

    /**
     * Grow the bacterium
     */
    public void grow(double dt, double localC, double Abar)
    {
        //dli/dt = Φ*(Ai/Ā)*f(C(xi,yi))
        double fC = localC/(1.0 + localC);
        double dL = growthRate * (getArea()/Abar) * fC * dt;

        if (Double.isNaN(dL) || dL <0) dL = 0.0; //ensure length >= 0
        length += dL;

        if (Double.isNaN(dL) || dL <0) length = 0.1; //minimum length
    }

    /**
     * Decide if the bacterium should divide
     */
    public boolean shouldDivide(double dt) {
        if (length < lmax) return false;
        double p = 1-Math.exp(-reproductionRate*dt);
        return Math.random() < p;
    }

    /**
     * Split the bacterium
     * @return the mother and new daughter cells
     */
    public Bacteria[] divide() {
        double childLength = length/2.0;
        this.length = childLength; //original cell becomes smaller after division
        Bacteria daughter = new Bacteria(this);
        daughter.length = childLength;
        //daughter.xPosition += 0.5;
        daughter.yPosition += 0.0;
        return new Bacteria[] {this, daughter};
    }
    
    /**
     * Checks whether it is time to make a new EPS or not, and if so the bacteria produces one unless
     * there are too many nearby EPS, or there are not enough nearby bacteria, then it doesn't produce an EPS yet.
     * this bacteria immediately produces a EPS once those cell and EPS density conditions are met, if its already due to make one
     */
    public void produceEPS() {
        timeToNextEPS--;
        
        //Check for sufficient area covered by bacteria nearby (this one should be enough so long as its grown a bit - depends on config though)
        //And check there aren't too many EPS nearby.
        int searchSize = (int) Math.ceil(length/2.0);
        Particle[] nearbyParticles = media.getNearbyParticles(searchSize, this);
        double nearbyBacteriaArea = getArea(), nearbyEPSarea = 0;
        //Count the number of nearby EPS and Bacteria
        for (Particle particle : nearbyParticles) {
            if (particle instanceof Bacteria) {
                nearbyBacteriaArea += ((Bacteria) particle).getArea();
            } else {
                nearbyEPSarea += EPS.size*EPS.size*Math.PI;
            }
        }
        searchSize = (2*searchSize+1)*(2*searchSize+1); //Set searchSize to instead be the total area searched
        //If there's more than the minimum bacteria in the area, AND there's a small enough EPS/um^2 density then it can produce
        if (nearbyBacteriaArea > EPSProductionMinCellDensity && nearbyEPSarea/searchSize < EPSProductionsMaxEPSDensity) {
            if (timeToNextEPS <= 0) {
                double xoffset = ThreadLocalRandom.current().nextInt(2)*2-1;
                double yoffset = ThreadLocalRandom.current().nextInt(2)*2-1;
                media.addEPSToArray(new EPS(xPosition+xoffset, yPosition+yoffset, media));
                timeToNextEPS = (int) EPSproductionRate;
            }
        } 
    }

    //Movement code----------------------------------------------------------------------------------------------------------------

    /**
     * Calculate the force currently acting on this bacterium
     */
    public ForceVector calculateFNet() {
        //Never hold 2 locks!
        synchronized (this) {
            FNet = FNet.add(motilityForce());           //Motility force
            //FNet = FNet.add(Particle.randomForce());    //Random force
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
            return BacteriaElasticModulus;
        }
        return EPSElasticModulus;
    }

    /**
     * @return the motility force of this bacterium
     */
    public ForceVector motilityForce()
    {
        ForceVector motilityVector = facing.multiply(motility);
        return motilityVector;
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
            if (nearbyParticle instanceof EPS) {
                EPS nearbyEPS = (EPS) nearbyParticle;
                attractiveForce = attractiveForce.add(calculateAttractiveForces(nearbyEPS));
            }
        }

        return attractiveForce;
    }
    
    //DEPRECATED CODE I THINK - WILL REMOVE LATER MAYBE IF UNNEEDED
    /**
     * Calculates an attractive force to a specific eps nearby
     */
    private ForceVector calculateAttractiveForces(EPS eps)
    {
        ForceVector attractiveVector = new ForceVector(eps.xPosition-xPosition, eps.yPosition-yPosition);
        attractiveVector = attractiveVector.toUnitVector().multiply(EPS_ATTRACTION_CONSTANT);
        return attractiveVector;
    }

    /**
     * Used for printing the bacteria details to the output file
     */
    public String toString() {
        return id + ", " + getxPosition() + ", " + getyPosition() + ", " + getxFacing() + ", " + getyFacing() + ", " + getLength() + ", " + clusterID + "\n";
    }

    //Code for clustering -------------------------

    /**
     * Returns true or false in relation to whether typeIsBacteria is true
     * If 'typeIsBacteria' is true, then this method says true since yes this is a bacteria
     * otherwise we input false and get that as output
     */
    public boolean isType(boolean typeIsBacteria) {
        return typeIsBacteria;
    }
}
