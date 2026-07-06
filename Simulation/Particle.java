package Simulation;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Byron Clarke
 */
public abstract class Particle {

    //Simulation parameter
    public static double FrictionCoefficientPascalsPerhour = 200;

    //Primary particle properties
    protected double xPosition; public int getxPosition() {return (int) Math.round(xPosition);}
    protected double yPosition; public int getyPosition() {return (int) Math.round(yPosition);}

    public abstract double getd0();
    public abstract double getLength();

    //Movement properties
    public ForceVector FNet;
    private ForceVector velocityMicrometerPerHour;

    //Cluster detection
    public int clusterID;

    //media link
    protected Medium media;
    public void setMedium(Medium media) {
        this.media = media;
    }





    /**
     * Constructor for the first bacteria
     */
    public Particle(double xpos, double ypos)
    {
        xPosition = xpos;
        yPosition = ypos;
        FNet = ForceVector.zeroVector();
    }

    /**
     * Must calculate all the forces acting on this particle, and set it to a stored ForceVector value
     * Also returns that ForceVector
     */
    public abstract ForceVector calculateFNet();

    /*
     * Recalculate the position of the particle based on the forces acting upon it.
     * Velocity (um/h) = (RepulsiveForces + AttractiveForces + MotilityForce (if applicable) + RandomForce)/(Friction*CellLength)
     * 
     * we get these units: (Pa*um^2)/(Pa*h * um) = um/h
     */
    public void move() {
        //Calculate the velocity
        double denominator = 1/FrictionCoefficientPascalsPerhour*getLength();
        velocityMicrometerPerHour = FNet.multiply(denominator);

        //Reset FNet so that it can be added to next time by multiple particles
        FNet = ForceVector.zeroVector();

        //Divide the velocity to work out the movement in a single tick
        ForceVector movementMicrometers = velocityMicrometerPerHour.multiply(1.0/(double) Simulation.TICKS_PER_HOUR);

        xPosition += movementMicrometers.xDir;
        yPosition += movementMicrometers.yDir;

        //Replace the particle into the media's grid
        media.placeParticle(this);
    }

    //Methods for different forces -----------------------------------

    /**
     * Generates a random force which is (a random number between 0 and 10^-3 inclusive)*(A unit vector with random direction)
     */
    public static ForceVector randomForce() {
        ForceVector random = generateFacingVector();
        double magnitude = ThreadLocalRandom.current().nextDouble(0.0, 0.001 + Double.MIN_VALUE);

        random = random.multiply(magnitude);
        return random;
    }

    /**
     * Calculates and sums up all the repulsive forces from all of the nearby particles
     */
    public ForceVector calculateRepulsiveForces()
    {
        //For all particles in a nearby square, check if they are overlapping with this bacterium. If so calculate the repulsive force
        Particle[] nearbyParticles = media.getNearbyParticles(2, this);
        ForceVector repulsiveForce = ForceVector.zeroVector();
        for (Particle nearbyParticle : nearbyParticles) {
            double distance = distanceToEdge(nearbyParticle);
            if (distance < 0) {
                repulsiveForce = repulsiveForce.add(calculateRepulsiveForce(nearbyParticle, distance));
            }
        }
        return repulsiveForce;
    }

    /**
     * Returns a force that points directly away from other, and has magnitude scaling on the distance of the overlap between the particles
     */
    public ForceVector calculateRepulsiveForce(Particle other, double distance)
    {
        /*Calculate the difference in positions.
        * These are in an order which would produce a vector that points from other to this
        * Hence if used as a force it'll repel this away from other
        */
        ForceVector repellingVector = new ForceVector(xPosition-other.xPosition, yPosition-other.yPosition);
        distance *=-1; //Invert the distance so it's positive

        //Repulsive force = ElasticModulus*(d0^1/2)*(overlapDistance^3/2)
        double repulsionMagnitude = getElasticModulus(other)*(Math.pow(getd0(), 1.0/2))*(Math.pow(distance, 3.0/2));
        //Force is divided by 2 since there will be a recipricol force from the other particle
        repellingVector = repellingVector.toUnitVector().multiply(repulsionMagnitude/2);

        //The force calculated is applied to both particles
        ForceVector forceForOther = repellingVector.multiply(-1);
        synchronized (other) {
            other.FNet = other.FNet.add(forceForOther);
        }

        return repellingVector;
    }

    //Helper methods for movement code--------------------------------------

    /**
     * Generates a unit vector randomly for bacteria direction
     */
    public static ForceVector generateFacingVector() {
        //Generate 2 numbers
        double x = ThreadLocalRandom.current().nextDouble();
        double y = ThreadLocalRandom.current().nextDouble();

        //Generates true or false values used to determine the sign of the x and y values
        if (ThreadLocalRandom.current().nextBoolean()) x*=-1;
        if (ThreadLocalRandom.current().nextBoolean()) y*=-1;

        //and then divide by magnitude to create a unit vector
        double magnitude = Math.sqrt((Math.pow(x, 2) + Math.pow(y, 2)));
        x = x/magnitude;
        y = y/magnitude;
        return new ForceVector(x, y);
    }

    /**
     * A simple pythagorean theorem to get the distance between particles' closest edges
     * then subtract both particles sizes to get the distance between their closest edges
     */
    public double distanceToEdge(Particle other) {
        double xDifference = other.xPosition-xPosition;
        double yDifference = other.yPosition-yPosition;

        return (Math.sqrt((Math.pow(xDifference, 2)) + Math.pow(yDifference, 2)))-getLength()/2.0-other.getLength()/2.0;
    }

    /**
     * This will be different for each of a bacteria-bacteria, bacteria-eps, and eps-eps interaction
     * it'll be in the range of (2 to 7)x10^5 Pa. These 3 values are specified in the config file
     * @param other the other particle
     * @return the elastic modulus between the 2 particles
     */
    public abstract double getElasticModulus(Particle other);

    //Code for clustering ----------------------------------------------------

    /**
     * A simple pythagorean theorem to get the distance between particles' centres
     */
    public double distanceToCentre(Particle other) {
        double xDifference = other.xPosition-xPosition;
        double yDifference = other.yPosition-yPosition;

        double result = (Math.sqrt(xDifference*xDifference + yDifference*yDifference));
        return result;
    }

    /**
     * @param typeIsBacteria this parameter asks if this particle should be a Bacteria or EPS
     * @return true if the particle matches the type requested by typeIsBacteria
     * If the particle is a Bacteria, and typeIsBacteria is true, then there is a match and so true is returned
     * If the particle is an EPS and typeIsBacteria is true, then there is not a match and so false is returned
     */
    public abstract boolean isType(boolean typeIsBacteria);
}
