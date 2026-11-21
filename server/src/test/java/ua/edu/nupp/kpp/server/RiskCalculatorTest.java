package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RiskCalculatorTest {

    @Test
    void factor1_normalPassengerFlow_returnsNormalized() {
        assertEquals(50.0, RiskCalculator.factor1(1250.0));
    }

    @Test
    void factor2_normalF2_returnsNormalized() {
        assertEquals(50.0, RiskCalculator.factor2(50.0));
    }

    @Test
    void factor3_normalDuration_returnsNormalized() {
        assertEquals(50.0, RiskCalculator.factor3(120.0));
    }

    @Test
    void calculate_normalValues_returnsWeightedSum() {
        assertEquals(58.5, RiskCalculator.calculate(50.0, 60.0, 70.0));
    }

    @Test
    void calculate_factorOutOfRange_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> RiskCalculator.calculate(101.0, 50.0, 50.0));
    }
}
