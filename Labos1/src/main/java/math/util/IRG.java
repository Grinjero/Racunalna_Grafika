package math.util;

import math.matrix.IMatrix;
import math.matrix.Matrix;
import math.vector.IVector;
import math.vector.Vector;

import java.util.ArrayList;
import java.util.List;

public class IRG {

    public static IMatrix createIdentityMatrix(int size) {
        Matrix matrix = new Matrix(size, size);

        for(int i = 0; i < size; i++) {
            matrix.set(i, i, 1);
        }

        return matrix;
    }

    public static IMatrix translate3D(float dx, float dy, float dz) {
        IMatrix matrix = createIdentityMatrix(4);

        matrix.set(3, 0, dx);
        matrix.set(3, 1, dy);
        matrix.set(3, 2, dz);

        return matrix;
    }

    public static IMatrix scale3D(float sx, float sy, float sz) {
        IMatrix matrix = createIdentityMatrix(4);

        matrix.set(0,0, sx);
        matrix.set(1, 1, sy);
        matrix.set(2, 2, sz);

        return matrix;
    }

    public static IMatrix lookAtMatrix(IVector eye, IVector center, IVector viewUp) {
        IMatrix orientation = createIdentityMatrix(4);

        IVector zAxis = (eye.nSub(center)).normalize();
        IVector xAxis = (viewUp.nVectorProduct(zAxis)).normalize();
        IVector yAxis = zAxis.nVectorProduct(xAxis);

        for(int row = 0; row < eye.getDimension(); row++) {
            orientation.set(row, 0, xAxis.get(row));
            orientation.set(row, 1, yAxis.get(row));
            orientation.set(row, 2, zAxis.get(row));
        }

        orientation.set(3, 0, -xAxis.scalarProduct(eye));
        orientation.set(3, 1, -yAxis.scalarProduct(eye));
        orientation.set(3, 2, -zAxis.scalarProduct(eye));

        return orientation;
    }

    public static IMatrix buildFrustumMatrix(double l, double r, double b, double t, int n, int f) {
        Matrix matrix = new Matrix(4, 4);

        matrix.set(0, 0, 2 * n / (r - l));
        matrix.set(1, 1, 2 * n / (t - b));
        matrix.set(2, 0, (r + l) / (r - l));
        matrix.set(2, 1, (t + b) / (t - b));
        matrix.set(2, 2, -(f + n) / (f - n));
        matrix.set(2, 3, -1);
        matrix.set(3, 2, -2 * f * n /(f - n));

        return matrix;
    }

    /**
     *
     * @param angle given in degrees
     * @param radius
     * @param y
     * @return
     */
    public static IVector calculatePointOnCircle(double angle, double radius, double y) {
        angle = Math.toRadians(angle);
        double x = Math.cos(angle) * radius;
        double z = Math.sin(angle) * radius;

        return new Vector(new double[] {x, y, z});
    }

    public static boolean isAntiClockwise(List<IVector> vertexes, IMatrix transformation) {
        List<IVector> transformedVertexes = new ArrayList<IVector>();

        for(int i = 0; i < vertexes.size(); i++) {
            IVector transformedVector = vertexes.get(i).copyPart(4);
            transformedVector.set(3, 1);

            transformedVector = transformedVector.toRowMatrix(false).nMultiply(transformation).toVector(false);
            transformedVertexes.add(transformedVector);
        }

        double sum = 0;

        for(int i = 0; i < transformedVertexes.size(); i++) {
            double x1 = transformedVertexes.get(i).get(0);
            double y1 = transformedVertexes.get(i).get(1);

            IVector other = null;

            if(i == transformedVertexes.size() - 1) {
                other = transformedVertexes.get(0);
            } else {
                other = transformedVertexes.get(i + 1);
            }

            double x2 = other.get(0);
            double y2 = other.get(1);

            sum += (x2 - x1) * (y2 + y1);
        }

        return sum < 0;
    }

    public static IVector arithmeticMiddleOfVectors(List<IVector> vectors) {
        IVector middle = new Vector(vectors.get(0).getDimension(), false);

        for(IVector vector : vectors) {
            for(int i = 0; i < vector.getDimension(); i++) {
                middle.set(i, middle.get(i) + vector.get(i));
            }
        }

        for(int i = 0; i < middle.getDimension(); i++) {
            middle.set(i, middle.get(i) / vectors.size());
        }

        return middle;
    }
}
