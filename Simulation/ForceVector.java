package Simulation;

/**
 * This class is immutable like Strings. so you need to say
 * myForceVector = myForceVector.multiple(someNumber) for example
 * @author Ruth Rudolph, Sonali Naidoo and Byron Clarke
 */
public class ForceVector {
    public double xDir;
    public double yDir;

    public ForceVector(double x, double y) {
        xDir = x;
        yDir = y;
    }

    /**
     * @return a vector with (0,0) as the directions
     */
    public static ForceVector zeroVector() {
        return new ForceVector(0, 0);
    }

    /**
     * @return the scalar multiplication of the vector with scalarMultiple
     * Mainly used for motility force of bacteria
     */
    public ForceVector multiply(double scalarMultiple) {
        return new ForceVector(scalarMultiple*xDir, scalarMultiple*yDir);
    }

    /**
     * @return the addition of the two vectors together
     * Mainly used for bacteria and eps net force calculations
     */
    public ForceVector add(ForceVector other) {
        //System.out.println("Original vector: " + toString() + "\nOther vector: " + other.toString());
        return new ForceVector(xDir + other.xDir, yDir + other.yDir);
    }

    /**
     * @return the magnitude of this ForceVector
     */
    public double getMagnitude() {
        return Math.sqrt((Math.pow(xDir, 2) + Math.pow(yDir, 2)));
    }

    /**
     * @return a unit vector in the same direction as the one given
     */
    public ForceVector toUnitVector() {
        double magnitude = getMagnitude();
        if (magnitude == 0) {
            return zeroVector();
        }
        return new ForceVector(xDir/magnitude, yDir/magnitude);
    }

    /**
     * return the vector notation of this forcevector
     */
    public String toString() {
        return "<" + xDir + ", " + yDir + ">";
    }
}
