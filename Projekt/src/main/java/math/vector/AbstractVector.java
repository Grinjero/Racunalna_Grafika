package math.vector;

import math.matrix.IMatrix;
import math.matrix.Matrix;
import math.matrix.MatrixVectorView;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;

public abstract class AbstractVector implements IVector {

    private static final int DEFAULT_PRECISION = 3;

    @Override
    public IVector add(IVector operand) {
        if(this.getDimension() != operand.getDimension()) {
            throw new IllegalArgumentException("Vectors must be of same sizes");
        }

        for(int i = this.getDimension() - 1; i >= 0; i--) {
            this.set(i, this.get(i) + operand.get(i));
        }

        return this;
    }

    @Override
    public IVector nAdd(IVector operand) {
        return this.copy().add(operand);
    }

    @Override
    public IVector sub(IVector subtrahend) {
        if(this.getDimension() != subtrahend.getDimension()) {
            throw new IllegalArgumentException("Vectors must be of same sizes");
        }

        for(int i = this.getDimension() - 1; i >= 0; i--) {
            this.set(i, this.get(i) - subtrahend.get(i));
        }

        return this;
    }

    @Override
    public IVector nSub(IVector subtrahend) {
        return this.copy().sub(subtrahend);
    }

    @Override
    public IVector scalarMultiply(double multiplier) {
       for(int i = this.getDimension() - 1; i >= 0; i--) {
           this.set(i, this.get(i) * multiplier);
       }

       return this;
    }

    @Override
    public IVector nScalarMultiply(double multiplier) {
        return this.copy().scalarMultiply(multiplier);
    }

    @Override
    public double norm() {
        double length = 0;

        for(int i = this.getDimension() - 1; i >= 0; i--) {
            length += Math.pow(this.get(i), 2);
        }

        return Math.sqrt(length);
    }

    @Override
    public IVector normalize() {
        double norm = norm();

        if(norm != 0) {
            for (int i = this.getDimension() - 1; i >= 0; i--) {
                this.set(i, this.get(i) / norm);
            }
        }

        return this;
    }

    @Override
    public IVector nNormalize() {
        return this.copy().normalize();
    }

    @Override
    public double cosine(IVector other) {
        return scalarProduct(other) / (this.norm() * other.norm());
    }

    @Override
    public double scalarProduct(IVector multiplier) {
        if(this.getDimension() != multiplier.getDimension()) {
            throw new IllegalArgumentException("Vectors must be of same sizes");
        }

        double product = 0;

        for(int i = this.getDimension() - 1; i >= 0; i--) {
            product += this.get(i) * multiplier.get(i);
        }

        return product;
    }

    @Override
    public IVector nVectorProduct(IVector multiplier) {
        if(this.getDimension() != multiplier.getDimension()) {
            throw new IllegalArgumentException("Vectors must be of same sizes");
        }

        if(this.getDimension() != 3) {
            throw new IllegalArgumentException("Vectors must be of size 3");

        }

        IVector vector = this.copy();

        vector.set(0, this.get(1) * multiplier.get(2) - this.get(2) * multiplier.get(1));
        vector.set(1, -(this.get(0) * multiplier.get(2) - this.get(2) * multiplier.get(0)));
        vector.set(2, this.get(0) * multiplier.get(1) - this.get(1) * multiplier.get(0));

        return vector;
    }

    @Override
    public IVector nFromHomogeneous() {
        IVector vector = this.copyPart(this.getDimension() - 1);
        for(int i = 0; i < vector.getDimension();  i++) {
            vector.set(i, vector.get(i) / this.get(this.getDimension() - 1));
        }

        return vector;
    }

    @Override
    public double[] toArray() {
        double[] array = new double[this.getDimension()];

        for(int i = 0; i < this.getDimension(); i++) {
            array[i] = this.get(i);
        }

        return array;
    }

    @Override
    public IVector copyPart(int vectorSize) {
        IVector vector = this.newInstance(vectorSize);

        for(int i = 0; i < vectorSize; i++) {
            if(i < this.getDimension()) {
                vector.set(i, this.get(i));

            } else {
                vector.set(i, 0);
            }
        }

        return vector;
    }

    @Override
    public String toString() {
        return toString(DEFAULT_PRECISION);
    }

    public String toString(int precision) {

        if(precision < 0) {
            throw new IllegalArgumentException("Precision must equal or greater than 0");
        }
        StringBuilder sb = new StringBuilder();

        sb.append("#.");

        for(int i = 0; i < precision; i++) {
            sb.append("#");
        }

        DecimalFormat df = new DecimalFormat(sb.toString());
        df.setRoundingMode(RoundingMode.HALF_UP);

        sb = new StringBuilder("[");

        for(int i = 0; i < this.getDimension(); i++) {
            sb.append(df.format(this.get(i)) + ", ");
        }

        sb.replace(sb.length() - 2, sb.length(), "]");

        return sb.toString();
    }

    @Override
    public IMatrix toRowMatrix(boolean liveView) {
        if(liveView == true) {
            return new MatrixVectorView(this, true);
        } else {
            double[][] elements = new double[1][this.getDimension()];
            elements[0] = Arrays.copyOf(this.toArray(), this.getDimension());

            return new Matrix(1, this.getDimension(), elements, false);
        }
    }

    @Override
    public IMatrix toColumnMatrix(boolean liveView) {
        if(liveView == true) {
            return new MatrixVectorView(this, false);
        } else {
            IMatrix matrix =  new Matrix(this.getDimension(), 1);

            for(int row = 0; row < this.getDimension(); ++row) {
                matrix.set(row, 0, this.get(row));
            }

            return matrix;
        }
    }

    public boolean lesserThan(double value) {
        for(int i = 0; i < this.getDimension(); i++) {
            if(this.get(i) > value) {
                return false;
            }
        }

        return true;
    }

    public boolean greaterThan(double value) {
        for(int i = 0; i < this.getDimension(); i++) {
            if(this.get(i) < value) {
                return false;
            }
        }

        return true;
    }

    public IVector absolute() {
        for(int i = 0; i < this.getDimension(); i++) {
            set(i, Math.abs(get(i)));
        }

        return this;
    }

    public IVector negative() {
        for(int i = 0; i < getDimension(); i++) {
            set(i, -get(i));
        }

        return this;
    }
}
