package com.philips.research.regression.primitives;

import com.philips.research.regression.Runner;
import dk.alexandra.fresco.lib.collections.Matrix;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.philips.research.regression.util.MatrixAssert.assertEquals;
import static com.philips.research.regression.util.MatrixConstruction.matrix;
import static java.math.BigDecimal.valueOf;

@DisplayName("Hessian matrix")
class HessianTest {

    private Runner<Matrix<BigDecimal>> runner = new Runner<>();

    @Test
    @DisplayName("calculates an approximation of the Hessian matrix")
    void calculatesHessian() {
        Matrix<BigDecimal> input = matrix(new BigDecimal[][]{
            {valueOf(1.0), valueOf(2.0)},
            {valueOf(3.0), valueOf(4.0)},
            {valueOf(5.0), valueOf(6.0)},
        });

        Matrix<BigDecimal> expected = matrix(new BigDecimal[][]{
            {valueOf(-8.75), valueOf(-11.0)},
            {valueOf(-11.0), valueOf(-14.0)},
        });

        assertEquals(expected, runner.run(new MatrixTransformation(input, Hessian::new)), 5);
    }
}
