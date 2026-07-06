import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class for the visualization module
 * designed to accept a file as input from the simulation module of the bacteria simulation
 * and produce a realistic output representing the file content
 */
public class Visualization extends JPanel{

    private ArrayList<Bacteria> bacteria = new ArrayList<>();  //list of all Bacteria objects from the file (updated every timestep)
    private ArrayList<EPS> epsList = new ArrayList<>(); //list of all EPS objects from the file (updated every timestep)
    private ArrayList<Cluster> bactClusters = new ArrayList<>(); //list of all bacteria clusters from the file (updated every timestep)
    private ArrayList<Cluster> epsClusters = new ArrayList<>();  //list of all EPS clusters from the file (updated every timestep)
    
    private Color bacteriaColour = Color.CYAN; //color of the bacteria (default to cyan if not set by user)
    private Color epsColour = Color.RED; //color of eps (default to red if not set by user)
    private Color backgroundColour = Color.BLACK; //color of the main panel (default to black if not set by user)
    
    private static int frameRate = 250; //int to store the delay between frames (default to 250 if not changed by user)
    private static String fileName = "simulation_output.txt"; //String name of the input file (Assumed to be simulation_output.txt if not changed by user)
    
    private volatile boolean paused = false; //volitile boolean to pause the display thread if user pauses the simulation
    private boolean showBacteriaClusters,showEPSClusters = true; //booleans to display clusters; set to false unless changed by user
    private boolean hide_Bacteria,hide_EPS,frameByFrame = false; //booleans to hide details (EPS or Bacteria) or to set manual frame transition.
    
    //simulation parameters
    private int max_Agents; //Maximum number of particles that could exist in the file
    private double ticks_Between_Output; //amount of simulation time that passes between frames

    //medium values
    private int simulationRadius = 200; //value from center to border (limits movement in simulation) - set to 200 as default
    private double initial_nutrient_concentration,consumption_rate,diffusion_rate; //additional simulation parameters - see simulation module for details

    //EPS vlaues
    private double EPS_EPS_Elastic_Modulus, EPSsize; //EPS size gives the diameter of EPS partcles in um
        
    //bacteria values
    private double bacteria_diameter, bacteria_LMax, bacteria_motility, //bacteria diameter gives the diameter (width) of bacteria partcles in um
        bacteria_growth_rate, bacteria_reproduction_rate ,bacteria_EPS_production_rate,
        bacteria_EPS_elastic_modulus, bacteria_bacteria_elastic_modulus,friction,
        EPS_Production_Min_Cell_Density,EPS_Production_Max_EPS_Density; //additional simulation parameters - see simulation module for details
    
    //simulation values - every loop
    private int timePoint, ticksSinceStart; //integers to store the value of the current time point as well as how long has passed since the simulation began
    //ticksSince start is later converted to simulation hours
    
    //colony values
    private double colonyX,colonyY,diameter,roughness,sharpness; //additional simulation parameters - see simulation module for details
    
    private static JButton backSettingsButton; //button appears once visualization completes; allows user to go back to the setting page
    private static JButton replayButton; //button appears once visualization completes; allows user to view the simulation again with the same setting
    private static JButton pauseButton; //Button is visible while the simulation runs; to allow the user to pause
    private static JButton nextFrameButton; //Button is visible if the user has chosen to manually click through frames; indicates that the program should show the next frame
    private static JLabel frameSimulationData; //JLable which will display all the simulation data specific to the given frame
    private static JLabel simulationSettingsData;//JLable which will display all the simulation data which is constant
    
    double cellSizePx = 1; //parameter which translates cell sizes to pixels; calculated later or 1 as default
    
    private static double artificial_size_increase_eps = 4.0; //variable to increase the size of eps
    private static double artificial_size_increase_bacteria = 1.0; //variable to increase the size of bacteria
    private static final int FRAME_RADIUS = 750; //constant int to hold the size of the simulation frame
    private static final int DRAW_PADDING = 25; //constant to padd the drawing area; to prevent bacteria and eps on the edge from disappearing
    
    /**
     * Constructor for the visualization; sets background color of the JPanel (Visualization)
     */
    public Visualization()
    {          
        setBackground(backgroundColour);
    }
    
    /**
     * Reads in the file; initializes all the simulation values from the file
     * sets the frameSimulationData and simulationSettingsData Labels
     * loops through the time points; filling and updating the ArrayLists; and then pausing till the next frame (per delay or until signaled)
     * calls redraw to update the display for each frame
     * @param filename
     * @throws IOException
     * @throws InterruptedException 
     */
    public void readSimulationFile(String filename) throws IOException, InterruptedException {
        //clear all ArrayLists in preparation for the new file
        bacteria.clear();
        epsList.clear();
        bactClusters.clear();
        epsClusters.clear();
        
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
        String line;
        //read in and set the simulation values
        max_Agents = Integer.parseInt(br.readLine().trim());
        ticks_Between_Output = Double.parseDouble(br.readLine().trim());
        br.readLine();
        
        //read in and set the medium values
        simulationRadius = Integer.parseInt(br.readLine().trim());
        initial_nutrient_concentration = Double.parseDouble(br.readLine().trim());
        consumption_rate = Double.parseDouble(br.readLine().trim());
        diffusion_rate = Double.parseDouble(br.readLine().trim());
        br.readLine();
        
        //read in and set the EPS vlaues
        EPSsize = Double.parseDouble(br.readLine().trim());
        EPS_EPS_Elastic_Modulus = Double.parseDouble(br.readLine().trim());
        br.readLine();
        
        //read in and set the bacteria values
        bacteria_LMax = Double.parseDouble(br.readLine().trim());
        bacteria_diameter = Double.parseDouble(br.readLine().trim());
        bacteria_motility = Double.parseDouble(br.readLine().trim());
        bacteria_growth_rate = Double.parseDouble(br.readLine().trim());
        bacteria_reproduction_rate = Double.parseDouble(br.readLine().trim());
        bacteria_EPS_production_rate = Double.parseDouble(br.readLine().trim());
        bacteria_EPS_elastic_modulus = Double.parseDouble(br.readLine().trim());
        bacteria_bacteria_elastic_modulus = Double.parseDouble(br.readLine().trim());
        friction = Double.parseDouble(br.readLine().trim());
        EPS_Production_Min_Cell_Density = Double.parseDouble(br.readLine().trim());
        EPS_Production_Max_EPS_Density = Double.parseDouble(br.readLine().trim());
        
        //set the JLabel with all the constant text values
        simulationSettingsData.setText(
        "<html>  Simulation Settings:<br><br>"
        + "  Bacteria Values:<br>"
        + "  Motility: <br>" + bacteria_motility + "<br>"
        + "  Reproduction Rate: <br>" + bacteria_reproduction_rate + "<br><br>"
        + "  Media Values:<br>"
        + "  Initial Nutrient Concentration: <br>" + initial_nutrient_concentration + "<br>"
        + "  Nutrient Diffusion Rate: <br>" + diffusion_rate
        + "</html>"
        );
        
        //while loop runs for all the timePoints
        while ((line = br.readLine()) != null) {

            //pause this thread if the user has paused the simulation or if manual clickthough; until user reusmes or singnals for next frame
            synchronized(this){
                while(paused)
                {
                    wait();
                }
            }

            line = line.trim();
            if (line.isEmpty()) continue;

            // --- Time point header ---
            timePoint = Integer.parseInt(line);
            ticksSinceStart = Integer.parseInt(br.readLine().trim());

            //Colony centre
            String[] parts = br.readLine().split(",\\s*");
            colonyX = Double.parseDouble(parts[0]);
            colonyY = Double.parseDouble(parts[1]);

            //other time point values
            diameter = Double.parseDouble(br.readLine().trim());
            roughness = Double.parseDouble(br.readLine().trim());
            sharpness = Double.parseDouble(br.readLine().trim());

            // --- Bacteria section ---
            br.readLine();
            //Read bacteria values and update ArrayList until next section
            while ((line = br.readLine()) != null && !line.startsWith("EPS:")) {
                line = line.trim();
                if (line.isEmpty()) continue;                
                String[] bparts = line.split(",\\s*");
                if (bparts.length >= 7) {
                        int ID = Integer.parseInt(bparts[0]);
                        double x = Double.parseDouble(bparts[1]);
                        double y = Double.parseDouble(bparts[2]);
                        double xf = Double.parseDouble(bparts[3]);
                        double yf = Double.parseDouble(bparts[4]);
                        double length = Double.parseDouble(bparts[5]);
                        int cluster = Integer.parseInt(bparts[6]);
                        
                        if(bacteria.size() <= ID) //If this bacteria is not yet in the array; add it
                        {
                           while (bacteria.size() <= ID) {
                              bacteria.add(null);
                            }
                            bacteria.add(ID,new Bacteria(ID, x, y, xf, yf, length, bacteria_diameter, cluster, bacteriaColour));
                        }
                        else //if the bacteria is already in the array; overwrite the values
                        {
                            bacteria.set(ID,new Bacteria(ID, x, y, xf, yf, length, bacteria_diameter, cluster, bacteriaColour));
                        }
                    }
            }
            
            // ---EPS section ---
            while ((line = br.readLine()) != null && !line.startsWith("Bacterial Clusters:")) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] eparts = line.split(",\\s*");
                if (eparts.length >= 4) {
                        int ID = Integer.parseInt(eparts[0]);
                        double x = Double.parseDouble(eparts[1]);
                        double y = Double.parseDouble(eparts[2]);
                        int cluster = Integer.parseInt(eparts[3]);
                        
                        
                        if(epsList.size() <= ID) //If this EPS is not yet in the array; add it
                        {
                            while (epsList.size() <= ID) {
                            epsList.add(null);
                            }
                            epsList.add(ID,new EPS(ID, x, y, EPSsize, cluster, epsColour));
                        }
                        else //if the eps is already in the array; overwrite the values
                        {
                            epsList.set(ID,new EPS(ID, x, y,EPSsize, cluster, epsColour));
                        }
                    }
            }
            
            // ---Bacterial clusters ---
            while ((line = br.readLine()) != null && !line.startsWith("EPS Clusters:")) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] bcparts = line.split(",\\s*");
                if (bcparts.length >= 2) {
                        int ID = Integer.parseInt(bcparts[0]);
                        int numMembers = Integer.parseInt(bcparts[1]);

                         if(bactClusters.size() <= ID) //If this cluster is not yet in the array; add it
                        {
                           while (bactClusters.size() <= ID) {
                            bactClusters.add(null);
                            }
                            bactClusters.add(ID,new Cluster(ID,numMembers));
                        }
                         else //if the cluster is already in the array; overwrite the vaues
                        {
                            bactClusters.set(ID,new Cluster(ID,numMembers));
                        }
                        
                    }
            }
            
            // ---EPS clusters ---
            while ((line = br.readLine()) != null && !line.trim().isEmpty()) {
                if (line.isEmpty()) continue;
                String[] ecparts = line.split(",\\s*");
                if (ecparts.length >= 2) {
                        int ID = Integer.parseInt(ecparts[0]);
                        int numMembers = Integer.parseInt(ecparts[1]);
                        
                        if(epsClusters.size() <= ID)
                        {
                           while (epsClusters.size() <= ID) {
                            epsClusters.add(null);
                            }
                            epsClusters.add(ID,new Cluster(ID,numMembers)); //If this cluster is not yet in the array; add it
                        }
                        else
                        {
                            epsClusters.set(ID,new Cluster(ID,numMembers)); //if the cluster is already in the array; overwrite the vaues
                        }
                    }
            }
            
            //set the JLabel with all the time-point specific values
            frameSimulationData.setText(
            "<html>  Time point data:<br><br>"
            + "  Time Point:<br>"
            + timePoint + "<br>"
            + "  Time: <br>" + ticksSinceStart/360.0 + " hours<br><br>"
            + "  Number of bacteria: <br>" + bacteria.size() + "<br>"
            + "  Bacteria clusters: <br>" + bactClusters.size() + "<br>"
            + "  Bacteria colony diameter: <br>" + diameter + "<br><br>"
            + "  Number of EPS: <br>" + epsList.size() + "<br>"
            + "  EPS clusters: <br>" + epsClusters.size() + "<br><br>"
            + "  Roughness: <br>" + roughness + "<br>"
            + "  Sharpness: <br>" + sharpness + "<br>"
            + "</html>"
            );
            
            //call repaint to update the display
            SwingUtilities.invokeLater(() -> {
                repaint();
            });
            
            if(!frameByFrame) //if this is not a manual click through visualization - delay the thread
            {
                Thread.sleep(frameRate);
            }
            else //if this is a manual click through - pause the thread until the user plays the next frame
            {
                togglePause();
            }
            
          }       
            pauseButton.setVisible(false); //hide the pause button once visualization is complete
            replayButton.setVisible(true); //show the replay button to give the user the option to watch again
            backSettingsButton.setVisible(true); //show the settings button to give the user the option to go back to the settings page
        }catch (InterruptedException ex){
        }
     }

    /**
     * paintComponent method to update the display with all visual particles
     * uses the ArrayLists to draw the particles onto the visualization panel
     * @param g - Graphics
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //calculates the translation variable from simulation size to viaual size; with a possible artificial increase
        cellSizePx = (FRAME_RADIUS/(simulationRadius*2.0));
        
        
        // --- draw bacteria as rods ---
        if(!hide_Bacteria) //provided the user has not hidden bacteria
        {
            for (Bacteria b : bacteria) {
                if (b ==null) continue;
                if(showBacteriaClusters)
                {
                    g2d.setColor(generateColor(b.getClusterID(),true));
                }
                else
                {
                    g2d.setColor(b.getColour());
                }
                Point p = toScreen(b.getX(), b.getY()); //use the toScreen method to find where the bacteria should be drawn
                double angle = Math.atan2(b.getXFacing(), b.getYFacing()); //calculate the angle the bacteria is facing
                int width  = (int) (cellSizePx*b.getWidth()*artificial_size_increase_bacteria); //translate width
                int length = (int) (cellSizePx * b.getLength()*artificial_size_increase_bacteria); //translate length
                width = Math.max(width, 1); //ensure that the width is never less than a single pixel; if it is; set it equal to one pixel
                length = Math.max(length, 1); //ensure that the length is never less than a single pixel; if it is; set it equal to one pixel
                AffineTransform old = g2d.getTransform();
                g2d.translate(p.x, p.y); //translate the point to be drawn onto the actual display
                g2d.rotate(angle/4); //rotate the bacteria
                g2d.fillRoundRect(-length / 2,-width / 2, length, width, width, width); //draw the bacteria
                g2d.setTransform(old);
            }
        }

        // --- draw EPS as small circles ---
        if(!hide_EPS)
        {
            for (EPS e : epsList) {
               if (e ==null) continue;
                if(showEPSClusters)
                {
                    g2d.setColor(generateColor(e.getClusterID(),false));
                }
                else
                {
                    g2d.setColor(e.getColour());
                }
                Point p = toScreen(e.getX(), e.getY()); //use the toScreen method to find where the eps should be drawn
                int s = (int) (cellSizePx*e.getSize()*artificial_size_increase_eps); //translate the size
                s = Math.max(s, 1); //ensure the eps is never less than a single pixel, if it is, set it equal to one pixel
                g2d.fillOval(p.x - s/2, p.y - s/2, s, s); //draw the eps
            }
        }
    
    }
    
    /**
     * translates the simulation x and y values to a point on the frame
     * @param simX - x coordinate from the simulation
     * @param simY - y coordinate from the simulation 
     * @return Point
     */
    private Point toScreen(double simX, double simY) {
        double scale = ((FRAME_RADIUS-DRAW_PADDING)/2.0)/simulationRadius;
        int px = (int)(FRAME_RADIUS/2.0 + simX * scale);
        int py = (int)(FRAME_RADIUS/2.0 - simY * scale);
        return new Point(px,py);
    }

    /**
     * Helper method to convert the String name of a color to a Color object
     * @param name - string name of the Color
     * @return Color
     */
    private static Color parseColor(String name) {
        switch (name.toLowerCase()) {
        case "red":     return Color.RED;
        case "blue":    return Color.BLUE;
        case "yellow":  return Color.YELLOW;
        case "cyan":    return Color.CYAN;
        case "magenta": return Color.MAGENTA;
        case "white":   return Color.WHITE;
        case "orange":  return Color.ORANGE;
        case "black":   return Color.BLACK;
        default:        return Color.CYAN;
        }
    } 
    
    /**
     * Uses the cluster ID and size to calculate the color to be used for each bacteria in that cluster
     * takes in cluster ID from the bacteria; then uses the cluster ArrayList to find the size and calculate the color
     * created with the help of ChatGPT
     * @param id
     * @param isBacteria
     * @return Color
     */
    private Color generateColor(int id, boolean isBacteria) {
    // Fixed base colors
    float hue = isBacteria ? 0.5f : 0.02f; //EPS = red-ish (0.02), bacteria = blue-ish (0.5)
    float saturation = 0.8f;

    float brightness;
        float minBrightness = 0.4f;
        float maxBrightness = 0.99f;
    if(isBacteria)
    {
        brightness = (float) (minBrightness + (bactClusters.get(id).getCount()) * (maxBrightness - minBrightness) / (max_Agents/2.5));
    }
    else
    {
        brightness = (float)(minBrightness + (epsClusters.get(id).getCount() - 1.0) * (maxBrightness - minBrightness) / (max_Agents/2.5));
    }
    
    return Color.getHSBColor(hue, saturation, brightness);
}
    /**
     * Settings display; shows the options to the user, validates input, sets the parameters
     * and then returns a Boolean; true if the user wants to play the simulation or false if they exit
     * @param view - Visualization object
     * @return Boolean to indicate if the user wishes to continue or exit
     */
    private static boolean showOptionsDialog(Visualization view) {
    while (true) {
        //fields to enter the file name and delay between frames
        JTextField fileField = new JTextField(Visualization.fileName);
        JTextField frameRateField = new JTextField(Integer.toString(Visualization.frameRate));
        
        JTextField epsSizeField = new JTextField(Double.toString(Visualization.artificial_size_increase_eps));
        JTextField bacteriaSizeField = new JTextField(Double.toString(Visualization.artificial_size_increase_bacteria));
        
        //Combo boxes to show available options for colors of bacteria, eps and background
        JComboBox<String> bactColorBox = new JComboBox<>(new String[]{"Cyan","Magenta","White","Orange","Green","Red","Blue","Yellow"});
        JComboBox<String> epsColorBox  = new JComboBox<>(new String[]{"Red","White","Cyan","Magenta","Orange","Green","Blue","Yellow"});
        JComboBox<String> backgroundColorBox = new JComboBox<>(new String[]{"black","White"});
        
        //Check boxes for displaying clusters
        JCheckBox clusterBox = new JCheckBox("Show Bacteria Clusters", false);
        JCheckBox clusterBoxEPS = new JCheckBox("Show EPS Clusters", false);

        //Check boxes for hiding bacteia or eps
        JCheckBox hideBacteria = new JCheckBox("Hide bacteria", false);
        JCheckBox hideEPS = new JCheckBox("Hide EPS", false);
        
        //check box to opt for manual frame click throgh rather than auto play
        JCheckBox frameByFrame = new JCheckBox("Manual frame clickthrough", false);

        //If the checkBox for showing bacteria clusters is clicked
        clusterBox.addItemListener(e -> {
            boolean showClusters = clusterBox.isSelected();
            //grey out the bacteria color selection box; to indicate that the user does not have the option to set bacteria color if they are showing clusters
            bactColorBox.setEnabled(!showClusters);
            hideBacteria.setEnabled(!showClusters);
        });

        //If the checkBox for showing eps clusters is clicked
        clusterBoxEPS.addItemListener(e -> {
            boolean showEPSClusters = clusterBoxEPS.isSelected();
            //grey out the eps color selection box; to indicate that the user does not have the option to set eps color if they are showing clusters
            epsColorBox.setEnabled(!showEPSClusters);
            hideEPS.setEnabled(!showEPSClusters);
        });

        //If the checkBox for hiding bacteria is clicked
        hideBacteria.addItemListener(e -> {
            boolean hide_Bacteria = hideBacteria.isSelected();
            //grey out the bacteria color selection box, hide EPS box
            //and the show Bacteria clusters box
            //to indicate that the user does not have the option to set these if they are hiding bacteria
            bactColorBox.setEnabled(!hide_Bacteria);      
            clusterBox.setEnabled(!hide_Bacteria);
            hideEPS.setEnabled(!hide_Bacteria);
            bacteriaSizeField.setEnabled(!hide_Bacteria);
        });

        //If the checkBox for hiding eps is clicked
        hideEPS.addItemListener(e -> {
            boolean hide_EPS = hideEPS.isSelected();
            //grey out the EPS color selection box, hide Bacteria box
            //and the show EPS clusters box
            //to indicate that the user does not have the option to set these if they are hiding EPS
            epsColorBox.setEnabled(!hide_EPS);
            clusterBoxEPS.setEnabled(!hide_EPS);
            hideBacteria.setEnabled(!hide_EPS);
            epsSizeField.setEnabled(!hide_EPS);
        });

        //If the checkBox for manual clickthrough is clicked
        frameByFrame.addItemListener(e -> {
            boolean frameClickthrough = frameByFrame.isSelected();
            //grey out the frame rate field; to indicate that the user cannot set this if they are using manual clickthrough
            frameRateField.setEnabled(!frameClickthrough);
        });

        //add all the eleemnts to the panel
        JPanel panel = new JPanel(new GridLayout(0,1));
        panel.add(new JLabel("Input file:"));
        panel.add(fileField);
        panel.add(new JLabel("Delay between frames (ms):"));
        panel.add(frameRateField);
        panel.add(new JLabel("Bacterium colour:"));
        panel.add(bactColorBox);
        panel.add(new JLabel("EPS colour:"));
        panel.add(epsColorBox);
        panel.add(new JLabel("Background colour:"));
        panel.add(backgroundColorBox);
        panel.add(clusterBox);
        panel.add(clusterBoxEPS);
        panel.add(hideBacteria);
        panel.add(hideEPS);
        panel.add(frameByFrame);
        panel.add(new JLabel(""));
        panel.add(new JLabel("Multiply EPS size: "));
        panel.add(epsSizeField);
        panel.add(new JLabel("Multiply Bacteria size: "));
        panel.add(bacteriaSizeField);

        int result = JOptionPane.showConfirmDialog(null, panel,
                "Simulation Settings", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return false;

        // --- Validation of input ---
        String fileNameInput = fileField.getText().trim();
        String delayInput = frameRateField.getText().trim();
        String epsBoostInput = epsSizeField.getText().trim();
        String bacteriaBoostInput = bacteriaSizeField.getText().trim();
        File f = new File(fileNameInput);
        int delay;
        double epsBoost,bactBoost;
        //check that the file name is of a file which exisits in the correct directory
        if (!f.exists() || !f.isFile()) 
        {
            JOptionPane.showMessageDialog(null,"File not found.","Invalid Input",JOptionPane.ERROR_MESSAGE);
            continue;
        }
        try 
        //check that the delay is a valid integer input
        {
            delay = Integer.parseInt(delayInput);
            if (delay <= 0) throw new NumberFormatException();
        } 
        catch (NumberFormatException ex) 
        {
            JOptionPane.showMessageDialog(null,"Delay must be a positive integer.",
                                          "Invalid Input",JOptionPane.ERROR_MESSAGE);
            continue;
        }
        try 
        //check that the eps multiplyer is valid
        {
            epsBoost = Double.parseDouble(epsBoostInput);
            if (delay <= 0) throw new NumberFormatException();
        } 
        catch (NumberFormatException ex) 
        {
            JOptionPane.showMessageDialog(null,"EPS multiplyer must be a positive float.",
                                          "Invalid Input",JOptionPane.ERROR_MESSAGE);
            continue;
        }
        //check that the bacteia multiplyer is valid
        try
        {
            bactBoost = Double.parseDouble(bacteriaBoostInput);
            if (delay <= 0) throw new NumberFormatException();
        } 
        catch (NumberFormatException ex) 
        {
            JOptionPane.showMessageDialog(null,"Bacteria multiplyer must be a positive float.",
                                          "Invalid Input",JOptionPane.ERROR_MESSAGE);
            continue;
        }

        // --- Apply settings; set variable values ---
        Visualization.fileName  = fileNameInput;
        Visualization.frameRate = delay;
        view.bacteriaColour = parseColor((String)bactColorBox.getSelectedItem());
        view.epsColour = parseColor((String)epsColorBox.getSelectedItem());
        view.showBacteriaClusters = clusterBox.isSelected();
        view.showEPSClusters = clusterBoxEPS.isSelected();
        view.backgroundColour = parseColor((String)backgroundColorBox.getSelectedItem());
        view.setBackground(view.backgroundColour);
        view.hide_Bacteria = hideBacteria.isSelected();
        view.hide_EPS = hideEPS.isSelected();
        view.frameByFrame = frameByFrame.isSelected();
        view.artificial_size_increase_eps = epsBoost;
        view.artificial_size_increase_bacteria = bactBoost;
        
        return true; //return to calling method
    }
    }
    
    /**
     * method to start a new thread for file processing
     * also sets button visibility
     */
    public void startPlayback()
    {
        replayButton.setVisible(false); //hide replay button until the end of the visualization
        backSettingsButton.setVisible(false); //hide settings button until the end of the visualization
        if(!frameByFrame) //if this is not a manual clickthrough
        {
            pauseButton.setVisible(true); //show pause button
        }
        else //otherwise (manual clickthrough)
        {
            nextFrameButton.setVisible(true); //show next frame button
        }
        //start the new thread
            new Thread(() -> {
                try {
                    readSimulationFile(Visualization.fileName);
                } catch (IOException | InterruptedException ex) {
                    Logger.getLogger(Visualization.class.getName()).log(Level.SEVERE, null, ex);
                }
            }).start();
    }
    
    /**
     * set pause to the opposite of what it is currently
     */
    public synchronized void togglePause()
    {
        paused = !paused;
    }
    
    /**
     * check the value of the paused Boolean
     * @return the value of paused
     */
    public boolean isPuased()
    {
        return paused;
    }
    /**
     * synchronized method to set paused to false
     * and notify threads
     */
    public synchronized void resumePlayback()
    {
        paused = false;
        notifyAll();
    }
    
    /**
     * main method to set central visual elements and coordinate other methods
     * @param args 
     */
    public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        try {
            Visualization view = new Visualization();
            
            //show settings options to user; continue if they do not cancel
            if(!showOptionsDialog(view)){
                return;
            }

            //create settigns button and add a listener
            backSettingsButton = new JButton("Return to settings");
            //if button is selected
            backSettingsButton.addActionListener( e -> {
                replayButton.setVisible(false); //hide the replay button
                backSettingsButton.setVisible(false); //hide this button
                if(showOptionsDialog(view)){ //show settings
                   view.startPlayback(); //continue if user did not cancel
                }
            }); 
            
            //create replay button and add listener
            replayButton = new JButton("Replay Simulation");
            replayButton.addActionListener(e -> {
                view.startPlayback(); //restart the visualization
           });
            
            //create the pause button and add listener
            pauseButton = new JButton("Pause");
            pauseButton.addActionListener(e ->{
                if(view.isPuased()) { //if the visualization is currently paused
                    view.resumePlayback(); //resume the visualization
                    pauseButton.setText("Pause"); //set the text back to Pause
                }
                else //if the simulation was not paused
                {
                    view.togglePause(); //set paused
                    pauseButton.setText("Resume"); //change text to resume
                }
            });
            
            //create next frame button and add listenter 
            nextFrameButton = new JButton("Next Frame/timestep");
            nextFrameButton.addActionListener(e ->{
                view.togglePause(); //use paused to control frames
                view.resumePlayback(); 
            });
            
            //create Labels
            frameSimulationData = new JLabel("  Time point data: ");
            frameSimulationData.setForeground(Color.WHITE);
            simulationSettingsData = new JLabel("  Simulation Settings: ");
            simulationSettingsData.setForeground(Color.WHITE);
            
            //--- Main panel ---
            view.setPreferredSize(new Dimension(Visualization.FRAME_RADIUS,Visualization.FRAME_RADIUS ));
            JFrame frame = new JFrame("Simulation Viewer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            
            //create a contols pannel for the buttons
            JPanel controls = new JPanel();
            controls.setBackground(Color.DARK_GRAY);
            controls.add(backSettingsButton);
            controls.add(replayButton);
            controls.add(pauseButton);
            controls.add(nextFrameButton);
            
           
            //Left column: for the simulation data
            JPanel leftPanel = new JPanel();
            leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
            leftPanel.setBackground(Color.DARK_GRAY); // or match background
            leftPanel.setPreferredSize(new Dimension(DRAW_PADDING*6, 0)); // width fixed
            leftPanel.add(simulationSettingsData);

            //Right column: for the time point data
            JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
            rightPanel.setBackground(Color.DARK_GRAY);
            rightPanel.setPreferredSize(new Dimension(DRAW_PADDING*6, 0));
            rightPanel.add(frameSimulationData);
 
            //add Panels to the frame, in their appropriate locations
            frame.add(controls, BorderLayout.NORTH);
            frame.add(rightPanel, BorderLayout.EAST);
            frame.add(leftPanel, BorderLayout.WEST);
            frame.add(view, BorderLayout.CENTER);
            
            backSettingsButton.setVisible(false); //hide settings button until after the visualiztion
            replayButton.setVisible(false); //hide replay button until after the visualization
            
            //frame listener to ensure the visualization does not start until the frame is set up and ready
            frame.addComponentListener(new ComponentAdapter(){
            @Override
                public void componentShown(ComponentEvent e) {
                    view.startPlayback();
                }
            });
            
            if(view.frameByFrame) //set buttons in the case of manual clickthrough
            {
                pauseButton.setVisible(false);
                nextFrameButton.setVisible(true);
                view.togglePause();
            }
            else //set buttons in case of normal play
            {
                nextFrameButton.setVisible(false);
                pauseButton.setVisible(true);
            }

            frame.pack(); //pack frame so that all components have their desired space and are visible in the correct places
            frame.setLocationRelativeTo(null);
            frame.setVisible(true); //show the frame
            
        } catch (HeadlessException e) {
        }
    });
    }
}
