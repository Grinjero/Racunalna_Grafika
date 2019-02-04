package curve;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import math.vector.IVector;
import math.vector.Vector;
import model.DrawableObjectModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Drawing implements GLEventListener {

    private static final Color CLEAR_COLOR = Color.WHITE;

    private static final int NUM_ARGS = 3;

    private final boolean DRAW_TANGENT = true;

    private final double TANGENT_FACTOR = 5;

    private final int TANGENT_DELAY = 20;

    private static final int FPS = 80;

    private IVector upVector = Vector.parseSimple("0 1 0");

    private IVector eye = Vector.parseSimple("5 5 -10");

    private IVector lookAt = Vector.parseSimple("5 5 60");

    private GLCanvas glCanvas;

    private ObjectOnCurve objectOnCurve;

    private GLU glu;

    private int objectStep = 0;

    /**
     * args[0] -> Object name, args[1] -> Trajectory, args[2] -> delta
     * @param args
     */
    public static void main(String[] args) {
        final GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);

        final GLCanvas glCanvas = new GLCanvas(capabilities);

        Drawing drawing = parse(args, glCanvas);

        glCanvas.addGLEventListener(drawing);
        glCanvas.setSize(1000, 1000);

        final JFrame frame = new JFrame("Krivulja");

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

    public Drawing(GLCanvas glCanvas, ObjectOnCurve objectOnCurve) {
        this.glCanvas = glCanvas;
        this.objectOnCurve = objectOnCurve;
    }

    private static Drawing parse(String[] args, GLCanvas glCanvas) {
        if(args.length != NUM_ARGS) {
            System.out.println("Expected " + NUM_ARGS + " arguments");
            System.exit(0);
        }

        int delta = 0;
        String objectName = args[0];
        String curveName = args[1];

        try {
            delta = Integer.parseInt(args[2]);
        } catch (NumberFormatException exc) {
            System.out.println("Third argument must be an integer");
        }

        List<IVector> points = getPoints(curveName);
        DrawableObjectModel model = getObjectModel(objectName);

        ObjectOnCurve objectOnCurve = new ObjectOnCurve(points, model, delta);
        return new Drawing(glCanvas, objectOnCurve);
    }

    private static List<IVector> getPoints(String name) {
        String path = name;
        Path systemPath = Paths.get(path);

        if(Files.exists(systemPath) == false) {
            throw new IllegalArgumentException("Given path \"" + path + "\" does not exist");
        }

        List<String> lines = null;

        try {
            lines = Files.readAllLines(systemPath);
        } catch(IOException exc) {
            System.out.println("File cannot be accessed");
            System.exit(1);
        }

        List<IVector> points = new ArrayList<IVector>();

        for(String line : lines) {
            points.add(Vector.parseSimple(line));
        }

        return points;
    }

    private static DrawableObjectModel getObjectModel(String name) {
        String path = name;
        Path systemPath = Paths.get(path);

        if(Files.exists(systemPath) == false) {
            throw new IllegalArgumentException("Given file does not exist");
        }

        List<String> lines = null;

        try {
            lines = Files.readAllLines(systemPath);
        } catch(IOException exc) {
            System.out.println("File cannot be accessed");
            System.exit(1);
        }

        return new DrawableObjectModel(lines, true);
    }


    public void init(GLAutoDrawable glAutoDrawable) {
        GL2 gl = glAutoDrawable.getGL().getGL2();

        glu = new GLU();

        gl.glClearColor(CLEAR_COLOR.getRed(), CLEAR_COLOR.getGreen(), CLEAR_COLOR.getBlue(), 1);

        gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_LINE);
        gl.glEnable(GL2.GL_CULL_FACE);
        gl.glCullFace(GL2.GL_BACK);

        FPSAnimator animator = new FPSAnimator(glAutoDrawable, FPS);
        animator.start();
    }

    public void dispose(GLAutoDrawable glAutoDrawable) {

    }

    public void display(GLAutoDrawable glAutoDrawable) {
        GL2 gl = glAutoDrawable.getGL().getGL2();

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        if(objectOnCurve.drawCurve(gl, DRAW_TANGENT, TANGENT_DELAY, TANGENT_FACTOR, objectStep) == true) {
            objectStep = 0;
        }
        objectStep++;
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

        glu.gluPerspective(60, (double) w / (double) h, 0.1, 60);
    }

    private void loadModelViewMatrix(GL2 gl) {
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        glu.gluLookAt(eye.get(0), eye.get(1), eye.get(2),
                lookAt.get(0), lookAt.get(1), lookAt.get(2),
                upVector.get(0), upVector.get(1), upVector.get(2));
    }
}
