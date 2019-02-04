package particles;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.util.texture.Texture;
import math.vector.IVector;
import models.MovableObject;
import models.Plane3D;

import java.io.IOException;
import java.util.Random;
import java.util.function.BiFunction;

public class ParticleGeneratingObject  extends ParticleSystem {

    /**
     * Texture for generated particles
     */
    public Texture texture;

    public int maxNumParticles;

    public double startingSpeed;

    public Plane3D plane;

    public boolean upOrDown;

    public BiFunction<IVector, Integer, IVector> accelFunction;

    public int lifespan;

    public ParticleGeneratingObject(MovableObject movableObject, Texture texture, double startingSpeed, Plane3D plane, boolean upOrDown, BiFunction<IVector, Integer, IVector> accelFunction, int lifespan, GL gl) {
        super(movableObject, gl);

        this.texture = texture;
        this.startingSpeed = startingSpeed;
        this.accelFunction = accelFunction;
        this.plane = plane;
        this.upOrDown = upOrDown;
        this.lifespan = lifespan;
    }

    @Override
    public void move(int timeElapsed) {
        super.move(timeElapsed);

        if(maxNumParticles > size()) {
            Random rand = new Random();

            double param1 = rand.nextDouble();
            double param2 = rand.nextDouble();

            IVector point = plane.getPoint(param1, param2);
            IVector normal = plane.getNormal();

            if(upOrDown == true) {
                normal = normal.negative();
            }

            IVector speedVector = MovableObject.calculateSpeedVectorOnNormal(startingSpeed, normal);
            MovableObject mo = new MovableObject(speedVector, point, accelFunction);
            Particle particle = new Particle(lifespan, mo, texture, 100);

            addParticle(particle);
        }
    }
}
