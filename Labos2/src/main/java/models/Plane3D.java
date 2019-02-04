package models;

import math.vector.IVector;

public class Plane3D {

    public IVector vector1;

    public IVector vector2;

    public IVector point;

    private IVector normal;

    public Plane3D(IVector vector1, IVector vector2, IVector point) {
        this.vector1 = vector1;
        this.vector2 = vector2;
        this.point = point;
        this.normal = vector1.nVectorProduct(vector2);
    }

    public IVector getPoint(double param1, double param2) {
        return vector1.nScalarMultiply(param1).add(vector2.nScalarMultiply(param2));
    }

    public IVector getNormal() {
        return normal;
    }
}
