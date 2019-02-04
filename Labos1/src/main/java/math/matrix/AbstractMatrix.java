package math.matrix;

import math.vector.IVector;
import math.vector.Vector;
import math.vector.VectorMatrixView;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static math.util.Util.createElements;
import static math.util.Util.removeElementFromArray;

public abstract class AbstractMatrix implements IMatrix {

    @Override
    public IMatrix nTranspose(boolean liveView) {
        if(liveView != true) {
            IMatrix matrix = this.newInstance(this.getColsCount(), this.getRowsCount());

            for(int row = 0; row < this.getColsCount(); row++) {
                for(int col = 0; col < this.getRowsCount(); col++) {
                    matrix.set(row, col, this.get(col, row));
                }
            }

            return matrix;

        } else {
            return new MatrixTransposeView(this);
        }
    }

    @Override
    public IMatrix add(IMatrix other) {
        if(this.getRowsCount() != other.getRowsCount() || this.getColsCount() != other.getColsCount()) {
            throw new IllegalArgumentException("Matrices must be of same sizes");
        }

        for(int row = 0; row < this.getRowsCount(); row++) {
            for(int col = 0; col < this.getColsCount(); col++) {
                this.set(row, col, this.get(row, col) + other.get(row, col));
            }
        }

        return this;
    }

    @Override
    public IMatrix nAdd(IMatrix other) {
        return this.copy().add(other);
    }

    @Override
    public IMatrix sub(IMatrix other) {
        if(this.getRowsCount() != other.getRowsCount() || this.getColsCount() != other.getColsCount()) {
            throw new IllegalArgumentException("Matrices must be of same sizes");
        }

        for(int row = 0; row < this.getRowsCount(); row++) {
            for(int col = 0; col < this.getColsCount(); col++) {
                this.set(row, col, this.get(row, col) - other.get(row, col));
            }
        }

        return this;
    }

    @Override
    public IMatrix nSub(IMatrix other) {
        return this.copy().sub(other);
    }

    @Override
    public IMatrix nMultiply(IMatrix other) {
        if(this.getColsCount() != other.getRowsCount()) {
            throw new IllegalArgumentException("Matrices cannot be used in multiplication with these dimensions");
        }

        IMatrix matrix = this.newInstance(this.getRowsCount(), other.getColsCount());

        for(int row = 0; row < matrix.getRowsCount(); row++) {
            for(int col = 0; col < matrix.getColsCount(); col++) {
                matrix.set(row, col, calculateField(row, col, other));
            }
        }

        return matrix;
    }

    private double calculateField(int row, int col, IMatrix other) {
        double sum = 0;
        for(int k = 0; k < this.getColsCount(); k++) {
            sum += this.get(row, k) * other.get(k, col);
        }

        return sum;
    }

    @Override
    public double determinant() {
        if(this.getColsCount() != this.getRowsCount()) {
            throw new IllegalArgumentException("Matrix must be a square matrix");
        }

        if(this.getColsCount() == 2) {
            return this.get(0, 0) * this.get(1, 1) - this.get(0, 1) * this.get(1, 0);
        }

        if(this.getRowsCount() == 1) {
            return this.get(0, 0);
        }

        double sum = 0;
        boolean plusOrMinus = true;
        for(int col = 0; col < this.getColsCount(); ++col) {

            IMatrix subMatrix = new MatrixSubMatrixView(this, 0, col);

            if (plusOrMinus == true) {
                sum += this.get(0, col) * subMatrix.determinant();
                plusOrMinus = false;
            } else {
                sum -= this.get(0, col) * subMatrix.determinant();
                plusOrMinus = true;
            }
        }

        return sum;
    }

    @Override
    public IMatrix subMatrix(int row, int column, boolean liveView) {
        if(liveView) {
            return new MatrixSubMatrixView(this, row, column);

        } else {
            int[] colsRemaining = new int[this.getColsCount() - 1];
            removeElementFromArray(colsRemaining, column);

            int[] rowsRemaining = new int[this.getRowsCount() - 1];
            removeElementFromArray(rowsRemaining, row);
            return new Matrix(rowsRemaining.length, colsRemaining.length, createElements(rowsRemaining, colsRemaining, this), true);
        }
    }

    @Override
    public IMatrix nInvert() {
        if(this.getColsCount() != this.getRowsCount()) {
            throw new IllegalArgumentException("Must be square matrix for inversion");
        }

        double determinant = this.determinant();
        if(determinant == 0) {
            throw new IllegalArgumentException("Inverse of this matrix is not possible");
        }

        IMatrix matrix = newInstance(this.getRowsCount(), this.getColsCount());
        for(int row = 0; row < this.getRowsCount(); ++row) {
            for(int col = 0; col < this.getColsCount(); ++col) {
                if((col + row) % 2 == 0) {
                    matrix.set(row, col, subMatrix(row, col, true).determinant());
                } else {
                    matrix.set(row, col, -subMatrix(row, col, true).determinant());
                }
            }
        }

        return matrix.nTranspose(true).multiplyDouble(1/determinant);
    }

    @Override
    public IMatrix multiplyDouble(double multiplier) {
        for(int row = 0; row < this.getRowsCount(); ++row) {
            for(int col = 0; col < this.getColsCount(); ++col) {
                set(row, col, get(row, col) * multiplier);
            }
        }

        return this;
    }

    @Override
    public double[][] toArray() {
        return createElements(this);
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

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.GERMAN);
        symbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat(sb.toString(), symbols);

        df.setRoundingMode(RoundingMode.HALF_UP);

        sb = new StringBuilder();
        for(int row = 0; row < this.getRowsCount(); row++) {

            sb.append("[");

            for(int col = 0; col < this.getColsCount(); col++) {
                sb.append(df.format(this.get(row, col)) + ", ");
            }
            sb.replace(sb.length() - 2, sb.length(), "]\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toString(3);
    }

    @Override
    public IVector toVector(boolean liveView) {
        if(this.getColsCount() != 1 && this.getRowsCount() != 1) {
            throw new IllegalArgumentException("Cannot create a vector from a matrix that is not a single row or a single column matrix");
        }

        if(liveView == true) {
            return new VectorMatrixView(this);
        }

        if(this.getRowsCount() == 1) {
            return new Vector(false, false, this.toArray()[0]);
        } else {
            double[] column = new double[this.getRowsCount()];

            for(int row = 0; row < this.getRowsCount(); ++row) {
                column[row] = this.get(row, 0);
            }

            return new Vector(false, false, column);
        }
    }
}
