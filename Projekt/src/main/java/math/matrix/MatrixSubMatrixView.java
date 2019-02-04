package math.matrix;

import java.util.Arrays;

import static math.util.Util.createElements;
import static math.util.Util.removeElementFromArray;

public class MatrixSubMatrixView extends AbstractMatrix {

    private int[] rowIndexes;

    private int[] colIndexes;

    private IMatrix original;

    public MatrixSubMatrixView(IMatrix original, int rowRemove, int colRemove) {
        this.original = original;

        colIndexes = new int[original.getColsCount() - 1];
        removeElementFromArray(colIndexes, colRemove);

        rowIndexes = new int[original.getRowsCount() - 1];
        removeElementFromArray(rowIndexes, rowRemove);
    }

    private MatrixSubMatrixView(IMatrix original, int[] rowsRemaining, int[] colsRemaining) {
        Arrays.sort(rowsRemaining);
        Arrays.sort(colsRemaining);

        rowIndexes = rowsRemaining;
        colIndexes = colsRemaining;
        this.original = original;
    }

    @Override
    public int getColsCount() {
        return colIndexes.length;
    }

    @Override
    public int getRowsCount() {
        return rowIndexes.length;
    }

    @Override
    public double get(int row, int column) {
        return original.get(rowIndexes[row], colIndexes[column]);
    }

    @Override
    public IMatrix set(int row, int column, double value) {
        original.set(rowIndexes[row], colIndexes[column], value);
        return this;
    }

    @Override
    public IMatrix copy() {
        return new MatrixSubMatrixView(original, rowIndexes, colIndexes);
    }

    @Override
    public IMatrix subMatrix(int row, int column, boolean liveView) {
        int[] colsRemaining = Arrays.copyOf(colIndexes, colIndexes.length);
        int[] rowsRemaining = Arrays.copyOf(rowIndexes, rowIndexes.length);

        removeElementFromArray(colsRemaining, column);
        removeElementFromArray(rowsRemaining, row);

        if(liveView == true) {
            return new MatrixSubMatrixView(original, rowsRemaining, colsRemaining);

        } else {
            return new Matrix(rowIndexes.length - 1, colIndexes.length - 1, createElements(rowsRemaining, colsRemaining, this), true);
        }
    }

    @Override
    public IMatrix newInstance(int rows, int columns) {
        return new Matrix(rows, columns);
    }
}
