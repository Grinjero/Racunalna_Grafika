package kinematics;

import math.matrix.IMatrix;
import math.vector.IVector;
import math.vector.Vector;

import java.util.List;

public class Segment {

    private double length;

    // Always 3 dimensional
    private IVector angleVector;

    private IVector startAngleVector;


    //  Always 3 dimensional, if rotation around certain axis
    //  is not allowed leave the value at 0 for min and max angles
    public IVector maxAngles;

    public IVector minAngles;

    public Segment(double length, IVector angleVector, IVector minAngles, IVector maxAngles) {
        this.length = length;
        this.angleVector = Vector.parseSimple("0 0 0");
        this.maxAngles = maxAngles;
        this.minAngles = minAngles;
        this.startAngleVector = angleVector;
    }

    public IVector getTranslation() {
        IVector translation = new Vector(3, false);
        return translation.set(0, length);
    }

    public IVector getAngleVector() {
        return angleVector;
    }

    public IVector shiftAngleVector(double xRot, double yRot, double zRot) {
        return angleVector.add(new Vector(new double[] { xRot, yRot, zRot}));
    }

    public IVector nShiftAngleVector(double xRot, double yRot, double zRot) {
        return angleVector.nAdd(new Vector(new double[] { xRot, yRot, zRot}));
    }

    public IVector getStartAngleVector() {
        return startAngleVector;
    }

    public void setAngleVector(IVector angleVector) {
        this.angleVector = angleVector;
    }
}
