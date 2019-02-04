package particles;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import math.matrix.IMatrix;
import math.matrix.Matrix;
import math.vector.IVector;
import math.vector.Vector;
import models.MovableObject;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.function.BiFunction;

public class Particle implements IDrawable, IMovable {

    private static final IVector startOrientation = Vector.parseSimple("1 0 0");

    /**
     * Milliseconds
     */
    private int lifespan;

    /**
     * Milliseconds
     */
    private int age;

    private Texture texture;

    private double size;

    private MovableObject movableObject;

    private static IMatrix squareVertices = Matrix.parseSimple(
            "-0.5 -0.5 0 |" +
                    "0.5 -0.5 0.0 |" +
                    "-0.5 0.5 0.0 |" +
                    "0.5 0.5 0.0");

    public Particle(int lifespan, String textureName, String textureExt, MovableObject movableObject, double size)  {
        this.lifespan = lifespan;
        this.age = 0;
        this.movableObject = movableObject;
        this.size = size;
        try {
            String fileName = "src/main/resources/textures/" + textureName + "." + textureExt;
            texture = loadTexture(fileName);
        } catch (IOException exc) {
            System.out.println("Texture load error");
            exc.printStackTrace();
        }
    }

    public Particle(int lifespan, MovableObject movableObject, Texture texture, double size) {
        this.lifespan = lifespan;
        this.age = 0;
        this.movableObject = movableObject;
        this.texture = texture;
        this.size = size;
    }


    //TO DO
    public void draw(GL2 gl, IVector eye) {
        gl.glPushMatrix();

        IVector position = movableObject.getPosition();
        gl.glTranslated(position.get(0), position.get(1), position.get(2));
//        applyRotation(gl, eye);

        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA,GL2.GL_ONE_MINUS_SRC_ALPHA);

        texture.enable(gl);
        texture.bind(gl);

        IMatrix modelViewMatrix = loadMatrix(gl);
        IVector particlePosition = new Vector(3, false);
        particlePosition.set(0, modelViewMatrix.get(0, 3));
        particlePosition.set(1, modelViewMatrix.get(1, 3));
        particlePosition.set(2, modelViewMatrix.get(2,3));

        IVector cameraRight = new Vector(3, false);
        cameraRight.set(0, modelViewMatrix.get(0, 0));
        cameraRight.set(1, modelViewMatrix.get(1, 0));
        cameraRight.set(2, modelViewMatrix.get(2,0));

        IVector upCamera = new Vector(3, false);
        upCamera.set(0, modelViewMatrix.get(0, 1));
        upCamera.set(1, modelViewMatrix.get(1, 1));
        upCamera.set(2, modelViewMatrix.get(2,1));

        gl.glColor4d(1, 1, 1, (double) (lifespan - age) / lifespan);
        gl.glBegin(GL2.GL_QUAD_STRIP);


        for(int i = 0; i < 4; i++)  {
            if(i == 0) {
                gl.glTexCoord2f(0, 0);
//                gl.glVertex3d(-size, -size, 0);
            } else if( i== 1) {
                gl.glTexCoord2f(1, 0);
//                gl.glVertex3d(-size, size, 0);
            } else if(i == 2) {
                gl.glTexCoord2f(0, 1);
//                gl.glVertex3d(size, size, 0);
            } else if(i == 3) {
                gl.glTexCoord2f(1, 1);
//                gl.glVertex3d(size, -size, 0);
            }

            IVector vertexPosition = new Vector(3, false);
            vertexPosition.add(cameraRight.nScalarMultiply(squareVertices.get(i, 0)).nScalarMultiply(size));
            vertexPosition.add(upCamera.nScalarMultiply(squareVertices.get(i, 1)).nScalarMultiply(size));

            gl.glVertex3d(vertexPosition.get(0), vertexPosition.get(1),vertexPosition.get(2));
        }

        gl.glEnd();
        gl.glPopMatrix();
    }

    private void saveMatrix(GL2 gl, IMatrix matrix) {
        double[] modelViewMatrix = new double[16];

        for(int row = 0; row < 4; row++) {
            for(int col = 0; col < 4; col++) {
                modelViewMatrix[row * 4 + col] = matrix.get(row, col);
            }
        }

        gl.glLoadMatrixd(modelViewMatrix, 0);
    }
    private IMatrix loadMatrix(GL2 gl) {
        double[] modelViewMatrix = new double[16];
        gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, modelViewMatrix, 0);

        IMatrix matrix = new Matrix(4, 4);

        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                matrix.set(i, j, modelViewMatrix[i * 4 + j]);
            }
        }

        return matrix;
    }

    private void applyRotation(GL2 gl, IVector eye) {
//        IVector position = movableObject.getPosition();
//
//        IVector goalOrientation = eye.nSub(position);
//        IVector rotationAxis = startOrientation.nVectorProduct(goalOrientation);
//
//        double angle = goalOrientation.cosine(startOrientation);
//
//        gl.glRotated(angle, rotationAxis.get(0), rotationAxis.get(1), rotationAxis.get(2));

        double[] modelViewMatrix = new double[16];

        gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, modelViewMatrix, 0);

//        for(int i = 0; i < 4; i++) {
//            for(int j = 0; j < 4; j++) {
//                if(i == j) {
//                    modelViewMatrix[i * 4 + j] = 1;
//                } else {
//                    modelViewMatrix[i * 4 + j] = 0;
//                }
//            }
//        }



        gl.glLoadMatrixd(modelViewMatrix, 0);
    }

    public void move(int timeElapsed) {
        age += timeElapsed;

        movableObject.move(timeElapsed);
    }

    public void setPosition(IVector position) {
        movableObject.setPosition(position);
    }

    public IVector getPosition() {
        return movableObject.getPosition();
    }

    public boolean isAlive() {
        return age < lifespan;
    }

    public void setAccelFunction(BiFunction<IVector, Integer, IVector> accelFunction) {
        this.movableObject.setAccelFunction(accelFunction);
    }

    public void enableGL(GL gl) {
        texture.enable(gl);
    }

    public Particle copy() {
        return new Particle(lifespan, movableObject.copy(), texture, size);
    }

    public static Texture loadTexture(String file) throws GLException, IOException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(ImageIO.read(new File(file)), "png", os);
        InputStream fis = new ByteArrayInputStream(os.toByteArray());
        return TextureIO.newTexture(fis, true, TextureIO.PNG);
    }
}
