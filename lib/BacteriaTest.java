package lib;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Assert;

import Simulation.Bacteria;
import Simulation.EPS;
import Simulation.Medium;
import Simulation.Simulation;

public class BacteriaTest {
    /**
     * Test produceEPS
     */
    @Test
    public void produceEPSshouldproduceEPSwhenconditionsAreRight() {
        Bacteria bact = new Bacteria(0, 0, 5);
        Bacteria.EPSproductionRate = 0; //Make it produce EPS instantly
        Bacteria.EPSProductionMinCellDensity = 5;
        Bacteria.EPSProductionsMaxEPSDensity = 0.3;
        Simulation.maxAgents = 100;
        Bacteria.d0 = 1;
        Medium media = new Medium(10, bact);
        bact.setMedium(media);
        media.placeParticle(bact);

        bact.produceEPS();

        assertEquals(2, media.particleCounter);
    }

    @Test
    public void produceEPSwaitingWorksCorrectly() {
        Bacteria.EPSproductionRate = 2;
        Bacteria bact = new Bacteria(0, 0, 5);
        Bacteria.EPSProductionMinCellDensity = 5;
        Bacteria.EPSProductionsMaxEPSDensity = 0.3;
        Simulation.maxAgents = 100;
        Bacteria.d0 = 1;
        Medium media = new Medium(10, bact);
        bact.setMedium(media);
        media.placeParticle(bact);
        
        bact.produceEPS();bact.produceEPS();bact.produceEPS();

        assertEquals(2, media.particleCounter);
        bact.produceEPS();
        assertEquals(3, media.particleCounter);
    }

    @Test
    public void produceEPSneedsCellToBeLong() {
        Bacteria bact = new Bacteria(0, 0, 1);
        Bacteria.EPSproductionRate = 0; //Make it produce EPS instantly
        Bacteria.EPSProductionMinCellDensity = 5;
        Bacteria.EPSProductionsMaxEPSDensity = 0.3;
        Simulation.maxAgents = 100;
        Bacteria.d0 = 1;
        Medium media = new Medium(10, bact);
        bact.setMedium(media);
        media.placeParticle(bact);

        bact.produceEPS();

        assertEquals(1, media.particleCounter);
    }

    @Test
    public void produceEPSneedsCellToBeLongOrMoreNearby() {
        Bacteria bact = new Bacteria(0, 0, 1);
        Bacteria.EPSproductionRate = 0; //Make it produce EPS instantly
        Bacteria.EPSProductionMinCellDensity = 5;
        Bacteria.EPSProductionsMaxEPSDensity = 0.3;
        Simulation.maxAgents = 100;
        Bacteria.d0 = 1;
        Medium media = new Medium(10, bact);
        bact.setMedium(media);
        media.placeParticle(bact);

        Bacteria bact2 = new Bacteria(1, 1, 2); media.placeParticle(bact2); bact2.setMedium(media);
        Bacteria bact3 = new Bacteria(-1, -1, 2); media.placeParticle(bact3); bact3.setMedium(media);

        bact.produceEPS();

        assertEquals(2, media.particleCounter);
    }

    @Test
    public void produceEPSshouldStopIfTooManyNearbyEPS() {
        EPS.size = 0.5;

        Bacteria bact = new Bacteria(0, 0, 5);
        Bacteria.EPSproductionRate = 0; //Make it produce EPS instantly
        Bacteria.EPSProductionMinCellDensity = 5;
        Bacteria.EPSProductionsMaxEPSDensity = 0.3;
        Simulation.maxAgents = 100;
        Bacteria.d0 = 1;
        Medium media = new Medium(100, bact);
        bact.setMedium(media);
        media.placeParticle(bact);

        for (int i = 0; i < 30; i++) {
            bact.produceEPS();
            media.clearCoordinates();
            media.moveParticles(); //This is used to place them onto the grid - they won't actually go anywhere as they haven't got a force
        }

        Assert.assertNotEquals(31, media.particleCounter);
    }

    @Test
    public void produceEPSshouldStopAtASpecificAmountOfEPSNearby() {
        EPS.size = 0.5; //Need 0.3um^2 of EPS/um^2 of searchArea. SearchArea = 49

        Bacteria bact = new Bacteria(0, 0, 5);
        Bacteria.EPSproductionRate = 1; //Make it produce EPS instantly
        Bacteria.EPSProductionMinCellDensity = 5;
        Bacteria.EPSProductionsMaxEPSDensity = 0.3;
        Simulation.maxAgents = 100;
        Bacteria.d0 = 1;
        Medium media = new Medium(100, bact);
        bact.setMedium(media);
        media.placeParticle(bact);

        for (int i = 0; i < 30; i++) {
            bact.produceEPS();
            media.clearCoordinates();
            media.moveParticles(); //This is used to place them onto the grid - they won't actually go anywhere as they haven't got a force
        }

        Assert.assertEquals(20, media.particleCounter);
    }


    /**
     * Test calcFNet
     */
    @Test
    public void calcFNetShouldSumOtherForces() {
        Bacteria bact = new Bacteria(0, 0, 1);
        Bacteria.motility = 0;
        Medium media = new Medium(10, bact);
        EPS eps = new EPS(0, 0.5, media);
        EPS.size = 1;
        Bacteria.EPSElasticModulus = 2;
        bact.setMedium(media);
        media.placeParticle(bact);
        media.placeParticle(eps);

        eps.calculateRepulsiveForces();
        
        //Motility+Random+ E*(d0^1/2)*(distance^3/2)
        assertEquals(Math.pow(0.5, 3.0/2)*2, bact.calculateFNet().getMagnitude(), 1e-3);
    }

    @Test
    public void calcFNetShouldSumOtherForces2() {
        Bacteria bact = new Bacteria(0, 0, 1);
        Bacteria.motility = 300;
        Medium media = new Medium(10, bact);
        EPS eps = new EPS(0, 0.5, media);
        EPS.size = 1;
        Bacteria.EPSElasticModulus = 2;
        bact.setMedium(media);
        media.placeParticle(bact);
        media.placeParticle(eps);

        eps.calculateRepulsiveForces();
        System.out.println(bact.FNet);
        
        //Motility+Random+ E*(d0^1/2)*(distance^3/2)
        assertEquals(300*bact.getyFacing()-Math.pow(0.5, 3.0/2)*2, bact.calculateFNet().yDir, 1e-3);
    }

    /**
     * Test motilityForce
     */
    @Test
    public void motilityForceShouldReturnItself() {
        Bacteria bact = new Bacteria(0, 0, 1);
        Bacteria.motility = 300;

        assertEquals(300, bact.motilityForce().getMagnitude(), 1e-9);
    }

    @Test
    public void motilityForceSplitsAlongAxesCorrectly() {
        Bacteria bact = new Bacteria(0, 0, 1);
        Bacteria.motility = 300;

        assertEquals(300*bact.getxFacing(), bact.motilityForce().xDir, 1e-9);
        assertEquals(300*bact.getyFacing(), bact.motilityForce().yDir, 1e-9);
    }

    /**
     * Test elasticModulus
     */
    @Test
    public void elasticModulusWithBacteria() {
        Bacteria bact = new Bacteria(0, 0, 1);
        Bacteria other = new Bacteria(2, 0, 1);
        Bacteria.BacteriaElasticModulus = 2;

        assertEquals(2, bact.getElasticModulus(other), 1e-9);
    }

    @Test
    public void elasticModulusWithEPS() {
        Bacteria bact = new Bacteria(0, 0, 1);
        EPS other = new EPS(2, 0, null);
        Bacteria.EPSElasticModulus = 2;

        assertEquals(2, bact.getElasticModulus(other), 1e-9);
    }

    /**
     * Testing isType
     */
    @Test
    public void isTypeEPS() {
        Bacteria bact = new Bacteria(0, 0, 1);

        //saying eps is not a bacteria should be true
        Assert.assertTrue(bact.isType(true));
    }

    @Test
    public void isTypeBacteria() {
        Bacteria bact = new Bacteria(0, 0, 1);

        //saying eps is a bacteria should be false
        Assert.assertTrue(!bact.isType(false));
    }
}