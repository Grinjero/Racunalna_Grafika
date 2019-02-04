package functions;

import math.vector.IVector;
import math.vector.Vector;
import models.MovableObject;

import java.util.function.BiFunction;

/**
 * Miliseconds
 */
public class AccelerationFunctions {

    public static final BiFunction<IVector, Integer, IVector> CONSTANT_FUNC = new BiFunction<IVector, Integer, IVector>() {

        public IVector apply(IVector speed, Integer timeElapsed) {
            return speed;
        }
    };

    public static final BiFunction<IVector, Integer, IVector> GRAVITY_FUNC = new BiFunction<IVector, Integer, IVector>() {

        private IVector gravityVector = Vector.parseSimple("0 -9.81E-6 0");

        public IVector apply(IVector speed, Integer timeElapsed) {

            return speed.nAdd(gravityVector.nScalarMultiply(timeElapsed));
        }
    };

    public static BiFunction<IVector, Integer, IVector> circularAcceleration(IVector center, IVector objectLocation, IVector normal) {
        final IVector c = center;
        final IVector location = objectLocation;
        final double radius = location.nSub(c).norm();
        final IVector n = normal;

        return new BiFunction<IVector, Integer, IVector>() {
            @Override
            public IVector apply(IVector speed, Integer time) {
                IVector acceleration = c.nSub(location).normalize();
                double accelerationNorm = Math.pow(speed.norm(), 2) / radius;

                acceleration.scalarMultiply(accelerationNorm);
                IVector newSpeed = acceleration.scalarMultiply(time);
                return newSpeed;
            }
        };
    }
}
