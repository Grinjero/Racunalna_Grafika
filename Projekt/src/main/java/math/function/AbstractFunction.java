package math.function;

import math.vector.IVector;

public abstract class AbstractFunction {

    public AbstractFunction() {
    }

    public double getValueAt(IVector element) {
        return valueAt(element);
    }

    protected abstract double valueAt(IVector element);

    public abstract int getInputDimension();
}
