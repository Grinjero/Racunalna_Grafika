package model;

import math.vector.IVector;
import math.vector.Vector;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Face3D {

    private int[] pointIndexes;

    private int currentSize;

    // Normal
    private IVector  coefficients;

    private boolean visible;

    public IVector polygonCenter;

    public Face3D(int size) {
        currentSize = 0;
        visible = true;
        pointIndexes = new int[size];
    }

    public Face3D(int[] pointIndexes, Vector coefficients) {
        this.pointIndexes = Arrays.copyOf(pointIndexes, pointIndexes.length);
        currentSize = pointIndexes.length;
        visible = true;
        if(coefficients != null) {
            this.coefficients = coefficients.copy();
        }
    }

    public boolean coefficientsSet() {
        return coefficients != null;
    }

    public boolean addNextIndex(int index, List<Vertex3D> points) {
        if(currentSize >= pointIndexes.length) {
            return false;
        }

        pointIndexes[currentSize] = index;
        ++currentSize;

        if(currentSize == pointIndexes.length) {
            calculateCoefficients(points);
        }

        return true;
    }

    /**
     * First is 1
     * @param index
     * @return
     */
    public int getElementAt(int index) {
        return pointIndexes[index] - 1;
    }

    public void calculateCoefficients(List<Vertex3D> points) {
        coefficients = new Vector(4, false);

        IVector pointOne = points.get(getElementAt(0)).getVector();
        IVector pointTwo = points.get(getElementAt(1)).getVector();
        IVector pointThree = points.get(getElementAt(2)).getVector();

        IVector normal = (pointTwo.nSub(pointOne)).nVectorProduct(pointThree.nSub(pointOne));

        double a = normal.get(0);
        double b = normal.get(1);
        double c = normal.get(2);

        coefficients.set(0, a);
        coefficients.set(1, b);
        coefficients.set(2, c);

        double d = -a * pointOne.get(0) - b * pointOne.get(1) - c * pointOne.get(2);

        coefficients.set(3, d);
    }

    public double calculateValue(Vector dot) {
        return dot.scalarProduct(coefficients);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("f");

        for(int i = 0; i < pointIndexes.length; i++) {
            sb.append(" " + pointIndexes[i]);
        }

        return sb.toString();
    }

    public int size() {
        return currentSize;
    }

    public void setVisibility(boolean visibility) {
        this.visible = visibility;
    }

    public IVector getCoefficients() {
        return coefficients;
    }

    public boolean isVisible() {
        return visible;
    }

    public List<IVector> getVertexVectors(List<Vertex3D> points) {
        List<IVector> vectors = new ArrayList<IVector>();

        for(int i = 0; i < pointIndexes.length; i++) {
            vectors.add(points.get(getElementAt(i)).getVector());
        }

        return vectors;
    }

    public List<Vertex3D> getVertices(List<Vertex3D> points) {
        List<Vertex3D> vectors = new ArrayList<Vertex3D>();

        for(int i = 0; i < pointIndexes.length; i++) {
            vectors.add(points.get(getElementAt(i)));
        }

        return vectors;
    }

    public boolean containsIndex(int index) {
        for(int i = 0; i < pointIndexes.length; i++) {
            if(pointIndexes[i] == index) {
                return true;
            }
        }
        return false;
    }
}
