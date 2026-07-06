import java.awt.Color;


/**
 * Child class inheriting from VisualParticle class
 * represents an EPS particle, extends VisualParticle by adding size
 */
public class EPS extends VisualParticle {

    public double size;
    /**
     * constructor for EPS object
     * @param ID - int representing the unique ID of the EPS
     * @param xPos - double representing the x position of the EPS on the grid
     * @param yPos - double representing the y position of the EPS on the grid
     * @param size - double representing the diameter of an EPS particle in um
     * @param clusterId - int representing the cluster to which a bacterium belongs
     * @param color - gives the colour of the bacteria for representation on the JPanel
     */
    public EPS(int ID, double xPos, double yPos, double size, int clusterId, Color color) 
    {
        super(ID, xPos, yPos, clusterId, color);
        this.size = size;
    }
    
    public double getSize ()
    {
        return size;
    }
    
    public void setSize(double s)
    {
        size = s;
    }
}
