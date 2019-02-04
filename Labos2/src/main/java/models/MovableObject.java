package models;

import math.vector.IVector;
import particles.IMovable;

import java.util.function.BiFunction;

public class MovableObject implements IMovable {

    private IVector speed;

    private IVector position;

    private BiFunction<IVector, Integer, IVector> accelFunction;

    public MovableObject(IVector speed, IVector position, BiFunction<IVector, Integer, IVector> accelFunction) {
        this.speed = speed;
        this.position = position;
        this.accelFunction = accelFunction;
    }

    public void move(int timeElapsed) {
        IVector nextSpeed = accelFunction.apply(speed, timeElapsed);
        IVector nextPosition = position.add(nextSpeed.nScalarMultiply(timeElapsed));

        if(speed.getDimension() != 1) {
            this.speed = nextSpeed;
        }
        this.position = nextPosition;
    }

    public void setAccelFunction(BiFunction<IVector, Integer, IVector> accelFunction) {
        this.accelFunction = accelFunction;
    }

    public IVector getSpeed() {
        return speed;
    }

    public IVector getPosition() {
        return position;
    }

    public void setPosition(IVector position) {
        this.position = position;
    }

    public static IVector calculateSpeedVectorOnNormal(double maxSpeed, IVector normal) {
        IVector speedVector = normal.nNormalize();
        speedVector.scalarMultiply(maxSpeed);
        return speedVector;
    }

    public MovableObject copy() {
        return new MovableObject(speed.copy(), position.copy(), accelFunction);
    }
}
