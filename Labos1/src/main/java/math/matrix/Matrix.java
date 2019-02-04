package math.matrix;

import java.util.Arrays;

public class Matrix extends AbstractMatrix {

    /**
     * [rows][cols]
     */
    private double[][] elements;

    private int rows;

    private int cols;

    public Matrix(int rows, int cols) {
        if(rows < 1 || cols < 1) {
            throw new IllegalArgumentException("Number columns and rows cannot be lower than 1");
        }

        elements = new double[rows][cols];
        this.rows = rows;
        this.cols = cols;
    }

    public Matrix(int rows, int cols, double[][] elements, boolean elementsAvailable) {
        if(rows < 1 || cols < 1) {
            throw new IllegalArgumentException("Number columns and rows cannot be lower than 1");
        }

        if(rows != elements.length || cols != elements[0].length) {
            throw new IllegalArgumentException("Given number of rows and columns must correspond to the size of given elements");
        }

        if(elementsAvailable == true) {
            this.elements = elements;

        } else {
            this.elements = new double[rows][cols];

            for(int i = 0; i < rows; i++) {
                this.elements[i] = Arrays.copyOf(elements[i], elements[i].length);
            }
        }

        this.rows = rows;
        this.cols = cols;
    }

    public IMatrix copy() {
        return new Matrix(rows, cols, elements, false);
    }

    public IMatrix newInstance(int rows, int columns) {
        return new Matrix(rows, columns);
    }

    public int getRowsCount() {
        return rows;
    }

    public int getColsCount() {
        return cols;
    }


    public double get(int row, int column) {
        if(row < 0 || row >= rows) {
            throw new IllegalArgumentException("Row index is lesser than zero or exceeds the max row");
        }

        if(column < 0 || column >= cols) {
            throw new IllegalArgumentException("Column index is lesser than zero or exceeds the max column");
        }
        return elements[row][column];
    }

    public IMatrix set(int row, int column, double value) {
        if(row < 0 || row >= rows) {
            throw new IllegalArgumentException("Row index is lesser than zero or exceeds the max row");
        }

        if(column < 0 || column >= cols) {
            throw new IllegalArgumentException("Column index is lesser than zero or exceeds the max column");
        }
        elements[row][column] = value;

        return this;
    }

    public static IMatrix parseSimple(String input) {
        String[] rows = input.split("\\|");

        String[] rowElements = rows[0].trim().split(" ");

        double[][] newElements = new double[rows.length][rowElements.length];

        for(int row = 0; row < rows.length; ++row) {

            rowElements = rows[row].trim().split(" ");

            for(int col = 0; col < rowElements.length; ++col) {
                try {
                    double value = Double.parseDouble(rowElements[col]);
                    newElements[row][col] = value;

                } catch(NumberFormatException exc) {
                    throw new IllegalArgumentException("Input must consist of double values separated by commas");
                }
            }
        }

        return new Matrix(rows.length, rowElements.length, newElements, true);
    }
}
