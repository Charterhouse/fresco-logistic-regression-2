package com.philips.research.regression.primitives;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Vector;

import static com.philips.research.regression.Runner.run;
import static com.philips.research.regression.util.ListConversions.unwrap;
import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Laplace Noise")
public class LaplaceNoiseTest {
    List<BigDecimal> noiseVector;
    DoubleSummaryStatistics stats;

    @BeforeEach
    void setUp() {
        noiseVector = run(new LaplaceTestApp());
        stats = noiseVector
            .stream()
            .mapToDouble(BigDecimal::doubleValue)
            .summaryStatistics();
    }
    
    @Test
    @DisplayName("mean is close to zero")
    public void meanIsCloseToZero() {
        assertEquals(0.0, stats.getAverage(), 0.1);
    }

    @Test
    @DisplayName("it has negative values")
    public void hasNegativeValues() {
        assertTrue(stats.getMin() < 0);
    }

    @Test
    @DisplayName("it has positive values")
    public void hasPositiveValues() {
        assertTrue(stats.getMax() > 0);
    }
}

class LaplaceTestApp implements Application<List<BigDecimal>, ProtocolBuilderNumeric> {
    @Override
    public DRes<List<BigDecimal>> buildComputation(ProtocolBuilderNumeric builder) {
        BigDecimal[] inputs = new BigDecimal[20];
        for (int i = 0; i < 20; ++i) {
            inputs[i] = valueOf(0);
        }
        DRes<Vector<DRes<SReal>>> closedInputs = builder.realLinAlg().input(new Vector<>(Arrays.asList(inputs)), 1);
        DRes<Vector<DRes<SReal>>> result = builder.seq(new LaplaceNoiseGenerator(closedInputs, valueOf(1.0 / 5.0), valueOf(0.001)));
        DRes<Vector<DRes<BigDecimal>>> opened = builder.realLinAlg().openVector(result);
        return () -> unwrap(opened);
    }
}
