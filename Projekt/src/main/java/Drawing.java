import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import kinematics.Manipulator;
import math.vector.IVector;
import math.vector.Vector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Drawing implements GLEventListener {

    private int fps;

    private FPSAnimator animator;

    //GL related
    private GLU glu;

    public GLCanvas canvas;

    // Perspective related

    private IVector eye = Vector.parseSimple("0 0 13");

    private IVector lookAt = Vector.parseSimple("0 0 0");

    private IVector up = Vector.parseSimple("0 1 0");

    private static final double NEAR = 1;

    private static final double FAR = 100;

    private List<IVector> referencePoints = new LinkedList<>();

    // Colors
    private static final Color CLEAR_COLOR = Color.WHITE;
    private static final Color SEGMENT_COLOR = Color.RED;
    private static final Color JOINT_COLOR = Color.BLUE;
    private static final Color REFERENCE_COLOR = Color.WHITE;

    // Kinematics related
    private Manipulator manipulator;

    /**
     *
     * @param args
     * [0] -> framerate
     * [1] -> path to the config file
     */
    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("First argument -> framerate \n" +
                    "Second argument -> path to the config file ");
            System.exit(-1);
        }
        final GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);

        final GLCanvas glCanvas = new GLCanvas(capabilities);

        int framerate = Integer.parseInt(args[0]);
        Drawing drawing = new Drawing(framerate, args[1]);

        glCanvas.addGLEventListener(drawing);
        glCanvas.setSize(1000, 1000);

        final JFrame frame = new JFrame("Kinematics");

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

    public Drawing(int framerate, String pathString) {
        try {
            manipulator = Manipulator.parse(pathString);
        } catch (IOException e) {
            System.out.println("Could not parse given config file");
            e.printStackTrace();
            System.exit(-1);
        }

        referencePoints.add(Vector.parseSimple("0 0 0"));
        referencePoints.add(Vector.parseSimple("1 0 0"));
        referencePoints.add(Vector.parseSimple("0 1 0"));
        referencePoints.add(Vector.parseSimple("0 0 1"));
        referencePoints.add(Vector.parseSimple("-1 0 0"));
        referencePoints.add(Vector.parseSimple("0 -1 0"));
        referencePoints.add(Vector.parseSimple("0 0 -1"));
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL2 gl = glAutoDrawable.getGL().getGL2();
        glu = new GLU();

        gl.glClearColor(CLEAR_COLOR.getRed(), CLEAR_COLOR.getGreen(), CLEAR_COLOR.getBlue(), 1);

        Runnable keyPressThread = new InputThread();
        Thread t = new Thread(keyPressThread);
        t.start();

        FPSAnimator animator = new FPSAnimator(glAutoDrawable, fps);
        animator.start();
    }



    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL2 gl = glAutoDrawable.getGL().getGL2();

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        gl.glColor3f(REFERENCE_COLOR.getRed(), REFERENCE_COLOR.getGreen(), REFERENCE_COLOR.getBlue());
        gl.glBegin(GL2.GL_POINTS);
        for(IVector referencePoint : referencePoints) {
            gl.glVertex3d(referencePoint.get(0), referencePoint.get(1), referencePoint.get(2));
        }
        gl.glEnd();

        List<IVector> joints = manipulator.jointLocations();

        gl.glColor3f(SEGMENT_COLOR.getRed(), SEGMENT_COLOR.getGreen(), SEGMENT_COLOR.getBlue());
        gl.glBegin(GL2.GL_LINE_STRIP);
        for(IVector joint : joints) {
            gl.glVertex3d(joint.get(0), joint.get(1), joint.get(2));
        }
        gl.glEnd();

        gl.glColor3f(JOINT_COLOR.getRed(), JOINT_COLOR.getGreen(), JOINT_COLOR.getBlue());
        gl.glBegin(GL2.GL_POINTS);
        for(IVector joint : joints) {
            gl.glVertex3d(joint.get(0), joint.get(1), joint.get(2));
        }

        gl.glEnd();
    }

    /**
     * Basic stuff
     */

    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int w, int h) {
        GL2 gl = glAutoDrawable.getGL().getGL2();

        gl.glViewport(0, 0, w, h);

        //Projection
        loadProjectionMatrix(gl, w, h);

        //model view matrix.
        loadModelViewMatrix(gl);
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {

    }

    private void setCamera(GL2 gl, int w, int h) {
        loadProjectionMatrix(gl, w, h);
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

    /**
     *  Basic stuff
     */


    public class InputThread implements Runnable {

        private Scanner scanner = new Scanner(System.in);


        public InputThread() {
        }

        public void run() {
            while (true) {
                System.out.println("Reach for point: \n");
                String input = scanner.nextLine();

                if (input != null && input.isEmpty() == false) {
                    Vector reachPoint = null;

                    try {
                        reachPoint = Vector.parseSimple(input);
                        manipulator.reachForPoint(reachPoint);

                        IVector reachedPoint = manipulator.currentReach();
                        System.out.println("Reached point " + reachedPoint);
                    } catch (Exception exc) {
                        System.out.println("Wrong input, must be 3 double values representing a 3D point");
                        exc.printStackTrace();
                    }

                }
            }
        }
    }
}
