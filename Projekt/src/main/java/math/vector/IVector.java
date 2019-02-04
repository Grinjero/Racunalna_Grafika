package math.vector;

import math.matrix.IMatrix;

public interface IVector {

    double get(int index);

    IVector set(int index, double value);

    int getDimension();

    IVector copy();

    IVector copyPart(int vectorSize);

    IVector newInstance(int vectorSize);

    IVector add(IVector operand);

    IVector nAdd(IVector operand);

    IVector sub(IVector subtrahend);

    IVector nSub(IVector subtrahend);

    IVector scalarMultiply(double multiplier);

    IVector nScalarMultiply(double multiplier);

    double norm();

    IVector normalize();

    IVector nNormalize();

    double cosine(IVector other);

    double scalarProduct(IVector multiplier);

    IVector nVectorProduct(IVector multiplier);

    IVector nFromHomogeneous();

    IMatrix toRowMatrix(boolean liveView);

    IMatrix toColumnMatrix(boolean liveView);

    double[] toArray();

    boolean lesserThan(double value);

    boolean greaterThan(double value);

    IVector negative();

    IVector absolute();
}
