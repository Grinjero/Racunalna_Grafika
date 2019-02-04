package curve;

import math.matrix.AbstractMatrix;
import math.matrix.IMatrix;
import math.matrix.Matrix;
import math.vector.IVector;
import math.vector.Vector;


import java.util.Iterator;
import java.util.List;

public class BSplineCurve {

    private List<IVector> points;

    private IMatrix bMatrixPoint;

    private IMatrix bMatrixTangent;

    private IMatrix bMatrixSecondDer;

    private double delta;

    public BSplineCurve(List<IVector> points, int delta) {
        if(points.size() < 4) {
            throw new IllegalArgumentException("Must hold at least 4 points");
        }

        this.points = points;

        setDelta(delta);

        bMatrixPoint = Matrix.parseSimple(
                "-1 3 -3 1 |" +
                "3 -6 3 0 |" +
                "-3 0 3 0 |" +
                "1 4 1 0").multiplyDouble((double) 1 / 6);

        bMatrixTangent = Matrix.parseSimple(
                "-1 3 -3 1 |" +
                 "2 -4 2 0 |" +
                 "-1 0 1 0").multiplyDouble(0.5);

        bMatrixSecondDer = Matrix.parseSimple(
                "-1 3 -3 1 |" +
                    "1 -2 1 0");
    }

    public bSplineIterator iterator() {
        return new bSplineIterator();
    }

    public void setDelta(int delta) {
        if(delta < 1) {
            throw new IllegalArgumentException("Delta cannot be lesser than 1");
        }

        this.delta = delta;
    }

    public class bSplineIterator  {

        private int segmentIndex = 0;

        private int currentStep = -1;

        private IVector tVector;

        private int tangentCounter = -1;

        private int pointCounter = -1;

        private IMatrix radiusMatrix = new Matrix(4, 3);


        public boolean hasNext() {
            return !(delta <= currentStep && segmentIndex >= points.size() - 4);
        }

        public IVector nextPoint() {
            if(pointCounter >= tangentCounter) {
                ++currentStep;
                ++pointCounter;
            } else {
                pointCounter = currentStep;
            }

            if(currentStep > delta) {
                if(segmentIndex == points.size() - 3) {
                    throw new IllegalArgumentException("Out of bounds");
                } else {
                    ++segmentIndex;
                    currentStep = 0;
                }
            }

            if(segmentIndex >= points.size() - 3) {
                throw new IllegalStateException("You have already passed the end of the curve");
            }

            if(currentStep == 0 && pointCounter >= tangentCounter) {
                setRadiusMatrix();
            }

            double t = currentStep * (1 / delta);
            IVector tVector = makeTVector(3, t);

            IMatrix temp = tVector.toRowMatrix(true).nMultiply(bMatrixPoint);
            IVector result = temp.nMultiply(radiusMatrix).toVector(false);
            return result;
        }

        public IVector nextTangent() {
            if(tangentCounter >= pointCounter) {
                ++currentStep;
                ++tangentCounter;
            } else {
                tangentCounter = currentStep;
            }

            if(currentStep > delta) {
                if(segmentIndex == points.size() - 3) {
                    throw new IllegalArgumentException("Out of bounds");
                } else {
                    ++segmentIndex;
                    currentStep = 0;
                }
            }

            if(currentStep == 0 && pointCounter <= tangentCounter) {
                setRadiusMatrix();
            }

            double t = currentStep * (1 / delta);
            IVector tVector = makeTVector(2, t);

            IMatrix temp = tVector.toRowMatrix(true).nMultiply(bMatrixTangent);
            return temp.nMultiply(radiusMatrix).toVector(false).normalize();
        }

        public IVector secondDerivation() {
            double t = currentStep * (1 / delta);
            IVector tVector = makeTVector(1, t);

            IMatrix temp = tVector.toRowMatrix(true).nMultiply(bMatrixSecondDer);
            IVector result = temp.nMultiply(radiusMatrix).toVector(false);
            return result.normalize();
        }


        private IVector makeTVector(int level, double t) {
            IVector tVector = new Vector(level + 1, false);

            for(int i = 0; i < tVector.getDimension(); i++) {
                tVector.set(i, Math.pow(t, level - i));
            }

            return tVector;
        }

        private void setRadiusMatrix() {
            for(int row = 0; row < 4; row++) {
                IVector currentPoint = points.get(row + segmentIndex);

                for(int col = 0; col < 3; col++) {
                    radiusMatrix.set(row, col, currentPoint.get(col));
                }
            }
        }
    }
}
