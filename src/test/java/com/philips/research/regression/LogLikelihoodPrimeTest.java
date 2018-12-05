package com.philips.research.regression;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.RealLinearAlgebra;
import dk.alexandra.fresco.lib.real.SReal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Vector;

import static com.philips.research.regression.MatrixConstruction.matrix;
import static com.philips.research.regression.VectorConversions.unwrapVector;
import static java.math.BigDecimal.valueOf;
import static java.util.Arrays.asList;

class LogLikelihoodPrimeTest {
    private Runner<Vector<BigDecimal>> runner = new Runner<>();

    @Test
    @DisplayName("first derivative of log likelihood")
    void logLikelihoodPrime() {
        Matrix<BigDecimal> x = matrix(new BigDecimal[][]{
            {valueOf(1.0), valueOf(2.0), valueOf(3.0), valueOf(4.0)},
            {valueOf(1.1), valueOf(2.2), valueOf(3.3), valueOf(4.4)}
        });
        Vector<BigDecimal> y = new Vector<>(asList(valueOf(0.0), valueOf(1.0)));
        Vector<BigDecimal> beta = new Vector<>(asList(valueOf(0.1), valueOf(0.2), valueOf(0.3), valueOf(0.4)));
        Vector<BigDecimal> expected = new Vector<>(asList(valueOf(-0.9134458), valueOf(-1.826892), valueOf(-2.740337), valueOf(-3.653783)));
        Vector<BigDecimal> result = runner.run(new LogLikelihoodApplication(x, y, beta));
        VectorAssert.assertEquals(expected, result, 3);
    }
}

class LogLikelihoodApplication implements Application<Vector<BigDecimal>, ProtocolBuilderNumeric> {

    private final Matrix<BigDecimal> x;
    private final Vector<BigDecimal> y;
    private final Vector<BigDecimal> beta;

    LogLikelihoodApplication(Matrix<BigDecimal> x, Vector<BigDecimal> y, Vector<BigDecimal>beta) {
        this.x = x;
        this.y = y;
        this.beta = beta;
    }

    @Override
    public DRes<Vector<BigDecimal>> buildComputation(ProtocolBuilderNumeric builder) {
        DRes<Matrix<DRes<SReal>>> closedX;
        DRes<Vector<DRes<SReal>>> closedY, closedBeta, closedResult;
        RealLinearAlgebra real = builder.realLinAlg();
        closedX = real.input(x, 1);
        closedY = real.input(y, 1);
        closedBeta = real.input(beta, 1);
        closedResult = builder.seq(new LogLikelihoodPrime(closedX, closedY, closedBeta));
        DRes<Vector<DRes<BigDecimal>>> opened = real.openVector(closedResult);
        return () -> unwrapVector(opened);
    }
}
