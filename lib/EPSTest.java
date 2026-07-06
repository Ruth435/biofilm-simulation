package lib;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.swing.plaf.basic.BasicDirectoryModel;

import org.junit.Assert;
import org.junit.Test;

import Simulation.Bacteria;
import Simulation.EPS;

public class EPSTest {
    /**
     * Test elasticModulus
     */
    @Test
    public void elasticModulusWithBacteria() {
        EPS eps = new EPS(0, 0, null);
        Bacteria other = new Bacteria(2, 0, 1);
        EPS.size = 2;
        Bacteria.EPSElasticModulus = 2;

        assertEquals(2, eps.getElasticModulus(other), 1e-9);
    }

    @Test
    public void elasticModulusWithEPS() {
        EPS eps = new EPS(0, 0, null);
        EPS other = new EPS(2, 0, null);
        EPS.size = 2;
        EPS.EPSElasticModulus = 3;

        assertEquals(3, eps.getElasticModulus(other), 1e-9);
    }

    /**
     * Testing isType
     */
    @Test
    public void isTypeEPS() {
        EPS eps = new EPS(0, 0, null);
        EPS.size = 2;

        //saying eps is not a bacteria should be true
        Assert.assertTrue(eps.isType(false));
    }

    @Test
    public void isTypeBacteria() {
        EPS eps = new EPS(0, 0, null);
        EPS.size = 2;

        //saying eps is a bacteria should be false
        Assert.assertTrue(!eps.isType(true));
    }
}
