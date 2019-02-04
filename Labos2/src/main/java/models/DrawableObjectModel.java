package models;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import math.vector.IVector;
import particles.IDrawable;
import particles.IMovable;

import java.io.IOException;

public class DrawableObjectModel extends ObjectModel implements IDrawable{

    private Texture texture;

    public DrawableObjectModel(String filename, Texture texture) throws IOException {
        super(filename);

        this.texture = texture;
    }


    public void draw(GL2 gl, IVector eye) {
        texture.enable(gl);

        for(Face3D face : faces) {
            texture.bind(gl);
            gl.glBegin(GL2.GL_POLYGON);

            for(int i = 0; i < face.size(); i++) {
                Vertex3D point = points.get(face.getElementAt(i));
                gl.glVertex3d(point.getElementAt(0), point.getElementAt(1), point.getElementAt(2));
            }

            gl.glEnd();
        }
    }
}
