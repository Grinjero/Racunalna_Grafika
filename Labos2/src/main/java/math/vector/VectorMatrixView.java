package math.vector;

import math.matrix.IMatrix;

public class VectorMatrixView extends AbstractVector {

    private IMatrix original;

    private int dimension;

    private boolean rowMatrix;

    public VectorMatrixView(IMatrix original) {
        this.original = original;

        if(original.getRowsCount() == 1) {
            rowMatrix = true;
        }
    }

    @Override
    public double get(int index) {
        if(rowMatrix == true) {
            return original.get(1, index);
        } else {
            return original.get(index, 1);
        }
    }

    @Override
    public IVector set(int index, double value) {
        if(rowMatrix == true) {
            original.set(1, index, value);
        } else {
            original.set(index, 1, value);
        }

        return this;
    }

    @Override
    public int getDimension() {
        if(rowMatrix) {
            return original.getColsCount();
        } else {
            return original.getRowsCount();
        }
    }

    @Override
    public IVector copy() {
        return new VectorMatrixView(original.copy());
    }

    @Override
    public IVector newInstance(int vectorSize) {
        return new Vector(vectorSize, false);
    }
}
