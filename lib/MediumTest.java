package lib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import Simulation.Bacteria;
import Simulation.Medium;
import Simulation.Particle;
import Simulation.Simulation;
import Simulation.EPS;

public class MediumTest {
    /**
     * Constructor test
     */
    @Test
    public void constructorTest() {
        Bacteria bacteria = new Bacteria(0, 0, 1);
        Medium.simulationBoundary = 100;
        Medium media = new Medium(10, bacteria);

        assertEquals(1, media.getParticleCounter()); //ParticleCounter is 1
        assertEquals(0, media.getParticles()[0].getxPosition()); //bacteria initialised into correct position
        assertEquals(0, media.getParticles()[0].getxPosition());

        assertEquals(10, media.getParticles().length); //particle array is correct length
        assertEquals(201, media.grid.length); //grid size is correct
        assertEquals(201, media.grid[0].length);

        assertEquals(bacteria, media.grid[100][100].getBacteria().get(0)); //bacteria is in correct mediaCoordinate
    }

    /**
     * Test addBacteria
     */
    @Test
    public void testAddBacteria() {
        Bacteria bacteria = new Bacteria(0, 0, 1);
        Medium.simulationBoundary = 100;
        Medium media = new Medium(10, bacteria);
        Bacteria bacteria2 = new Bacteria(1, 0, 1);
        media.addBacteriaToArray(bacteria2);

        assertEquals(bacteria2, media.particles[1]);
    }

    /**
     * Test addEPS
     */
    @Test
    public void testAddEPS() {
        Bacteria bacteria = new Bacteria(0, 0, 1);
        Medium.simulationBoundary = 100;
        Medium media = new Medium(10, bacteria);
        EPS eps = new EPS(1, 0, null);
        media.addEPSToArray(eps);

        assertEquals(eps, media.particles[1]);
    }

    /**
     * Test placeParticle
     */
    @Test
    public void testPlaceParticle() {
        Bacteria bacteria = new Bacteria(0, 0, 1);
        Medium.simulationBoundary = 100;
        Medium media = new Medium(10, bacteria);
        EPS eps = new EPS(1, 0, null);
        media.addEPSToArray(eps);

        media.placeParticle(eps);

        assertEquals(eps, media.grid[100][101].getParticles().get(0));
    }

    @Test
    public void testPlaceParticlePutsParticleInCorrectSpot() {
        Bacteria bacteria = new Bacteria(0, 0, 1);
        Medium.simulationBoundary = 100;
        Medium media = new Medium(21, bacteria);
        bacteria.setMedium(media);

        for (int i = 1; i < Medium.DISTANCE_FOR_CLUSTERS*20; i+=Medium.DISTANCE_FOR_CLUSTERS) {
            EPS eps = new EPS(i, 0, media);
            media.addEPSToArray(eps);
            media.placeParticle(eps);

            int epsX = eps.getxPosition();
            int epsY = eps.getyPosition();

            Particle shouldBeEPS = media.grid[media.convertToArrayIndex(epsY)][media.convertToArrayIndex(epsX)].getParticles().get(0);

            System.out.println(epsX + " " + epsY + " | " + shouldBeEPS);
            assertEquals(eps, shouldBeEPS);
        }
        
    }

    /**
     * Test clearCoords
     */
    @Test
    public void testClearCoords() {
        Bacteria bacteria = new Bacteria(0, 0, 1);
        Medium.simulationBoundary = 100;
        Simulation.maxAgents = 100;
        Medium media = new Medium(100, bacteria);
        bacteria.setMedium(media);

        for (int i = 1; i < Medium.DISTANCE_FOR_CLUSTERS*20; i+=Medium.DISTANCE_FOR_CLUSTERS) {
            EPS eps = new EPS(i, 0, media);
            media.addEPSToArray(eps); System.out.println(media.particleCounter);
            media.placeParticle(eps);
        }
        media.clearCoordinates();
        for (int i = 1; i < Medium.DISTANCE_FOR_CLUSTERS*20; i+=Medium.DISTANCE_FOR_CLUSTERS) {
            int shouldbezero = media.grid[media.convertToArrayIndex(0)][media.convertToArrayIndex(i)].getParticles().size();
            assertEquals(0, shouldbezero);
        }
        
    }

    /**
     * Test moveParticles
     */
    @Test
    public void testMoveParticlesMovesThings() {
        Bacteria.motility = 1000;
        Particle.FrictionCoefficientPascalsPerhour = 1;
        Bacteria first = new Bacteria(0, 0, 1);
        Medium.simulationBoundary = 100;
        Simulation.maxAgents = 100;
        Medium media = new Medium(100, first);
        first.setMedium(media);
        
        media.calculateMovements();
        media.clearCoordinates();
        media.moveParticles();
        //Bacteria should be in different coords
        int shouldbezero = media.grid[media.convertToArrayIndex(0)][media.convertToArrayIndex(0)].getParticles().size();
        for (Particle particle : media.grid[media.convertToArrayIndex(0)][media.convertToArrayIndex(0)].getParticles()) {
            System.out.println(particle);
        }
        assertEquals(0, shouldbezero); 
    }

    @Test
    public void testProduceEPSshouldproduceEPSsometimes() {
        Bacteria.motility = 5;
        Particle.FrictionCoefficientPascalsPerhour = 1;
        
        Bacteria.d0 = 1;
        Bacteria.EPSProductionMinCellDensity = 0;
        Bacteria.EPSProductionsMaxEPSDensity = 100;
        Bacteria.EPSproductionRate = 0;

        Bacteria first = new Bacteria(0, 0, 1);
        Medium.simulationBoundary = 100;
        Simulation.maxAgents = 100;
        Medium media = new Medium(100, first);
        first.setMedium(media);
        
        for (int i = 0; i < 10; i++) {
            media.calculateMovements();
            media.clearCoordinates();
            media.moveParticles();
            media.produceEPS();
        }

        // //There should be many particles now
        // for (Particle particle : media.particles) {
        //     System.out.println(particle);
        // }
        Assert.assertNotEquals(1, media.particleCounter); 
    }

    /**
     * Test getNearbyParticles
     */
    @Test
    public void testGetNearbyParticlesWithParticlesAtEdgeOfSearch() {
        Bacteria bacteria = new Bacteria(0, 0, 1);
        Medium.simulationBoundary = 100;
        Medium media = new Medium(10, bacteria);
        EPS eps = new EPS(1, 0, null);
        media.addEPSToArray(eps);
        media.placeParticle(eps);

        //One of the nearby particles is the eps
        Particle[] nearbyParticles = media.getNearbyParticles(1, bacteria);
        boolean result = eps.equals(nearbyParticles[0]) 
            || eps.equals(nearbyParticles[1]); 
        Assert.assertTrue(result);
    }

    @Test
    public void testGetNearbyParticlesWithParticlesAtOtherEdgeOfSearch() {
        Bacteria bacteria = new Bacteria(0, 0, 1);
        Medium.simulationBoundary = 100;
        Medium media = new Medium(10, bacteria);
        EPS eps = new EPS(-1, 0, null);
        media.addEPSToArray(eps);
        media.placeParticle(eps);

        //One of the nearby particles is the eps
        Particle[] nearbyParticles = media.getNearbyParticles(1, bacteria);
        boolean result = eps.equals(nearbyParticles[0]) 
            || eps.equals(nearbyParticles[1]); 
        Assert.assertTrue(result);
    }

    @Test
    public void testGetNearbyParticlesWithParticlesOutsideEdgeOfSearch() {
        Bacteria bacteria = new Bacteria(0, 0, 1);
        Medium.simulationBoundary = 100;
        Medium media = new Medium(10, bacteria);
        EPS eps = new EPS(2, 0, null);
        media.addEPSToArray(eps);
        media.placeParticle(eps);

        //One of the nearby particles is not the eps
        Particle[] nearbyParticles = media.getNearbyParticles(1, bacteria);

        boolean result = false;
        if (nearbyParticles.length != 0) {
            result = eps.equals(nearbyParticles[0]);
        }
        Assert.assertFalse(result);
    }

    /**
     * Test countNearbyParticles
     */
    @Test
    public void testCountNearbyParticlesWithParticlesAtEdgeOfSearch() {
        Bacteria bacteria = new Bacteria(0, 0, 1);
        Medium.simulationBoundary = 100;
        Medium media = new Medium(10, bacteria);
        EPS eps = new EPS(1, 0, null);
        media.addEPSToArray(eps);
        media.placeParticle(eps);

        assertEquals(2, media.countNearbyParticles(1, bacteria));
    }

    @Test
    public void testCountNearbyParticlesWithParticlesAtOtherEdgeOfSearch() {
        Bacteria bacteria = new Bacteria(0, 0, 1);
        Medium.simulationBoundary = 100;
        Medium media = new Medium(10, bacteria);
        EPS eps = new EPS(-1, 0, null);
        media.addEPSToArray(eps);
        media.placeParticle(eps);

        assertEquals(2, media.countNearbyParticles(1, bacteria));
    }

    @Test
    public void testCountNearbyParticlesWithParticlesOutsideEdgeOfSearch() {
        Bacteria bacteria = new Bacteria(0, 0, 1);
        Medium.simulationBoundary = 100;
        Medium media = new Medium(10, bacteria);
        EPS eps = new EPS(-2, 0, null);
        media.addEPSToArray(eps);
        media.placeParticle(eps);

        assertEquals(1, media.countNearbyParticles(1, bacteria));
    }

    /**
     * Test calcMovements
     */
    @Test
    public void testCalcMovementsUpdatesAllPArticlesFNet() {
        Bacteria bacteria = new Bacteria(0, 0, 1);
        Medium.simulationBoundary = 100;
        Medium media = new Medium(21, bacteria);
        bacteria.setMedium(media);

        for (int i = 0; i < Medium.DISTANCE_FOR_CLUSTERS*20; i+=Medium.DISTANCE_FOR_CLUSTERS) {
            EPS eps = new EPS(i, 0, media);
            media.addEPSToArray(eps);
            media.placeParticle(eps);
        }
        media.calculateMovements();
        boolean allParticlesHaveNonZeroFNet = true;
        for (int i = 0; i < media.particleCounter; i++) {
            if (media.particles[i].FNet.getMagnitude() == 0) {
                allParticlesHaveNonZeroFNet = false;
                break;
            }
        }
        Assert.assertTrue(allParticlesHaveNonZeroFNet);
    }

    /**
     * Test detectClusters
     */
    @Test
    public void testDetectClustersPutsAChainOfEPSAtMaxClusterDistanceinOneCluster() {
        Bacteria bacteria = new Bacteria(0, 0, 1);
        Medium.simulationBoundary = 100;
        Medium media = new Medium(21, bacteria);

        for (int i = 0; i < Medium.DISTANCE_FOR_CLUSTERS*20; i+=Medium.DISTANCE_FOR_CLUSTERS) {
            EPS eps = new EPS(i, 0, null);
            media.addEPSToArray(eps);
            media.placeParticle(eps);
        }

        String clusters = media.detectClusters();
        System.out.println(media.toString());
        System.out.println(clusters);

        assertEquals("Bacterial Clusters:\n0, 1\nEPS Clusters:\n0, 20\n\n", media.detectClusters());
    }

    @Test
    public void testDetectClustersPutsAChainOfEPSAtMaxClusterDistanceinOneClusterButNegativeCoords() {
        Bacteria bacteria = new Bacteria(0, 0, 1);
        Medium.simulationBoundary = 100;
        Medium media = new Medium(21, bacteria);

        for (int i = 0; i < Medium.DISTANCE_FOR_CLUSTERS*20; i+=Medium.DISTANCE_FOR_CLUSTERS) {
            EPS eps = new EPS(-1*i, 0, null);
            media.addEPSToArray(eps);
            media.placeParticle(eps);
        }

        String clusters = media.detectClusters();
        System.out.println(media.toString());
        System.out.println(clusters);

        assertEquals("Bacterial Clusters:\n0, 1\nEPS Clusters:\n0, 20\n\n", media.detectClusters());
    }

    @Test
    public void testDetectClustersDoesntPutAChainOfEPSPastMaxClusterDistanceinOneCluster() {
        Bacteria bacteria = new Bacteria(0, 0, 1);
        Medium.simulationBoundary = 100;
        Medium media = new Medium(21, bacteria);

        for (int i = 0; i < (Medium.DISTANCE_FOR_CLUSTERS+1)*20; i+=Medium.DISTANCE_FOR_CLUSTERS+1) {
            EPS eps = new EPS(i, 0, null);
            media.addEPSToArray(eps);
            media.placeParticle(eps);
        }
        String expected = "Bacterial Clusters:\n0, 1\nEPS Clusters:\n";
        for (int i = 0; i < 20; i++) {
            expected += i + ", " + 1 + "\n";
        }

        String clusters = media.detectClusters();
        System.out.println(media.toString());
        System.out.println(clusters);

        assertEquals(expected+"\n", media.detectClusters());
    }

    @Test
    public void testDetectClustersDoesntPutAChainOfEPSAtMaxDistanceButIn2DirectionsInACluster() {
        Bacteria bacteria = new Bacteria(0, 0, 1);
        Medium.simulationBoundary = 100;
        Medium media = new Medium(21, bacteria);

        for (int i = 0; i < (Medium.DISTANCE_FOR_CLUSTERS)*20; i+=Medium.DISTANCE_FOR_CLUSTERS) {
            EPS eps = new EPS(i, i, null);
            media.addEPSToArray(eps);
            media.placeParticle(eps);
        }
        String expected = "Bacterial Clusters:\n0, 1\nEPS Clusters:\n";
        for (int i = 0; i < 20; i++) {
            expected += i + ", " + 1 + "\n";
        }

        String clusters = media.detectClusters();
        System.out.println(media.toString());
        System.out.println(clusters);

        assertEquals(expected+"\n", media.detectClusters());
    }
     
    @Test
    public void convertToArrayIndexShouldGiveXPlusSimulationBoundary() {
        Bacteria bacteria = new Bacteria(0, 0, 1);
        Medium.simulationBoundary = 100;
        Medium media = new Medium(10, bacteria);

        assertEquals(200, media.convertToArrayIndex(100));
    }

    @Test
    public void convertToArrayIndexShouldGiveXPlusSimulationBoundarytest2() {
        Bacteria bacteria = new Bacteria(0, 0, 1);
        Medium.simulationBoundary = 100;
        Medium media = new Medium(10, bacteria);
        

        assertEquals(100, media.convertToArrayIndex(0));
    }

    @Test
    public void convertToArrayIndexShouldGiveXPlusSimulationBoundaryTest3() {
        Bacteria bacteria = new Bacteria(0, 0, 1);
        Medium.simulationBoundary = 100;
        Medium media = new Medium(10, bacteria);

        assertEquals(0, media.convertToArrayIndex(-100));
    }
}
