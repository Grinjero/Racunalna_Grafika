package models;

import math.vector.IVector;
import math.vector.Vector;

public class Vertex3D {

    private IVector point;

    private IVector gouradNormal;

    public Vertex3D(double x, double y, double z) {
        this.point = new Vector(new double[]{x, y, z});
    }

    public Vertex3D(IVector point) {
        this.point = point;
    }

    public double getElementAt(int index) {
        return point.get(index);
    }

    public IVector getVector() {
        return point;
    }

    public Vertex3D copy() {
        return new Vertex3D(point.copy());
    }

    @Override
    public String toString() {
        return "v " + point.get(0) + " " + point.get(1) + " " + point.get(2);
    }

    public void setNormalVector(IVector normalVector) {
        gouradNormal = normalVector.normalize();
    }

    public IVector getNormalVector() {
        if(gouradNormal == null) {
            throw new IllegalArgumentException("Normal not initialized");
        }
        return gouradNormal;
    }

    public boolean isNormalVectorSet() {
       return gouradNormal != null;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof  Vertex3D)) {
            return false;
        }

        Vertex3D other = (Vertex3D) obj;

        return this.point.equals(other);
    }
}
