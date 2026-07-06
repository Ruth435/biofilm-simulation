import java.awt.Color;

/**
 * Child class inheriting from VisualParticle class
 * inherits all details of VisualParticle and adds
 * XFacing and YFacing as doubles which indicate the direction the bacterium is facing in terms of proportion x and y
 * length and width as doubles which give the bacteria dimensions in um
 */
public class Bacteria extends VisualParticle {
    private double xFacing, yFacing, length, width;

    /**
     * constructor for Bacteria object
     * uses parent constructor and assigns additional variables
     * @param ID - int representing the unique ID of the bacteria
     * @param xPos - double representing the x position of the bacteria on the grid
     * @param yPos - double representing the y position of the bacteria on the grid
     * @param xFacing  - double representing the direction of the bacteria on the grid in terms of the x axis
     * @param yFacing - double representing the direction of the bacteria on the grid in terms of the y axis
     * @param length - double representing the length of the bacterium in um
     * @param width  - double representing the width of the bacterium in um
     * @param clusterId - int representing the cluster to which a bacterium belongs
     * @param color - gives the colour of the bacteria for representation on the JPanel
     */
    public Bacteria(int ID, double xPos, double yPos, double xFacing, double yFacing,
                     double length, double width, int clusterId, Color color) 
    {
        super(ID, xPos, yPos, clusterId, color);
        this.xFacing = xFacing;
        this.yFacing = yFacing;
        this.length = length;
        this.width = width;
    }
    
    public double getLength()
    {
        return length;
    }
    
    public double getWidth()
    {
        return width;
    }
    
    public double getYFacing()
    {
        return yFacing;
    }
    
    public double getXFacing()
    {
        return xFacing;
    }
}
