import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import functions.AccelerationFunctions;
import math.matrix.IMatrix;
import math.matrix.Matrix;
import math.vector.IVector;
import math.vector.Vector;
import models.MovableObject;
import particles.Particle;
import particles.RainCloud;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class Demo implements GLEventListener {

    //GL related
    private GLU glu;

    public GLCanvas canvas;

    // Perspective related

    private IVector eye = Vector.parseSimple("0 0 0");

    private IVector lookAt = Vector.parseSimple("0 0 600");

    private IVector up = Vector.parseSimple("0 1 0");

    private static final double NEAR = 50;

    private static final double FAR = 700;

    // Particle related

    private Particle snowflakeParticle;

    private Texture snowflakeTexture;

    private Particle staticSnowflake;

    private RainCloud cloud;

    // Camera movement related
    private double lookAtAngleShift = 5;

    private double eyeShift = 10;

//    private double leftRightAngle = 0;
//
//    private double upDownAngle = 0;

    private double radius = lookAt.nSub(eye).norm();

    private static final int X_AXIS = 0;

    private static final int Y_AXIS = 1;

    private static final int Z_AXIS = 2;

    // Colors

    private static final Color CLEAR_COLOR = Color.BLACK;

//    public Texture beeTexture;
//
//    public Texture snowTexture;
//
//    public Texture lightTexture;

    // Framerate related

    private int fps;

    private int millisBetweenFrames;

    public static void main(String[] args) {
        final GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);

        final GLCanvas glCanvas = new GLCanvas(capabilities);

        int framerate = Integer.parseInt(args[0]);
        Demo drawing = new Demo(glCanvas, framerate);

        glCanvas.addGLEventListener(drawing);
        glCanvas.setSize(1000, 1000);

        final JFrame frame = new JFrame("Particles");

        frame.getContentPane().add(glCanvas);
        frame.setSize(frame.getContentPane().getPreferredSize());
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.dispose();
                System.exit(0);
            }
        });

        frame.setVisible(true);

    }

    public Demo(GLCanvas canvas, int framerate) {
        this.canvas = canvas;
        this.millisBetweenFrames = (int) 1000d / framerate;
        this.fps = framerate;

        setUpInput();
    }

    public void init(GLAutoDrawable glAutoDrawable) {
        GL2 gl = glAutoDrawable.getGL().getGL2();
        glu = new GLU();

        gl.glClearColor(CLEAR_COLOR.getRed(), CLEAR_COLOR.getGreen(), CLEAR_COLOR.getBlue(), 1);

        try {
            snowflakeTexture = Particle.loadTexture("src/main/resources/textures/snow.bmp");
        } catch (IOException exc) {
            System.out.println("Texture load error");
            System.exit(1);
        }

        snowflakeTexture.enable(gl);
        snowflakeTexture.bind(gl);
        MovableObject snowflakeObject = new MovableObject(Vector.parseSimple("0 -10E-2 0"), Vector.parseSimple("0 250 300"), AccelerationFunctions.GRAVITY_FUNC);
        snowflakeParticle = new Particle(3000, snowflakeObject, snowflakeTexture, 10);


        IVector cloudPosition = Vector.parseSimple("0 50 30");
        MovableObject cloudMO = new MovableObject(Vector.parseSimple("1"), cloudPosition, AccelerationFunctions.circularAcceleration(Vector.parseSimple("0 50 0"), cloudPosition, Vector.parseSimple("0 1 0")));
        cloud = new RainCloud(cloudMO, 50, 50, 100, snowflakeParticle, gl);

        MovableObject staticMO = new MovableObject(Vector.parseSimple("0 0 0"), Vector.parseSimple("0 0 50"), AccelerationFunctions.CONSTANT_FUNC);
        staticSnowflake = new Particle(20000, staticMO, snowflakeTexture, 50);

        FPSAnimator animator = new FPSAnimator(glAutoDrawable, fps);
        animator.start();
    }

    public void dispose(GLAutoDrawable glAutoDrawable) {

    }

    public void display(GLAutoDrawable glAutoDrawable) {
        GL2 gl = glAutoDrawable.getGL().getGL2();

        setCamera(gl, canvas.getWidth(), canvas.getHeight());

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        snowflakeParticle.draw(gl, eye);
        snowflakeParticle.move(millisBetweenFrames);

        cloud.draw(gl, eye);
        cloud.move(millisBetweenFrames);

        staticSnowflake.draw(gl, eye);

        int i = 0;
        i++;
    }

    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int w, int h) {
        GL2 gl = glAutoDrawable.getGL().getGL2();

        gl.glViewport(0, 0, w, h);

        //Projection
        loadProjectionMatrix(gl, w, h);

        //model view matrix.
        loadModelViewMatrix(gl);
    }

    private void loadProjectionMatrix(GL2 gl, int w, int h) {
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        glu.gluPerspective(60, (double) w / (double) h, NEAR, FAR);
    }

    private void loadModelViewMatrix(GL2 gl) {
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        glu.gluLookAt(eye.get(0), eye.get(1), eye.get(2),
                lookAt.get(0), lookAt.get(1), lookAt.get(2),
                up.get(0), up.get(1), up.get(2));
    }

    private void setCamera(GL2 gl, int w, int h) {
        loadProjectionMatrix(gl, w, h);
        loadModelViewMatrix(gl);
    }

    private void setUpInput() {
        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case(KeyEvent.VK_LEFT):
//                        leftRightAngle += lookAtAngleShift;
                        moveLookAtLeftRight(-lookAtAngleShift);
                        break;

                    case(KeyEvent.VK_RIGHT):
//                        leftRightAngle -= lookAtAngleShift;
                        moveLookAtLeftRight(lookAtAngleShift);
                        break;

                    case(KeyEvent.VK_UP):
//                        upDownAngle += lookAtAngleShift;
                        moveLookAtUpDown(lookAtAngleShift);
                        break;

                    case(KeyEvent.VK_DOWN):
//                        upDownAngle -= lookAtAngleShift;
                        moveLookAtUpDown(-lookAtAngleShift);
                        break;

                    case(KeyEvent.VK_W):
                        moveEye(eyeShift, Direction.FORWARD);
                        break;

                    case(KeyEvent.VK_A):
                        moveEye(eyeShift, Direction.LEFT);
                        break;

                    case(KeyEvent.VK_D):
                        moveEye(eyeShift, Direction.RIGHT);
                        break;

                    case(KeyEvent.VK_S):
                        moveEye(eyeShift, Direction.BACKWARD);
                        break;
                }
            }
        });
    }

    private void moveLookAtLeftRight(double angle) {
        IMatrix leftRightRotation = new Matrix(3, 3);
        double radians = Math.toRadians(angle / 5);

        double cosLeftRight = Math.cos(radians);
        double sinLeftRight = Math.sin(radians);

        leftRightRotation.set(0, 0, cosLeftRight);
        leftRightRotation.set(1, 1, 1);
        leftRightRotation.set(2, 2, cosLeftRight);
        leftRightRotation.set(2, 0, -sinLeftRight);
        leftRightRotation.set(0, 2, sinLeftRight);

        IVector rotatedEyeLookVector = lookAt.nSub(eye);
        rotatedEyeLookVector = rotatedEyeLookVector.toRowMatrix(false).nMultiply(leftRightRotation).toVector(false);

        lookAt = eye.nAdd(rotatedEyeLookVector.normalize().nScalarMultiply(radius));
    }

    private void moveLookAtUpDown(double angle) {
        double radians = Math.toRadians(angle * 10);

        IMatrix upDownRotation = new Matrix(3, 3);
        double sinUpDown = Math.sin(Math.toRadians(radians));
        double cosUpDown = Math.cos(Math.toRadians(radians));

        upDownRotation.set(0, 0, 1);
        upDownRotation.set(1, 1, cosUpDown);
        upDownRotation.set(2, 2, cosUpDown);
        upDownRotation.set(2, 1, sinUpDown);
        upDownRotation.set(1, 2, -sinUpDown);

        IVector rotatedEyeLookVector = lookAt.nSub(eye);
        rotatedEyeLookVector = rotatedEyeLookVector.toRowMatrix(false).nMultiply(upDownRotation).toVector(false);

        lookAt = eye.nAdd(rotatedEyeLookVector.normalize().nScalarMultiply(radius));
    }

    private void moveVector(IVector vector, int index, double shift) {
        vector.set(index, vector.get(index) + shift);
    }

    private void moveEye(double shift, Direction direction) {
        IVector eyeLookAtVector = lookAt.nSub(eye).normalize();

        IVector shiftVector = null;
        if(direction == Direction.FORWARD) {
            shiftVector = eyeLookAtVector.scalarMultiply(shift);

        } else if(direction == Direction.BACKWARD) {
            shiftVector = eyeLookAtVector.scalarMultiply(-shift);

        } else if(direction == Direction.LEFT) {
            shiftVector = up.nVectorProduct(eyeLookAtVector).normalize().scalarMultiply(shift);

        } else {
            shiftVector = up.nVectorProduct(eyeLookAtVector).normalize().scalarMultiply(-shift);
        }

        eye.add(shiftVector);
        lookAt.add(shiftVector);
    }

    private enum Direction {
        FORWARD,
        LEFT,
        RIGHT,
        BACKWARD
    }
}
