package math.vector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Vector extends AbstractVector {

    private double[] elements;

    private int dimension;

    private boolean readOnly;

    public Vector(int vectorSize, boolean readOnly) {
        elements = new double[vectorSize];
        dimension = vectorSize;
        this.readOnly = readOnly;
    }

    public Vector(double[] values) {
        elements = Arrays.copyOf(values, values.length);
        readOnly = false;
        dimension = elements.length;
    }

    public Vector(boolean readOnly, boolean valuesAvailable, double[] values) {
        this.readOnly = readOnly;

        if(valuesAvailable == true) {
            elements = values;

        } else {
            elements = Arrays.copyOf(values, values.length);
        }

        dimension = values.length;
    }

    @Override
    public double get(int index) {
        if(index >= dimension) {
            throw new IllegalArgumentException("Index must be in range of [0, sizeOfVector]");
        }

        return elements[index];
    }

    @Override
    public IVector set(int index, double value) {
        if(index >= dimension) {
            throw new IllegalArgumentException("Index must be in range of [0, sizeOfVector]");
        }

        elements[index] = value;

        return this;
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    @Override
    public IVector copy() {
        return new Vector(readOnly, false, elements);
    }

    @Override
    public IVector newInstance(int vectorSize) {
        if(vectorSize < 1) {
            throw new IllegalArgumentException("Size of vector cannot be lesser than 1");
        }
        return new Vector(new double[vectorSize]);
    }

    public static Vector parseSimple(String input) {
        String[] splits = input.split(" ");

        double[] values = new double[splits.length];

        for(int i = 0; i < splits.length; i++) {
            try {
                double value = Double.parseDouble(splits[i]);
                values[i] = value;

            } catch(NumberFormatException exc) {
                throw new IllegalArgumentException("Input must consist of double values separated by commas");
            }
        }

        return new Vector(values);
    }

    public static Vector parse(String path) throws IOException  {
        Path systemPath = Paths.get(path);

        if(Files.exists(systemPath) == false) {
            throw new IllegalArgumentException("File does not exist");
        }

        List<String> lines = Files.readAllLines(Paths.get(path));

        if(lines.size() != 1) {
            throw new IllegalArgumentException("File must only contain one line");
        }

        return Vector.parseSimple(lines.get(0));
    }

    @Override
    public IVector add(IVector operand) {
        if(readOnly == true) {
            throw new ReadOnlyException();
        }

        return super.add(operand);
    }

    @Override
    public IVector sub(IVector subtrahend) {
        if(readOnly == true) {
            throw new ReadOnlyException();
        }

        return super.sub(subtrahend);
    }

    @Override
    public IVector scalarMultiply(double multiplier) {
        if(readOnly == true) {
            throw new ReadOnlyException();
        }

        return super.scalarMultiply(multiplier);
    }

    @Override
    public IVector normalize() {
        if(readOnly == true) {
            throw new ReadOnlyException();
        }

        return super.normalize();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof  Vector)) {
            return false;
        }

        Vector other = (Vector) obj;

        return Arrays.equals(this.elements, other.elements);
    }
}
