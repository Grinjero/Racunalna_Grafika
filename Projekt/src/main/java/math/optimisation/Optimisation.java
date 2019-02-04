package math.optimisation;

import math.function.AbstractFunction;
import math.function.RestrictionFunction;
import math.function.SubFunction;
import math.vector.IVector;
import math.vector.Vector;

import java.util.List;

public class Optimisation {

    public static IVector hookeJeevesAlgorithm(AbstractFunction function, IVector startingPoint, IVector dx, double epsilon, boolean toPrint) {
        IVector basePoint = startingPoint.copy();
        IVector searchStartPoint = startingPoint.copy();
        dx = dx.copy();

        do {
            IVector searchResult = search(searchStartPoint, function, dx);

            double searchValue = function.getValueAt(searchResult);
            double baseValue = function.getValueAt(basePoint);

            if (searchValue < baseValue) {
                searchStartPoint = searchResult.nScalarMultiply(2);
                searchStartPoint.sub(basePoint);
                basePoint = searchResult;

            } else {
                dx.scalarMultiply(0.5);
                searchStartPoint = basePoint;
            }

            if (toPrint) {
                System.out.println("Base point: " + basePoint);
                System.out.println("Start point: " + searchStartPoint);
                System.out.println("Search result: " + searchResult + "\n");
            }

        } while (dx.lesserThan(epsilon) == false);

        return basePoint;
    }

    private static IVector search(IVector searchStartPoint, AbstractFunction function, IVector dx) {
        IVector point = searchStartPoint.copy();

        for (int i = 0; i < searchStartPoint.getDimension(); i++) {
            double value = function.getValueAt(point);
            point.set(i, point.get(i) + dx.get(i));
            double newValue = function.getValueAt(point);

            if (newValue > value) {
                point.set(i, point.get(i) - 2 * dx.get(i));
                newValue = function.getValueAt(point);

                if (newValue > value) {
                    point.set(i, point.get(i) + dx.get(i));
                }
            }
        }

        return point;
    }

    public static IVector initDxVector(double dxValue, int size) {
        IVector dx = new Vector(size, false);

        for (int i = 0; i < size; i++) {
            dx.set(i, dxValue);
        }

        return dx;
    }

    private static boolean testLimits(List<SubFunction> limits, IVector point) {
        for(SubFunction limit : limits) {
            if(limit.valueAt(point) < 0) {
                return false;
            }
        }

        return true;
    }

    public static IVector iterativeTransformation(AbstractFunction function, IVector startingPoint, List<SubFunction> inequalities, List<SubFunction> equality, double t, IVector dx, double epsilon) {
        IVector previousPoint = null;
        IVector currentPoint = startingPoint.copy();

        RestrictionFunction restrictionFunction = new RestrictionFunction(function, inequalities, equality, t);

        if(restrictionFunction.isLegal(startingPoint) == false) {
            System.out.println("Starting point not valid");
            currentPoint = restrictionFunction.findStartingPoint(currentPoint, dx, epsilon);

            if(restrictionFunction.isLegal(currentPoint) == false) {
                System.out.println("Could not find legal starting point");
                return null;
            }

            System.out.println("Found a valid starting point " + currentPoint);
        }

        double currentValue = Double.POSITIVE_INFINITY, previousValue;
        int iteration = 0;
        do {
            previousPoint = currentPoint.copy();
            previousValue = currentValue;

            currentPoint = hookeJeevesAlgorithm(restrictionFunction, currentPoint, dx, epsilon, false);
            currentValue = restrictionFunction.valueAt(currentPoint);

            t *= 10;
            restrictionFunction.setT(t);

            System.out.println("New point " + currentPoint + ", t = " + t + ", Restriction value = " + currentValue);

            iteration++;

        } while((currentPoint.nSub(previousPoint)).absolute().lesserThan(epsilon) == false || iteration == 1);

        System.out.println("Iterative done in " + iteration + " iterations, Restriction function value = " + restrictionFunction.valueAt(currentPoint));
        return currentPoint;
    }
}
