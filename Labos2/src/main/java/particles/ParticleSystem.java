package particles;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import math.vector.IVector;
import models.MovableObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiFunction;

public class ParticleSystem implements IDrawable, IMovable {

    protected MovableObject movableObject;

    private List<Particle> particles;

    protected GL gl;

    public ParticleSystem(MovableObject movableObject, GL gl) {
        this.movableObject = movableObject;

        this.particles = new ArrayList<>();
        this.gl = gl;
    }

    public void addParticle(Particle particle) {
        if(particle == null) {
            throw new IllegalArgumentException("Argument cannot be null");
        }

        particles.add(particle);
    }


    public void draw(GL2 gl, IVector eye) {
        gl.glPushMatrix();

        IVector position = movableObject.getPosition();
        gl.glTranslated(position.get(0), position.get(1), position.get(2));

        ListIterator<Particle> iterator = particles.listIterator();

        while(iterator.hasNext()) {
            Particle particle = iterator.next();

            particle.draw(gl, eye);
        }

        gl.glPopMatrix();
    }


    public void move(int timeElapsed) {
        movableObject.move(timeElapsed);

        Iterator<Particle> iterator = particles.iterator();

        while(iterator.hasNext()) {
            Particle particle = iterator.next();
            particle.move(timeElapsed);

            if(particle.isAlive() == false) {
                iterator.remove();
            }
        }
    }

    public void setAccelFunction(BiFunction<IVector, Integer, IVector> accelFunction) {
        this.movableObject.setAccelFunction(accelFunction);
    }

    /**
     * @return number of particles
     */
    public int size() {
        return particles.size();
    }
}
