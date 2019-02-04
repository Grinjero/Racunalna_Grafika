package math.util;

import math.matrix.IMatrix;
import math.vector.IVector;

public class Util {

    public static int[] removeElementFromArray(int[] array, int element) {
        for(int i = 0, offset = 0; i < array.length + 1; ++i) {
            if(i == element) {
                ++offset;
            } else {
                array[i - offset] = i;
            }
        }

        return array;
    }

    public static double[][] createElements(int[] rows, int[] cols, IMatrix original) {
        double[][] elements = new double[rows.length][cols.length];

        for(int row = 0; row < rows.length; ++row) {
            for(int col = 0;  col < cols.length; ++col) {
                elements[row][col] = original.get(rows[row], cols[col]);
            }
        }

        return elements;
    }

    public static double[][] createElements(IMatrix original) {
        double[][] elements = new double[original.getRowsCount()][original.getColsCount()];

        for(int row = 0; row < original.getRowsCount(); ++row) {
            for(int col = 0;  col < original.getColsCount(); ++col) {
                elements[row][col] = original.get(row, col);
            }
        }

        return elements;
    }

    public static IVector reflectVector(IVector toBeReflected, IVector inRelationTo) {
        return inRelationTo
                .nScalarMultiply(toBeReflected.norm() / inRelationTo.norm())
                .nScalarMultiply(toBeReflected.cosine(inRelationTo))
                .nScalarMultiply(2)
                .sub(toBeReflected);
    }

    public static double[] parseArray(String input) {
        String[] splits = input.split(" ");

        double[] inputs = new double[splits.length];

        for(int i = 0; i < splits.length; i++) {
            double value = Double.parseDouble(splits[i]);
            inputs[i] = value;
        }

        return inputs;
    }
}
