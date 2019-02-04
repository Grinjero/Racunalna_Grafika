package math.matrix;

import math.vector.IVector;

public class MatrixVectorView extends AbstractMatrix {

    private IVector original;

    private boolean asRowMatrix;

    public MatrixVectorView(IVector original, boolean asRowMatrix) {
        this.original = original;
        this.asRowMatrix = asRowMatrix;
    }

    @Override
    public int getRowsCount() {
        if(asRowMatrix == true) {
            return 1;
        } else {
            return original.getDimension();
        }
    }

    @Override
    public int getColsCount() {
        if(asRowMatrix == true) {
            return original.getDimension();
        } else {
            return 1;
        }
    }

    @Override
    public double get(int row, int column) {
        if(asRowMatrix == true) {
            return original.get(column);
        } else {
            return original.get(row);
        }
    }

    @Override
    public IMatrix set(int row, int column, double value) {
        if(asRowMatrix == true) {
            original.set(column, value);
        } else {
            original.set(row, value);
        }

        return this;
    }

    @Override
    public IMatrix copy() {
        return new MatrixVectorView(original.copy(), asRowMatrix);
    }

    @Override
    public IMatrix newInstance(int rows, int columns) {
        return new Matrix(rows, columns);
    }
}

