package math.function;

import kinematics.Manipulator;
import kinematics.Segment;
import math.vector.IVector;
import math.vector.Vector;

import java.util.LinkedList;
import java.util.List;

public class InverseKinematicFunction extends AbstractFunction {

    public Manipulator manipulator;

    public IVector reachForPoint;

    public InverseKinematicFunction(Manipulator manipulator) {
        this.manipulator = manipulator;
    }

    public void setReachForPoint(IVector reachForPoint) {
        this.reachForPoint = reachForPoint;
    }


    @Override
    protected double valueAt(IVector element) {
        List<IVector> angles = arrayToListAngles(element);

        IVector reach = manipulator.reachForGivenAngles(angles);
        double distance = euclidDistance(reach);

        return distance;
    }

    private double euclidDistance(IVector reach) {
        double distance = 0;

        for(int i = 0; i < reachForPoint.getDimension(); i++) {
            distance += Math.pow(reach.get(i) - reachForPoint.get(i), 2);
        }

        return Math.sqrt(distance);
    }

    @Override
    public int getInputDimension() {
        return manipulator.size() * 3;
    }

    public IVector listAnglesToArray() {
        IVector angleVectors = new Vector(manipulator.size() * 3, false);

        for(int i = 0; i < manipulator.size(); i++) {
            Segment segment = manipulator.getSegment(i);
            IVector angleVector = segment.getAngleVector();

            for(int j = 0; j < 3; j++) {
                angleVectors.set(i * 3 + j, angleVector.get(j));
            }
        }

        return angleVectors;
    }

    public List<IVector> arrayToListAngles(IVector element) {
        List<IVector> angles = new LinkedList<>();

        for(int i = 0; i < element.getDimension() / 3; i++) {
            IVector angleVector = new Vector(3, false);

            for(int j = 0; j < 3; j++) {
                angleVector.set(j, element.get(i * 3 + j));
            }

            angles.add(angleVector);
        }

        return angles;
    }

    public List<SubFunction> createInequalities() {
        List<SubFunction> inequalities = new LinkedList<>();

        for(int i = 0; i < manipulator.size(); i++) {
            Segment segment = manipulator.getSegment(i);

            for(int j = 0; j < 3; j++) {
                double lowerAngleLimit = segment.minAngles.get(j);
                final int segmentIndex = i;
                final int axisIndex = j;
                SubFunction lowerLimit = new SubFunction() {
                    @Override
                    public double valueAt(IVector elements) {
                        return elements.get(3 * segmentIndex + axisIndex) - lowerAngleLimit;
                    }
                };

                double upperAngleLimit = segment.maxAngles.get(j);
                SubFunction upperLimit = new SubFunction() {
                    @Override
                    public double valueAt(IVector elements) {
                        return -elements.get(3 * segmentIndex + axisIndex) + upperAngleLimit;
                    }
                };

                inequalities.add(lowerLimit);
                inequalities.add(upperLimit);
            }
        }

        return inequalities;
    }
}
