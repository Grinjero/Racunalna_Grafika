package particles;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import math.vector.IVector;
import math.vector.Vector;
import models.MovableObject;

import java.util.Random;

public class RainCloud extends ParticleSystem {

    private double width;

    private double length;

    private Particle templateParticle;

    private int numParticles;

    public RainCloud(MovableObject movableObject, double width, double length, int numParticles, Particle templateParticle, GL gl) {
        super(movableObject, gl);

        this.templateParticle = templateParticle.copy();
        this.width = width;
        this.length = length;
        this.numParticles = numParticles;

        generateParticles(gl);
    }

    @Override
    public void draw(GL2 gl, IVector eye) {
        super.draw(gl, eye);

        generateParticles(gl);
    }

    private void generateParticles(GL gl) {
        Random rand = new Random();

        for(int i = size(); i < numParticles; i++) {
            double creationChance = 1d / numParticles;
            if(rand.nextDouble() > creationChance) {
                continue;
            }

            double offsetX = rand.nextDouble() * length - length / 2;
            double offsetY = rand.nextDouble() * width - width / 2;

            IVector offset = new Vector(new double[] {offsetX, offsetY, 0d});
            Particle particle = templateParticle.copy();

            particle.setPosition(offset.add(templateParticle.getPosition()));
            particle.enableGL(gl);

            addParticle(particle);
        }
    }
}
