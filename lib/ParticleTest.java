package lib;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import Simulation.Bacteria;
import Simulation.EPS;
import Simulation.ForceVector;
import Simulation.Medium;
import Simulation.Particle;
import Simulation.Simulation;

public class ParticleTest {
    /**
     * Test move
     */
    @Test
    public void testMove() {
        Bacteria bact = new Bacteria(0, 0, 1);
        Bacteria.motility = 300;
        Bacteria.FrictionCoefficientPascalsPerhour = 1;
        Medium.simulationBoundary = 500;
        Medium media = new Medium(10, bact);
        EPS.size = 1;
        Bacteria.EPSElasticModulus = 2;
        bact.setMedium(media);
        media.placeParticle(bact);

        assertEquals(bact, media.grid[media.convertToArrayIndex(0)][media.convertToArrayIndex(0)].getBacteria().get(0));

        bact.FNet = new ForceVector(200*Simulation.TICKS_PER_HOUR, 0);
        bact.move();
        System.out.println(bact.getxPosition());

        assertEquals(bact, media.grid[media.convertToArrayIndex(0)][media.convertToArrayIndex(200)].getBacteria().get(0));
        
        //Movement = Fnet/Friction*Length = 300/1*1 = 300
        assertEquals(0, 0);
    }

    /**
     * Test random force is within -10^-3 to 10^-3
     */
    @Test
    public void randomForceShouldBeWithinThisRange() {
        ForceVector test = Particle.randomForce();

        assertEquals(0.0, test.getMagnitude(), 1e-3);
    }

    //Techincally this test can fail by mistake. basically impossible though
    @Test
    public void randomForceShouldBeNon0() {
        ForceVector test = Particle.randomForce();

        Assert.assertNotEquals(0.0, test.getMagnitude(), 0);
    }

    /**
     * Test calculateRepulsiveForce
     */
    @Test
    public void calculateRepulsiveForceShouldMeetFormula_HalfxExd0toTheHalfxDistanceToThe3Over2() {
        EPS eps = new EPS(0, 0, null);
        EPS other = new EPS(2, 0, null);
        EPS.size = 4;
        EPS.EPSElasticModulus = 2;

        //Expect 1/2*E*(d0^1/2)*(overlap)^3/2
        double result = eps.calculateRepulsiveForce(other, eps.distanceToEdge(other)).xDir;
        assertEquals(-1*Math.pow(2, 5.0/2), result, 1e-9);
        //Other particle should experience this too (in opposite direction)
        assertEquals(Math.pow(2, 5.0/2), other.FNet.xDir, 1e-9);
    }

    @Test
    public void calculateRepulsiveForceWithParticleOnOtherSideShouldReturnNegativeForce() {
        EPS eps = new EPS(0, 0, null);
        EPS other = new EPS(-2, 0, null);
        EPS.size = 4;
        EPS.EPSElasticModulus = 2;

        //Expect 1/2*E*(d0^1/2)*(overlap)^3/2
        double result = eps.calculateRepulsiveForce(other, eps.distanceToEdge(other)).xDir;
        assertEquals(1*Math.pow(2, 5.0/2), result, 1e-9);
        //Other particle should experience this too (in opposite direction)
        assertEquals(-1*Math.pow(2, 5.0/2), other.FNet.xDir, 1e-9);
    }

    @Test
    public void calculateRepulsiveForceWithBacteriaShouldUseDifferentElasticModulus() {
        EPS eps = new EPS(0, 0, null);
        Bacteria other = new Bacteria(-2, 0, 4);
        EPS.size = 4;
        EPS.EPSElasticModulus = 2;
        Bacteria.EPSElasticModulus = 4;

        //Expect 1/2*E*(d0^1/2)*(overlap)^3/2
        double result = eps.calculateRepulsiveForce(other, eps.distanceToEdge(other)).xDir;
        assertEquals(2*Math.pow(2, 5.0/2), result, 1e-9);
        //Other particle should experience this too (in opposite direction)
        assertEquals(-2*Math.pow(2, 5.0/2), other.FNet.xDir, 1e-9);
    }

    @Test
    public void repulsiveForceBetweenParticlesInSamePositionShouldBeZero() {
        EPS eps = new EPS(0, 0, null);
        EPS other = new EPS(0, 0, null);
        EPS.size = 2;
        EPS.EPSElasticModulus = 2;

        assertEquals(0, eps.calculateRepulsiveForce(other, eps.distanceToEdge(other)).xDir, 1e-9);
    }


    /**
     * Test generateFacingVector
     */
    @Test
    public void generateFacingVectorShouldHaveMagnitude1() {
        ForceVector test = Particle.generateFacingVector();

        assertEquals(1.0, test.getMagnitude(), 1e-9);
    }

    @Test
    public void generateFacingVectorShouldBeWithinThisRange() {
        ForceVector test = Particle.generateFacingVector();

        assertEquals(0.0, test.xDir, 1);
        assertEquals(0.0, test.yDir, 1);
    }

    @Test
    public void generateFacingVectorShouldOnlyHaveEqualComponentsIfTheyreRoot2Over2() {
        ForceVector test = Particle.generateFacingVector();

        Assert.assertTrue(test.xDir != test.yDir || test.xDir == Math.sqrt(2)/2);
    }

    /**
     * Test distanceToEdge
     */
    @Test
    public void distanceToEdgeEPSInSamePlaceShouldHaveDistanceMinusSize() {
        EPS eps = new EPS(0, 0, null);
        EPS other = new EPS(0, 0, null);
        EPS.size = 1;

        assertEquals(-1, eps.distanceToEdge(other), 1e-9);
    }

    @Test
    public void distanceToEdgeEPSsize1EPS2UnitsAwayShouldHaveDistance1() {
        EPS eps = new EPS(0, 0, null);
        EPS other = new EPS(2, 0, null);
        EPS.size = 1;

        assertEquals(1, eps.distanceToEdge(other), 1e-9);
    }

    @Test
    public void distanceToEdgeEPSsize1EPS1UnitAwayShouldHaveDistance0() {
        EPS eps = new EPS(0, 0, null);
        EPS other = new EPS(0, 1, null);
        EPS.size = 1;

        assertEquals(0, eps.distanceToEdge(other), 1e-9);
    }

    @Test
    public void distanceToEdgeEPSsize1EPS3UnitAwayShouldHaveDistance4() {
        EPS eps = new EPS(0, 0, null);
        EPS other = new EPS(3, 0, null);
        EPS.size = 1;

        assertEquals(2.0, eps.distanceToEdge(other), 1e-9);
    }

    @Test
    public void distanceToEdgeEPSsize1EPS3and4UnitAwayShouldHaveDistance4() {
        EPS eps = new EPS(0, 0, null);
        EPS other = new EPS(-3, -4, null);
        EPS.size = 1;

        assertEquals(4.0, eps.distanceToEdge(other), 1e-9);
    }

    /**
     * Test distance to centre
     */
    @Test
    public void distanceToCentreEPS2UnitsAwayShouldHaveDistance2() {
        EPS eps = new EPS(0, 0, null);
        EPS other = new EPS(2, 0, null);
        EPS.size = 1;

        assertEquals(2, eps.distanceToCentre(other), 1e-9);
    }

    @Test
    public void distanceToCentreEPS0UnitAwayShouldHaveDistance0() {
        EPS eps = new EPS(0, 0, null);
        EPS other = new EPS(0, 0, null);
        EPS.size = 1;

        assertEquals(0.0, eps.distanceToCentre(other), 1e-9);
    }

    @Test
    public void distanceToCentreEPS3UnitAwayShouldHaveDistance3() {
        EPS eps = new EPS(0, 0, null);
        EPS other = new EPS(0, 3, null);
        EPS.size = 1;

        assertEquals(3.0, eps.distanceToCentre(other), 1e-9);
    }

    @Test
    public void distanceToCentreEPS3and4UnitAwayShouldHaveDistance5() {
        EPS eps = new EPS(0, 0, null);
        EPS other = new EPS(-3, -4, null);
        EPS.size = 1;

        assertEquals(5.0, eps.distanceToCentre(other), 1e-9);
    }
}
