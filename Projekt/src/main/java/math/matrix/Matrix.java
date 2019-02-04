package math.matrix;

import math.util.Util;
import math.vector.IVector;
import math.vector.Vector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Matrix extends AbstractMatrix {

    /**
     * [rows][cols]
     */
    private double[][] elements;

    private int rows;

    private int cols;

    public int[] rowPermutation;

    public int[] colPermutation;

    private static final double EPSILON = 10E-6;

    public void initPermutations() {
        rowPermutation = new int[this.getRowsCount()];
        for(int i = 0; i < this.getRowsCount(); i++) {
            rowPermutation[i] = i;
        }

        colPermutation = new int[this.getColsCount()];
        for(int i = 0; i < this.getColsCount(); i++) {
            colPermutation[i] = i;
        }
    }

    public Matrix(int rows, int cols) {
        if(rows < 1 || cols < 1) {
            throw new IllegalArgumentException("Number columns and rows cannot be lower than 1");
        }

        elements = new double[rows][cols];
        this.rows = rows;
        this.cols = cols;

        initPermutations();
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
        initPermutations();
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
        return elements[rowPermutation[row]][colPermutation[column]];
    }

    public void switchRows(int row1, int row2) {
        if(row1 < 0 || row1 >= rows || row2 < 0 || row2 >= rows) {
            throw new IllegalArgumentException("Row index is lesser than zero or exceeds the max row");
        }

        int temp = rowPermutation[row2];
        rowPermutation[row2] = rowPermutation[row1];
        rowPermutation[row1] = temp;
    }

    public void switchCols(int col1, int col2) {
        if(col1 < 0 || col1 >= cols || col2 < 0 || col2 >= cols) {
            throw new IllegalArgumentException("Column index is lesser than zero or exceeds the max column");
        }

        rowPermutation[col1] = col2;
        rowPermutation[col2] = col1;
    }

    public IMatrix set(int row, int column, double value) {
        if(row < 0 || row >= rows) {
            throw new IllegalArgumentException("Row index is lesser than zero or exceeds the max row");
        }

        if(column < 0 || column >= cols) {
            throw new IllegalArgumentException("Column index is lesser than zero or exceeds the max column");
        }
        elements[rowPermutation[row]][colPermutation[column]] = value;

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

    public static Matrix parse(String path) throws IOException {
        Path systemPath = Paths.get(path);

        if(Files.exists(systemPath) == false) {
            throw new IllegalArgumentException("File does not exist");
        }

        List<String> lines = Files.readAllLines(Paths.get(path));

        int num_cols = lines.get(0).split("\\s+").length;

        double[][] elements = new double[lines.size()][num_cols];

        int row = 0;
        for(String line : lines) {
            String[] splits = line.split("\\s+");

            if(splits.length != num_cols) {
                throw new IllegalArgumentException("All rows must have an equal number of elements");
            }

            for(int col = 0; col < splits.length; col++) {
                try {
                    elements[row][col] = Double.parseDouble(splits[col]);
                } catch(NumberFormatException exc) {
                    throw new IllegalArgumentException(splits[col] + " cannot be parsed as a Double");
                }
            }

            ++row;
        }

        return new Matrix(row, num_cols, elements, true);
    }


    /**
     * Method assumes that this is an L/U matrix, hence diagonal elements will be treated as ones and upper right
     * elements will be treated as zeroes.
     * @param freeVectorB
     * @return solution of the system, vector Y
     */
    public Matrix forwardSupstitution(Matrix freeVectorB) {
        if(this.getColsCount() != this.getRowsCount()) {
            throw new IllegalStateException("This must be a square matrix.");
        }
        if(freeVectorB.getRowsCount() != this.getRowsCount()) {
            throw new IllegalArgumentException("Length of the vector must be equal to the dimension of the this matrix");
        }

        Matrix vectorY = freeVectorB.copyWithPermutation();

        for(int i = 0; i < this.getRowsCount(); ++i) {

            for(int j = i + 1; j < this.getRowsCount(); ++j) {
                double value = vectorY.get(j, 0) - this.get(j, i) * vectorY.get(i, 0);
                vectorY.set(j, 0, value);
            }


        }
        System.out.println("Vector y: \n" + vectorY);
        return vectorY;
    }

    /**
     * Method assumes that this is an L/U matrix, hence lower left elements will be treated as zeroes
     * @param freeVectorY
     * @return solution of the system, vector X
     */
    public Matrix backwardSupstitution(Matrix freeVectorY) {
        if(this.getColsCount() != this.getRowsCount()) {
            throw new IllegalStateException("This must be a square matrix.");
        }
        if(freeVectorY.getRowsCount() != this.getRowsCount()) {
            throw new IllegalArgumentException("Length of the vector must be equal to the dimension of the this matrix");
        }

        Matrix vectorX = freeVectorY.copyWithPermutation();
        for(int i = this.getRowsCount() - 1; i >= 0; i--) {
            double value = vectorX.get(i, 0) / this.get(i, i);
            vectorX.set(i, 0, value);

            for(int j = 0; j < i ; j++) {
                value = vectorX.get(j, 0) - this.get(j, i) * vectorX.get(i, 0);
                vectorX.set(j, 0, value);
            }
        }
        System.out.println("Vector x: \n" + vectorX);
        return vectorX;
    }

    public Matrix copyWithPermutation() {
        Matrix newMatrix = (Matrix) this.copy();
        newMatrix.rowPermutation = Arrays.copyOf(this.rowPermutation, this.rowPermutation.length);
        newMatrix.colPermutation = Arrays.copyOf(this.colPermutation, this.colPermutation.length);

        return newMatrix;
    }

    @Override
    public IMatrix nInvert() {
        if(this.getColsCount() != this.getRowsCount()) {
            throw new IllegalArgumentException("Must be square matrix for inversion");
        }

        double determinant = this.determinant();
        if(determinant == 0) {
            throw new IllegalArgumentException("Inverse of this matrix is not possible (determinant is equal to 0");
        }

        IMatrix inverse = new Matrix(getColsCount(), getColsCount());
        for(int col = 0; col < getColsCount(); col++) {
            IVector freeVectorB = new Vector(getRowsCount(), false);

            for(int i = 0; i < getRowsCount(); i++) {
                if(i == col) {
                    freeVectorB.set(i, 1);
                } else {
                    freeVectorB.set(i, 0);
                }
            }

            Matrix inCol = this.decomposition(true, (Matrix) (freeVectorB.toColumnMatrix(false)), EPSILON, false);

            for(int i = 0; i < getRowsCount(); i++) {
                inverse.set(i, col, inCol.get(i, 0));
            }
        }

        return inverse;
    }

    public Matrix decomposition(boolean luOrLup, Matrix freeVectorB, double epsilon, boolean regularize) {
        if(freeVectorB == null) {
            throw new IllegalArgumentException("Free vector must be given");
        }

        Matrix tempVectorB = freeVectorB.copyWithPermutation();

        Matrix luMatrix = findLUMatrix(luOrLup, epsilon, tempVectorB, regularize);

        Matrix vectorY = luMatrix.forwardSupstitution(tempVectorB);


        Matrix vectorX = luMatrix.backwardSupstitution(vectorY);

        freeVectorB.initPermutations();
        return vectorX;
    }

    public Matrix findLUMatrix(boolean luOrLup, double epsilon, Matrix vectorB, boolean regularize) {
        if(this.getRowsCount() != this.getColsCount()) {
            throw new IllegalArgumentException("This must be a square matrix");
        }

        Matrix luMatrix =  copyWithPermutation();

        if(regularize == true) {
            regularize(luMatrix, vectorB);
        }

        for(int i = 0; i < this.getRowsCount() - 1; i++) {
            for(int j = i + 1; j < this.getRowsCount(); j++) {
                if(luOrLup == true) {
                    permutateForPivot(i, luMatrix, vectorB);
                }

                if( Util.doubleEquals(luMatrix.get(i, i), 0, epsilon) == true) {
                    if(luOrLup == false) {
                        System.out.println("Dividing by zero, switching to LUP");
                        vectorB.initPermutations();

                        return this.findLUMatrix(true, epsilon, vectorB, regularize);
                    } else {
                        throw new IllegalArgumentException("LUP decomposition not possible, dividing by zero");
                    }
                }

                double value = luMatrix.get(j, i) / luMatrix.get(i, i);
                luMatrix.set(j, i, value, epsilon);

                for(int k = i + 1; k < this.getRowsCount(); k++) {
                    value = luMatrix.get(j, k) - luMatrix.get(j, i) * luMatrix.get(i, k);
                    luMatrix.set(j, k, value, epsilon);
                }
            }
        }

        if(Util.doubleEquals(luMatrix.get(getRowsCount() - 1, getRowsCount() - 1), 0, epsilon) == true) {
            throw new IllegalArgumentException("Matrix is singular");
        }

        System.out.println("LU Matrix: \n" + luMatrix);
        return luMatrix;
    }

    public void regularize(Matrix luMatrix, Matrix vectorB) {
        for(int row = 0; row < getRowsCount(); ++row) {
            boolean minSet = false;
            double min = 0;
            for(int col = 0; col < getColsCount(); ++col) {
                if(minSet == false) {
                    min = Math.abs(luMatrix.get(row, col));
                } else if(min > Math.abs(luMatrix.get(row, col))){
                    min = Math.abs(luMatrix.get(row, col));
                }
            }

            if(min > Math.abs(vectorB.get(row, 0))) {
                min = Math.abs(vectorB.get(row, 0));
            }

            for(int col = 0; col < getColsCount(); ++col) {
                luMatrix.set(row, col, luMatrix.get(row, col) / min);
            }

            vectorB.set(row, 0, vectorB.get(row, 0));
        }
    }

    public void set(int row, int col, double value, double epsilon) {
        if(Math.abs(value) < epsilon) {
            set(row, col, 0);
        } else {
            set(row, col, value);
        }
    }

    private void permutateForPivot(int row, Matrix luMatrix, Matrix vectorB) {
        int pivot = row;
        for(int j = row + 1; j < this.getRowsCount(); j++) {
            if (Math.abs(luMatrix.get(j, row)) > Math.abs(luMatrix.get(pivot, row))) {
                pivot = j;
            }
        }

        luMatrix.switchRows(pivot, row);

        if(vectorB != null) {
            vectorB.switchRows(pivot, row);
        }
    }

    public static void rungeKutta(Matrix a, IVector x, double integrationStep, double maxTime, int printPeriod, String fileName) throws IOException {
        x = x.copy();
        IMatrix xMatrix = x.toColumnMatrix(false);
        List<IMatrix> xs = new ArrayList<>();

        double t = 0;
        int iteration = 0;
        while(t < maxTime) {
            IMatrix m1 = a.nMultiply(xMatrix);
            IMatrix m2 = a.nMultiply(xMatrix.nAdd(m1.nMultiplyDouble(integrationStep * 0.5)));
            IMatrix m3 = a.nMultiply(xMatrix.nAdd(m2.nMultiplyDouble(integrationStep * 0.5)));
            IMatrix m4 = a.nMultiply(xMatrix.nAdd(m3.nMultiplyDouble(integrationStep)));

            IMatrix update = m1.add(m2.multiplyDouble(2d));
            update.add(m3.multiplyDouble(2d));
            update.add(m4);
            update.multiplyDouble(integrationStep / 6);
            xMatrix.add(update);

            if(iteration % printPeriod == 0) {
                System.out.println("t = " + t + ", X = " + xMatrix.toVector(false));
            }

            xs.add(xMatrix.copy());

            ++iteration;
            t += integrationStep;
        }

        writeToFile(xs, maxTime, integrationStep, 1000, fileName);
    }

    public static void trapeze(Matrix a, IVector x, double integrationStep, double maxTime, int printPeriod, String fileName) throws IOException  {
        IMatrix r = new Matrix(a.getRowsCount(), a.getColsCount());
        for(int i = 0; i < r.getRowsCount(); i++) {
            r.set(i, i, 1);
        }

        IMatrix rLeft = r.nSub(a.nMultiplyDouble(integrationStep / 2));
        IMatrix rRight = r.nAdd(a.nMultiplyDouble(integrationStep / 2));

        r = rLeft.nInvert().nMultiply(rRight);

        x = x.copy();
        IMatrix xMatrix = x.toColumnMatrix(false);
        List<IMatrix> xs = new ArrayList<>();

        double t = 0;
        int iteration = 0;
        while(t < maxTime) {

            xMatrix = r.nMultiply(xMatrix);

            if(iteration % printPeriod == 0) {
                System.out.println("t = " + t + ", X = " + xMatrix.toVector(false));
            }

            xs.add(xMatrix.copy());

            ++iteration;
            t += integrationStep;
        }

        writeToFile(xs, maxTime, integrationStep, 1000, fileName);
    }

    private static void writeToFile(List<IMatrix> xs, double maxTime, double integrationStep, int numLines, String file) throws IOException {
        int writePeriod = (int) ((double) xs.size() / numLines);

        FileWriter fw = null;
        Path path = Paths.get("src/main/resources/results/" + file);

        if(Files.exists(path)) {
            return;
        }

        try {
            fw = new FileWriter(new File("src/main/resources/results/" + file));

            double t = 0;

            StringBuilder sb = new StringBuilder();

            sb.append("[ ");
            for(IMatrix x : xs) {
                sb.append("[ " + t + ", ");

                for(int row = 0; row < x.getRowsCount(); row++) {
                    sb.append(x.get(row, 0) + " ");

                    if(row == x.getRowsCount() - 1) {
                        sb.append("],\n");
                    } else {
                        sb.append(", ");
                    }
                }

                t += integrationStep;
            }
            sb.replace(sb.length() - 2, sb.length(), "]" );
            fw.write(sb.toString());
            fw.flush();
        } catch(IOException exc) {
            System.out.println("Writing to file error");
            return;
        } finally {
            if(fw != null) {
                fw.close();
            }
        }
    }

    public static IMatrix identityMatrix(int dimensions) {
        IMatrix matrix = new Matrix(dimensions, dimensions);

        for(int i = 0; i < dimensions; i++) {
            matrix.set(i, i, 1);
        }

        return matrix;
    }
}
