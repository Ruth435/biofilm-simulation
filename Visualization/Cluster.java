/**
 * basic object to store details for clusters
 * of bacteria or EPS particles
 * stores ID as an integer 
 * and count as an integer, representing the number of particles in the cluster
 */
public class Cluster {
    public int id, count;
    
    /**
     * constructor for Cluster object
     * @param id - int representing the clusters unique ID
     * @param count - int representing the number of particles in the cluster
     */
    public Cluster(int id, int count) 
    {
        this.id = id; this.count = count;
    }
    
    public int getId() 
    { 
        return id; 
    }
    
    public int getCount() 
    { 
        return count; 
    }
}