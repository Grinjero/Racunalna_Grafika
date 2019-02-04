package math.function;

import math.vector.IVector;
import math.optimisation.Optimisation;

import java.util.List;

public class RestrictionFunction extends AbstractFunction {

    public AbstractFunction function;

    public List<SubFunction> inequalities;

    public List<SubFunction> equalities;

    private StartingPointFunction startingPointFunction;

    private double t;

    public RestrictionFunction(AbstractFunction function, List<SubFunction> inequalities, List<SubFunction> equalities, double t) {
        this.function = function;
        this.inequalities = inequalities;
        this.equalities = equalities;
        this.t = t;
        this.startingPointFunction = new StartingPointFunction();
    }

    public void setT(double t) {
        this.t = t;
    }


    public double valueAt(IVector elements) {
        double funcValue = function.getValueAt(elements);

        double limitSum = 0;
        for(SubFunction inequality : inequalities) {
            double value = inequality.valueAt(elements);

            if(value <= 0) {
                return Double.POSITIVE_INFINITY;
            } else {
                limitSum += (1d / t) * Math.log(value);
            }
         }

        double equalitySum = 0;
        for(SubFunction equality : equalities) {
            equalitySum += t * Math.pow(equality.valueAt(elements), 2);
        }

        double result = funcValue - limitSum + equalitySum;
        return  result;
    }

    public IVector findStartingPoint(IVector point, IVector dx, double epsilon) {
        return Optimisation.hookeJeevesAlgorithm(startingPointFunction, point, dx, epsilon, false);
    }

    public boolean isLegal(IVector point) {
        return startingPointFunction.isLegalPoint(point);
    }

    public int getInputDimension() {
        return function.getInputDimension();
    }

    protected SubFunction[][] createHesse() {
        throw new IllegalArgumentException("Does not have Hesse");
    }

    protected SubFunction[] createGradient() {
        throw new IllegalArgumentException("Does not have Gradient");
    }







    private class StartingPointFunction extends AbstractFunction {

        public double valueAt(IVector element) {
            return  calculateValue(element);
        }

        public boolean isLegalPoint(IVector point) {
            return calculateValue(point) <= 0;
        }

        private double calculateValue(IVector element) {
            double value = 0;

            for(SubFunction inequality : inequalities) {
                double inequalityValue = inequality.valueAt(element);

                if(inequalityValue < 0) {
                    value += t * inequalityValue;
                }
            }

            return -value;
        }

        public int getInputDimension() {
            return function.getInputDimension();
        }

        protected SubFunction[][] createHesse() {
            throw new IllegalArgumentException("Does not have Hesse");
        }

        protected SubFunction[] createGradient() {
            throw new IllegalArgumentException("Does not have Gradient");
        }
    }
}
