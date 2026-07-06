import java.awt.*;
/**
 * parent class which represents a particle object  having parameters:
 * ID as an int (unique identifier), color, xPos and yPos as doubles representing the x and y position of the particle
 * on the grid, and cluster ID as an int representing the cluster to which the particle belongs
 */
public class VisualParticle {

    private final int ID;
    public Color colour;
    public double xPos;
    public double yPos;
    public int clusterID;
    
    /** constructor for a particle object
     * @param ID - int representing the unique ID of the particle
     * @param xPos - double representing the x position of the particle on the grid
     * @param yPos - double representing the y position of the particle on the grid
     * @param clusterID - int representing the cluster to which a particle belongs
     * @param colour - gives the colour of the particle for representation on the JPanel
    */
    public VisualParticle(int ID, double xPos, double yPos,int clusterID, Color colour) 
    {
        this.ID = ID;
        this.colour = colour;
        this.xPos = xPos;
        this.yPos = yPos;
        this.clusterID = clusterID;
    }

    public Color getColour() 
    {
        return colour;
    }

    public double getX() 
    {
        return xPos;
    }

    public double getY() 
    {
        return yPos;
    }
    
    public int getClusterID()
    {
        return clusterID;
    }

    public void setColour(Color colour) 
    {
        this.colour = colour;
    }

    public void setX(double xPos) 
    {
        this.xPos = xPos;
    }

    public void setY(double yPos) 
    {
        this.yPos = yPos;
    }
}
