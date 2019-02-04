package particles;

import com.jogamp.opengl.GL2;
import math.vector.IVector;

public interface IDrawable {

    void draw(GL2 gl, IVector eye);
}
