package kinematics;

import math.function.InverseKinematicFunction;
import math.function.SubFunction;
import math.matrix.IMatrix;
import math.matrix.Matrix;
import math.optimisation.Optimisation;
import math.vector.IVector;
import math.vector.Vector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class Manipulator {

    private List<Segment> segments;

    private IVector baseLocation;

    private List<SubFunction> angleRestrictions;

    private InverseKinematicFunction kinematicFunction;

    private static double T = 1;

    private static double EPSILON = 10E-6;

    public Manipulator(List<Segment> segments, IVector baseLocation) {
        this.segments = segments;
        this.baseLocation = baseLocation;

        kinematicFunction = new InverseKinematicFunction(this);
        angleRestrictions = kinematicFunction.createInequalities();
    }

    public IVector currentReach() {
        return jointLocations().get(jointLocations().size() - 1);
    }

    public IVector reachForGivenAngles(List<IVector> angleVectors) {
        List<IVector> joints = new LinkedList<>();
        joints.add(baseLocation);
        IVector currentPoint = baseLocation.copy();

        IMatrix transformationMatrix = Matrix.identityMatrix(4);

        int i = 0;
        for(Segment segment : segments) {
            IMatrix currentTransformation = Matrix.identityMatrix(4);

            IVector translationVector = segment.getTranslation();
            IVector startAngleVector = segment.getStartAngleVector();

            currentTransformation = currentTransformation.translate(translationVector);
            currentTransformation = currentTransformation.rotate(angleVectors.get(i));
            currentTransformation = currentTransformation.rotate(startAngleVector);

            transformationMatrix = currentTransformation.nMultiply(transformationMatrix);
            currentPoint = Vector.parseSimple("0 0 0 1").toRowMatrix(false).nMultiply(transformationMatrix).toVector(false);

            joints.add(currentPoint.copy());
            i++;
        }

        return joints.get(joints.size() - 1);
    }

    public void reachForPoint(IVector point) {
        kinematicFunction.setReachForPoint(point);
        IVector startingPoint = kinematicFunction.listAnglesToArray();
        IVector dxVector = Optimisation.initDxVector(0.5, startingPoint.getDimension());
        IVector optimisedAngleVectors = Optimisation.iterativeTransformation(kinematicFunction, startingPoint, angleRestrictions, new LinkedList<>(), T, dxVector, EPSILON);

        setNewAngleVectors(kinematicFunction.arrayToListAngles(optimisedAngleVectors));
    }

    public void setNewAngleVectors(List<IVector> angleVectors) {
        int i = 0;
        for(Segment segment : segments) {
            segment.setAngleVector(angleVectors.get(i));

            i++;
        }
    }

    public List<IVector> jointLocations() {
        List<IVector> joints = new LinkedList<>();
        joints.add(baseLocation);
        IVector currentPoint = baseLocation.copy();

        IMatrix transformationMatrix = Matrix.identityMatrix(4);

        for(Segment segment : segments) {
            IMatrix currentTransformation = Matrix.identityMatrix(4);

            IVector translationVector = segment.getTranslation();
            IVector angleVector = segment.getAngleVector();
            IVector startAngleVector = segment.getStartAngleVector();

            currentTransformation = currentTransformation.translate(translationVector);
            currentTransformation = currentTransformation.rotate(angleVector);
            currentTransformation = currentTransformation.rotate(startAngleVector);

            transformationMatrix = currentTransformation.nMultiply(transformationMatrix);
            currentPoint = Vector.parseSimple("0 0 0 1").toRowMatrix(false).nMultiply(transformationMatrix).toVector(false);

            joints.add(currentPoint.copy());
        }

        return joints;
    }

    public int size() {
        return segments.size();
    }

    public Segment getSegment(int index) {
        return segments.get(index);
    }

    /**
     * Every line must contain 10 doubles separated by empty spaces which represent:
     * starting rotation angles, min rotation angles, max rotation angles and segment length
     */
    public static Manipulator parse(String pathString) throws IOException {
        Path path = Paths.get(pathString);

        List<String> lines = Files.readAllLines(path);
        List<Segment> segments = new LinkedList<>();
        for(String line : lines) {
            IVector[] angleParams = new IVector[3];

            if(line == null || line.length() == 0) {
                continue;
            }

            String[] splits = line.split(" ");
            if(splits.length != 10) {
                throw new IllegalArgumentException("Every line must have 10 doubles");
            }

            for(int angleParam = 0; angleParam < 3; angleParam++) {
                angleParams[angleParam] = new Vector(3, false);

                for(int i = 0; i < 3; i++) {
                    double value = Double.parseDouble(splits[3 * angleParam + i]);
                    angleParams[angleParam].set(i, value);
                }
            }

            double length = Double.parseDouble(splits[9]);

            segments.add(new Segment(length, angleParams[0], angleParams[1], angleParams[2]));
        }

        return new Manipulator(segments, Vector.parseSimple("0 0 0 1"));
    }
}
