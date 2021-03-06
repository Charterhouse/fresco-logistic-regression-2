package com.philips.research.regression.app;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.RealLinearAlgebra;
import dk.alexandra.fresco.lib.real.SReal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.philips.research.regression.util.ListConversions.unwrap;
import static com.philips.research.regression.util.MatrixConstruction.matrixWithZeros;
import static com.philips.research.regression.util.VectorUtils.vectorWithZeros;
import static java.math.BigDecimal.valueOf;

class LogisticRegression implements Application<List<BigDecimal>, ProtocolBuilderNumeric> {
    private final int myId;
    private final Matrix<BigDecimal> matrix;
    private final Vector<BigDecimal> vector;
    private final double lambda;
    private final int iterations;
    private final double privacyBudget;

    LogisticRegression(int myId, Matrix<BigDecimal> matrix, Vector<BigDecimal> vector, double lambda, int iterations, double privacyBudget) {
        this.myId = myId;
        this.matrix = new MatrixWithIntercept(matrix);
        this.vector =  vector;
        this.lambda = lambda;
        this.iterations = iterations;
        this.privacyBudget = privacyBudget;
    }

    @Override
    public DRes<List<BigDecimal>> buildComputation(ProtocolBuilderNumeric builder) {
        return builder.par(par -> {
            DRes<Matrix<DRes<SReal>>> x1, x2;
            DRes<Vector<DRes<SReal>>> y1, y2;
            RealLinearAlgebra linAlg = par.realLinAlg();
            if (myId == 1) {
                x1 = linAlg.input(matrix, 1);
                y1 = linAlg.input(vector, 1);
                x2 = linAlg.input(matrixWithZeros(matrix.getHeight(), matrix.getWidth()), 2);
                y2 = linAlg.input(vectorWithZeros(vector.size()), 2);
            } else {
                x1 = linAlg.input(matrixWithZeros(matrix.getHeight(), matrix.getWidth()), 1);
                y1 = linAlg.input(vectorWithZeros(vector.size()), 1);
                x2 = linAlg.input(matrix, 2);
                y2 = linAlg.input(vector, 2);
            }

            List<DRes<Matrix<DRes<SReal>>>> closedXs = new ArrayList<>();
            closedXs.add(x1);
            closedXs.add(x2);

            List<DRes<Vector<DRes<SReal>>>> closedYs = new ArrayList<>();
            closedYs.add(y1);
            closedYs.add(y2);

            return () -> new Pair<>(closedXs, closedYs);
        }).seq((seq, inputs) -> {
            List<DRes<Matrix<DRes<SReal>>>> closedXs = inputs.getFirst();

            DRes<Vector<DRes<SReal>>> result = privacyBudget > 0
                ? seq.seq(new FitLogisticModel(closedXs, lambda, iterations, matrix, vector, valueOf(privacyBudget)))
                : seq.seq(new FitLogisticModel(closedXs, lambda, iterations, matrix, vector));
            DRes<Vector<DRes<BigDecimal>>> opened = seq.realLinAlg().openVector(result);
            return () -> unwrap(opened);
        });
    }
}

class MatrixWithIntercept extends Matrix<BigDecimal> {
    MatrixWithIntercept(Matrix<BigDecimal> matrix) {
        super(matrix.getHeight(), matrix.getWidth() + 1, (rowNumber) -> {
            ArrayList<BigDecimal> row = matrix.getRow(rowNumber);
            return Stream
                .concat(row.stream(), Stream.of(valueOf(1)))
                .collect(Collectors.toCollection(ArrayList::new));
        });
    }
}
