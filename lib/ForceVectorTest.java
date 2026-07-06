package lib;
import static org.junit.Assert.assertEquals;

import org.junit.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import Simulation.ForceVector;

public class ForceVectorTest {

    /**
     * Test for the zerovector func
     */
    @Test
    public void TestZeroVector() {
        ForceVector forceVector = ForceVector.zeroVector();
        Assert.assertEquals(0, forceVector.xDir, 1e-9);
        Assert.assertEquals(0, forceVector.yDir, 1e-9);
    }

    /**
     * Tests for the multiply function
     */
    @Test
    public void multiplyByZeroShouldReturnZeroVector() {
        ForceVector v = new ForceVector(3.0, -4.0);
        ForceVector result = v.multiply(0);

        assertEquals(0.0, result.xDir, 1e-9);
        assertEquals(0.0, result.yDir, 1e-9);
    }

    @Test
    public void multiplyByPositiveScalarShouldScaleBothComponents() {
        ForceVector v = new ForceVector(2.0, 5.0);
        ForceVector result = v.multiply(3.0);

        assertEquals(6.0, result.xDir, 1e-9);
        assertEquals(15.0, result.yDir, 1e-9);
    }

    @Test
    public void multiplyByNegativeScalarShouldFlipDirection() {
        ForceVector v = new ForceVector(1.0, -2.0);
        ForceVector result = v.multiply(-2.0);

        assertEquals(-2.0, result.xDir, 1e-9);
        assertEquals(4.0, result.yDir, 1e-9);
    }

    @Test
    public void multiplyByOneShouldReturnSameVector() {
        ForceVector v = new ForceVector(7.5, -3.2);
        ForceVector result = v.multiply(1.0);

        assertEquals(v.xDir, result.xDir, 1e-9);
        assertEquals(v.yDir, result.yDir, 1e-9);
    }

    /**
     * Tests for the add method of ForceVector
     */
    @ParameterizedTest(name = "({0},{1}) + ({2},{3}) = ({4},{5})")
    @CsvSource({
        // x1, y1, x2, y2, expectedX, expectedY
        "0.0,0.0,0.0,0.0,0.0,0.0",       // adding two zero vectors
        "1.0,2.0,3.0,4.0,4.0,6.0",       // positive components
        "-1.0,-2.0,-3.0,-4.0,-4.0,-6.0", // negative components
        "5.5,-2.2,-1.5,2.2,4.0,0.0"      // mixed positive/negative
    })
    void testAdd(double x1, double y1,
                 double x2, double y2,
                 double expectedX, double expectedY) {

        ForceVector v1 = new ForceVector(x1, y1);
        ForceVector v2 = new ForceVector(x2, y2);

        ForceVector result = v1.add(v2);

        assertEquals(expectedX, result.xDir, 1e-9);
        assertEquals(expectedY, result.yDir, 1e-9);
    }

    /**
     * Test for magnitude
     */
    @Test
    public void magnitudeOfZeroVectorShouldEqualZero() {
        ForceVector v = new ForceVector(0, 0);
        double result = v.getMagnitude();

        assertEquals(0.0, result, 1e-9);
    }

    @Test
    public void magnitudeOfPositiveVectorShouldBePositive() {
        ForceVector v = new ForceVector(3, 4);
        double result = v.getMagnitude();

        assertEquals(5.0, result, 1e-9);
    }

    @Test
    public void magnitudeOfNegativeVectorShouldBePositive() {
        ForceVector v = new ForceVector(-3, -4);
        double result = v.getMagnitude();

        assertEquals(5.0, result, 1e-9);
    }

    /**
     * Unit vector tests
     */
    @Test
    public void unitVectorOfZeroVectorisZeroVector() {
        ForceVector v = new ForceVector(0, 0);
        ForceVector u = v.toUnitVector();

        assertEquals(0, u.xDir, 1e-9);
        assertEquals(0, u.yDir, 1e-9);
    }

    @Test
    public void magnitudeOfUnitVectorShouldBeOneXOnly() {
        ForceVector v = new ForceVector(5, 0);
        ForceVector u = v.toUnitVector();

        assertEquals(1, u.xDir, 1e-9);
    }

    @Test
    public void magnitudeOfUnitVectorShouldBeOneYOnly() {
        ForceVector v = new ForceVector(0, 10);
        ForceVector u = v.toUnitVector();

        assertEquals(1, u.yDir, 1e-9);
    }

    @Test
    public void magnitudeOfUnitVectorShouldBeOne() {
        ForceVector v = new ForceVector(5, 5);
        ForceVector u = v.toUnitVector();

        assertEquals(Math.sqrt(2)/2, u.xDir, 1e-9);
    }
}
